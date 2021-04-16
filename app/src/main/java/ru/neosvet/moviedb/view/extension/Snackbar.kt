package ru.neosvet.moviedb.view.extension

import android.view.View
import com.google.android.material.snackbar.Snackbar
import ru.neosvet.moviedb.R

fun View.showError(
    message: String?,
    titleAction: String?,
    eventAction: View.OnClickListener?
): Snackbar {
    val msg = message?.let {
        ": " + it
    } ?: ""
    val bar = Snackbar.make(
        this,
        this.context.getString(R.string.error) + msg,
        Snackbar.LENGTH_INDEFINITE
    ).setAction(titleAction, eventAction)
    bar.show()
    return bar
}