package ru.neosvet.moviedb.utils

import android.content.Context
import ru.neosvet.moviedb.R

abstract class MyException : Exception("") {
    abstract fun getTranslate(context: Context): String
}

class ItemNoFoundExc : MyException() {
    override fun getTranslate(context: Context): String {
        return context.getString(R.string.item_no_found)
    }
}

class ListNoFoundExc : MyException() {
    override fun getTranslate(context: Context): String {
        return context.getString(R.string.list_no_found)
    }
}

class IncorrectResponseExc : MyException() {
    override fun getTranslate(context: Context): String {
        return context.getString(R.string.incorrect_response)
    }
}