package ru.neosvet.moviedb.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.neosvet.moviedb.repository.PeopleRepository
import ru.neosvet.moviedb.repository.PersonRepoCallbacks
import ru.neosvet.moviedb.repository.room.PersonEntity
import ru.neosvet.moviedb.utils.IncorrectResponseExc

class PersonModel : ViewModel(), PersonRepoCallbacks {
    private val state: MutableLiveData<PersonState> = MutableLiveData()
    private val repository = PeopleRepository(this)

    fun getState() = state

    fun loadPerson(id: Int) {
        state.value = PersonState.Loading
        repository.requestPerson(id)
    }

    override fun onSuccess(person: PersonEntity) {
        state.postValue(PersonState.Success(person))
    }

    override fun onFailure(error: Throwable) {
        if (error.message == null)
            state.postValue(PersonState.Error(IncorrectResponseExc("")))
        else
            state.postValue(PersonState.Error(error))
    }
}