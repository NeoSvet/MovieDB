package ru.neosvet.moviedb.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.utils.PosterHelper
import java.util.*

class MoviesAdapter(val poster: PosterHelper, val callbacks: ListCallbacks) :
    RecyclerView.Adapter<MovieHolder>() {
    private val data = ArrayList<MovieItem>()

    fun notifyItemById(id: Int) {
        for (i in data.indices) {
            if (data[i].id == id) {
                notifyItemChanged(i)
                return
            }
        }
    }

    fun addItem(item: MovieItem) {
        data.add(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.movie_item, parent, false)
        return MovieHolder(view)
    }

    override fun onBindViewHolder(holder: MovieHolder, position: Int) {
        val url = data[position].poster
        val pic = poster.getFile(url)
        if (pic.exists())
            holder.setItem(data[position], pic.path, callbacks)
        else {
            poster.load(data[position].id, url)
            holder.setItem(data[position], null, callbacks)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}