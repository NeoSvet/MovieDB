package ru.neosvet.moviedb.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.neosvet.moviedb.databinding.PeopleItemBinding

class PeopleAdapter(
    private val showAll: (Int, Type) -> Unit
) : RecyclerView.Adapter<PeopleAdapter.Holder>() {
    enum class Type {
        CREW, CAST
    }

    data class Data(
        val title: String,
        val movieId: Int,
        val type: Type
    )

    private val adapters = ArrayList<PersonsAdapter>()
    private val data = ArrayList<Data>()

    fun addItem(title: String, movieId: Int, type: Type, adapter: PersonsAdapter) {
        data.add(Data(title, movieId, type))
        adapters.add(adapter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(
            PeopleItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.setItem(data[position], adapters[position])
    }

    override fun getItemCount() = data.size

//HOLDER:

    inner class Holder(
        private val binding: PeopleItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            val manager = LinearLayoutManager(
                binding.root.context,
                RecyclerView.HORIZONTAL, false
            )
            binding.rvPersons.layoutManager = manager
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setItem(data: Data, adapter: PersonsAdapter) = binding.run {
            tvTitle.text = data.title
            tvShowAll.setOnClickListener {
                showAll.invoke(data.movieId, data.type)
            }

            rvPersons.adapter = adapter
            adapter.notifyDataSetChanged()
            if (adapter.index > 0)
                rvPersons.scrollToPosition(adapter.index)
        }
    }
}