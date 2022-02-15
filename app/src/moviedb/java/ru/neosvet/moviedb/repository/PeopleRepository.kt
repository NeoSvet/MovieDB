package ru.neosvet.moviedb.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.neosvet.moviedb.app.App
import ru.neosvet.moviedb.repository.room.PersonEntity
import ru.neosvet.moviedb.utils.DateUtils
import ru.neosvet.moviedb.utils.IncorrectResponseExc

class PeopleRepository(val callbacks: PersonRepoCallbacks) {
    private val source = RemoteSource()
    private val base = App.getPeopleBase()

    fun requestPerson(id: Int, isRefresh: Boolean) {
        if (isRefresh) {
            source.getPerson(id, callBackPerson)
            return
        }
        val person = base.peopleDao().get(id)
        if (person == null)
            source.getPerson(id, callBackPerson)
        else
            callbacks.onSuccess(person)
    }

    fun addPerson(item: PersonEntity) {
        base.peopleDao().add(item)
    }

//CALLBACKS

    private val callBackPerson = object : Callback<Person> {
        override fun onResponse(call: Call<Person>, response: Response<Person>) {
            val person: Person? = response.body()

            if (response.isSuccessful && person != null) {
                if (person.biography.isNullOrEmpty() &&
                    call.request().url().toString().contains("ru-RU")
                ) {
                    person.id?.let {
                        source.getPersonEn(it, this)
                    }
                    return
                }
                val personEntity = PersonEntity(
                    id = person.id ?: -1,
                    name = person.name ?: "",
                    birthday = DateUtils.format(person.birthday),
                    deathday = DateUtils.format(person.deathday),
                    gender = person.gender ?: 0,
                    biography = person.biography ?: "",
                    photo = person.profile_path ?: "",
                    popularity = person.popularity ?: 0f,
                    place_of_birth = person.place_of_birth ?: ""
                )
                addPerson(personEntity)
                callbacks.onSuccess(personEntity)
            } else {
                callbacks.onFailure(IncorrectResponseExc(response.message()))
            }
        }

        override fun onFailure(call: Call<Person>, error: Throwable) {
            callbacks.onFailure(error)
        }
    }
}