package ru.neosvet.moviedb.model

import ru.neosvet.moviedb.repository.room.DetailsEntity
import ru.neosvet.moviedb.repository.room.MovieEntity

sealed class MovieState {
    data class SuccessMovie(val movie: MovieEntity) : MovieState()
    data class SuccessDetails(val details: DetailsEntity) : MovieState()
    data class SuccessAll(val movie: MovieEntity, val details: DetailsEntity) : MovieState()
    data class Error(val error: Throwable) : MovieState()
    object Loading : MovieState()
    object Finished : MovieState()
}