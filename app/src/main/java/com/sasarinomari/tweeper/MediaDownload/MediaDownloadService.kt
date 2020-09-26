package com.sasarinomari.tweeper.MediaDownload

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import com.google.gson.Gson
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.Base.BaseService
import com.sasarinomari.tweeper.FirebaseLogger
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.TwitterAdapter
import twitter4j.Status
import java.io.File
import java.util.*


class MediaDownloadService: BaseService() {
    enum class Parameters {
        StatusId
    }

    companion object {
        private const val LOG_TAG = "MediaDownloadService"
        fun checkServiceRunning(context: Context) = BaseService.checkServiceRunning(context, MediaDownloadService::class.java.name)
    }

    private val mt = MediaTool(this, object: MediaTool.MediaToolInterface{
        override fun GifConvertFailed() {
            TODO("Not yet implemented")
        }

        override fun onUnknownTypeError() {
            TODO("Not yet implemented")
        }

        override fun onDownloadCompleted(uri: Uri) {
            val openFileIntent = Intent(Intent.ACTION_VIEW)
            openFileIntent.data = uri
            sendNotification(getString(R.string.MediaDownloader), getString(R.string.DownloadCompleted),
                silent = false, cancelable = true, redirect = openFileIntent, id = uri.hashCode())
        }

        override fun onFinished() {
            finish()
        }
    })

    private val twitterAdapter = TwitterAdapter()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (super.onStartCommand(intent!!, flags, startId) == START_NOT_STICKY) return START_NOT_STICKY

        startForeground(NotificationId, createNotification(getString(R.string.app_name), "Initializing...", silent = true))

        // 트위터 로그인
        val statusId = intent.getLongExtra(Parameters.StatusId.name, -1)
        if(statusId == (-1).toLong()) throw NullPointerException()
        TwitterAdapter.TwitterInterface.setOAuthConsumer(this)
        twitterAdapter.initialize(AuthData.Recorder(this).getFocusedUser()!!.token!!)

        runOnManagedThread {
            downloadMedia(statusId)
        }

        return START_REDELIVER_INTENT
    }

    /**
     * Status ID로 트윗을 조회한 뒤 안에있는 미디어 객체의 다운로드를 요청합니다.
     */
    private fun downloadMedia(id: Long) {
        Log.i(LOG_TAG, "id: $id")
        sendNotification(getString(R.string.MediaDownloader), getString(R.string.DownloadStarted), silent = true)
        twitterAdapter.lookStatus(id, object: TwitterAdapter.FoundObjectInterface {
            override fun onStart() { }

            override fun onFinished(obj: Any) {
                val targetUrls = LinkedList<Pair<String, MediaTool.MediaType>>()
                val status = obj as Status
                if(status.mediaEntities.isEmpty()) {
                    finish(); return
                }
                for (entitie in status.mediaEntities) {
                    when(entitie.type) {
                        "photo" -> {
                            targetUrls.add(Pair(entitie.mediaURLHttps, MediaTool.MediaType.Image))
                            // TODO: 여러 사진 다운로드에 대한 콜백 처리
                        }
                        "animated_gif" -> {
                            val target = entitie.videoVariants.maxBy{ v -> v.bitrate }
                            if(target!=null) targetUrls.add(Pair(target.url, MediaTool.MediaType.Animation))
                            else FirebaseLogger(this@MediaDownloadService)
                                .log("VideoVariantResultNull", Pair("entitie.videoVariants", Gson().toJson(entitie.videoVariants)))
                        }
                        "video" -> {
                            val target = entitie.videoVariants.maxBy{ v -> v.bitrate }
                            if(target!=null) targetUrls.add(Pair(target.url, MediaTool.MediaType.Video))
                            else FirebaseLogger(this@MediaDownloadService)
                                .log("VideoVariantResultNull", Pair("entitie.videoVariants", Gson().toJson(entitie.videoVariants)))
                        }
                    }
                }

                mt.save(targetUrls)
            }

            override fun onRateLimit() {
                sendNotification(getString(R.string.MediaDownloader), getString(R.string.RateLimitWaiting))

            }

            override fun onNotFound() {
                sendNotification(getString(R.string.MediaDownloader), getString(R.string.StatusNotFound),
                    false, true, Intent(), NotificationId +1)
                finish()
            }

            override fun onUncaughtError() {
                TODO("Not yet implemented")
            }

            override fun onNetworkError(retry: () -> Unit) {
                TODO("Not yet implemented")
            }
        })
    }
}