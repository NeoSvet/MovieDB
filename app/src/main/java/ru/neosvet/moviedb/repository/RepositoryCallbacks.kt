package ru.neosvet.moviedb.repository

import ru.neosvet.moviedb.repository.room.CatalogEntity
import ru.neosvet.moviedb.repository.room.MovieEntity

interface ListRepoCallbacks {
    fun onSuccess(catalog: CatalogEntity)
    fun onFailure(error: Throwable)
}

interface MovieRepoCallbacks {
    fun onSuccess(movie: MovieEntity)
    fun onFailure(error: Throwable)
}