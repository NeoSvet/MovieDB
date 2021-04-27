package ru.neosvet.moviedb.repository

data class Playlist(
    val created_by: String?,
    val description: String?,
    val favorite_count: Int?,
    val id: Int?,
    val items: List<Item>
)

data class Page(
    val page: Int?,
    val results: List<Item>,
    val total_pages: Int?,
    val total_results: Int?,
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

data class Movie(
    val adult: Boolean,
    val backdrop_path: String?,
    val budget: Int?,
    //val genres: List<T>?,
    val id: Int?,
    val original_language: String?,
    val original_title: String?,
    val overview: String?,
    val popularity: Float?,
    val poster_path: String?,
    //val production_companies: List<T>?,
    val production_countries: List<Country>?,
    val release_date: String?,
    val revenue: Int?,
    //val spoken_languages: List<T>?,
    val status: String?,
    val title: String?,
    val video: Boolean?,
    val vote_average: Float?,
    val vote_count: Int?
)

data class Country(
    val iso_3166_1: String?,
    val name: String?,
)

data class Credits(
    val id: Int?,
    val cast: List<Cast>?,
    val crew: List<Cast>?
)

data class Cast(
    val adult: Boolean,
    val gender: Int?,
    val id: Int?,
    val known_for_department: String?,
    val name: String?,
    val original_name: String?,
    val popularity: Float?,
    val profile_path: String?,
    val cast_id: Int?,
    val character: String?,
    val credit_id: String?,
    val order: Int?,
    val department: String?,
    val job: String?
)

data class Person(
    val birthday: String?,
    val known_for_department: String?,
    val deathday: String?,
    val id: Int?,
    val name: String?,
    val gender: Int?,
    val biography: String?,
    val popularity: Float?,
    val place_of_birth: String?,
    val profile_path: String?,
    val adult: Boolean,
    val imdb_id: String?,
    val homepage: String?
)