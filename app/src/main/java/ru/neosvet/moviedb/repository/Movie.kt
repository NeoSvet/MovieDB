package ru.neosvet.moviedb.repository

data class Movie(val id: Int, val title: String, val description: String, val genres: String,
                 val year: Int, val country: String, val poster: String)