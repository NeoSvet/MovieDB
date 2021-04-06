package ru.neosvet.moviedb.list

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.neosvet.moviedb.R

class CatalogHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val rvMovies: RecyclerView = itemView.findViewById(R.id.rvMovies)

    init {
        rvMovies.layoutManager = LinearLayoutManager(
            rvMovies.context,
            RecyclerView.HORIZONTAL, false
        )
    }

    fun setItem(adapter: MoviesAdapter) {
        rvMovies.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}