package ru.neosvet.moviedb.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import ru.neosvet.moviedb.R
import java.util.*

data class Contact(
    val name: String,
    var phone: String
)

class ContactsAdapter(val callbacks: ContactCallbacks) : RecyclerView.Adapter<ContactHolder>() {
    private val contacts = ArrayList<Contact>()

    fun addConctact(name: String, phone: String) {
        contacts.add(Contact(name, phone))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        return ContactHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        holder.setItem(contacts[position], callbacks)
    }

    override fun getItemCount() = contacts.size
}

class ContactHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tvName: MaterialTextView = itemView.findViewById(R.id.tvName)
    private val tvPhone: MaterialTextView = itemView.findViewById(R.id.tvPhone)

    fun setItem(item: Contact, callbacks: ContactCallbacks) {
        tvName.text = item.name
        tvPhone.text = item.phone

        itemView.setOnClickListener {
            callbacks.onContactClicked(item.phone)
        }
    }
}

interface ContactCallbacks {
    fun onContactClicked(phone: String)
}