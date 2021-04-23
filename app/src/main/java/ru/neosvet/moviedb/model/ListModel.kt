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
        repository.requestCatalog(list_id.toString(), getLoadMode(isReload))
    }

    fun loadUpcoming(isReload: Boolean, adult: Boolean) {
        state.value = ListState.Loading
        this.adult = adult
        repository.requestCatalog(UPCOMING, getLoadMode(isReload))
    }

    fun loadPopular(isReload: Boolean, adult: Boolean) {
        state.value = ListState.Loading
        this.adult = adult
        repository.requestCatalog(POPULAR, getLoadMode(isReload))

    }

    fun loadTopRated(isReload: Boolean, adult: Boolean) {
        state.value = ListState.Loading
        this.adult = adult
        repository.requestCatalog(TOP_RATED, getLoadMode(isReload))
    }

    fun search(query: String, page: Int, adult: Boolean) {
        state.value = ListState.Loading
        this.adult = adult
        repository.requestSearch(query, page, adult)
    }

    fun lastSearch(page: Int) {
        repository.requestCatalog(SEARCH + page, ListRepository.Mode.ONLY_CACHE)
    }

//PRIVATE

    private fun getLoadMode(reload: Boolean): ListRepository.Mode {
        return if (reload)
            ListRepository.Mode.ONLY_LOAD
        else
            ListRepository.Mode.CACHE_OR_LOAD
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