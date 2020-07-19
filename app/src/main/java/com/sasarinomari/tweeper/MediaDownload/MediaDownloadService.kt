package com.sasarinomari.tweeper.MediaDownload

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
import com.github.kittinunf.fuel.Fuel
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
                                download(target.url) { file ->
                                    sendNotification(getString(R.string.MediaDownloader), getString(R.string.Converting), silent = true)
                                    val newPath = convertWithFFMPEG(file)
                                    val i = getOpenFileIntent(newPath)
                                    sendNotification(getString(R.string.MediaDownloader), getString(R.string.DownloadCompleted),
                                        silent = false,
                                        cancelable = true,
                                        redirect = i,
                                        id = NotificationId + 1
                                    )
                                    finish()
                                }
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

        })
    }

    private fun convertWithFFMPEG(file: File): File {
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

    /**
     * 실제 파일 다운로드 코드
     */
    private fun download(url: String, callback: (File)-> Unit = {
        val i = getOpenFileIntent(it)
        sendNotification(getString(R.string.MediaDownloader), getString(R.string.DownloadCompleted),
            silent = false,
            cancelable = true,
            redirect = i,
            id = NotificationId + 1
        )
        finish()
    }) {
        val fileName = url.substringAfterLast("/").substringBefore("?")
        val filePath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), fileName)
        Fuel.download(url).fileDestination { _, _ -> filePath }.progress { readBytes, totalBytes ->
            val progress = readBytes.toFloat() / totalBytes.toFloat()
            restrainedNotification(getString(R.string.MediaDownloader), getString(R.string.FetchingMedia, progress.toInt()))
        }.response { _, _, _ ->
            Log.d("mediaDownload", "File downloaded to : $filePath")
            callback(filePath)
        }
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