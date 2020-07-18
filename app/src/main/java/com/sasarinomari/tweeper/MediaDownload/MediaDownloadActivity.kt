package com.sasarinomari.tweeper.MediaDownload

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.github.kittinunf.fuel.Fuel
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.Permission.PermissionHelper
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.TwitterAdapter
import twitter4j.MediaEntity
import java.io.File

open class MediaDownloadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PermissionHelper.activatePermission(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if(Intent.ACTION_SEND == intent.action) {
                /**
                 * 공유하기를 통해 접근한 경우 인텐트에서 url을 추출해 작업을 시작합니다.
                 */
                if("text/plain" == intent.type) {
                    val url = intent.getStringExtra(Intent.EXTRA_TEXT)?:return@activatePermission
                    downloadMedia(url)
                    finish()
                }
            }
            else {
                /**
                 * 공유하기를 통해 오지 않은 경우 UI를 초기화하고 직접 url을 입력받습니다.
                 */
                setContentView(R.layout.full_recycler_view)

                downloadMedia("https://twitter.com/khm_bl/status/1284334328080732161?s=20")
            }
        }
    }


    /**
     * 잘못된 url로 요청되었을 경우.
     */
    private fun invalidUrl() {
        TODO("Not yet implemented")
    }

    /**
     * url로부터 status id만을 추출합니다.
     */
    private fun getStatusId(url: String): Long {
        val b = url.substringAfterLast("/")
        val regex = """[0-9]+""".toRegex()
        val matchResult = regex.find(b)?.value ?: return -1
        return matchResult.toLong()
    }

    /**
     * url로 파일을 다운로드하는 메인 코드
     */
    private fun downloadMedia(url: String) {
        Log.i("downloadMedia", url)
        val id = getStatusId(url)
        if (id == (-1).toLong()) {
            invalidUrl()
        }

        val i = Intent(this, MediaDownloadService::class.java)
        i.putExtra(MediaDownloadService.Parameters.StatusId.name, id)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(i)
        } else {
            startService(i)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionHelper.onRequestPermissionsResult(this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), requestCode, grantResults) {
            Toast.makeText(this, getString(R.string.PermissionDenied), Toast.LENGTH_LONG).show()
            Log.i("log", getString(R.string.PermissionDenied))
            finish()
        }
    }

}