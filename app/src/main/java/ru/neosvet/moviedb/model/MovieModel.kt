package ru.neosvet.moviedb.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.neosvet.moviedb.repository.MovieRepository

class MovieModel(
    private val state: MutableLiveData<MovieState> = MutableLiveData(),
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {

    fun getState() = state

    fun loadList(catalog_id: Int) {
        state.value = MovieState.Loading
        Thread {
            try {
                state.postValue(
                    MovieState.SuccessList(
                        "catalog" + catalog_id,
                        repository.getList(catalog_id)
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                state.postValue(MovieState.Error(e))
            }
        }.start()
    }

    fun loadDetails(id: Int?) {
        if (id == null)
            return
        state.value = MovieState.Loading
        Thread {
            try {
                val item = repository.getItem(id);
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