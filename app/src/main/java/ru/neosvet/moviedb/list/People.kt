package ru.neosvet.moviedb.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import ru.neosvet.moviedb.R
import java.util.*

data class Person(
    val name: String,
    val des: String
)

class PeopleAdapter(val context: Context) :
    RecyclerView.Adapter<PeopleAdapter.Holder>() {

//BASE:

    private val people = ArrayList<Person>()

    fun addPerson(name: String) {
        if (name.contains(" (")) {
            val i = name.indexOf(" (")
            people.add(
                Person(
                    name.substring(0, i),
                    name.substring(i + 2, name.length - 1)
                )
            )
        } else
            people.add(Person(name, ""))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.setItem(people[position])
    }

    override fun getItemCount() = people.size

//HOLDER:

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: MaterialTextView = itemView.findViewById(R.id.tvName)
        private val tvDes: MaterialTextView = itemView.findViewById(R.id.tvPhone)

        fun setItem(item: Person) {
            tvName.text = item.name
            tvDes.text = item.des
        }
    }
}