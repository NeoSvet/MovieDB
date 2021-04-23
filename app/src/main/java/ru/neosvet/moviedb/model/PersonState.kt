package ru.neosvet.moviedb.model

import ru.neosvet.moviedb.repository.room.PersonEntity

sealed class PersonState {
    data class Success(val person: PersonEntity) : PersonState()
    data class Error(val error: Throwable) : PersonState()
    object Loading : PersonState()
}
