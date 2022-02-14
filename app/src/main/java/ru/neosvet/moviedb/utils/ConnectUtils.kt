package ru.neosvet.moviedb.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import ru.neosvet.moviedb.R

class ConnectUtils : BroadcastReceiver() {
    companion object {
        var CONNECTED: Boolean? = null
        var observer: ConnectObserver? = null
        private var isDisconnected = false

        fun subscribe(observer: ConnectObserver) {
            ConnectUtils.observer = observer
            CONNECTED?.let {
                observer.connectChanged(it)
            }
        }

        fun unSubscribe(observer: ConnectObserver) {
            if (ConnectUtils.observer?.equals(observer) == true)
                ConnectUtils.observer = null
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        CONNECTED = if (intent.getBooleanExtra("noConnectivity", false)) {
            isDisconnected = true
            if (observer != null)
                Toast.makeText(context, R.string.no_connected, Toast.LENGTH_SHORT).show()
            false
        } else {
            if (observer != null && isDisconnected)
                Toast.makeText(context, R.string.connected, Toast.LENGTH_SHORT).show()
            isDisconnected = false
            true
        }
        observer?.connectChanged(CONNECTED ?: false)
    }
}

interface ConnectObserver {
    fun connectChanged(connected: Boolean)
}