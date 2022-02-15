package ru.neosvet.moviedb.utils

import android.content.Context
import android.content.SharedPreferences

class ListUtils(val context: Context) {
    companion object {
        private const val NAME = "list_state"
    }
    private val pref: SharedPreferences by lazy {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }
    private val editor: SharedPreferences.Editor by lazy {
        pref.edit()
    }

    fun getIndex(name: String) = pref.getInt(name, 0)

    fun setIndex(name: String, value: Int) {
        editor.putInt(name, value)
    }

    fun getPage(name: String) = pref.getInt(name + "p", 1)

    fun setPage(name: String, value: Int) {
        editor.putInt(name + "p", value)
    }

    fun save() = editor.apply()

    fun clear() {
        editor.clear()
        editor.apply()
    }
}