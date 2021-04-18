package ru.neosvet.moviedb.model

import ru.neosvet.moviedb.repository.room.MovieEntity

sealed class ListState {
    data class Success(val title: String, val list: List<MovieEntity>) : ListState()
    data class Error(val error: Throwable) : ListState()
    object Loading : ListState()
    object Finished : ListState()
}