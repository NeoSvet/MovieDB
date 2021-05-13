package ru.neosvet.moviedb.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.neosvet.moviedb.app.App
import ru.neosvet.moviedb.repository.room.PersonEntity
import ru.neosvet.moviedb.utils.DateUtils
import ru.neosvet.moviedb.utils.IncorrectResponseExc
import java.lang.StringBuilder

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

    val callBackPerson = object : Callback<Person> {
        override fun onResponse(call: Call<Person>, response: Response<Person>) {
            val person: Person? = response.body()

            if (response.isSuccessful && person != null) {
                val s = StringBuilder()
                person.profession?.let {
                    s.append(it)
                    s.appendLine(".")
                    s.appendLine()
                }
                person.facts?.forEach {
                    s.appendLine(it)
                }
                val gender = if (person.sex == null || person.sex.contains("MALE")) 0 else 1
                val personEntity = PersonEntity(
                    id = person.personId ?: -1,
                    name = person.nameRu ?: "",
                    birthday = DateUtils.format(person.birthday),
                    deathday = DateUtils.format(person.death),
                    gender = gender,
                    biography = s.toString(),
                    photo = person.posterUrl ?: "",
                    popularity = 0f,
                    place_of_birth = person.birthplace ?: ""
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