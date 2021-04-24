package ru.neosvet.moviedb.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import ru.neosvet.moviedb.R
import java.util.*

class CatalogAdapter : RecyclerView.Adapter<CatalogAdapter.Holder>() {
    private val adapters = ArrayList<MoviesAdapter>()
    private val titles = ArrayList<String>()

    fun addItem(title: String, adapter: MoviesAdapter) {
        titles.add(title)
        adapters.add(adapter)
        notifyDataSetChanged()
    }

    fun replaceItem(index: Int, adapter: MoviesAdapter) {
        adapters[index] = adapter
        notifyItemChanged(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context).inflate(R.layout.catalog_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.setItem(titles[position], adapters[position])
    }

    override fun getItemCount() = titles.size

    fun clear() {
        adapters.clear()
        titles.clear()
    }

//HOLDER:

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: MaterialTextView = itemView.findViewById(R.id.tvTitle)
        private val rvPages: RecyclerView = itemView.findViewById(R.id.rvPages)
        private val rvMovies: RecyclerView = itemView.findViewById(R.id.rvMovies)

        init {
            rvPages.layoutManager = LinearLayoutManager(
                rvPages.context,
                RecyclerView.HORIZONTAL, false
            )
            rvMovies.layoutManager = LinearLayoutManager(
                rvMovies.context,
                RecyclerView.HORIZONTAL, false
            )
        }

        fun setItem(title: String, movies: MoviesAdapter) {
            tvTitle.text = title
            rvMovies.adapter = movies
            movies.notifyDataSetChanged()

            val pages = PageAdapter(movies)
            rvPages.adapter = pages
            pages.notifyDataSetChanged()
        }
    }
}

//CALLBACKS
interface CatalogCallbacks {
    fun onItemClicked(id: Int)
    fun onPageClicked(page: Int, adapter: MoviesAdapter)
}