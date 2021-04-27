package ru.neosvet.moviedb.repository

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import ru.neosvet.moviedb.app.API_KEY
import ru.neosvet.moviedb.model.ListModel
import java.util.concurrent.TimeUnit

class RemoteSource {
    val MONTHS = listOf(
        "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE", "JULY",
        "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
    )

    private val retrofitApi = Retrofit.Builder()
        .baseUrl("https://kinopoiskapiunofficial.tech")
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder().setLenient().create()
            )
        )
        .client(createOkHttpClient())
        .build().create(ApiRetrofit::class.java)

    private fun createOkHttpClient(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        httpClient.connectTimeout(3, TimeUnit.SECONDS)
        httpClient.readTimeout(3, TimeUnit.SECONDS)
        httpClient.writeTimeout(3, TimeUnit.SECONDS)
        httpClient.addInterceptor(Interceptor { chain ->
            Log.d("mylog", "url: " + chain.request().url().toString())
            return@Interceptor chain.proceed(chain.request())
        })
        return httpClient.build()
    }

    fun getReleases(month: Int, year: Int, page: Int, callback: Callback<Releases>) {
        retrofitApi.getReleases(year, MONTHS[month], page, API_KEY).enqueue(callback)
    }

    fun getPopular(page: Int, callback: Callback<Films>) {
        retrofitApi.getTop(ListModel.POPULAR, page, API_KEY).enqueue(callback)
    }

    fun getTopRated(page: Int, callback: Callback<Films>) {
        retrofitApi.getTop(ListModel.TOP_RATED, page, API_KEY).enqueue(callback)
    }

    fun getDetails(movie_id: Int, callback: Callback<Data>) {
        val call: Call<Data> = retrofitApi.getDetails(movie_id, API_KEY)
        try {
            callback.onResponse(call, call.execute())
        } catch (e: Exception) {
            callback.onFailure(call, e)
        }
        //retrofitApi.getDetails(movie_id, API_KEY).enqueue(callback)
    }

    fun getCredits(movie_id: Int, callback: Callback<List<Cast>>) {
        val call: Call<List<Cast>> = retrofitApi.getCredits(movie_id, API_KEY)
        try {
            callback.onResponse(call, call.execute())
        } catch (e: Exception) {
            callback.onFailure(call, e)
        }
        //retrofitApi.getCredits(movie_id, API_KEY).enqueue(callback)
    }

    fun getPerson(person_id: Int, callback: Callback<Person>) {
        retrofitApi.getPerson(person_id, API_KEY).enqueue(callback)
    }

    fun search(query: String, page: Int, adult: Boolean, callback: Callback<Films>) {
        retrofitApi.search(page, query, API_KEY).enqueue(callback)
    }
}

interface ApiRetrofit {
    @GET("/api/v2.1/films/releases")
    fun getReleases(
        @Query("year") year: Int,
        @Query("month") month: String,
        @Query("page") page: Int,
        @Header("X-API-KEY") api_key: String
    ): Call<Releases>

    @GET("/api/v2.2/films/top")
    fun getTop(
        @Query("type") type: String,
        @Query("page") page: Int,
        @Header("X-API-KEY") api_key: String
    ): Call<Films>

    @GET("/api/v2.1/films/search-by-keyword")
    fun search(
        @Query("page") page: Int,
        @Query("keyword") query: String,
        @Header("X-API-KEY") api_key: String
    ): Call<Films>

    @GET("/api/v2.1/films/{ID}")
    fun getDetails(
        @Path("ID") movie_id: Int,
        @Header("X-API-KEY") api_key: String
    ): Call<Data>

    @GET("/api/v1/staff/{ID}")
    fun getPerson(
        @Path("ID") person_id: Int,
        @Header("X-API-KEY") api_key: String
    ): Call<Person>

    @GET("/api/v1/staff")
    fun getCredits(
        @Query("filmId") movie_id: Int,
        @Header("X-API-KEY") api_key: String
    ): Call<List<Cast>>
}