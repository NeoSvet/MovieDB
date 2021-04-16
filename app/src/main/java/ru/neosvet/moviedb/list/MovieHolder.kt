package ru.neosvet.moviedb.list

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import ru.neosvet.moviedb.R

class MovieHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val ivPoster: ShapeableImageView = itemView.findViewById(R.id.ivPoster)
    private val tvTitle: MaterialTextView = itemView.findViewById(R.id.tvTitle)
    private val tvDescription: MaterialTextView = itemView.findViewById(R.id.tvDescription)

    fun setItem(item: MovieItem, picPath: String?, callbacks: ListCallbacks) {
        tvTitle.text = item.title
        tvDescription.text = item.description
        if (picPath == null)
            ivPoster.setImageResource(R.drawable.no_poster)
        else
            ivPoster.setImageURI(android.net.Uri.parse(picPath))

        itemView.setOnClickListener {
            callbacks.onItemClicked(item.id)
        }
    }
}