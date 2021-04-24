package ru.neosvet.moviedb.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.utils.ImageUtils
import java.util.*

class MoviesAdapter(val callbacks: CatalogCallbacks) :
    RecyclerView.Adapter<MoviesAdapter.Holder>() {
    private val data = ArrayList<MovieItem>()

    fun addItem(item: MovieItem) {
        data.add(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.movie_item, parent, false)
        return Holder(view)
    }

    override fun onViewRecycled(holder: Holder) {
        ImageUtils.cancel(holder.poster)
        super.onViewRecycled(holder)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.setItem(data[position], callbacks)
        ImageUtils.load(data[position].poster, holder.poster)
    }

    override fun getItemCount(): Int {
        return data.size
    }

//HOLDER:

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPoster: ShapeableImageView = itemView.findViewById(R.id.ivPoster)
        val poster get() = ivPoster
        private val tvTitle: MaterialTextView = itemView.findViewById(R.id.tvTitle)
        private val tvDescription: MaterialTextView = itemView.findViewById(R.id.tvDescription)
        private val indVote: ProgressBar = itemView.findViewById(R.id.barVote)

        fun setItem(item: MovieItem, callbacks: CatalogCallbacks) {
            tvTitle.text = item.title
            tvDescription.text = item.description
            indVote.progress = item.vote

            itemView.setOnClickListener {
                callbacks.onItemClicked(item.id)
            }
        }
    }
}

data class MovieItem(
    val id: Int,
    val title: String,
    val description: String,
    val poster: String,
    val vote: Int
)