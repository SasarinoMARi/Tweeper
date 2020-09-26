package com.sasarinomari.tweeper.MediaDownload

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class MediaTool(private val context: Context,
                private val i: MediaToolInterface) {

    /**
     * 클래스 콜백 인터페이스
     */
    interface MediaToolInterface {
        fun GifConvertFailed()
        fun onUnknownTypeError()
        fun onDownloadCompleted(uri: Uri)
        fun onFinished()
    }

    /**
     * 저장하려는 미디어의 종류를 나타내는 열거형
     */
    enum class MediaType {
        Image, Video, Animation
    }

    /**
     * 해당 Url의 미디어 파일을 저장합니다.
     * Pair.first : 다운로드 할 파일 url
     * Pair.second : 해당 파일 미디어 종류
     */
    fun save(urls: Queue<Pair<String, MediaType>>) {
        val url = urls.poll()
        if(url == null) {
            i.onFinished()
            return
        }
        downloadFile(url.first) { content ->
            val path = writeFile(url, content)?: return@downloadFile
            i.onDownloadCompleted(path)
            save(urls)
        }
    }

    /**
     * url에서 파일명만 추출
     */
    private fun getFileName(url: String): String {
        return url.substringAfterLast("/").substringBefore("?")
    }

    /**
     * 실제 파일 다운로드 코드
     */
    private fun downloadFile(url: String, callback: (ByteArray) -> Unit) {
        Thread {
            val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            callback(connection.inputStream.readBytes())
        }.start()
    }

    /**
     * ByteArray로 읽어들인 파일 내용을 디바이스에 저장하는 함수
     */
    private fun writeFile(url: Pair<String, MediaType>, content: ByteArray): Uri? {
        val fileName = getFileName(url.first)
        val mimeType = getMimeType(fileName)
        if(mimeType==null) {
            i.onUnknownTypeError()
            return null
        }

        val values = ContentValues()
        val DISPLAY_NAME = if(isVideo(mimeType)) MediaStore.Video.Media.DISPLAY_NAME else MediaStore.Images.Media.DISPLAY_NAME
        val MIME_TYPE = if(isVideo(mimeType)) MediaStore.Video.Media.MIME_TYPE else MediaStore.Images.Media.MIME_TYPE
        val CONTENT_URI = if(isVideo(mimeType)) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        values.put(DISPLAY_NAME, fileName)
        values.put(MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val IS_PENDING = if(isVideo(mimeType)) MediaStore.Video.Media.IS_PENDING else MediaStore.Images.Media.IS_PENDING
            values.put(IS_PENDING, 1)
        }
        val contentResolver: ContentResolver = context.contentResolver
        var path = contentResolver.insert(CONTENT_URI, values)!!
        try {
            val pdf = contentResolver.openFileDescriptor(path, "w", null)
            if (pdf == null) {
                throw NullPointerException("ParcelFileDescriptor is Null")
            } else {
                val fos = FileOutputStream(pdf.fileDescriptor)
                fos.write(content)
                fos.close()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    val IS_PENDING = if(isVideo(mimeType)) MediaStore.Video.Media.IS_PENDING else MediaStore.Images.Media.IS_PENDING
                    values.put(IS_PENDING, 0)
                    contentResolver.update(path, values, null, null)
                }

                // Mp4 to Gif 인코딩 작업
                if(url.second == MediaType.Animation) {
                    path = encodeMp4toGif(path)
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return path
    }

    /**
     * Mp4로 저장된 파일을 Gif로 다시 저장하는 함수
     */
    private fun encodeMp4toGif(videoUri: Uri): Uri {
        val fileName = videoUri.toString().substringAfterLast("/").substringBefore(".") + ".gif"
        val mimeType = "image/gif"

        val values = ContentValues()

        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val contentResolver: ContentResolver = context.contentResolver
        var path = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
        try {
            val pdf = contentResolver.openFileDescriptor(path, "w", null)
            if (pdf == null) {
                throw NullPointerException("ParcelFileDescriptor is Null")
            } else {
                if(!convertMP4ToGif(videoUri, path)) {
                    // Gif로 변환 실패시 원본 주소 반환
                    path = videoUri
                    i.GifConvertFailed()
                }

            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return path
    }

    /**
     * mimeType이 비디오라면 참을 반환합니다.
     */
    private fun isVideo(mimeType: String): Boolean {
        return mimeType.startsWith("video/")
    }

    /**
     * 파일 이름으로 확장자를 추정해 반환합니다.
     */
    private fun getMimeType(fileName: String): String? {
        var type: String? = null
        val extension: String = MimeTypeMap.getFileExtensionFromUrl(fileName)
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        return type
    }

    /**
     * FFMPEG를 이용해 Mp4 파일을 Gif 파일로 변환하는 함수
     */
    fun convertMP4ToGif(src: Uri, dest: Uri): Boolean {
        // TODO: Content URi에서 바로 작업할 수 없는 듯 함. FileURi로 변환 후 작업 시도해볼 것
        val command = "-i $src $dest"
        return when (val result = FFmpeg.execute(command)) {
            Config.RETURN_CODE_SUCCESS -> {
                Log.i(Config.TAG, "Command execution completed successfully.")
                true
            }
            Config.RETURN_CODE_CANCEL -> {
                Log.i(Config.TAG, "Command execution cancelled by user.")
                false
            }
            else -> {
                Log.i(
                    Config.TAG,
                    String.format("Command execution failed with rc=%d and the output below.", result)
                )
                Config.printLastCommandOutput(Log.INFO)
                false
            }
        }
    }
}