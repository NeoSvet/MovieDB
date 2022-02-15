package ru.neosvet.moviedb.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.neosvet.moviedb.databinding.PeopleItemBinding
import java.util.*

class PeopleAdapter(
  private val showAll: (List<Person>) -> Unit
) : RecyclerView.Adapter<PeopleAdapter.Holder>() {
    private val adapters = ArrayList<PersonsAdapter>()
    private val listTitle = ArrayList<String>()
    private val listPeople = ArrayList<List<Person>>()

    fun addItem(title: String, people: List<Person>, adapter: PersonsAdapter) {
        listTitle.add(title)
        listPeople.add(people)
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
        holder.setItem(position, adapters[position])
    }

    override fun getItemCount() = listTitle.size

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
        fun setItem(position: Int, adapter: PersonsAdapter) = binding.run {
            tvTitle.text = listTitle[position]
            tvShowAll.setOnClickListener {
                showAll.invoke(listPeople[position])
            }

            rvPersons.adapter = adapter
            adapter.notifyDataSetChanged()
            if (adapter.index > 0)
                rvPersons.scrollToPosition(adapter.index)
        }
    }
}