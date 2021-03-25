package ru.neosvet.moviedb.model

import ru.neosvet.moviedb.repository.Movie

sealed class MovieState {
    data class SuccessList(val list: ArrayList<Movie>) : MovieState()
    data class SuccessItem(val item: Movie) : MovieState()
    data class Error(val error: Throwable) : MovieState()
    object Loading : MovieState()
}