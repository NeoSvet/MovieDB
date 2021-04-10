package ru.neosvet.moviedb.utils

import android.widget.ImageView
import com.squareup.picasso.Picasso
import ru.neosvet.moviedb.R

object PosterUtils {
    private val BASE_URL = "https://www.themoviedb.org/t/p/w220_and_h330_face"

    fun load(url: String, sender: ImageView) {
        Picasso.get()
            .load(BASE_URL + url)
            .placeholder(R.drawable.no_poster)
            .into(sender);
    }

    fun cancel(sender: ImageView) {
        Picasso.get().cancelRequest(sender)
    }
}