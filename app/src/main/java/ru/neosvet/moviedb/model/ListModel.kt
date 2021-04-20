package ru.neosvet.moviedb.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.neosvet.moviedb.repository.ListRepoCallbacks
import ru.neosvet.moviedb.repository.ListRepository
import ru.neosvet.moviedb.repository.room.CatalogEntity
import ru.neosvet.moviedb.utils.IncorrectResponseExc

class ListModel : ViewModel(), ListRepoCallbacks {
    companion object {
        val UPCOMING = "upcoming"
        val POPULAR = "popular"
        val TOP_RATED = "top_rated"
        val SEARCH = "query"
    }

    private val state: MutableLiveData<ListState> = MutableLiveData()
    private val repository = ListRepository(this)
    var adult: Boolean = false

    fun getState() = state

//PUBLIC    

    fun loadList(list_id: Int, isReload: Boolean, adult: Boolean) {
        state.value = ListState.Loading
        this.adult = adult
        repository.getCatalog(list_id.toString(), getLoadMode(isReload))
    }

    fun loadUpcoming(isReload: Boolean, adult: Boolean) {
        state.value = ListState.Loading
        this.adult = adult
        repository.getCatalog(UPCOMING, getLoadMode(isReload))
    }

    fun loadPopular(isReload: Boolean, adult: Boolean) {
        state.value = ListState.Loading
        this.adult = adult
        repository.getCatalog(POPULAR, getLoadMode(isReload))

    }

    fun loadTopRated(isReload: Boolean, adult: Boolean) {
        state.value = ListState.Loading
        this.adult = adult
        repository.getCatalog(TOP_RATED, getLoadMode(isReload))
    }

    private fun getLoadMode(reload: Boolean): ListRepository.Mode {
        return if (reload)
            ListRepository.Mode.ONLY_LOAD
        else
            ListRepository.Mode.CACHE_OR_LOAD
    }

    fun search(query: String, page: Int, adult: Boolean) {
        state.value = ListState.Loading
        this.adult = adult
        try {
            repository.clearCatalog(SEARCH + page)
            repository.search(query, page, adult)
        } catch (e: Exception) {
            e.printStackTrace()
            state.postValue(ListState.Error(e))
        }
    }

    fun lastSearch(page: Int) {
        repository.getCatalog(SEARCH + page, ListRepository.Mode.ONLY_CACHE)
    }

//OVERRIDE

    override fun onSuccess(catalog: CatalogEntity) {
        val list = repository.getMoviesList(catalog.movie_ids, adult)
        state.postValue(ListState.Success(catalog.title, list))
    }

    override fun onFailure(error: Throwable) {
        if (error.message == null)
            state.postValue(ListState.Error(IncorrectResponseExc("")))
        else
            state.postValue(ListState.Error(error))
    }
}