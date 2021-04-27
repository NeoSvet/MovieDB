package ru.neosvet.moviedb.utils

import android.widget.ImageView
import com.squareup.picasso.Picasso
import ru.neosvet.moviedb.R

object ImageUtils {
    private val BASE_URL = "https://www.themoviedb.org/t/p/w220_and_h330_face"
    private val BASE_URL_BIG = "https://www.themoviedb.org/t/p/w600_and_h900_bestv2"

    fun load(url: String, target: ImageView) {
        Picasso.get()
            .load(BASE_URL + url)
            .placeholder(R.drawable.no_image)
            .into(target)
    }

    fun loadBig(url: String, target: ImageView) {
        Picasso.get()
            .load(BASE_URL_BIG + url)
            .placeholder(R.drawable.no_image)
            .into(target)
    }

    fun cancel(target: ImageView) {
        Picasso.get().cancelRequest(target)
    }
}