package ru.neosvet.moviedb.repository

data class Releases(
    val page: Int?,
    val total: Int?,
    val releases: List<Item>
)
data class Films(
    val pagesCount: Int?,
    val films: List<Item>
)

data class Item(
    val filmId: Int?,
    val nameRu: String?,
    val nameEn: String?,
    val posterUrl: String?,
    val posterUrlPreview: String?,
    val countries: List<Country>?,
    val genres: List<Genre>?,
    val rating: String?,
    val ratingVoteCount: Int?,
    val type: String?, //films/search "FILM"
    val year: String?, //films, releases - int?
    val filmLength: String?, //films maybe with ":"
    val duration: Int?, //releases
    val releaseDate: String? //releases
)

data class Genre(
    val genre: String?
)

data class Data(
    val data: Movie?
)

data class Movie(
    val filmId: Int?,
    val nameRu: String?,
    val nameEn: String?,
    val webUrl: String?,
    val posterUrl: String?,
    val posterUrlPreview: String?,
    val year: String?,
    val filmLength: String?,
    val slogan: String?,
    val description: String?,
    val type: String?,
    val ratingAgeLimits: Int?,
    val premiereRu: String?,
    val premiereWorld: String?,
    val premiereDigital: String?,
    val countries: List<Country>?,
    val genres: List<Genre>?,
    val facts: List<String>?
)

data class Country(
    val country: String?,
)

data class Cast(
    val staffId: Int?,
    val nameRu: String,
    val nameEn: String,
    val posterUrl: String?,
    val professionText: String?,
    val professionKey: String?
)

data class Person(
    val personId: Int?,
    val nameRu: String?,
    val nameEn: String?,
    val sex: String?, //MALE, FEMALE
    val posterUrl: String?,
    val growth: String?,
    val birthday: String?,
    val death: String?,
    val age: Int?,
    val birthplace: String?,
    val deathplace: String?,
    val hasAwards: Int?,
    val profession: String?,
    val facts: List<String>?
    //val spouses: List<T>?,
    //val films: List<T>?,
)