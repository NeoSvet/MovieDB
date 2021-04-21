package ru.neosvet.moviedb.repository

import ru.neosvet.moviedb.repository.room.CatalogEntity
import ru.neosvet.moviedb.repository.room.DetailsEntity
import ru.neosvet.moviedb.repository.room.MovieEntity
import ru.neosvet.moviedb.repository.room.PersonEntity

interface ListRepoCallbacks {
    fun onSuccess(catalog: CatalogEntity)
    fun onFailure(error: Throwable)
}

interface MovieRepoCallbacks {
    fun onSuccessMovie(movie: MovieEntity)
    fun onSuccessDetails(details: DetailsEntity)
    fun onSuccessAll(movie: MovieEntity, details: DetailsEntity)
    fun onFailure(error: Throwable)
}

interface PersonRepoCallbacks {
    fun onSuccess(person: PersonEntity)
    fun onFailure(error: Throwable)
}