package ru.neosvet.moviedb.utils

import android.app.IntentService
import android.content.Intent
import android.provider.ContactsContract.CommonDataKinds.Phone
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class GetterPhones : IntentService("GetterPhones") {
    companion object {
        val ID = "id"
        val PHONE = "phone"
        val INTENT_FILTER = "GetterPhones"
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
        )
        if (cursor == null)
            return null
        var phone: String? = null
        while (cursor.moveToNext()) {
            val type = cursor.getInt(cursor.getColumnIndex(Phone.TYPE))
            if (type == Phone.TYPE_MOBILE) {
                phone = cursor.getString(cursor.getColumnIndex(Phone.NUMBER))
                break;
            }
        }
        cursor.close()
        if (phone == null || phone.contains("#") || phone.length < 11)
            return null

        return phone
    }
}