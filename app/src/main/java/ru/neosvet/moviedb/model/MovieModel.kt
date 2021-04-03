package ru.neosvet.moviedb.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import ru.neosvet.moviedb.model.api.API_KEY
import ru.neosvet.moviedb.model.api.Catalog
import ru.neosvet.moviedb.repository.Movie
import ru.neosvet.moviedb.repository.MovieRepository
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection

class MovieModel(
    private val state: MutableLiveData<MovieState> = MutableLiveData(),
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {
    val BASE_URL = "https://api.themoviedb.org/3/"
    val LANG = "&language=ru-RU"
    fun getState() = state

    fun loadList(category_id: Int) {
        state.value = MovieState.Loading
        Thread {
            try {
                launchLoadList(category_id)
                val list = repository.getList(category_id)
                list?.let {
                    state.postValue(MovieState.SuccessList(it.title, it.movies))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                state.postValue(MovieState.Error(e))
            }
        }.start()
    }

    private fun launchLoadList(category_id: Int) {
        //API_KEY = "?api_key={key}"
        val uri = URL(BASE_URL + "list/" + category_id + API_KEY + LANG)

        lateinit var urlConnection: HttpsURLConnection
        try {
            urlConnection = uri.openConnection() as HttpsURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.readTimeout = 10000
            val reader = BufferedReader(InputStreamReader(urlConnection.inputStream))

            val catalog: Catalog = Gson().fromJson(reader.readLine(), Catalog::class.java)
            val movies = ArrayList<Movie>()
            catalog.items.forEach {
                movies.add(
                    Movie(
                        it.id ?: -1,
                        it.title ?: "",
                        getOriginal(it.original_title, it.original_language),
                        it.overview ?: "",
                        it.genre_ids ?: ArrayList<Int>(),
                        it.release_date ?: "",
                        it.poster_path ?: "",
                        it.vote_average ?: 0f
                    )
                )
            }
            repository.addCatalog(category_id, catalog.description ?: "no title", movies)
        } catch (e: Exception) {
            e.printStackTrace()
            state.postValue(MovieState.Error(e))
        } finally {
            urlConnection.disconnect()
        }
    }

    private fun getOriginal(title: String?, language: String?): String {
        title?.let {
            return it + language?.let { " [${it.toUpperCase()}]" }
        }
        return ""
    }

    fun loadDetails(id: Int?) {
        if (id == null)
            return
        state.value = MovieState.Loading
        Thread {
            try {
                val item = repository.getItem(id)
                if (item == null)
                    state.postValue(MovieState.Error(Exception("Item no found")))
                else
                    state.postValue(MovieState.SuccessItem(item))
            } catch (e: Exception) {
                e.printStackTrace()
                state.postValue(MovieState.Error(e))
            }
        }.start()
    }
}