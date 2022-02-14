package ru.neosvet.moviedb.utils

import android.app.IntentService
import android.content.Intent
import android.provider.ContactsContract.CommonDataKinds.Phone
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class GetterPhones : IntentService("GetterPhones") {
    companion object {
        const val ID = "id"
        const val PHONE = "phone"
        const val INTENT_FILTER = "GetterPhones"
    }

    override fun onHandleIntent(intent: Intent?) {
        intent?.let {
            foundPhone(it.getIntExtra(ID, -1))
        }
    }

    private fun foundPhone(id: Int) {
        if (id == -1)
            return
        try {
            val phone = getPhone(id)

            val broadcastIntent = Intent(INTENT_FILTER)
            broadcastIntent.putExtra(ID, id)
            broadcastIntent.putExtra(PHONE, phone)
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getPhone(id: Int): String? {
        val cursor = contentResolver.query(
            Phone.CONTENT_URI, null,
            Phone.CONTACT_ID + " = " + id, null, null
        ) ?: return null
        var phone: String? = null
        if (cursor.moveToFirst()) {
            val iType = cursor.getColumnIndex(Phone.TYPE)
            val iNumber = cursor.getColumnIndex(Phone.NUMBER)
            do {
                val type = cursor.getInt(iType)
                if (type == Phone.TYPE_MOBILE) {
                    phone = cursor.getString(iNumber)
                    break
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        if (phone == null || phone.contains("#") || phone.length < 11)
            return null

        return phone
    }
}