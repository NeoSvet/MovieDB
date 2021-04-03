package ru.neosvet.moviedb.repository

data class Movie(
    val id: Int,
    val title: String,
    val original: String,
    val description: String,
    val genres: List<Int>,
    val date: String,
    val poster: String,
    val vote: Float
)

data class MoviesList(val title: String, val movies: ArrayList<Movie>)