package ru.neosvet.moviedb.view.extension

import androidx.fragment.app.Fragment

abstract class OnBackFragment : Fragment() {
    abstract fun onBackPressed(): Boolean
}