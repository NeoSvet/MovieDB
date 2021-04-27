package ru.neosvet.moviedb.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.neosvet.moviedb.repository.room.DetailsEntity
import ru.neosvet.moviedb.utils.IncorrectResponseExc
import ru.neosvet.moviedb.utils.ItemNoFoundExc

class MovieRepository(val callbacks: MovieRepoCallbacks) {
    companion object {
        val SEPARATOR = '@'
    }

    private val source = RemoteSource()
    private val cache = LocalSource()
    private lateinit var details: DetailsEntity

    fun requestMovie(id: Int) {
        Thread {
            try {
                val movie = cache.getMovie(id)
                if (movie == null)
                    callbacks.onFailure(ItemNoFoundExc())
                else {
                    val details = cache.getDetails(id)
                    if (details == null || movie.description.isEmpty()) {
                        callbacks.onSuccessMovie(movie)
                        loadDetails(id)
                    } else
                        callbacks.onSuccessAll(movie, details)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callbacks.onFailure(e)
            }
        }.start()
    }

    private fun loadDetails(id: Int) {
        details = cache.getDetails(id) ?: DetailsEntity(id = id)
        source.getDetails(id, callBackDetails)
    }

    private fun loadCredits(id: Int) {
        source.getCredits(id, callBackCredits)
    }

    fun addNote(id: Int, content: String) {
        cache.addNote(id, content)
    }

    fun getNote(id: Int): String {
        val note = cache.getNote(id)
        note?.let {
            return it.content
        }
        return ""
    }

//CALLBACKS

    val callBackDetails = object : Callback<Data> {
        override fun onResponse(call: Call<Data>, response: Response<Data>) {
            val data: Data? = response.body()
            val movie = data?.data

            if (response.isSuccessful && movie != null) {
                movie.filmId?.let {
                    val m = cache.updateMovieDes(it, movie.description ?: "")
                    if (m != null)
                        callbacks.onSuccessMovie(m)
                    loadCredits(it)
                }
            } else {
                callbacks.onFailure(IncorrectResponseExc(response.message()))
            }
        }

        override fun onFailure(call: Call<Data>, error: Throwable) {
            callbacks.onFailure(error)
        }
    }

    val callBackCredits = object : Callback<List<Cast>> {
        override fun onResponse(call: Call<List<Cast>>, response: Response<List<Cast>>) {
            val staff: List<Cast>? = response.body()

            if (response.isSuccessful && staff != null) {

                staff?.let {
                    fillListPeople(it)
                }

                cache.addDetails(details)
                callbacks.onSuccessDetails(details)
            } else {
                callbacks.onFailure(IncorrectResponseExc(response.message()))
            }
        }

        override fun onFailure(call: Call<List<Cast>>, error: Throwable) {
            callbacks.onFailure(error)
        }
    }

    private fun fillListPeople(list: List<Cast>) {
        val names = StringBuilder()
        val ids = StringBuilder()
        val namesA = StringBuilder()
        val idsA = StringBuilder()

        list.forEach {
            if (it.professionKey != null)
                if (it.professionKey.contains("ACTOR")) {
                    namesA.append(selectName(it.nameRu, it.nameEn))
                    idsA.append(SEPARATOR)
                    idsA.append(it.staffId)
                    if (!it.professionText.isNullOrEmpty())
                        namesA.append(getProf(it.professionText))
                } else {
                    names.append(selectName(it.nameRu, it.nameEn))
                    ids.append(SEPARATOR)
                    ids.append(it.staffId)
                    if (!it.professionText.isNullOrEmpty())
                        names.append(getProf(it.professionText))
                }
        }
        if (namesA.isNotEmpty()) {
            namesA.delete(0, 1)
            idsA.delete(0, 1)
            details.cast = namesA.toString()
            details.cast_ids = idsA.toString()
            namesA.clear()
            idsA.clear()
        }
        if (names.isNotEmpty()) {
            names.delete(0, 1)
            ids.delete(0, 1)
            details.crew = names.toString()
            details.crew_ids = ids.toString()
            names.clear()
            ids.clear()
        }
    }

    private fun selectName(nameRu: String, nameEn: String) =
        if (nameRu.isEmpty())
            SEPARATOR + nameEn
        else
            SEPARATOR + nameRu

    private fun getProf(s: String) =
        " (" + s.substring(0, s.length - 1) + ")"
}