package com.sasarinomari.tweetcleaner

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sasarinomari.tweetcleaner.permissionhelper.PermissionHelper

class TweetReport : Adam() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tweet_report)

        PermissionHelper.activatePermission(this, arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        PermissionHelper.onRequestPermissionsResult(this, permissions, requestCode, grantResults) {
            finish()
        }
    }
}
