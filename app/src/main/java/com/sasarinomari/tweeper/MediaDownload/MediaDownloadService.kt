package com.sasarinomari.tweeper.MediaDownload

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.google.gson.Gson
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.Base.BaseService
import com.sasarinomari.tweeper.FirebaseLogger
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.TwitterAdapter
import twitter4j.Status
import java.io.File


class MediaDownloadService: BaseService() {
    enum class Parameters {
        StatusId
    }

    companion object {
        private const val LOG_TAG = "MediaDownloadService"
        fun checkServiceRunning(context: Context) = BaseService.checkServiceRunning(context, MediaDownloadService::class.java.name)
        
        fun convertWithFFMPEG(file: File): File {
            val newPath = File("${file.toString().substringBeforeLast(".")}.gif")
            if(newPath.exists()) newPath.delete()
            // val command = "-ss 30 -t 3 -i $file -vf \"fps=10,scale=320:-1:flags=lanczos,split[s0][s1];[s0]palettegen[p];[s1][p]paletteuse\" -loop 0 -f gif $newPath"
            val command = "-i $file $newPath"
            val rc = FFmpeg.execute(command)
            when (rc) {
                RETURN_CODE_SUCCESS -> {
                    Log.i(Config.TAG, "Command execution completed successfully.")
                }
                RETURN_CODE_CANCEL -> {
                    Log.i(Config.TAG, "Command execution cancelled by user.")
                }
                else -> {
                    Log.i(
                        Config.TAG,
                        String.format("Command execution failed with rc=%d and the output below.", rc)
                    )
                    Config.printLastCommandOutput(Log.INFO)
                }
            }
            return newPath
        }
    }

    private val twitterAdapter = TwitterAdapter()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (super.onStartCommand(intent!!, flags, startId) == START_NOT_STICKY) return START_NOT_STICKY

        startForeground(NotificationId, createNotification(getString(R.string.app_name), "Initializing...", silent = true))

        val statusId = intent.getLongExtra(Parameters.StatusId.name, -1)
        if(statusId == (-1).toLong()) throw NullPointerException()
        TwitterAdapter.TwitterInterface.setOAuthConsumer(this)
        twitterAdapter.initialize(this, AuthData.Recorder(this).getFocusedUser()!!.token!!)

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
                val status = obj as Status
                if(status.mediaEntities.isEmpty()) {
                    finish(); return
                }
                for (entitie in status.mediaEntities) {
                    when(entitie.type) {
                        "photo" -> {
                            download(entitie.mediaURLHttps)
                        }
                        "animated_gif" -> {
                            val target = entitie.videoVariants.maxBy{ v -> v.bitrate }
                            if(target!=null) {
                                download(target.url, true)
                            }
                            else {
                                FirebaseLogger(this@MediaDownloadService)
                                    .log("VideoVariantResultNull",
                                        Pair("entitie.videoVariants", Gson().toJson(entitie.videoVariants)))
                            }
                        }
                        "video" -> {
                            val target = entitie.videoVariants.maxBy{ v -> v.bitrate }
                            if(target!=null) {
                                download(target.url)
                            }
                            else {
                                FirebaseLogger(this@MediaDownloadService)
                                    .log("VideoVariantResultNull",
                                        Pair("entitie.videoVariants", Gson().toJson(entitie.videoVariants)))
                            }
                        }
                    }
                }
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
                sendNotification(getString(R.string.MediaDownloader), getString(R.string.UncaughtError),
                    false, true, Intent(), NotificationId +1)
                finish()
            }

            override fun onNetworkError(retry: () -> Unit) {
                sendNotification(getString(R.string.MediaDownloader), getString(R.string.NetworkError),
                    false, true, Intent(), NotificationId +1)
                finish()
            }
        })
    }


    /**
     * 실제 파일 다운로드 코드
     */
    private fun download(url: String, isGif: Boolean = false) {
        val fileName = url.substringAfterLast("/").substringBefore("?")

        val request = DownloadManager.Request(Uri.parse(url))
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setRequiresCharging(false) // 충전 중일 때만 다운로드 받도록 설정 해제
            .setAllowedOverMetered(true) // 데이터 네트워크에서의 다운로드 허용
            .setAllowedOverRoaming(true) // 로밍 네트워크에서의 다운로드 허용
            .setVisibleInDownloadsUi(true)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val downloadId = (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(request)
        if(isGif) {
            // TODO: 수정
            DownloadReceiver.add(this, Pair(downloadId,
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName).toString()))
        }
        finish()
    }

    
    private fun getImageContentUri(context: Context, file: File): Uri? {
        val filePath = file.absolutePath
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Images.Media._ID),
            MediaStore.Images.Media.DATA + "=? ", arrayOf(filePath), null
        )
        return if (cursor != null && cursor.moveToFirst()) {
            val id: Int = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            cursor.close()
            Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id)
        } else {
            if (file.exists()) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DATA, filePath)
                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                )
            } else {
                null
            }
        }
    }
    private fun getVideoContentUri(context: Context, file: File): Uri? {
        val filePath = file.absolutePath
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Video.Media._ID),
            MediaStore.Video.Media.DATA + "=? ", arrayOf(filePath), null
        )
        return if (cursor != null && cursor.moveToFirst()) {
            val id: Int = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            cursor.close()
            Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "" + id)
        } else {
            if (file.exists()) {
                val values = ContentValues()
                values.put(MediaStore.Video.Media.DATA, filePath)
                context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values
                )
            } else {
                null
            }
        }
    }
    private fun getOpenFileIntent(file: File): Intent {
        val map: MimeTypeMap = MimeTypeMap.getSingleton()
        val ext: String = MimeTypeMap.getFileExtensionFromUrl(file.name)
        val type: String = map.getMimeTypeFromExtension(ext) ?: "*/*"

        val data: Uri? = when{
            type.startsWith("image") -> {
                getImageContentUri(this, file)
            }
            type.startsWith("video") -> {
                getVideoContentUri(this, file)
            }
            else -> return Intent()
        }

        val intent = Intent(Intent.ACTION_VIEW)

        intent.setDataAndType(data, type)
        return intent
    }
}