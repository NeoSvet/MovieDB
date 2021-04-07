package ru.neosvet.moviedb.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import ru.neosvet.moviedb.R

class ConnectRec : BroadcastReceiver() {
    companion object {
        var CONNECTED: Boolean = false
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getBooleanExtra("noConnectivity", false)) {
            Toast.makeText(context, R.string.no_connected, Toast.LENGTH_SHORT).show()
            CONNECTED = false
        } else {
            Toast.makeText(context, R.string.connected, Toast.LENGTH_SHORT).show()
            CONNECTED = true
        }
    }
}