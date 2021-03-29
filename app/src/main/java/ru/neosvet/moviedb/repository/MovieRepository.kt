package ru.neosvet.moviedb.repository

import java.util.ArrayList

private val catalog = HashMap<Int, ArrayList<Movie>>()
private var lastId = -1

class MovieRepository {

    fun getList(catalog_id: Int): ArrayList<Movie> {
        if (catalog.containsKey(catalog_id))
            return catalog[catalog_id] ?: ArrayList<Movie>()

        val list = ArrayList<Movie>()
        list.add(
            Movie(
                ++lastId, "title" + lastId, "des",
                "genres" + catalog_id, 2000,
                "country", "poster"
            )
        )
        list.add(
            Movie(
                ++lastId, "title" + lastId, "des1",
                "genres" + catalog_id, 2001,
                "country1", "poster1"
            )
        )
        list.add(
            Movie(
                ++lastId, "title" + lastId, "des2",
                "genres" + catalog_id, 2002,
                "country2", "poster2"
            )
        )
        list.add(
            Movie(
                ++lastId, "title" + lastId, "des3",
                "genres" + catalog_id, 2003,
                "country3", "poster3"
            )
        )
        list.add(
            Movie(
                ++lastId, "title" + lastId, "des4",
                "genres" + catalog_id, 2004,
                "country4", "poster4"
            )
        )
        catalog.put(catalog_id, list)

        return list
    }

    fun getItem(id: Int): Movie? {
        for (list in catalog.values) {
            for (item in list) {
                if (item.id == id)
                    return item
            }
        }
        return null
    }
}