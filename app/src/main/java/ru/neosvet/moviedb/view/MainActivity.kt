package ru.neosvet.moviedb.view

import android.content.IntentFilter
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.utils.ConnectRec

class MainActivity : AppCompatActivity() {
    private val recConnect = ConnectRec()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerReceiver(recConnect, IntentFilter(CONNECTIVITY_ACTION));
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ListFragment())
                .commitNow()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(recConnect)
    }
}