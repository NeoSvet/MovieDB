package ru.neosvet.moviedb.list

import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import ru.neosvet.moviedb.R

class MovieHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val ivPoster: ShapeableImageView = itemView.findViewById(R.id.ivPoster)
    val poster get() = ivPoster
    private val tvTitle: MaterialTextView = itemView.findViewById(R.id.tvTitle)
    private val tvDescription: MaterialTextView = itemView.findViewById(R.id.tvDescription)
    private val indVote: ProgressBar = itemView.findViewById(R.id.barVote)

    fun setItem(item: MovieItem, callbacks: ListCallbacks) {
        tvTitle.text = item.title
        tvDescription.text = item.description
        indVote.progress = item.vote

        itemView.setOnClickListener {
            callbacks.onItemClicked(item.id)
        }
    }
}