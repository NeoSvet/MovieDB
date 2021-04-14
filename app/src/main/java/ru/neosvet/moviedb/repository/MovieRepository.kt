package ru.neosvet.moviedb.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.neosvet.moviedb.model.MovieModel
import ru.neosvet.moviedb.repository.room.CatalogEntity
import ru.neosvet.moviedb.repository.room.GenreEntity
import ru.neosvet.moviedb.repository.room.MovieEntity
import java.lang.StringBuilder
import java.util.ArrayList

class MovieRepository(val model: MovieModel) {
    private val source = RemoteSource()
    private val cache = LocalSource()
    fun getCatalog(name: String) = cache.getCatalog(name)
    fun getMovie(id: Int) = cache.getMovie(id)
    fun getGenre(id: Int) = cache.getGenre(id)
    fun containsGenre(id: Int) = cache.containsGenre(id)

    fun getNewName(name: String?): String {
        val n = if (name == null || name.length == 0) "Unnamed" else name
        if (cache.containsCatalog(n)) {
            var i = 1
            var t = n
            do {
                i++
                t = n + " [" + i + "]"
            } while (cache.containsCatalog(t))
            return t
        }
        return n
    }

    fun addCatalog(name: String, desc: String?, list: List<Item>): CatalogEntity? {
        if (list.size == 0)
            return null
        val new_list = parseList(list)
        val ids = StringBuilder()
        new_list.forEach {
            cache.addMovie(it)
            ids.append(",")
            ids.append(it.id)
        }
        ids.delete(0, 1)
        val d = if (desc?.length == 0) null else desc
        val catalog = CatalogEntity(name, d ?: name, ids.toString())
        cache.addCatalog(catalog)
        return catalog
    }

    private fun parseList(list: List<Item>): ArrayList<MovieEntity> {
        val genres_for_load = ArrayList<Int>()
        val movies = ArrayList<MovieEntity>()
        list.forEach {
            val genres = it.genre_ids ?: ArrayList<Int>()
            val genre_ids = StringBuilder()
            genres.forEach {
                genre_ids.append(",")
                genre_ids.append(it)
                if (!containsGenre(it) && !genres_for_load.contains(it))
                    genres_for_load.add(it)
            }
            genre_ids.delete(0, 1)
            movies.add(
                MovieEntity(
                    it.id ?: -1,
                    it.title ?: "",
                    getOriginal(it.original_title, it.original_language),
                    it.overview ?: "",
                    "",
                    genre_ids.toString(),
                    formatDate(it.release_date),
                    it.poster_path ?: "",
                    it.vote_average ?: 0f,
                    it.adult
                )
            )
        }
        if (genres_for_load.size > 0)
            loadGenres(genres_for_load)
        return movies
    }

    private fun formatDate(date: String?): String {
        date?.let {
            val m = it.split("-")
            if (m.size != 3)
                return it
            return "${m[2]}.${m[1]}.${m[0]}"
        }
        return ""
    }

    private fun loadGenres(list: ArrayList<Int>) {
        Thread {
            list.forEach {
                source.getGenre(it, callBackGenre)
            }
        }.start()
    }

    private fun getOriginal(title: String?, language: String?): String {
        title?.let {
            return it + language?.let { " [${it.toUpperCase()}]" }
        }
        return ""
    }

    private fun addGenre(genre: Genre) {
        genre.id?.let {
            genre.name?.run {
                cache.addGenre(it, this)
            }
        }
    }

    fun clearCatalog(name: String) {
        cache.clearCatalog(name)
    }

    fun search(query: String, page: Int, adult: Boolean) {
        source.search(query, page, adult, model.callBackPage)
    }

    fun getList(name: String) {
        source.getList(name, model.callBackList)
    }

    fun getPage(name: String) {
        source.getPage(name, model.callBackPage)
    }

    fun getMoviesList(movie_ids: String, adult: Boolean): List<MovieEntity> {
        return cache.getMoviesList(movie_ids, adult)
    }

    fun getGenreList(genre_ids: String): List<GenreEntity> {
        return cache.getGenreList(genre_ids)
    }

    private val callBackGenre = object : Callback<Genre> {

        override fun onResponse(call: Call<Genre>, response: Response<Genre>) {
            val genre: Genre? = response.body()

            if (response.isSuccessful && genre != null)
                addGenre(genre)
        }

        override fun onFailure(call: Call<Genre>, t: Throwable) {
        }
    }
}