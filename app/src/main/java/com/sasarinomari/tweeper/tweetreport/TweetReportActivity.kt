package com.sasarinomari.tweeper.tweetreport

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import cn.pedant.SweetAlert.SweetAlertDialog
import com.sasarinomari.tweeper.Adam
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SharedTwitterProperties
import kotlinx.android.synthetic.main.activity_tweet_report.*

class TweetReportActivity : Adam() {
    private val engine = TweetReport(this, object : TweetReport.ActivityInterface {
        override fun onRateLimit(apiPoint: String) {
            runOnUiThread {
                dProcessing.dismissWithAnimation()
                SweetAlertDialog(this@TweetReportActivity, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getString(R.string.Error))
                    .setContentText(getString(R.string.RateLimitError, apiPoint))
                    .show()
            }
        }

        override fun onFinished() {
            SharedTwitterProperties.reportWritten = true
            runOnUiThread {
                dProcessing.dismissWithAnimation()
                val dDone = SweetAlertDialog(this@TweetReportActivity, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(getString(R.string.Done))
                    .setContentText(getString(R.string.TweetReportDone))

                dDone.setOnDismissListener {
                        runOnUiThread {
                            updateReportRecordList()
                        }
                    }

                dDone.show()
            }
        }
    })

    private var adapter = TweetReportItem()

    private lateinit var dProcessing: SweetAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tweet_report)

        setOvalColor()
        initListView()
        initUpdateButton()
        updateReportRecordList()
    }

    private fun initListView() {
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            if (adapter.count - 1 == position) return@setOnItemClickListener
            val intent = Intent(this@TweetReportActivity, TweetReportDetail::class.java)
            intent.putExtra(TweetReportDetail.currentData, adapter.getItemToJson(position))
            intent.putExtra(TweetReportDetail.previousData, adapter.getItemToJson(position + 1))
            startActivity(intent)
        }
    }

    private fun updateReportRecordList() {
        val reports = engine.getReports()
        adapter.reports = reports
        adapter.notifyDataSetChanged()
    }

    private fun initUpdateButton() {
        button_update.setOnClickListener {
            if (!SharedTwitterProperties.reportWritten) {
                dProcessing = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                dProcessing.contentText = getString(R.string.TweetReportProcessing)
                dProcessing.setCancelable(false)
                dProcessing.show()
                engine.start()
            } else {
                SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setContentText(getString(R.string.TweetReportCant))
                    .show()
            }
        }
    }

    private fun setOvalColor() {
        val shape = oval.drawable as GradientDrawable
        shape.setColor(Color.RED)
    }
}
