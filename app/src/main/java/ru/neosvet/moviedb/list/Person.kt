package ru.neosvet.moviedb.list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.databinding.PersonItemBinding
import ru.neosvet.moviedb.repository.PeopleRepository
import ru.neosvet.moviedb.repository.PersonRepoCallbacks
import ru.neosvet.moviedb.repository.room.PersonEntity
import ru.neosvet.moviedb.utils.ImageUtils

data class Person(
    val id: Int,
    var photo: String? = null,
    val name: String,
    val role: String
)

class PersonsAdapter(
    val onClick: (Int) -> Unit
) : RecyclerView.Adapter<PersonsAdapter.Holder>(), PersonRepoCallbacks {
    private val repository = PeopleRepository(this)
    private val data = ArrayList<Person>()
    private val holders = ArrayList<Holder>()
    var index: Int = 0

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<Person>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    fun addItem(item: Person) {
        data.add(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val holder = Holder(
            PersonItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        holders.add(holder)
        return holder
    }

    override fun onViewRecycled(holder: Holder) {
        holder.cancelLoad()
        super.onViewRecycled(holder)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if (data[position].photo == null)
            repository.requestPerson(data[position].id, false)
        holder.setItem(data[position])
    }

    override fun onSuccess(person: PersonEntity) {
        for (i in data.indices) {
            if (data[i].id == person.id) {
                data[i].photo = person.photo
                searchHolder(person.id)?.startLoad(person.photo)
            }
        }
    }

    private fun searchHolder(id: Int): Holder? {
        for (holder in holders) {
            if (holder.id == id)
                return holder
        }
        return null
    }

    override fun onFailure(error: Throwable) {
    }

    override fun getItemCount(): Int {
        return data.size
    }

//HOLDER:

    inner class Holder(
        private val binding: PersonItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        var id = -1
            private set

        fun setItem(item: Person) = binding.run {
            ivPhoto.setImageResource(R.drawable.no_image)
            item.photo?.let {
                ImageUtils.load(it, ivPhoto)
            }
            id = item.id
            tvName.text = item.name
            tvRole.text = item.role
            root.setOnClickListener {
                onClick.invoke(id)
            }
        }

        fun startLoad(photo: String) {
            ImageUtils.load(photo, binding.ivPhoto)
        }

        fun cancelLoad() {
            ImageUtils.cancel(binding.ivPhoto)
        }
    }
}