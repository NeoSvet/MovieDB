package ru.neosvet.moviedb.repository

import java.util.ArrayList

data class Movie(val id: Int, val title: String, val description: String, val genres: String,
                 val year: Int, val country: String, val poster: String)

data class MoviesList(val title: String, val movies: ArrayList<Movie>)