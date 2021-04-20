package ru.neosvet.moviedb.repository

import ru.neosvet.moviedb.repository.room.GenreEntity

class MovieRepository {
    private val cache = LocalSource()
    fun getMovie(id: Int) = cache.getMovie(id)

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