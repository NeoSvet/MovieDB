package ru.neosvet.moviedb.utils

import android.content.Context
import android.content.SharedPreferences

class SettingsUtils(val context: Context) {
    companion object {
        private const val NAME = "settings"
        private const val ADULT = "adult"
    }
    private val pref: SharedPreferences by lazy {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }
    private val editor: SharedPreferences.Editor by lazy {
        pref.edit()
    }

    fun getAdult(): Boolean = pref.getBoolean(ADULT, false)

    fun setAdult(value: Boolean) {
        editor.putBoolean(ADULT, value)
        editor.apply()
    }
}