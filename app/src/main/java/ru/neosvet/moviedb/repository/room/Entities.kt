package ru.neosvet.moviedb.repository.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MovieEntity(
    @PrimaryKey
    val id: Int = -1,
    val title: String = "",
    val original: String = "",
    val description: String = "",
    val note: String = "",
    val genre_ids: String = "",
    val date: String = "",
    val poster: String = "",
    val vote: Float = 0f,
    val adult: Boolean = false
)

@Entity
data class GenreEntity(
    @PrimaryKey
    val id: Int = -1,
    val title: String = ""
)

@Entity
data class CatalogEntity(
    @PrimaryKey
    val name: String = "",
    val title: String = "",
    val movie_ids: String = ""
)