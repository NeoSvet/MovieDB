package ru.neosvet.moviedb.model.api

data class Playlist(
    val created_by: String?,
    val description: String?,
    val favorite_count: Int?,
    val id: Int?,
    val items: List<Item>
)

data class Item(
    val adult: Boolean,
    val backdrop_path: String?,
    val genre_ids: List<Int>?,
    val id: Int?,
    val media_type: String?,
    val original_language: String?,
    val original_title: String?,
    val overview: String?,
    val popularity: Float?,
    val poster_path: String?,
    val release_date: String?,
    val title: String?,
    val video: Boolean?,
    val vote_average: Float?,
    val vote_count: Int?
)

data class Genre(
    val id: Int?,
    val name: String?
)