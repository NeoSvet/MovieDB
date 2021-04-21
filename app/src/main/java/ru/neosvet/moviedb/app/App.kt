package ru.neosvet.moviedb.app

import android.app.Application
import androidx.room.Room
import ru.neosvet.moviedb.repository.room.MoviesDataBase
import ru.neosvet.moviedb.repository.room.PeopleDataBase

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        appInstance = this
    }

    companion object {
        private var appInstance: App? = null
        private var dbMovies: MoviesDataBase? = null
        private var dbPeople: PeopleDataBase? = null
        private const val DB_MOVIES = "Movies.db"
        private const val DB_PEOPLE = "People.db"

        fun getBase(): MoviesDataBase {
            if (dbMovies == null) {
                synchronized(MoviesDataBase::class.java) {
                    if (dbMovies == null) {
                        if (appInstance == null) throw IllegalStateException("Application is null while creating DataBase")
                        dbMovies = Room.databaseBuilder(
                            appInstance!!.applicationContext,
                            MoviesDataBase::class.java,
                            DB_MOVIES
                        )
                            .allowMainThreadQueries()
                            .build()
                    }
                }
            }

            return dbMovies!!
        }

        fun getPeopleBase(): PeopleDataBase {
            if (dbPeople == null) {
                synchronized(PeopleDataBase::class.java) {
                    if (dbPeople == null) {
                        if (appInstance == null) throw IllegalStateException("Application is null while creating DataBase")
                        dbPeople = Room.databaseBuilder(
                            appInstance!!.applicationContext,
                            PeopleDataBase::class.java,
                            DB_PEOPLE
                        )
                            .allowMainThreadQueries()
                            .build()
                    }
                }
            }

            return dbPeople!!
        }
    }
}