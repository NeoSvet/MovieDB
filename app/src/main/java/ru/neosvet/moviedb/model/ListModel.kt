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

class ListModel : ViewModel(), ConnectObserver {
    companion object {
        val UPCOMING = "upcoming"
        val POPULAR = "popular"
        val TOP_RATED = "top_rated"
        val SEARCH = "query"
    }

    private val state: MutableLiveData<ListState> = MutableLiveData()
    private val repository = MovieRepository()
    var nameWaitLoad: String? = null
    var adult: Boolean = false

    fun getState() = state

    fun loadList(list_id: Int, fromCache: Boolean, adult: Boolean) {
        this.adult = adult
        preLoadListByName(list_id.toString(), fromCache)
    }

    fun loadUpcoming(fromCache: Boolean, adult: Boolean) {
        this.adult = adult
        preLoadListByName(UPCOMING, fromCache)
    }

    fun loadPopular(fromCache: Boolean, adult: Boolean) {
        this.adult = adult
        preLoadListByName(POPULAR, fromCache)
    }

    fun loadTopRated(fromCache: Boolean, adult: Boolean) {
        this.adult = adult
        preLoadListByName(TOP_RATED, fromCache)
    }

    fun search(query: String, page: Int, adult: Boolean) {
        this.adult = adult
        try {
            state.value = ListState.Loading
            repository.clearCatalog(SEARCH + page)
            repository.search(query, page, adult, callBackPage)
        } catch (e: Exception) {
            e.printStackTrace()
            state.postValue(ListState.Error(e))
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
                state.postValue(ListState.Error(NoConnectionExc()))
        }
    }

    fun onSuccess() {
        ConnectUtils.unSubscribe(this)
        nameWaitLoad = null
    }

    private fun preLoadListByName(name: String, fromCache: Boolean) {
        var needLoad = true
        if (fromCache) {
            val catalog = repository.getCatalog(name)
            if (catalog != null) {
                needLoad = DateUtils.olderThenDay(catalog.updated)
                if (!needLoad || ConnectUtils.CONNECTED != true)
                    pushCatalog(catalog)
            }
        }
        if (needLoad) {
            if (ConnectUtils.CONNECTED == true)
                loadListByName(name)
            else {
                nameWaitLoad = name
                ConnectUtils.subscribe(this)
            }
        }
    }

    private fun loadListByName(name: String) {
        try {
            state.value = ListState.Loading
            if (name.isDigitsOnly())
                repository.getList(name, callBackList)
            else
                repository.getPage(name, callBackPage)
        } catch (e: Exception) {
            e.printStackTrace()
            state.postValue(ListState.Error(e))
        }
    }

//PRIVATE

    private fun pushCatalog(catalog: CatalogEntity) {
        val list = repository.getMoviesList(catalog.movie_ids, adult)
        state.postValue(ListState.Success(catalog.title, list))
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
                    state.postValue(ListState.Error(ListNoFoundExc()))
                    return
                }
                pushCatalog(catalog)
                onSuccess()
            } else {
                state.postValue(ListState.Error(IncorrectResponseExc(response.message())))
            }
        }

        override fun onFailure(call: Call<Page>, t: Throwable) {
            if (t.message == null)
                state.postValue(ListState.Error(IncorrectResponseExc("")))
            else
                state.postValue(ListState.Error(t))
        }
    }

    val callBackList = object : Callback<Playlist> {

        override fun onResponse(call: Call<Playlist>, response: Response<Playlist>) {
            val list: Playlist? = response.body()

            if (response.isSuccessful && list != null) {
                val name = repository.getNewName(list.description)
                val catalog = repository.addCatalog(name, list.description, list.items)
                if (catalog == null) {
                    state.postValue(ListState.Error(ListNoFoundExc()))
                    return
                }
                pushCatalog(catalog)
                onSuccess()
            } else {
                state.postValue(ListState.Error(IncorrectResponseExc(response.message())))
            }
        }

        override fun onFailure(call: Call<Playlist>, t: Throwable) {
            if (t.message == null)
                state.postValue(ListState.Error(IncorrectResponseExc("")))
            else
                state.postValue(ListState.Error(t))
        }
    }
}