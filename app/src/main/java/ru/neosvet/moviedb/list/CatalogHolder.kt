package ru.neosvet.moviedb.list

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import ru.neosvet.moviedb.R

class CatalogHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tvTitle: MaterialTextView = itemView.findViewById(R.id.tvTitle)
    private val rvMovies: RecyclerView = itemView.findViewById(R.id.rvMovies)

    init {
        rvMovies.layoutManager = LinearLayoutManager(
            rvMovies.context,
            RecyclerView.HORIZONTAL, false
        )
    }

    fun setItem(title: String, adapter: MoviesAdapter) {
        tvTitle.text = title
        rvMovies.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}