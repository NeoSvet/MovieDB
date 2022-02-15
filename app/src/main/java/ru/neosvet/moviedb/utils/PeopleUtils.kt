package ru.neosvet.moviedb.utils

import ru.neosvet.moviedb.list.Person
import ru.neosvet.moviedb.repository.MovieRepository

object PeopleUtils {
    private const val LIMIT_PERSON = 6

    fun createShortList(strNames: String, strIds: String) =
        createList(strNames, strIds, LIMIT_PERSON)

    fun createFullList(strNames: String, strIds: String) =
        createList(strNames, strIds, -1)

    private fun createList(strNames: String, strIds: String, limit: Int): List<Person> {
        val list = arrayListOf<Person>()
        val names = strNames.split(MovieRepository.SEPARATOR)
        val ids = strIds.split(MovieRepository.SEPARATOR)
        for (i in names.indices) {
            val person = createPerson(names[i], ids[i].toInt())
            list.add(person)
            if (list.size == limit)
                return list
        }
        return list
    }

    private fun createPerson(name: String, id: Int): Person {
        val n = name.indexOf(" (")
        return if (n > -1) {
            Person(
                id = id,
                name = name.substring(0, n),
                role = name.substring(n + 2, name.length - 1)
            )
        } else {
            Person(
                id = id,
                name = name,
                role = ""
            )
        }
    }
}