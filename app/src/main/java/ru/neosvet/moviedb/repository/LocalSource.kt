package ru.neosvet.moviedb.repository

import ru.neosvet.moviedb.app.App
import ru.neosvet.moviedb.repository.room.*
import ru.neosvet.moviedb.utils.DateUtils

class LocalSource {
    private val base = App.getBase()

    fun getCatalog(name: String, page: Int) = base.catalogeDao().get(name, page)
    fun getMovie(id: Int) = base.movieDao().get(id)
    fun getNote(id: Int) = base.noteDao().get(id)
    fun containsGenre(id: Int) = base.genreDao().get(id) != null

    fun addMovie(item: MovieEntity) {
        base.movieDao().add(item)
    }

    fun updateMovieDes(id: Int, des: String): MovieEntity? {
        val movie = getMovie(id) ?: return null
        movie.updated = DateUtils.getNow()
        movie.description = des
        base.movieDao().update(movie)
        return movie
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
        if (adult)
            return base.movieDao().getListWithAdult(convertStrToList(ids))
        else
            return base.movieDao().getList(convertStrToList(ids))
    }

    fun getGenreList(ids: String): List<GenreEntity> {
        return base.genreDao().getList(convertStrToList(ids))
    }

    fun getDetails(id: Int): DetailsEntity? {
        return base.detailsDao().get(id)
    }

    fun addDetails(details: DetailsEntity) {
        base.detailsDao().add(details)
    }

    private fun convertStrToList(ids: String): List<Int> {
        val list = ArrayList<Int>()
        if (ids.isEmpty())
            return list
        val m = ids.split(",")
        m.forEach { list.add(it.toInt()) }
        return list
    }
}