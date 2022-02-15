package ru.neosvet.moviedb.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import ru.neosvet.moviedb.R

class PageAdapter(private val movies: MoviesAdapter) : RecyclerView.Adapter<PageHolder>() {
    private val current: Int = movies.catalog.page
    private val count: Int = movies.catalog.total_pages

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHolder {
        return PageHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.page_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PageHolder, position: Int) {
        val page = position + 1
        holder.setItem(page, page == current, movies)
    }

    override fun getItemCount() = count
}

class PageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tvPage: MaterialTextView = itemView as MaterialTextView

    @SuppressLint("ResourceAsColor")
    fun setItem(page: Int, select: Boolean, movies: MoviesAdapter) {
        tvPage.text = page.toString()
        if (select) {
            tvPage.background = ContextCompat.getDrawable(
                tvPage.context, R.drawable.field_with_border
            )
        } else {
            tvPage.setBackgroundColor(android.R.color.transparent)
            tvPage.setOnClickListener {
                movies.callbacks.onPageClicked(page, movies)
            }
        }
    }
}