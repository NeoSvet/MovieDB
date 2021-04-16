package ru.neosvet.moviedb.model

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.neosvet.moviedb.model.api.*
import ru.neosvet.moviedb.repository.Catalog
import ru.neosvet.moviedb.repository.Movie
import ru.neosvet.moviedb.repository.MovieRepository
import ru.neosvet.moviedb.utils.*
import java.util.*

class MovieModel(
    private val state: MutableLiveData<MovieState> = MutableLiveData(),
    private val repository: MovieRepository = MovieRepository(),
    private val source: RemoteDataSource = RemoteDataSource()
) : ViewModel(), ConnectObserver {
    var nameWaitLoad: String? = null

    companion object {
        val UPCOMING = "upcoming"
        val POPULAR = "popular"
        val TOP_RATED = "top_rated"
    }

    fun getState() = state

    fun loadList(list_id: Int) {
        preLoadListByName(list_id.toString())
    }

    fun loadUpcoming() {
        preLoadListByName(UPCOMING)
    }

    fun loadPopular() {
        preLoadListByName(POPULAR)
    }

    fun loadTopRated() {
        preLoadListByName(TOP_RATED)
    }

    override fun connectChanged(connected: Boolean) {
        nameWaitLoad?.let {
            if (connected)
                loadListByName(it)
            else
                state.postValue(MovieState.Error(NoConnectionExc()))
        }
    }

    fun onSuccess() {
        ConnectUtils.unSubscribe(this)
        nameWaitLoad = null
    }

    private fun preLoadListByName(name: String) {
        val catalog = repository.getCatalog(name)
        if (catalog == null) {
            nameWaitLoad = name
            ConnectUtils.subscribe(this)
        } else
            pushCatalog(name, catalog)
    }

    private fun loadListByName(name: String) {
        try {
            state.value = MovieState.Loading
            if (name.isDigitsOnly())
                source.getList(name, callBackList)
            else
                source.getPage(name, callBackPage)
        } catch (e: Exception) {
            e.printStackTrace()
            state.postValue(MovieState.Error(e))
        }
    }

    fun loadDetails(id: Int?) {
        if (id == null)
            return
        state.value = MovieState.Loading
        Thread {
            try {
                val item = repository.getMovie(id)
                if (item == null)
                    state.postValue(MovieState.Error(ItemNoFoundExc()))
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

//PRIVATE

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

    private fun getNamePage(url: String): String {
        return if (url.contains(UPCOMING))
            UPCOMING
        else if (url.contains(POPULAR))
            POPULAR
        else if (url.contains(TOP_RATED))
            TOP_RATED
        else
            url.substring(url.lastIndexOf("/") + 1)
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
        Thread {
            list.forEach {
                source.getGenre(it, callBackGenre)
            }
        }.start()
    }

    private fun getOriginal(title: String?, language: String?): String {
        title?.let {
            return it + language?.let { " [${it.toUpperCase()}]" }
        }
        return ""
    }

//CALLBACKS

    private val callBackPage = object : Callback<Page> {

        override fun onResponse(call: Call<Page>, response: Response<Page>) {
            val page: Page? = response.body()

            if (response.isSuccessful && page != null) {
                val name = getNamePage(call.request().url().toString())
                val movies = parseList(page.results)
                repository.addCatalog(name, null, movies)
                val catalog = repository.getCatalog(name)
                    ?: throw ListNoFoundExc()
                pushCatalog(name, catalog)
                onSuccess()
            } else {
                state.postValue(MovieState.Error(IncorrectResponseExc()))
            }
        }

        override fun onFailure(call: Call<Page>, t: Throwable) {
            if (t.message == null)
                state.postValue(MovieState.Error(IncorrectResponseExc()))
            else
                state.postValue(MovieState.Error(t))
        }
    }

    private val callBackList = object : Callback<Playlist> {

        override fun onResponse(call: Call<Playlist>, response: Response<Playlist>) {
            val list: Playlist? = response.body()

            if (response.isSuccessful && list != null) {
                val name = list.description ?: "Unnamed"
                val movies = parseList(list.items)
                repository.addCatalog(name, null, movies)
                val catalog = repository.getCatalog(name)
                    ?: throw ListNoFoundExc()
                pushCatalog(name, catalog)
                onSuccess()
            } else {
                state.postValue(MovieState.Error(IncorrectResponseExc()))
            }
        }

        override fun onFailure(call: Call<Playlist>, t: Throwable) {
            if (t.message == null)
                state.postValue(MovieState.Error(IncorrectResponseExc()))
            else
                state.postValue(MovieState.Error(t))
        }
    }

    private val callBackGenre = object : Callback<Genre> {

        override fun onResponse(call: Call<Genre>, response: Response<Genre>) {
            val genre: Genre? = response.body()

            if (response.isSuccessful && genre != null)
                repository.addGenre(genre)
            else
                state.postValue(MovieState.Error(IncorrectResponseExc()))
        }

        override fun onFailure(call: Call<Genre>, t: Throwable) {
            if (t.message == null)
                state.postValue(MovieState.Error(IncorrectResponseExc()))
            else
                state.postValue(MovieState.Error(t))
        }
    }
}