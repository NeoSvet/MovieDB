package ru.neosvet.moviedb.model

import ru.neosvet.moviedb.repository.room.CatalogEntity
import ru.neosvet.moviedb.repository.room.MovieEntity

sealed class ListState {
    data class Success(val index: Int, val catalog: CatalogEntity, val list: List<MovieEntity>) : ListState()
    data class Error(val error: Throwable) : ListState()
    object Loading : ListState()
    object Finished : ListState()
}