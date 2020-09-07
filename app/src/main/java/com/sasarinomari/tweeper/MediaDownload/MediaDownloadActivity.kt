package com.sasarinomari.tweeper.MediaDownload

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.Permission.PermissionHelper
import com.sasarinomari.tweeper.R
import kotlinx.android.synthetic.main.activity_media_download.*
import kotlinx.android.synthetic.main.fragment_card_button.view.*
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*

open class MediaDownloadActivity : BaseActivity() {

    private val permissions= arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE)

    private var layoutInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionHelper.activatePermission(this, permissions) {
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
                setContentView(R.layout.activity_media_download)
                setDefaultBoxColor(ContextCompat.getColor(this, R.color.white))
                layoutInitialized = true

                layout_title.title_text.text = getString(R.string.MediaDownloader)
                layout_title.title_description.text = getString(R.string.MediaDownloaderDescription)

                layout_button.cardbutton_image.setOvalColor(ContextCompat.getColor(this@MediaDownloadActivity, R.color.bluegrey))
                layout_button.cardbutton_image.setImageResource(R.drawable.download)
                layout_button.cardbutton_text.text = getString(R.string.Download)
                layout_button.setOnClickListener {
                    val url = input_url.text.toString()
                    downloadMedia(url)
                    input_url.setText("")
                }
            }
        }
    }

    private fun setDefaultBoxColor(color: Int) {
        try {
            val defaultStrokeColorField = TextInputLayout::class.java.getDeclaredField("defaultStrokeColor")
            defaultStrokeColorField.isAccessible = true
            defaultStrokeColorField.set(_input1, color)
            val defaultTextColorFiled = TextInputLayout::class.java.getDeclaredField("defaultTextColor")
            defaultTextColorFiled.isAccessible = true
            defaultTextColorFiled.set(input_url, color)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * 잘못된 url로 요청되었을 경우.
     */
    private fun invalidUrl() {
        runOnUiThread {
            if(layoutInitialized) {
                da.error(getString(R.string.Error), getString(R.string.InvalidUrl)).show()
            }
            else {
                Toast.makeText(this, getString(R.string.InvalidUrl), Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    /**
     * url로부터 status id만을 추출합니다.
     */
    private fun getStatusId(url: String): Long {
        if(url.isEmpty()) return -1
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
            return
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
        PermissionHelper.onRequestPermissionsResult(this, permissions, requestCode, grantResults) {
            Toast.makeText(this, getString(R.string.PermissionDenied), Toast.LENGTH_LONG).show()
            Log.i("log", getString(R.string.PermissionDenied))
            finish()
        }
    }

}