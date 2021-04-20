package ru.neosvet.moviedb.repository

import androidx.core.text.isDigitsOnly
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.neosvet.moviedb.model.ListModel
import ru.neosvet.moviedb.repository.room.CatalogEntity
import ru.neosvet.moviedb.repository.room.MovieEntity
import ru.neosvet.moviedb.utils.*
import java.util.*

class ListRepository(val callbacks: ListRepoCallbacks) : ConnectObserver {
    private val source = RemoteSource()
    private val cache = LocalSource()
    private var nameWaitLoad: String? = null

//PUBLIC

    enum class Mode {
        CACHE_OR_LOAD, ONLY_CACHE, ONLY_LOAD
    }

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

    fun requestCatalog(name: String, mode: Mode) {
        var needLoad = true
        if (mode != Mode.ONLY_LOAD) {
            val catalog = cache.getCatalog(name)
            if (catalog != null) {
                if (mode == Mode.ONLY_CACHE) {
                    callbacks.onSuccess(catalog)
                    return
                }
                needLoad = DateUtils.olderThenDay(catalog.updated)
                if (!needLoad || ConnectUtils.CONNECTED != true)
                    callbacks.onSuccess(catalog)
            }
        }
        if (needLoad) {
            if (ConnectUtils.CONNECTED == true)
                loadList(name)
            else {
                nameWaitLoad = name
                ConnectUtils.subscribe(this)
            }
        }
    }

    fun clearCatalog(name: String) {
        cache.clearCatalog(name)
    }

    fun requestSearch(query: String, page: Int, adult: Boolean) {
        if (ConnectUtils.CONNECTED == true) {
            clearCatalog(ListModel.SEARCH + page)
            source.search(query, page, adult, callBackPage)
        } else {
            callbacks.onFailure(NoConnectionExc())
        }
    }

    fun getMoviesList(movie_ids: String, adult: Boolean): List<MovieEntity> {
        return cache.getMoviesList(movie_ids, adult)
    }

//PRIVATE

    private fun loadList(name: String) {
        try {
            if (name.isDigitsOnly())
                source.getList(name, callBackList)
            else
                source.getPage(name, callBackPage)
        } catch (e: Exception) {
            e.printStackTrace()
            callbacks.onFailure(e)
        }
    }

    private fun addCatalog(name: String, desc: String?, list: List<Item>): CatalogEntity? {
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
        val catalog = CatalogEntity(name, DateUtils.getNow(), d ?: name, ids.toString())
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
                    id = it.id ?: -1,
                    updated = DateUtils.getNow(),
                    title = it.title ?: "",
                    original = getOriginal(it.original_title, it.original_language),
                    description = it.overview ?: "",
                    genre_ids = genre_ids.toString(),
                    date = formatDate(it.release_date),
                    poster = it.poster_path ?: "",
                    vote = it.vote_average ?: 0f,
                    adult = it.adult
                )
            )
        }
        if (genres_for_load.size > 0)
            loadGenres(genres_for_load)
        return movies
    }

    private fun containsGenre(id: Int) = cache.containsGenre(id)

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

    private fun getNamePage(url: String): String {
        return if (url.contains(ListModel.UPCOMING))
            ListModel.UPCOMING
        else if (url.contains(ListModel.POPULAR))
            ListModel.POPULAR
        else if (url.contains(ListModel.TOP_RATED))
            ListModel.TOP_RATED
        else if (url.contains(ListModel.SEARCH)) {
            ListModel.SEARCH + getNumberPage(url)
        } else
            url.substring(url.lastIndexOf("/") + 1)
    }

    private fun getNumberPage(url: String): String {
        val i = url.indexOf("page")
        return url.substring(i + 5, url.indexOf("&", i))
    }

    private fun onSuccess(catalog: CatalogEntity) {
        callbacks.onSuccess(catalog)
        ConnectUtils.unSubscribe(this)
        nameWaitLoad = null
    }

//CALLBACKS

    val callBackPage = object : Callback<Page> {
        override fun onResponse(call: Call<Page>, response: Response<Page>) {
            val page: Page? = response.body()

            if (response.isSuccessful && page != null) {
                val name = getNamePage(call.request().url().toString())
                val catalog = addCatalog(name, null, page.results)
                if (catalog == null) {
                    callbacks.onFailure(ListNoFoundExc())
                    return
                }
                onSuccess(catalog)
            } else {
                callbacks.onFailure(IncorrectResponseExc(response.message()))
            }
        }

        override fun onFailure(call: Call<Page>, error: Throwable) {
            callbacks.onFailure(error)
        }
    }

    val callBackList = object : Callback<Playlist> {
        override fun onResponse(call: Call<Playlist>, response: Response<Playlist>) {
            val list: Playlist? = response.body()

            if (response.isSuccessful && list != null) {
                val name = getNewName(list.description)
                val catalog = addCatalog(name, list.description, list.items)
                if (catalog == null) {
                    callbacks.onFailure(ListNoFoundExc())
                    return
                }
                onSuccess(catalog)
            } else {
                callbacks.onFailure(IncorrectResponseExc(response.message()))
            }
        }

        override fun onFailure(call: Call<Playlist>, error: Throwable) {
            callbacks.onFailure(error)
        }
    }

    private val callBackGenre = object : Callback<Genre> {
        override fun onResponse(call: Call<Genre>, response: Response<Genre>) {
            val genre: Genre? = response.body()

            if (response.isSuccessful && genre != null)
                addGenre(genre)
        }

        override fun onFailure(call: Call<Genre>, error: Throwable) {
        }
    }

//OVERRIDE

    override fun connectChanged(connected: Boolean) {
        nameWaitLoad?.let {
            if (connected)
                loadList(it)
            else
                callbacks.onFailure(NoConnectionExc())
        }
    }
}