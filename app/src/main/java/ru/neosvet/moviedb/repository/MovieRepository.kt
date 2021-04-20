package ru.neosvet.moviedb.repository

import ru.neosvet.moviedb.repository.room.GenreEntity
import ru.neosvet.moviedb.utils.ItemNoFoundExc

class MovieRepository(val callbacks: MovieRepoCallbacks) {
    private val cache = LocalSource()

    fun requestMovie(id: Int) {
        Thread {
            try {
                val movie = cache.getMovie(id)
                if (movie == null)
                    callbacks.onFailure(ItemNoFoundExc())
                else
                    callbacks.onSuccess(movie)
            } catch (e: Exception) {
                e.printStackTrace()
                callbacks.onFailure(e)
            }
        }.start()
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
}