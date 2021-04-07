package ru.neosvet.moviedb.list

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.utils.Poster
import java.util.*

class MoviesAdapter(val callbacks: ListCallbacks) : RecyclerView.Adapter<MovieHolder>() {
    private val data = ArrayList<MovieItem>()
    private lateinit var context: Context
    private lateinit var poster: Poster
    private var isRegRec = false
    private val recPoster = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getIntExtra(Poster.ID, -1)
            if (id == -1) {
                LocalBroadcastManager.getInstance(context).unregisterReceiver(this)
                isRegRec = false
            } else
                notifyItemById(id)
        }
    }

    private fun notifyItemById(id: Int) {
        for (i in data.indices) {
            if (data[i].id == id) {
                notifyItemChanged(i)
                return
            }
        }
    }

    fun addItem(item: MovieItem) {
        data.add(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.movie_item, parent, false)
        context = view.context
        poster = Poster(context)
        return MovieHolder(view)
    }

    override fun onBindViewHolder(holder: MovieHolder, position: Int) {
        val url = data[position].poster
        val pic = poster.getFile(url)
        if (pic.exists())
            holder.setItem(data[position], pic.path, callbacks)
        else {
            if (!isRegRec)
                LocalBroadcastManager.getInstance(context)
                    .registerReceiver(recPoster, IntentFilter(Poster.BROADCAST_INTENT_FILTER))
            poster.startService(data[position].id, url)
            holder.setItem(data[position], null, callbacks)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}