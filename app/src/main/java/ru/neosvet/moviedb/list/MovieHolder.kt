package ru.neosvet.moviedb.list

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import ru.neosvet.moviedb.R

class MovieHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var ivPoster: ShapeableImageView? = null
    var tvTitle: MaterialTextView? = null
    var tvDescription: MaterialTextView? = null

    init {
        ivPoster = itemView?.findViewById(R.id.ivPoster)
        tvTitle = itemView?.findViewById(R.id.tvTitle)
        tvDescription = itemView?.findViewById(R.id.tvDescription)
    }
}