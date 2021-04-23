package ru.neosvet.moviedb.repository.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = arrayOf(
        PersonEntity::class
    ), version = 1, exportSchema = false
)
abstract class PeopleDataBase : RoomDatabase() {
    abstract fun peopleDao(): PeopleDao
}

