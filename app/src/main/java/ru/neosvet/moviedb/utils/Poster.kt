package ru.neosvet.moviedb.utils

import android.app.IntentService
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class Poster(val context: Context) {
    companion object {
        val ID = "id"
        val URL = "url"
        val BROADCAST_INTENT_FILTER = "POSTER FILTER"
    }

    val FOLDER = "/posters"

    fun getFile(url: String): File {
        var file = File(context.filesDir.path + FOLDER)
        if (!file.exists()) file.mkdirs()
        file = File(file.path + url)
        return file
    }

    fun startService(id: Int, url: String) {
        val intent = Intent(context, PosterSrv::class.java)
        intent.putExtra(ID, id)
        intent.putExtra(URL, url)
        context.startService(intent)
    }
}

interface PosterHelper {
    fun getFile(url: String) : File
    fun load(id: Int, url: String)
}

class PosterSrv : IntentService("Poster Loader") {
    val BASE_URL = "https://www.themoviedb.org/t/p/w220_and_h330_face"
    val queue = ArrayList<Int>()

    override fun onHandleIntent(intent: Intent?) {
        intent?.let {
            startLoad(it.getIntExtra(Poster.ID, -1), it.getStringExtra(Poster.URL))
        }
    }

    private fun startLoad(id: Int, url: String?) {
        if (url == null || queue.contains(id))
            return
        queue.add(id)
        val uri = URL(BASE_URL + url)
        val poster = Poster(applicationContext)
        val file = poster.getFile(url)

        lateinit var urlConnection: HttpsURLConnection
        var writer: FileOutputStream? = null
        var reader: BufferedInputStream? = null
        try {
            urlConnection = uri.openConnection() as HttpsURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.readTimeout = 10000
            val size_max = urlConnection.contentLength
            writer = FileOutputStream(file)
            reader = BufferedInputStream(urlConnection.inputStream)
            var b: Int
            var size: Int = 0
            val buf = ByteArray(1024)
            while (reader.read(buf).also { b = it } > 0) {
                writer.write(buf, 0, b)
                writer.flush()
                size += b
                if (size == size_max)
                    break
            }
            reportFinishLoad(id)
        } catch (e: Exception) {
            e.printStackTrace()
            if (file.exists())
                file.delete()
        } finally {
            writer?.close()
            reader?.close()
            urlConnection.disconnect()
        }
        queue.remove(id)
    }

    private fun reportFinishLoad(id: Int) {
        val broadcastIntent = Intent(Poster.BROADCAST_INTENT_FILTER)
        broadcastIntent.putExtra(Poster.ID, id)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)
    }

    override fun onDestroy() {
        reportFinishLoad(-1)
        super.onDestroy()
    }
}