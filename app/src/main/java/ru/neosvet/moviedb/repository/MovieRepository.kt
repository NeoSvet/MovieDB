package ru.neosvet.moviedb.repository

import java.util.ArrayList

private val catalog = HashMap<Int, MoviesList>()
private var lastId = -1

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
}