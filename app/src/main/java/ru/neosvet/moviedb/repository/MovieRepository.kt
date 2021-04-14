package ru.neosvet.moviedb.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.neosvet.moviedb.model.MovieModel
import java.util.ArrayList

class MovieRepository(val model: MovieModel) {
    companion object {
        private val catalogs = HashMap<String, Catalog>()
        private val movies = HashMap<Int, Movie>()
        private val genres = HashMap<Int, String>()
    }

    private val source: RemoteSource = RemoteSource()
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

    fun addCatalog(name: String, desc: String?, list: List<Item>) {
        val new_list = parseList(list)
        val ids = ArrayList<Int>()
        new_list.forEach {
            movies[it.id] = it
            ids.add(it.id)
        }
        val d = if (desc?.length == 0) null else desc
        catalogs[name] = Catalog(d, ids)
    }

    private fun parseList(list: List<Item>): ArrayList<Movie> {
        val genres_for_load = ArrayList<Int>()
        val movies = ArrayList<Movie>()
        list.forEach {
            val genres = it.genre_ids ?: ArrayList<Int>()
            genres.forEach {
                if (!containsGenre(it) && !genres_for_load.contains(it))
                    genres_for_load.add(it)
            }
            movies.add(
                Movie(
                    it.id ?: -1,
                    it.title ?: "",
                    getOriginal(it.original_title, it.original_language),
                    it.overview ?: "",
                    genres,
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
            genres[it] = genre.name ?: ""
        }
    }

    fun clearCatalog(name: String) {
        catalogs.remove(name)
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