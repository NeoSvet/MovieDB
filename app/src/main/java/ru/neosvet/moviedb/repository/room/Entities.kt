package ru.neosvet.moviedb.repository.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MovieEntity(
    @PrimaryKey
    val id: Int = -1,
    var updated: Long = 0,
    val title: String = "",
    val original: String = "",
    var description: String = "",
    val genre_ids: String = "",
    val date: String = "",
    val poster: String = "",
    val vote: Float = 0f,
    val adult: Boolean = false
)

@Entity
data class NoteEntity(
    @PrimaryKey
    val id: Int = -1,
    val content: String = ""
)

@Entity
data class DetailsEntity(
    @PrimaryKey
    val id: Int = -1,
    var countries: String = "",
    var cast_ids: String = "",
    var cast: String = "",
    var crew_ids: String = "",
    var crew: String = ""
)

@Entity
data class PersonEntity(
    @PrimaryKey
    val id: Int = -1,
    val name: String = "",
    val birthday: String = "",
    val deathday: String = "",
    val gender: Int = 0,
    val biography: String = "",
    val photo: String = "",
    val popularity: Float = 0f,
    val place_of_birth: String = ""
)

@Entity
data class GenreEntity(
    @PrimaryKey
    val id: Int = -1,
    val title: String = ""
)

@Entity
data class CatalogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val updated: Long = 0,
    val page: Int = 1,
    val total_pages: Int = 1,
    val movie_ids: String = ""
)