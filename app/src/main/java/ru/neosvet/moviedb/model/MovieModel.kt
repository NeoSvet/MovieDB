package ru.neosvet.moviedb.model

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.neosvet.moviedb.repository.MovieRepository
import ru.neosvet.moviedb.repository.Page
import ru.neosvet.moviedb.repository.Playlist
import ru.neosvet.moviedb.repository.room.CatalogEntity
import ru.neosvet.moviedb.utils.*

class MovieModel : ViewModel(), ConnectObserver {
    companion object {
        val UPCOMING = "upcoming"
        val POPULAR = "popular"
        val TOP_RATED = "top_rated"
        val SEARCH = "query"
    }

    private val state: MutableLiveData<MovieState> = MutableLiveData()
    private val repository: MovieRepository = MovieRepository(this)
    var nameWaitLoad: String? = null
    var adult: Boolean = false


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
            repository.search(query, page, adult)
        } catch (e: Exception) {
            e.printStackTrace()
            state.postValue(MovieState.Error(e))
        }
    }

    fun lastSearch(page: Int) {
        val name = SEARCH + page
        val catalog = repository.getCatalog(name)
        if (catalog != null)
            pushCatalog(catalog)
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
            if (ConnectUtils.CONNECTED == true)
                loadListByName(name)
            else {
                nameWaitLoad = name
                ConnectUtils.subscribe(this)
            }
        } else
            pushCatalog(catalog)
    }

    private fun loadListByName(name: String) {
        try {
            state.value = MovieState.Loading
            if (name.isDigitsOnly())
                repository.getList(name)
            else
                repository.getPage(name)
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

    fun genresToString(genre_ids: String): String {
        val list = repository.getGenreList(genre_ids)
        val s = StringBuilder()
        list.forEach {
            s.append(", ")
            s.append(it.title)
        }
        s.delete(0, 2)
        return s.toString()
    }

//PRIVATE

    private fun pushCatalog(catalog: CatalogEntity) {
        val list = repository.getMoviesList(catalog.movie_ids, adult)
        state.postValue(MovieState.SuccessList(catalog.title, list))
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

    val callBackPage = object : Callback<Page> {

        override fun onResponse(call: Call<Page>, response: Response<Page>) {
            val page: Page? = response.body()

            if (response.isSuccessful && page != null) {
                val name = getNamePage(call.request().url().toString())
                val catalog = repository.addCatalog(name, null, page.results)
                if (catalog == null) {
                    state.postValue(MovieState.Error(ListNoFoundExc()))
                    return
                }
                pushCatalog(catalog)
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

    val callBackList = object : Callback<Playlist> {

        override fun onResponse(call: Call<Playlist>, response: Response<Playlist>) {
            val list: Playlist? = response.body()

            if (response.isSuccessful && list != null) {
                val name = repository.getNewName(list.description)
                val catalog = repository.addCatalog(name, list.description, list.items)
                if (catalog == null) {
                    state.postValue(MovieState.Error(ListNoFoundExc()))
                    return
                }
                pushCatalog(catalog)
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