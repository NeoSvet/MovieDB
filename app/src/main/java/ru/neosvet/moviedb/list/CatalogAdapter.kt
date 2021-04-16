package ru.neosvet.moviedb.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.neosvet.moviedb.R
import java.util.*

class CatalogAdapter : RecyclerView.Adapter<CatalogHolder>() {
    private val adapters = ArrayList<MoviesAdapter>()
    private val titles = ArrayList<String>()

    fun addItem(title: String, adapter: MoviesAdapter) {
        titles.add(title)
        adapters.add(adapter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogHolder {
        return CatalogHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.catalog_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CatalogHolder, position: Int) {
        holder.setItem(titles[position], adapters[position])
    }

    override fun getItemCount() = titles.size

    fun clear() {
        adapters.clear()
        titles.clear()
    }
}