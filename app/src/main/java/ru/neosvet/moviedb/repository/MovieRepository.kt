package ru.neosvet.moviedb.repository

import ru.neosvet.moviedb.model.api.Genre
import java.util.ArrayList

private val catalog = HashMap<Int, MoviesList>()
private val genres = HashMap<Int, String>()

class MovieRepository {

    fun getList(category_id: Int): MoviesList? {
        if (catalog.containsKey(category_id))
            return catalog[category_id] ?: MoviesList("", ArrayList<Movie>())

        return null
    }

    fun getItem(id: Int): Movie? {
        for (list in catalog.values) {
            for (item in list.movies) {
                if (item.id == id)
                    return item
            }
        }
        return null
    }

    fun addCatalog(id: Int, title: String, movies: ArrayList<Movie>) {
        catalog.put(id, MoviesList(title, movies))
    }

    fun containsGenre(id: Int): Boolean = genres.containsKey(id)

    fun addGenre(genre: Genre) {
        genre.id?.let {
            genres.put(it, genre.name ?: "")
        }
    }

    fun getGenre(id: Int): String? {
        return genres.get(id)
    }
}