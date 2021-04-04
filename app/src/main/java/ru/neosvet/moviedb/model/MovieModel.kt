package ru.neosvet.moviedb.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.neosvet.moviedb.repository.MovieRepository

class MovieModel(
    private val state: MutableLiveData<MovieState> = MutableLiveData(),
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {

    fun getState() = state

    fun loadList() {
        state.value = MovieState.Loading
        Thread {
            try {
                state.postValue(MovieState.SuccessList(repository.getList()))
            } catch (e: Exception) {
                e.printStackTrace()
                state.postValue(MovieState.Error(e))
            }
        }.start()
    }
}