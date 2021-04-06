package ru.neosvet.moviedb.list

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.neosvet.moviedb.R

class CatalogHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var rvMovies: RecyclerView

    init {
        rvMovies = itemView.findViewById(R.id.rvMovies)
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