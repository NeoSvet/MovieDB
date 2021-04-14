package ru.neosvet.moviedb.model

import ru.neosvet.moviedb.repository.room.MovieEntity

sealed class MovieState {
    data class SuccessList(val title: String, val list: List<MovieEntity>) : MovieState()
    data class SuccessItem(val item: MovieEntity) : MovieState()
    data class Error(val error: Throwable) : MovieState()
    object Loading : MovieState()
    object Finished : MovieState()
}