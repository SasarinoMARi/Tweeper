package com.sasarinomari.tweeper.MediaDownload

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.Base.BaseService
import com.sasarinomari.tweeper.Permission.PermissionHelper
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.TwitterAdapter
import twitter4j.MediaEntity
import java.io.File
import java.lang.NullPointerException

class MediaDownloadService: BaseService() {
    enum class Parameters {
        StatusId
    }

    companion object {
        fun checkServiceRunning(context: Context) = BaseService.checkServiceRunning(context, MediaDownloadService::class.java.name)
    }

    private val twitterAdapter = TwitterAdapter()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (super.onStartCommand(intent!!, flags, startId) == START_NOT_STICKY) return START_NOT_STICKY


        startForeground(NotificationId, createNotification(getString(R.string.app_name), "Initializing...", silent = true))

        val statusId = intent.getLongExtra(Parameters.StatusId.name, -1)
        if(statusId == (-1).toLong()) throw NullPointerException()
        twitterAdapter.initialize(AuthData.Recorder(this).getFocusedUser()!!.token!!)

        runOnManagedThread {
            downloadMedia(statusId)
        }

        return START_REDELIVER_INTENT
    }


    private fun downloadMedia(id: Long) {
        sendNotification(getString(R.string.MediaDownloader), getString(R.string.DownloadStarted), silent = true)
        val status = twitterAdapter.twitter.client.showStatus(id)
        for (entitie in status.mediaEntities) {
            when(entitie.type) {
                "photo" -> {
                    download(entitie.mediaURLHttps)
                }
                "animated_gif" -> {
                    val target = entitie.videoVariants.maxBy{ v -> v.bitrate }
                    if(target!=null) {
                        download(target.url)
                        // TODO: 이것을 변환
                    }
                    else {
                        // TODO: logigng
                    }
                }
                "video" -> {
                    val target = entitie.videoVariants.maxBy{ v -> v.bitrate }
                    if(target!=null) {
                        download(target.url)
                    }
                    else {
                        // TODO: logigng
                    }
                }
            }
        }
        Log.i("downloadMedia", status.text)
    }

    /**
     * 실제 파일 다운로드 코드
     */
    private fun download(url: String) {
        val fileName = url.substringAfterLast("/").substringBefore("?")
        val filePath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), fileName)
        Fuel.download(url).fileDestination { _, _ -> filePath }.progress { readBytes, totalBytes ->
            val progress = readBytes.toFloat() / totalBytes.toFloat()
            restrainedNotification(getString(R.string.MediaDownloader), getString(R.string.FetchingMedia, progress.toInt()))
        }.response { _, _, result ->
            sendNotification(getString(R.string.MediaDownloader), getString(R.string.DownloadCompleted),
                silent = false,
                cancelable = true,
                redirect = Intent(),
                /*redirect = getOpenFileIntent(filePath.toString()),*/
                id = NotificationId + 1
            )
            Log.d("mediaDownload", "File downloaded to : $filePath")

            // 서비스 종료
            this@MediaDownloadService.stopForeground(true)
            this@MediaDownloadService.stopSelf()
        }
    }

    private fun getOpenFileIntent(filePath: String): Intent {
        val file = File(filePath)
        val map: MimeTypeMap = MimeTypeMap.getSingleton()
        val ext: String = MimeTypeMap.getFileExtensionFromUrl(file.name)
        val type: String = map.getMimeTypeFromExtension(ext) ?: "*/*"

        val intent = Intent(Intent.ACTION_VIEW)
        val data: Uri = Uri.fromFile(file)

        intent.setDataAndType(data, type)
        return intent
    }
}