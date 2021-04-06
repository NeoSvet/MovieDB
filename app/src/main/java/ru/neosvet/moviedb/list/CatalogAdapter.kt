package ru.neosvet.moviedb.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.neosvet.moviedb.R
import java.util.*


class CatalogAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_TITLE = 0
    private val TYPE_CATALOG = 1
    private val adapters = ArrayList<MoviesAdapter>()
    private val titles = ArrayList<String>()

    fun addItem(title: String, adapter: MoviesAdapter) {
        titles.add(title)
        adapters.add(adapter)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position % 2 == 0) TYPE_TITLE else TYPE_CATALOG
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TITLE -> {
                TitleHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.title_item, parent, false)
                )
            }
            TYPE_CATALOG -> {
                CatalogHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.catalog_item, parent, false)
                )
            }
            else -> throw Exception("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TitleHolder -> {
                holder.setItem(titles[position / 2])
            }
            is CatalogHolder -> {
                holder.setItem(adapters[position / 2])
            }
        }
    }

    override fun getItemCount(): Int {
        return titles.size + adapters.size
    }
}