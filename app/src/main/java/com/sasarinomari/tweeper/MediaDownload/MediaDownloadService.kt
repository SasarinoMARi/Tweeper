package com.sasarinomari.tweeper.MediaDownload

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.ContextCompat
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
        /*
        Log.i(LOG_TAG, "id: $id")
        sendNotification(getString(R.string.MediaDownloader), getString(R.string.DownloadStarted), silen                    val newPath = convertWithFFMPEG(file)
t = true)
        MediaTool.lookup(twitterAdapter, id, object: MediaTool.LookupInterface {
            override fun onMediaEmpty() {
                finish()
            }

            override fun onGottenUrls(fileUrl: String) {
                download(fileUrl)
            }

            override fun onDownloadGif(fileUrl: String) {
                download(fileUrl){ file ->
                    sendNotification(getString(R.string.MediaDownloader), getString(R.string.Converting), silent = true)
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
            override fun onNeedFirebaseLog(title: String, content: Pair<String, String>) {
                FirebaseLogger(this@MediaDownloadService).log(title, content)
            }
        })
         */
    }


    /**
     * 실제 파일 다운로드 코드
     */
    /*
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val filePath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            Fuel.download(url).fileDestination { _, _ -> filePath }.progress { readBytes, totalBytes ->
                val progress = readBytes.toFloat() / totalBytes.toFloat()
                restrainedNotification(getString(R.string.MediaDownloader), getString(R.string.FetchingMedia, progress.toInt()))
            }.response { _, _, _ ->
                Log.d("mediaDownload", "File downloaded to : $filePath")
                callback(filePath)
            }
        } else {
            // 이거 우선 Activity로 옮기자
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName.substringAfterLast("."));
            Log.d(LOG_TAG, "mimeType: $mimeType")

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = mimeType
            intent.putExtra(Intent.EXTRA_TITLE, fileName)

            (this.applicationContext as MediaDownloadActivity).startActivityForResult(intent, 0)
        }
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