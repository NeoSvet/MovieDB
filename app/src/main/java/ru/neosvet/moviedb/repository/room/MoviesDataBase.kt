package ru.neosvet.moviedb.repository.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(MovieEntity::class, CatalogEntity::class, GenreEntity::class), version = 1, exportSchema = false)
abstract class MoviesDataBase : RoomDatabase() {

    abstract fun movieDao(): MovieDao
    abstract fun catalogeDao(): CatalogDao
    abstract fun genreDao(): GenreDao
}

