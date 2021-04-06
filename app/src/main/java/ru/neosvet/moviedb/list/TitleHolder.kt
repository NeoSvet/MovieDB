package ru.neosvet.moviedb.list

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import ru.neosvet.moviedb.R

class TitleHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var tvTitle: MaterialTextView

    init {
        tvTitle = itemView.findViewById(R.id.tvTitle)
    }

    fun setItem(title: String) {
        tvTitle.text = title
    }
}