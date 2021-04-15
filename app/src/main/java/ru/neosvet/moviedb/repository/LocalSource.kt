package ru.neosvet.moviedb.repository

import ru.neosvet.moviedb.App
import ru.neosvet.moviedb.repository.room.CatalogEntity
import ru.neosvet.moviedb.repository.room.GenreEntity
import ru.neosvet.moviedb.repository.room.MovieEntity
import ru.neosvet.moviedb.repository.room.NoteEntity

class LocalSource {
    private val base = App.getBase()

    fun getCatalog(name: String) = base.catalogeDao().get(name)
    fun containsCatalog(name: String) = getCatalog(name) != null
    fun getMovie(id: Int) = base.movieDao().get(id)
    fun getNote(id: Int) = base.noteDao().get(id)
    fun getGenre(id: Int) = base.genreDao().get(id)
    fun containsGenre(id: Int) = getGenre(id) != null

    fun addMovie(item: MovieEntity) {
        base.movieDao().add(item)
    }

    fun addNote(id: Int, content: String) {
        base.noteDao().add(NoteEntity(id, content))
    }

    fun addCatalog(catalog: CatalogEntity) {
        base.catalogeDao().add(catalog)
    }

    fun addGenre(id: Int, title: String) {
        base.genreDao().add(GenreEntity(id, title))
    }

    fun clearCatalog(name: String) {
        base.catalogeDao().delete(name)
    }

    fun getMoviesList(ids: String, adult: Boolean): List<MovieEntity> {
        return base.movieDao().getList(convertStrToList(ids))
    }

    fun getGenreList(ids: String): List<GenreEntity> {
        return base.genreDao().getList(convertStrToList(ids))
    }

    private fun convertStrToList(ids: String): List<Int> {
        val m = ids.split(",")
        val list = ArrayList<Int>()
        m.forEach { list.add(it.toInt()) }
        return list
    }

    fun updateMovie(movie: MovieEntity) {
        base.movieDao().update(movie)
    }
}