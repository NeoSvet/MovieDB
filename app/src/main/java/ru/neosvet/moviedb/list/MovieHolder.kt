package ru.neosvet.moviedb.list

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import ru.neosvet.moviedb.R

class MovieHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var ivPoster: ShapeableImageView
    var tvTitle: MaterialTextView
    var tvDescription: MaterialTextView

    init {
        ivPoster = itemView.findViewById(R.id.ivPoster)
        tvTitle = itemView.findViewById(R.id.tvTitle)
        tvDescription = itemView.findViewById(R.id.tvDescription)
    }

    fun setItem(item: MovieItem, callbacks: ListCallbacks) {
        tvTitle.text = item.title
        tvDescription.text = item.description
        itemView.setOnClickListener {
            callbacks.onItemClicked(item.id)
        }
    }
}