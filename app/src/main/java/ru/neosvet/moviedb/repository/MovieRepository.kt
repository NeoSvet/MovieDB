package ru.neosvet.moviedb.repository

import ru.neosvet.moviedb.model.api.Genre
import java.util.ArrayList

private val catalogs = HashMap<String, Catalog>()
private val movies = HashMap<Int, Movie>()
private val genres = HashMap<Int, String>()

class MovieRepository {
    fun getCatalog(name: String) = catalogs[name]
    fun getMovie(id: Int) = movies[id]
    fun getGenre(id: Int) = genres[id]
    fun containsGenre(id: Int) = genres.containsKey(id)

    fun getNewName(name: String?): String {
        val n = if (name == null || name.length == 0) "Unnamed" else name
        if (catalogs.containsKey(n)) {
            var i = 1
            var t = n
            do {
                i++
                t = n + " [" + i + "]"
            } while (catalogs.containsKey(t))
            return t
        }
        return n
    }

    fun addCatalog(name: String, desc: String?, list: ArrayList<Movie>) {
        val ids = ArrayList<Int>()
        list.forEach {
            movies[it.id] = it
            ids.add(it.id)
        }
        val d = if (desc?.length == 0) null else desc
        catalogs[name] = Catalog(d, ids)
    }

    fun addGenre(genre: Genre) {
        genre.id?.let {
            genres[it] = genre.name ?: ""
        }
    }
}