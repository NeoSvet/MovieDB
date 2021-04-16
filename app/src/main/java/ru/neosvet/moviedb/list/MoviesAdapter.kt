package ru.neosvet.moviedb.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.utils.PosterUtils
import java.util.*

class MoviesAdapter(val callbacks: ListCallbacks) : RecyclerView.Adapter<MovieHolder>() {
    private val data = ArrayList<MovieItem>()

    fun addItem(item: MovieItem) {
        data.add(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.movie_item, parent, false)
        return MovieHolder(view)
    }

    override fun onViewRecycled(holder: MovieHolder) {
        PosterUtils.cancel(holder.poster)
        super.onViewRecycled(holder)
    }

    override fun onBindViewHolder(holder: MovieHolder, position: Int) {
        holder.setItem(data[position], callbacks)
        PosterUtils.load(data[position].poster, holder.poster)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}