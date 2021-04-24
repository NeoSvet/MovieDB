package ru.neosvet.moviedb.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.neosvet.moviedb.model.ListModel
import ru.neosvet.moviedb.repository.room.CatalogEntity
import ru.neosvet.moviedb.repository.room.MovieEntity
import ru.neosvet.moviedb.utils.*
import java.net.URLEncoder
import java.util.*

class ListRepository(val callbacks: ListRepoCallbacks) : ConnectObserver {
    private val source = RemoteSource()
    private val cache = LocalSource()
    private var waitLoad: DataPage? = null

//PUBLIC

    enum class Mode {
        CACHE_OR_LOAD, ONLY_CACHE, ONLY_LOAD
    }

    fun requestCatalog(name: String, page: Int, mode: Mode) {
        var needLoad = true
        if (mode == Mode.ONLY_LOAD) {
            cache.clearCatalog(name)
        } else {
            val catalog = cache.getCatalog(name, page)
            if (catalog != null) {
                if (mode == Mode.ONLY_CACHE) {
                    callbacks.onSuccess(catalog)
                    return
                }
                needLoad = DateUtils.olderThenDay(catalog.updated)
                if (!needLoad || ConnectUtils.CONNECTED != true)
                    callbacks.onSuccess(catalog)
            } else if (mode == Mode.ONLY_CACHE)
                return
        }
        if (needLoad) {
            if (ConnectUtils.CONNECTED == true)
                loadPage(name, page)
            else {
                waitLoad = DataPage(name, page)
                ConnectUtils.subscribe(this)
            }
        }
    }

    fun requestSearch(query: String, page: Int, isReload: Boolean, adult: Boolean) {
        val name = ListModel.SEARCH + "=" + URLEncoder.encode(query, "utf-8")
        val catalog = cache.getCatalog(name, page)
        if (!isReload && catalog != null) {
            callbacks.onSuccess(catalog)
            return
        }
        if (ConnectUtils.CONNECTED == true) {
            if (catalog != null)
                cache.clearCatalog(name)
            source.search(query, page, adult, callBackPage)
        } else {
            callbacks.onFailure(NoConnectionExc())
        }
    }

    fun getMoviesList(movie_ids: String, adult: Boolean): List<MovieEntity> {
        return cache.getMoviesList(movie_ids, adult)
    }

//PRIVATE

    /*private fun getNewName(name: String?): String {
        val n = if (name.isNullOrEmpty()) "Unnamed" else name
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
    }*/

    private fun loadPage(name: String, page: Int) {
        try {
            /*if (name.isDigitsOnly())
                source.getList(name, callBackList)
            else*/
            source.getPage(name, page, callBackPage)
        } catch (e: Exception) {
            e.printStackTrace()
            callbacks.onFailure(e)
        }
    }

    private fun addCatalog(name: String, page: Page): CatalogEntity? {
        if (page.results.size == 0)
            return null
        val list = parseList(page.results)
        val ids = StringBuilder()
        list.forEach {
            cache.addMovie(it)
            ids.append(",")
            ids.append(it.id)
        }
        ids.delete(0, 1)
        val catalog = CatalogEntity(
            name = name,
            updated = DateUtils.getNow(),
            page = page.page ?: 1,
            total_pages = page.total_pages ?: 1,
            movie_ids = ids.toString()
        )
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
                    date = DateUtils.format(it.release_date),
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
            url.substring(url.indexOf(ListModel.SEARCH))
        } else
            url.substring(url.lastIndexOf("/") + 1)
    }

    private fun onSuccess(catalog: CatalogEntity) {
        callbacks.onSuccess(catalog)
        ConnectUtils.unSubscribe(this)
        waitLoad = null
    }

//CALLBACKS

    val callBackPage = object : Callback<Page> {
        override fun onResponse(call: Call<Page>, response: Response<Page>) {
            val page: Page? = response.body()

            if (response.isSuccessful && page != null) {
                val name = getNamePage(call.request().url().toString())
                val catalog = addCatalog(name, page)
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

    /*val callBackList = object : Callback<Playlist> {
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
    }*/

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
        waitLoad?.let {
            if (connected)
                loadPage(it.name, it.page)
            else
                callbacks.onFailure(NoConnectionExc())
        }
    }

    data class DataPage(
        val name: String,
        val page: Int
    )
}