package ru.neosvet.moviedb.model

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.neosvet.moviedb.model.api.Page
import ru.neosvet.moviedb.model.api.Playlist
import ru.neosvet.moviedb.model.api.RemoteDataSource
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
    var adult: Boolean = false

    companion object {
        val UPCOMING = "upcoming"
        val POPULAR = "popular"
        val TOP_RATED = "top_rated"
        val SEARCH = "query"
    }

    fun getState() = state

    fun loadList(list_id: Int, adult: Boolean) {
        this.adult = adult
        preLoadListByName(list_id.toString())
    }

    fun loadUpcoming(adult: Boolean) {
        this.adult = adult
        preLoadListByName(UPCOMING)
    }

    fun loadPopular(adult: Boolean) {
        this.adult = adult
        preLoadListByName(POPULAR)
    }

    fun loadTopRated(adult: Boolean) {
        this.adult = adult
        preLoadListByName(TOP_RATED)
    }

    fun search(query: String, page: Int, adult: Boolean) {
        this.adult = adult
        try {
            state.value = MovieState.Loading
            repository.clearCatalog(SEARCH + page)
            source.search(query, page, adult, callBackPage)
        } catch (e: Exception) {
            e.printStackTrace()
            state.postValue(MovieState.Error(e))
        }
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
                if (checkAdult(it.isAdult))
                    list.add(it)
            }
        }
        state.postValue(
            MovieState.SuccessList(
                catalog.desc ?: name, list
            )
        )
    }

    private fun checkAdult(itIsAdult: Boolean): Boolean {
        if (adult)
            return !itIsAdult
        else
            return true
    }

    private fun getNamePage(url: String): String {
        return if (url.contains(UPCOMING))
            UPCOMING
        else if (url.contains(POPULAR))
            POPULAR
        else if (url.contains(TOP_RATED))
            TOP_RATED
        else if (url.contains(SEARCH)) {
            SEARCH + getNumberPage(url)
        } else
            url.substring(url.lastIndexOf("/") + 1)
    }

    private fun getNumberPage(url: String): String {
        val i = url.indexOf("page")
        return url.substring(i + 5, url.indexOf("&", i))
    }

//CALLBACKS

    private val callBackPage = object : Callback<Page> {

        override fun onResponse(call: Call<Page>, response: Response<Page>) {
            val page: Page? = response.body()

            if (response.isSuccessful && page != null) {
                val name = getNamePage(call.request().url().toString())
                repository.addCatalog(name, null, page.results)
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
                val name = repository.getNewName(list.description)
                repository.addCatalog(name, list.description, list.items)
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
}