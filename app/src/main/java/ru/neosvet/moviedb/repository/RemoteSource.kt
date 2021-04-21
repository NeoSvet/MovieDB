package ru.neosvet.moviedb.repository

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.neosvet.moviedb.app.API_KEY

class RemoteSource {
    val LANG = "ru-RU"

    private val retrofitApi = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder().setLenient().create()
            )
        )
        .client(createOkHttpClient())
        .build().create(ApiRetrofit::class.java)

    fun getPage(url: String, callback: Callback<Page>) {
        retrofitApi.getPage(url, API_KEY, LANG).enqueue(callback)
    }

    fun getList(id: String, callback: Callback<Playlist>) {
        retrofitApi.getList(id, API_KEY, LANG).enqueue(callback)
    }

    fun getDetails(movie_id: Int, callback: Callback<Movie>) {
        val call: Call<Movie> = retrofitApi.getDetails(movie_id, API_KEY, LANG)
        try {
            callback.onResponse(call, call.execute())
        } catch (e: Exception) {
            callback.onFailure(call, e)
        }
        //retrofitApi.getDetails(movie_id, API_KEY, LANG).enqueue(callback)
    }

    fun getCredits(movie_id: Int, callback: Callback<Credits>) {
        val call: Call<Credits> = retrofitApi.getCredits(movie_id, API_KEY, LANG)
        try {
            callback.onResponse(call, call.execute())
        } catch (e: Exception) {
            callback.onFailure(call, e)
        }
        //retrofitApi.getCredits(movie_id, API_KEY, LANG).enqueue(callback)
    }

    fun getPerson(person_id: Int, callback: Callback<Person>) {
        retrofitApi.getPerson(person_id, API_KEY, LANG).enqueue(callback)
    }

    fun getGenre(id: Int, callback: Callback<Genre>) {
        val call: Call<Genre> = retrofitApi.getGenre(id, API_KEY, LANG)
        try {
            callback.onResponse(call, call.execute())
        } catch (e: Exception) {
            callback.onFailure(call, e)
        }
    }

    fun search(query: String, page: Int, adult: Boolean, callback: Callback<Page>) {
        retrofitApi.search(
            API_KEY, LANG, page, adult, query
        ).enqueue(callback)
    }

    private fun createOkHttpClient(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        return httpClient.build()
    }
}

interface ApiRetrofit {
    @GET("person/{ID}")
    fun getPerson(
        @Path("ID") person_id: Int,
        @Query("api_key") api_key: String,
        @Query("language") lang: String
    ): Call<Person>

    @GET("movie/{ID}")
    fun getDetails(
        @Path("ID") movie_id: Int,
        @Query("api_key") api_key: String,
        @Query("language") lang: String
    ): Call<Movie>

    @GET("movie/{ID}/credits")
    fun getCredits(
        @Path("ID") movie_id: Int,
        @Query("api_key") api_key: String,
        @Query("language") lang: String
    ): Call<Credits>

    @GET("search/movie")
    fun search(
        @Query("api_key") api_key: String,
        @Query("language") lang: String,
        @Query("page") page: Int,
        @Query("include_adult") adult: Boolean,
        @Query("query") query: String
    ): Call<Page>

    @GET("movie/{URL}")
    fun getPage(
        @Path("URL") url: String,
        @Query("api_key") api_key: String,
        @Query("language") lang: String
    ): Call<Page>

    @GET("list/{ID}")
    fun getList(
        @Path("ID") id: String,
        @Query("api_key") api_key: String,
        @Query("language") lang: String
    ): Call<Playlist>

    @GET("genre/{ID}")
    fun getGenre(
        @Path("ID") id: Int,
        @Query("api_key") api_key: String,
        @Query("language") lang: String
    ): Call<Genre>
}