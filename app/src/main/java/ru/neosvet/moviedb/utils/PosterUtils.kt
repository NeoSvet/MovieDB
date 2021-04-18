package ru.neosvet.moviedb.utils

import android.widget.ImageView
import com.squareup.picasso.Picasso
import ru.neosvet.moviedb.R

object PosterUtils {
    private val BASE_URL = "https://www.themoviedb.org/t/p/w220_and_h330_face"
    private val BASE_URL_BIG = "https://www.themoviedb.org/t/p/w600_and_h900_bestv2"

    fun load(url: String, sender: ImageView) {
        Picasso.get()
            .load(BASE_URL + url)
            .placeholder(R.drawable.no_poster)
            .into(sender);
    }

    fun loadBig(url: String, sender: ImageView) {
        Picasso.get()
            .load(BASE_URL_BIG + url)
            .placeholder(R.drawable.no_poster)
            .into(sender);
    }

    fun cancel(sender: ImageView) {
        Picasso.get().cancelRequest(sender)
    }
}