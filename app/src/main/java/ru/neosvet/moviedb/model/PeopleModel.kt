package ru.neosvet.moviedb.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.neosvet.moviedb.list.Person
import ru.neosvet.moviedb.repository.LocalSource
import ru.neosvet.moviedb.utils.PeopleUtils

class PeopleModel : ViewModel() {
    private val state: MutableLiveData<List<Person>> = MutableLiveData()
    private val cache = LocalSource()

    fun getState(): LiveData<List<Person>> = state

    fun getCrew(movieId: Int) {
        Thread {
            try {
                cache.getDetails(movieId)?.let {
                    val list = PeopleUtils.createFullList(it.crew, it.crew_ids)
                    state.postValue(list)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun getCast(movieId: Int) {
        Thread {
            try {
                cache.getDetails(movieId)?.let {
                    val list = PeopleUtils.createFullList(it.cast, it.cast_ids)
                    state.postValue(list)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}