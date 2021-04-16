package ru.neosvet.moviedb.repository

import ru.neosvet.moviedb.model.api.Genre
import java.util.ArrayList

private val catalogs = HashMap<Int, Catalog>()
private val movies = HashMap<Int, Movie>()
private val genres = HashMap<Int, String>()

class MovieRepository {
    fun getCatalog(id: Int) = catalogs[id]
    fun getMovie(id: Int) = movies[id]
    fun getGenre(id: Int) = genres[id]
    fun containsGenre(id: Int) = genres.containsKey(id)

    fun addCatalog(id: Int, title: String, list: ArrayList<Movie>) {
        val ids = ArrayList<Int>()
        list.forEach {
            movies.put(it.id, it)
            ids.add(it.id)
        }
        catalogs.put(id, Catalog(title, ids))
    }

    fun addGenre(genre: Genre) {
        genre.id?.let {
            genres.put(it, genre.name ?: "")
        }
    }
}