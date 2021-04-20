package ru.neosvet.moviedb.repository

import ru.neosvet.moviedb.repository.room.CatalogEntity

interface ListRepoCallbacks {
    fun onSuccess(catalog: CatalogEntity)
    fun onFailure(error: Throwable)
}