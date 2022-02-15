package ru.neosvet.moviedb.repository

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.neosvet.moviedb.model.ListModel
import ru.neosvet.moviedb.repository.room.CatalogEntity
import ru.neosvet.moviedb.repository.room.DetailsEntity
import ru.neosvet.moviedb.repository.room.MovieEntity
import ru.neosvet.moviedb.utils.*
import java.util.*

class ListRepository(val callbacks: ListRepoCallbacks) : ConnectObserver {
    private val source = RemoteSource()
    private val cache = LocalSource()
    private var waitLoad: DataPage? = null
    private var lastPage: Int = 1

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

    private fun loadPage(name: String, page: Int) {
        when (name) {
            ListModel.UPCOMING -> loadReleases(page)
            ListModel.POPULAR -> loadPopular(page)
            ListModel.TOP_RATED -> loadTopRated(page)
        }
    }

    fun requestSearch(query: String, page: Int, isReload: Boolean, adult: Boolean) {
        val name = ListModel.getSearchName(query)
        val catalog = cache.getCatalog(name, page)
        if (!isReload && catalog != null) {
            callbacks.onSuccess(catalog)
            return
        }
        if (ConnectUtils.CONNECTED == true) {
            if (catalog != null)
                cache.clearCatalog(name)
            source.search(query, page, adult, callBackFilms)
        } else {
            callbacks.onFailure(NoConnectionExc)
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

    private fun loadReleases(page: Int) {
        try {
            val calendar = Calendar.getInstance()
            source.getReleases(
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.YEAR),
                page, callBackReleases
            )
        } catch (e: Exception) {
            e.printStackTrace()
            callbacks.onFailure(e)
        }
    }

    private fun loadPopular(page: Int) {
        try {
            lastPage = page
            source.getPopular(page, callBackFilms)
        } catch (e: Exception) {
            e.printStackTrace()
            callbacks.onFailure(e)
        }
    }

    private fun loadTopRated(page: Int) {
        try {
            lastPage = page
            source.getTopRated(page, callBackFilms)
        } catch (e: Exception) {
            e.printStackTrace()
            callbacks.onFailure(e)
        }
    }

    private fun parseList(list: List<Item>): ArrayList<MovieEntity> {
        val movies = ArrayList<MovieEntity>()
        var r: Float
        list.forEach {
            if (it.filmId != null) {
                val genres = it.genres ?: ArrayList<Genre>(0)
                val s = StringBuilder()
                genres.forEach {
                    s.append(", ")
                    s.append(it.genre)
                }
                s.delete(0, 2)
                if (it.rating == null)
                    r = 0f
                else if (it.rating.contains("%"))
                    r = it.rating.substring(0, it.rating.length - 1).toFloat() / 10f
                else
                    r = it.rating.toFloat()
                movies.add(
                    MovieEntity(
                        id = it.filmId,
                        updated = DateUtils.getNow(),
                        title = it.nameRu ?: "",
                        original = it.nameEn ?: "",
                        genre_ids = s.toString(),
                        date = it.year ?: "",
                        poster = it.posterUrlPreview + MovieRepository.SEPARATOR + it.posterUrl,
                        vote = r
                    )
                )

                s.clear()
                val countries = it.countries ?: ArrayList<Country>(0)
                countries.forEach {
                    s.append(", ")
                    s.append(it.country)
                }
                s.delete(0, 2)
                val details = DetailsEntity(
                    id = it.filmId,
                    countries = s.toString()
                )
                cache.addDetails(details)
            }
        }
        return movies
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

    val callBackReleases = object : Callback<Releases> {
        override fun onResponse(call: Call<Releases>, response: Response<Releases>) {
            val page: Releases? = response.body()

            if (response.isSuccessful && page != null) {
                val name = getNamePage(call.request().url().toString())

                if (page.releases.size == 0) {
                    callbacks.onFailure(ListNoFoundExc())
                    return
                }

                val list = parseList(page.releases)
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
                    total_pages = page.total ?: 1,
                    movie_ids = ids.toString()
                )
                cache.addCatalog(catalog)

                onSuccess(catalog)
            } else {
                callbacks.onFailure(IncorrectResponseExc(response.message()))
            }
        }

        override fun onFailure(call: Call<Releases>, error: Throwable) {
            callbacks.onFailure(error)
        }
    }

    val callBackFilms = object : Callback<Films> {
        override fun onResponse(call: Call<Films>, response: Response<Films>) {
            val page: Films? = response.body()

            if (response.isSuccessful && page != null) {
                val name = getNamePage(call.request().url().toString())

                if (page.films.size == 0) {
                    callbacks.onFailure(ListNoFoundExc())
                    return
                }

                val list = parseList(page.films)
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
                    page = lastPage,
                    total_pages = page.pagesCount ?: 1,
                    movie_ids = ids.toString()
                )
                cache.addCatalog(catalog)

                onSuccess(catalog)
            } else {
                callbacks.onFailure(IncorrectResponseExc(response.message()))
            }
        }

        override fun onFailure(call: Call<Films>, error: Throwable) {
            callbacks.onFailure(error)
        }
    }

//OVERRIDE

    override fun connectChanged(connected: Boolean) {
        waitLoad?.let {
            if (connected)
                loadPage(it.name, it.page)
            else
                callbacks.onFailure(NoConnectionExc)
        }
    }

    data class DataPage(
        val name: String,
        val page: Int
    )
}