package com.sasarinomari.tweeper.tweetreport

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import com.sasarinomari.tweeper.Adam
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SharedTwitterProperties
import kotlinx.android.synthetic.main.activity_tweet_report.*

class TweetReportActivity : Adam() {
    private val engine = TweetReport(this, object : TweetReport.ActivityInterface {
        override fun onRateLimit(apiPoint: String) {
            runOnUiThread {
                da.error(getString(R.string.Error), getString(R.string.RateLimitError, apiPoint))
            }
        }

        override fun onFinished() {
            SharedTwitterProperties.reportWritten = true
            runOnUiThread {
                da.success(getString(R.string.Done), getString(R.string.TweetReportDone)) {
                    runOnUiThread {
                        updateReportRecordList()
                    }
                }.show()
            }
        }
    })

    private var adapter = TweetReportItem()

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
                da.progress(null, getString(R.string.TweetReportProcessing)).show()
                engine.start()
            } else {
                da.warning(null, getString(R.string.TweetReportCant)).show()
            }
        }
    }

    private fun setOvalColor() {
        val shape = oval.drawable as GradientDrawable
        shape.setColor(Color.RED)
    }
}
