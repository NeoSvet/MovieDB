package ru.neosvet.moviedb.repository

import java.util.ArrayList

private val list = ArrayList<Movie>()

class MovieRepository {

    fun getList(): ArrayList<Movie> {
        if (list.size > 0)
            return list

        list.add(
            Movie(
                0, "title0", "des0",
                "genres0", 2000,
                "country0", "poster0"
            )
        )
        list.add(
            Movie(
                1, "title1", "des1",
                "genres1", 2001,
                "country1", "poster1"
            )
        )
        list.add(
            Movie(
                2, "title2", "des2",
                "genres2", 2002,
                "country2", "poster2"
            )
        )
        list.add(
            Movie(
                3, "title3", "des3",
                "genres3", 2003,
                "country3", "poster3"
            )
        )
        list.add(
            Movie(
                4, "title4", "des4",
                "genres4", 2004,
                "country4", "poster4"
            )
        )
        return list
    }

    fun getItem(id: Int): Movie? {
        for (item in list) {
            if (item.id == id)
                return item
        }
        return null
    }
}