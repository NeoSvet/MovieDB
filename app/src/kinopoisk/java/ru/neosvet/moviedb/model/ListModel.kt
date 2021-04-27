package ru.neosvet.moviedb.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.neosvet.moviedb.repository.ListRepoCallbacks
import ru.neosvet.moviedb.repository.ListRepository
import ru.neosvet.moviedb.repository.room.CatalogEntity
import ru.neosvet.moviedb.utils.IncorrectResponseExc
import java.net.URLEncoder

class ListModel : ViewModel(), ListRepoCallbacks {
    companion object {
        val UPCOMING = "releases"
        val POPULAR = "TOP_100_POPULAR_FILMS"
        val TOP_RATED = "TOP_250_BEST_FILMS"
        val SEARCH = "keyword"
        fun getSearchName(query: String) = SEARCH + "=" + URLEncoder.encode(query, "utf-8")
    }

    private val state: MutableLiveData<ListState> = MutableLiveData()
    private val repository = ListRepository(this)
    var index = 0
    var adult: Boolean = false
        set(value) {
            field = value
        }

    fun getState() = state

//PUBLIC

    fun loadUpcoming(isReload: Boolean, page: Int) {
        state.value = ListState.Loading
        index = 0
        repository.requestCatalog(UPCOMING, page, getLoadMode(isReload))
    }

    fun loadPopular(isReload: Boolean, page: Int) {
        state.value = ListState.Loading
        index = 1
        repository.requestCatalog(POPULAR, page, getLoadMode(isReload))

    }

    fun loadTopRated(isReload: Boolean, page: Int) {
        state.value = ListState.Loading
        index = 2
        repository.requestCatalog(TOP_RATED, page, getLoadMode(isReload))
    }

    fun search(query: String, page: Int, isReload: Boolean) {
        state.value = ListState.Loading
        index = 0
        repository.requestSearch(query, page, isReload, adult)
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
        state.postValue(ListState.Success(index, catalog, list))
    }

    override fun onFailure(error: Throwable) {
        if (error.message == null)
            state.postValue(ListState.Error(IncorrectResponseExc("")))
        else
            state.postValue(ListState.Error(error))
    }
}