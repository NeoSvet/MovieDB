package ru.neosvet.moviedb.utils

import android.widget.ImageView
import com.squareup.picasso.Picasso
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.repository.MovieRepository

object ImageUtils {

    fun load(url: String, target: ImageView) {
        val link = if (url.contains(MovieRepository.SEPARATOR))
            url.substring(0, url.indexOf(MovieRepository.SEPARATOR))
        else
            url
        Picasso.get()
            .load(link)
            .placeholder(R.drawable.no_image)
            .into(target)
    }

    fun loadBig(url: String, target: ImageView) {
        val link = if (url.contains(MovieRepository.SEPARATOR))
            url.substring(url.indexOf(MovieRepository.SEPARATOR) + 1)
        else
            url
        Picasso.get()
            .load(link)
            .placeholder(R.drawable.no_image)
            .into(target)
    }

    fun cancel(target: ImageView) {
        Picasso.get().cancelRequest(target)
    }
}