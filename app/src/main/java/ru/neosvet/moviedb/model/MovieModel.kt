package ru.neosvet.moviedb.model

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import ru.neosvet.moviedb.model.api.*
import ru.neosvet.moviedb.repository.Catalog
import ru.neosvet.moviedb.repository.Movie
import ru.neosvet.moviedb.repository.MovieRepository
import ru.neosvet.moviedb.utils.ConnectRec
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

    companion object {
        val UPCOMING = "upcoming"
        val POPULAR = "popular"
        val TOP_RATED = "top_rated"
    }

    fun loadList(list_id: Int) {
        loadUrl(BASE_URL + "list/" + list_id)
    }

    fun loadUpcoming() {
        loadUrl(BASE_URL + "movie/" + UPCOMING)
    }

    fun loadPopular() {
        loadUrl(BASE_URL + "movie/" + POPULAR)
    }

    fun loadTopRated() {
        loadUrl(BASE_URL + "movie/" + TOP_RATED)
    }

    private fun loadUrl(url: String) {
        try {
            val name = url.substring(url.lastIndexOf("/") + 1)
            val catalog = repository.getCatalog(name)
            if (catalog == null) {
                Thread {
                    if (isConnect())
                        launchLoadList(name, url)
                }.start()
            } else
                pushCatalog(name, catalog)
        } catch (e: Exception) {
            e.printStackTrace()
            state.postValue(MovieState.Error(e))
        }
    }

    private fun pushCatalog(name: String, catalog: Catalog) {
        val list = ArrayList<Movie>()
        catalog.movie_ids.forEach {
            val movie = repository.getMovie(it)
            movie?.let {
                list.add(it)
            }
        }
        state.postValue(
            MovieState.SuccessList(
                catalog.desc ?: name, list
            )
        )
    }

    private fun isConnect(): Boolean {
        while (ConnectRec.CONNECTED == null)
            Thread.sleep(15)
        if (ConnectRec.CONNECTED ?: false)
            return true
        state.postValue(MovieState.Error(Exception("No connection")))
        return false
    }

    private fun launchLoadList(name: String, url: String) {
        state.postValue(MovieState.Loading)
        //API_KEY = "?api_key={key}"
        val uri = URL(url + API_KEY + LANG)

        val genres_for_load = ArrayList<Int>()
        lateinit var urlConnection: HttpsURLConnection
        try {
            urlConnection = uri.openConnection() as HttpsURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.readTimeout = 10000
            val reader = BufferedReader(InputStreamReader(urlConnection.inputStream))

            if (name.isDigitsOnly()) {
                val playlist: Playlist = Gson().fromJson(reader.readLine(), Playlist::class.java)
                val movies = parseList(playlist.items)
                repository.addCatalog(name, playlist.description ?: "no title", movies)
            } else {
                val page: Page = Gson().fromJson(reader.readLine(), Page::class.java)
                val movies = parseList(page.results)
                repository.addCatalog(name, null, movies)
            }

            val catalog = repository.getCatalog(name)
                ?: throw Exception("No list")
            pushCatalog(name, catalog)
        } catch (e: Exception) {
            e.printStackTrace()
            state.postValue(MovieState.Error(e))
            genres_for_load.clear()
        } finally {
            urlConnection.disconnect()
        }
        if (genres_for_load.size > 0)
            loadGenres(genres_for_load)
    }

    private fun parseList(list: List<Item>): ArrayList<Movie> {
        val genres_for_load = ArrayList<Int>()
        val movies = ArrayList<Movie>()
        list.forEach {
            val genres = it.genre_ids ?: ArrayList<Int>()
            genres.forEach {
                if (!repository.containsGenre(it) && !genres_for_load.contains(it))
                    genres_for_load.add(it)
            }
            movies.add(
                Movie(
                    it.id ?: -1,
                    it.title ?: "",
                    getOriginal(it.original_title, it.original_language),
                    it.overview ?: "",
                    genres,
                    formatDate(it.release_date),
                    it.poster_path ?: "",
                    it.vote_average ?: 0f
                )
            )
        }
        if (genres_for_load.size > 0)
            loadGenres(genres_for_load)
        return movies
    }

    private fun formatDate(date: String?): String {
        date?.let {
            val m = it.split("-")
            return "${m[2]}.${m[1]}.${m[0]}"
        }
        return ""
    }

    private fun loadGenres(list: ArrayList<Int>) {
        lateinit var urlConnection: HttpsURLConnection
        try {
            list.forEach {
                val uri = URL(BASE_URL + "genre/" + it + API_KEY + LANG)
                urlConnection = uri.openConnection() as HttpsURLConnection
                urlConnection.requestMethod = "GET"
                urlConnection.readTimeout = 10000
                val reader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                val genre: Genre = Gson().fromJson(reader.readLine(), Genre::class.java)
                repository.addGenre(genre)
                urlConnection.disconnect()
            }
        } catch (e: Exception) {
            urlConnection.disconnect()
            e.printStackTrace()
            state.postValue(MovieState.Error(e))
        }
    }

    private fun getOriginal(title: String?, language: String?): String {
        title?.let {
            return it + language?.let { " [${it.toUpperCase()}]" }
        }
        return ""
    }

    fun loadDetails(id: Int?) {
        if (id == null || !isConnect())
            return
        state.value = MovieState.Loading
        Thread {
            try {
                val item = repository.getMovie(id)
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

    fun genresToString(genres: List<Int>): String {
        val s = StringBuilder()
        var name: String?
        for (i in genres.indices) {
            name = repository.getGenre(genres[i])
            name?.let {
                s.append(it)
                if (i < genres.size - 1)
                    s.append(", ")
            }
        }
        return s.toString()
    }
}