package ru.neosvet.moviedb.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.neosvet.moviedb.R
import java.util.*

class MoviesAdapter(val callbacks: ListCallbacks) : RecyclerView.Adapter<MovieHolder>() {
    private val data: ArrayList<MovieItem> = ArrayList<MovieItem>()

    fun addItem(item: MovieItem) {
        data.add(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.movie_item, parent, false)
        return MovieHolder(view)
    }

    override fun onBindViewHolder(holder: MovieHolder, position: Int) {
        holder.setItem(data[position], callbacks)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}