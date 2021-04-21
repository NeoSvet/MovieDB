package ru.neosvet.moviedb.utils

object DateUtils {
    val DAY_IN_MILLS = 86400000
    fun getNow() = System.currentTimeMillis()
    fun olderThenDay(time: Long): Boolean {
        return getNow() - time > DAY_IN_MILLS
    }
    fun format(date: String?): String {
        date?.let {
            val m = it.split("-")
            if (m.size != 3)
                return it
            return "${m[2]}.${m[1]}.${m[0]}"
        }
        return ""
    }
}

