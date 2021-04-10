package ru.neosvet.moviedb.model.api

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.IOException
import java.lang.Exception

class RemoteDataSource {
    val LANG = "&language=ru-RU"

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
        retrofitApi.getPage(url).enqueue(callback)
    }

    fun getList(id: String, callback: Callback<Playlist>) {
        retrofitApi.getList(id).enqueue(callback)
    }

    fun getGenre(id: Int, callback: Callback<Genre>) {
        val call: Call<Genre> = retrofitApi.getGenre(id)
        try {
            callback.onResponse(call, call.execute())
        } catch (e: Exception) {
            callback.onFailure(call, e)
        }
    }

    private fun createOkHttpClient(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(MoviesApiInterceptor())
        return httpClient.build()
    }

    inner class MoviesApiInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val requestBuilder: Request.Builder = chain.request().newBuilder()
            //API_KEY = "?api_key={key}"
            requestBuilder.url(chain.request().url().toString() + API_KEY + LANG)
            return chain.proceed(requestBuilder.build());
        }
    }
}


interface ApiRetrofit {
    @GET("movie/{URL}")
    fun getPage(
        @Path("URL") url: String
    ): Call<Page>

    @GET("list/{ID}")
    fun getList(
        @Path("ID") id: String
    ): Call<Playlist>

    @GET("genre/{ID}")
    fun getGenre(
        @Path("ID") id: Int
    ): Call<Genre>
}