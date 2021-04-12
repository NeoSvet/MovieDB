package ru.neosvet.moviedb.list

data class MovieItem(
    val id: Int,
    val title: String,
    val description: String,
    val poster: String,
    val vote: Int
)