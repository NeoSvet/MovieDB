package ru.neosvet.moviedb.list

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.utils.Poster
import ru.neosvet.moviedb.utils.PosterHelper
import java.io.File
import java.util.*

class CatalogAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    PosterHelper {
    private val TYPE_TITLE = 0
    private val TYPE_CATALOG = 1
    private val adapters = ArrayList<MoviesAdapter>()
    private val titles = ArrayList<String>()
    private var isRegRec = false
    private val poster: Poster by lazy {
        Poster(context)
    }
    private val recPoster = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getIntExtra(Poster.ID, -1)
            if (id == -1) {
                isRegRec = false
                LocalBroadcastManager.getInstance(context).unregisterReceiver(this)
            } else
                notifyItemById(id)
        }
    }

    private fun notifyItemById(id: Int) {
        for (adapter in adapters)
            adapter.notifyItemById(id)
    }

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
                    LayoutInflater.from(context).inflate(R.layout.title_item, parent, false)
                )
            }
            TYPE_CATALOG -> {
                CatalogHolder(
                    LayoutInflater.from(context).inflate(R.layout.catalog_item, parent, false)
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

    fun clear() {
        adapters.clear()
        titles.clear()
    }

    override fun getFile(url: String): File {
        return poster.getFile(url)
    }

    override fun load(id: Int, url: String) {
        poster.startService(id, url)
        if (!isRegRec)
            LocalBroadcastManager.getInstance(context)
                .registerReceiver(recPoster, IntentFilter(Poster.BROADCAST_INTENT_FILTER))
    }
}