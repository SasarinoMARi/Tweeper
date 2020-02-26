package com.sasarinomari.tweetcleaner.tweetreport

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import cn.pedant.SweetAlert.SweetAlertDialog
import com.sasarinomari.tweetcleaner.Adam
import com.sasarinomari.tweetcleaner.R
import com.sasarinomari.tweetcleaner.permissionhelper.PermissionHelper
import kotlinx.android.synthetic.main.activity_tweet_report.*

class TweetReportActivity : Adam() {
    companion object {
        var reportWritten = false
    }

    private val engine = TweetReport(this, object : TweetReport.ActivityInterface {
        override fun onFinished() {
            reportWritten = true
            runOnUiThread {
                dProcessing.dismiss()
                dDone.show()
            }
        }
    })

    private var adapter = TweetReportItem()

    private lateinit var dProcessing: SweetAlertDialog
    private lateinit var dDone: SweetAlertDialog
    private lateinit var dCant : SweetAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tweet_report)

        setOvalColor()
        listView.adapter = adapter
        initDialogs()
        initUpdateButton()
        updateReportRecordList()
    }

    private fun initDialogs() {
        dProcessing = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        dProcessing.setContentText(getString(R.string.TweetReportProcessing))
        dProcessing.setCancelable(false)

        dDone = SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
        dDone.setTitleText(getString(R.string.Done))
            .setContentText(getString(R.string.TweetReportDone))
            .setOnDismissListener {
                runOnUiThread {
                    updateReportRecordList()
                }
            }

        dCant = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
        dCant.setContentText(getString(R.string.TweetReportCant))
    }

    private fun updateReportRecordList() {
        val reports = engine.getReports()
        adapter.reports = reports
        adapter.notifyDataSetChanged()
    }

    private fun initUpdateButton() {
        button_update.setOnClickListener {
            if(!reportWritten) {
                engine.start()
                dProcessing.show()
            }
            else {
                dCant.show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        PermissionHelper.onRequestPermissionsResult(this, permissions, requestCode, grantResults) {
            finish()
        }
    }

    private fun setOvalColor() {
        val shape = oval.drawable as GradientDrawable
        shape.setColor(Color.RED)
    }
}
