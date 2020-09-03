package com.sasarinomari.tweeper.MediaDownload

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sasarinomari.tweeper.R
import java.io.File
import java.io.FileNotFoundException

class DownloadReceiver : BroadcastReceiver() {
    internal companion object GifList {
        private val key = "gifIds"

        fun add(context: Context, gifIdAndString: Pair<Long, String>) {
            val list = fetch(context)
            val prefs = context.getSharedPreferences(this::class.java.name, Context.MODE_PRIVATE).edit()
            list.add(gifIdAndString)
            prefs.putString(key, Gson().toJson(list))
            prefs.apply()
        }

        fun fetch(context: Context) : ArrayList<Pair<Long, String>> {
            val prefs = context.getSharedPreferences(this::class.java.name, Context.MODE_PRIVATE)
            // Custrom Pair 읽어오고 gif면 변환 후 리스트에서 삭제
            val json = prefs.getString(key, null)?: return ArrayList()
            return Gson().fromJson(json,  object : TypeToken<ArrayList<Pair<Long, String>>>() {}.type)
        }

        fun drop(context: Context, gifIdAndString: Pair<Long, String>) {
            val list = fetch(context)
            val prefs = context.getSharedPreferences("fr${this::class.java.name}", Context.MODE_PRIVATE).edit()
            list.remove(gifIdAndString)
            prefs.putString(key, Gson().toJson(list))
            prefs.apply()
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent.action) {
            val gifList = fetch(context)
            for(it in gifList)
                if(it.first == id) {
                    val query: DownloadManager.Query = DownloadManager.Query()
                    query.setFilterById(id)
                    val cursor = (context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).query(query)
                    if (!cursor.moveToFirst()) {
                        return
                    }

                    val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = cursor.getInt(columnIndex)
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        val file = File(it.second)
                        if(!file.exists()) {
                            throw FileNotFoundException()
                        }
                        else {
                            // sendNotification(getString(R.string.MediaDownloader), getString(R.string.Converting), silent = true)
                            val newPath = MediaDownloadService.convertWithFFMPEG(file)
                            Log.i("GIF", "GIF 내보내기 완료")
                            /*
                            val i = getOpenFileIntent(newPath)
                            sendNotification(getString(R.string.MediaDownloader), getString(R.string.DownloadCompleted),
                                silent = false,
                                cancelable = true,
                                redirect = i,
                                id = NotificationId + 1
                            )
                            finish()
                             */
                        }
                    }
                }
        } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(intent.action)) {
            Toast.makeText(context, "Notification clicked", Toast.LENGTH_SHORT).show()
        }
    }
}