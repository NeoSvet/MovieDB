package ru.neosvet.moviedb.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.neosvet.moviedb.repository.room.DetailsEntity
import ru.neosvet.moviedb.repository.room.GenreEntity
import ru.neosvet.moviedb.utils.IncorrectResponseExc
import ru.neosvet.moviedb.utils.ItemNoFoundExc

class MovieRepository(val callbacks: MovieRepoCallbacks) {
    companion object {
        val SEPARATOR = '@'
    }

    private val source = RemoteSource()
    private val cache = LocalSource()
    private lateinit var details: DetailsEntity

    fun requestMovie(id: Int) {
        Thread {
            try {
                val movie = cache.getMovie(id)
                if (movie == null)
                    callbacks.onFailure(ItemNoFoundExc())
                else {
                    val details = cache.getDetails(id)
                    if (details == null) {
                        callbacks.onSuccessMovie(movie)
                        loadDetails(id)
                    } else
                        callbacks.onSuccessAll(movie, details)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callbacks.onFailure(e)
            }
        }.start()
    }

    private fun loadDetails(id: Int) {
        details = DetailsEntity(id = id)
        source.getDetails(id, callBackDetails)
    }

    private fun loadCredits(id: Int) {
        source.getCredits(id, callBackCredits)
    }

    fun getGenreList(genre_ids: String): List<GenreEntity> {
        return cache.getGenreList(genre_ids)
    }

    fun addNote(id: Int, content: String) {
        cache.addNote(id, content)
    }

    fun getNote(id: Int): String {
        val note = cache.getNote(id)
        note?.let {
            return it.content
        }
        return ""
    }

//CALLBACKS

    val callBackDetails = object : Callback<Movie> {
        override fun onResponse(call: Call<Movie>, response: Response<Movie>) {
            val movie: Movie? = response.body()

            if (response.isSuccessful && movie != null) {
                val s = StringBuilder()
                movie.production_countries?.forEach {
                    s.append(", ")
                    s.append(it.name)
                }
                if (s.length > 0) {
                    s.delete(0, 2)
                    details.countries = s.toString()
                }
                movie.id?.let { loadCredits(it) }
            } else {
                callbacks.onFailure(IncorrectResponseExc(response.message()))
            }
        }

        override fun onFailure(call: Call<Movie>, error: Throwable) {
            callbacks.onFailure(error)
        }
    }

    val callBackCredits = object : Callback<Credits> {
        override fun onResponse(call: Call<Credits>, response: Response<Credits>) {
            val credits: Credits? = response.body()

            if (response.isSuccessful && credits != null) {
                val names = StringBuilder()
                val ids = StringBuilder()

                credits.cast?.forEach {
                    names.append(SEPARATOR)
                    names.append(it.name)
                    ids.append(SEPARATOR)
                    ids.append(it.id)
                    it.character?.run {
                        names.append(" (")
                        names.append(this)
                        names.append(")")
                    }
                }
                if (names.length > 0) {
                    names.delete(0, 1)
                    details.cast = names.toString()
                    names.clear()
                    ids.delete(0, 1)
                    details.cast_ids = ids.toString()
                    ids.clear()
                }

                credits.crew?.forEach {
                    names.append(SEPARATOR)
                    names.append(it.name)
                    ids.append(SEPARATOR)
                    ids.append(it.id)
                    it.job?.run {
                        names.append(" (")
                        names.append(this)
                        names.append(")")
                    }
                }
                if (names.length > 0) {
                    names.delete(0, 1)
                    details.crew = names.toString()
                    ids.delete(0, 1)
                    details.crew_ids = ids.toString()
                }

                cache.addDetails(details)
                callbacks.onSuccessDetails(details)
            } else {
                callbacks.onFailure(IncorrectResponseExc(response.message()))
            }
        }

        override fun onFailure(call: Call<Credits>, error: Throwable) {
            callbacks.onFailure(error)
        }
    }
}