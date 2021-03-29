package ru.neosvet.moviedb.repository

import java.util.ArrayList

private val catalog = HashMap<Int, MoviesList>()
private var lastId = -1

class MovieRepository {

    fun getList(category_id: Int): MoviesList {
        if (catalog.containsKey(category_id))
            return catalog[category_id] ?: MoviesList("", ArrayList<Movie>())

        val movies = ArrayList<Movie>()
        movies.add(
            Movie(
                ++lastId, "title" + lastId, "des",
                "genres" + category_id, 2000,
                "country", "poster"
            )
        )
        movies.add(
            Movie(
                ++lastId, "title" + lastId, "des1",
                "genres" + category_id, 2001,
                "country1", "poster1"
            )
        )
        movies.add(
            Movie(
                ++lastId, "title" + lastId, "des2",
                "genres" + category_id, 2002,
                "country2", "poster2"
            )
        )
        movies.add(
            Movie(
                ++lastId, "title" + lastId, "des3",
                "genres" + category_id, 2003,
                "country3", "poster3"
            )
        )
        movies.add(
            Movie(
                ++lastId, "title" + lastId, "des4",
                "genres" + category_id, 2004,
                "country4", "poster4"
            )
        )

        val list = MoviesList("category" + category_id, movies)
        catalog.put(category_id, list)

        return list
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
}