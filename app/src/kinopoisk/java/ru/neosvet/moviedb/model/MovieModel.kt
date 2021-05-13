package ru.neosvet.moviedb.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.neosvet.moviedb.repository.MovieRepoCallbacks
import ru.neosvet.moviedb.repository.MovieRepository
import ru.neosvet.moviedb.repository.room.DetailsEntity
import ru.neosvet.moviedb.repository.room.MovieEntity
import ru.neosvet.moviedb.utils.IncorrectResponseExc

class MovieModel : ViewModel(), MovieRepoCallbacks {
    private val state: MutableLiveData<MovieState> = MutableLiveData()
    private val repository: MovieRepository = MovieRepository(this)

    fun getState() = state

    fun loadDetails(id: Int) {
        state.value = MovieState.Loading
        repository.requestMovie(id)
    }

    fun loadDetailsEn(id: Int) {
        state.value = MovieState.Loading
        repository.requestMovie(id)
    }

    fun genresToString(genres: String) = genres

    fun addNote(id: Int, content: String) {
        repository.addNote(id, content)
    }

    fun getNote(id: Int) = repository.getNote(id)

//OVERRIDE

    override fun onSuccessMovie(movie: MovieEntity) {
        state.postValue(MovieState.SuccessMovie(movie))
    }

    override fun onSuccessDetails(details: DetailsEntity) {
        state.postValue(MovieState.SuccessDetails(details))
    }

    override fun onSuccessAll(movie: MovieEntity, details: DetailsEntity) {
        state.postValue(MovieState.SuccessAll(movie, details))
    }

    override fun onFailure(error: Throwable) {
        if (error.message == null)
            state.postValue(MovieState.Error(IncorrectResponseExc("")))
        else
            state.postValue(MovieState.Error(error))
    }
}

fun MovieEntity.getLink() = "https://www.kinopoisk.ru/film/" + this.id