package ru.neosvet.moviedb.model

import ru.neosvet.moviedb.list.Person
import ru.neosvet.moviedb.repository.room.MovieEntity

sealed class MovieState {
    data class SuccessMovie(val movie: MovieEntity) : MovieState()
    data class SuccessDetails(val details: Details) : MovieState()
    data class SuccessAll(val movie: MovieEntity, val details: Details) : MovieState()
    data class Error(val error: Throwable) : MovieState()
    object Loading : MovieState()
    object Finished : MovieState()
}

data class Details(
    val countries: String,
    val cast: List<Person>,
    val crew: List<Person>
)