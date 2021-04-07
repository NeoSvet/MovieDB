package ru.neosvet.moviedb.view

import android.view.View
import com.google.android.material.snackbar.Snackbar
import ru.neosvet.moviedb.R

fun View.showError(message: String?, titleAction: String?, eventAction: View.OnClickListener?) {
    val msg = message?.let {
        ": " + it
    } ?: ""
    Snackbar.make(
        this,
        this.context.getString(R.string.error) + msg,
        Snackbar.LENGTH_INDEFINITE
    ).setAction(titleAction, eventAction)
        .show()
}