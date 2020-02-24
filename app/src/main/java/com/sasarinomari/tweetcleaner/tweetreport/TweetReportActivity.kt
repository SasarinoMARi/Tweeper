package com.sasarinomari.tweetcleaner.tweetreport

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import com.sasarinomari.tweetcleaner.Adam
import com.sasarinomari.tweetcleaner.R
import com.sasarinomari.tweetcleaner.permissionhelper.PermissionHelper
import kotlinx.android.synthetic.main.activity_tweet_report.*

class TweetReportActivity : Adam() {

    private val engine = TweetReport(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tweet_report)

        setOvalColor()
        initUpdateButton()
        initReportRecord()
    }

    private fun initReportRecord() {
        engine.getReports()
    }

    private fun initUpdateButton() {
        button_update.setOnClickListener {
            engine.start()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        PermissionHelper.onRequestPermissionsResult(this, permissions, requestCode, grantResults) {
            finish()
        }
    }

    private fun setOvalColor() {
        val shape = oval.drawable as GradientDrawable
        shape.setColor(Color.RED)
    }
}
