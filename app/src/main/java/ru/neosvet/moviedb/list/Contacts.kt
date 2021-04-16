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
import com.google.android.material.textview.MaterialTextView
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.utils.GetterPhones
import java.util.*

data class Contact(
    val id: Int,
    val name: String,
    var phone: String? = null
)

class ContactsAdapter(val context: Context, val callbacks: ContactCallbacks) :
    RecyclerView.Adapter<ContactsAdapter.Holder>() {
    //BASE:
    private val contacts = ArrayList<Contact>()

    fun addConctact(id: Int, name: String) {
        contacts.add(Contact(id, name))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.setItem(contacts[position], callbacks)
    }

    override fun getItemCount() = contacts.size

    //GETTER PHONES:
    private val getterPhones = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getIntExtra(GetterPhones.ID, -1)
            if (id == -1)
                return
            val phone = intent.getStringExtra(GetterPhones.PHONE)
            notifyItemById(id, phone)
        }
    }

    init {
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(getterPhones, IntentFilter(GetterPhones.INTENT_FILTER))
    }

    private fun notifyItemById(id: Int, phone: String?) {
        for (i in contacts.indices) {
            if (contacts[i].id == id) {
                if (phone == null) {
                    contacts.removeAt(i)
                    notifyItemRemoved(i)
                } else {
                    contacts[i].phone = phone
                    notifyItemChanged(i)
                }
                return
            }
        }
    }

    fun getPhoneFor(id: Int) {
        val intent = Intent(context, GetterPhones::class.java)
        intent.putExtra(GetterPhones.ID, id)
        context.startService(intent)
    }

    fun onDestory() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(getterPhones)
    }

    //HOLDER:
    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: MaterialTextView = itemView.findViewById(R.id.tvName)
        private val tvPhone: MaterialTextView = itemView.findViewById(R.id.tvPhone)
        private var phone: String? = null

        fun setItem(item: Contact, callbacks: ContactCallbacks) {
            tvName.text = item.name
            if (item.phone == null) {
                tvPhone.text = "..."
                getPhoneFor(item.id)
            } else {
                tvPhone.text = item.phone
                phone = item.phone
            }

            itemView.setOnClickListener {
                phone?.let {
                    callbacks.onContactClicked(it)
                }
            }
        }
    }
}

//CALLBACKS
interface ContactCallbacks {
    fun onContactClicked(phone: String)
}