package com.sasarinomari.tweeper.hetzer

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import com.sasarinomari.tweeper.Adam
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.report.ReportListActivity
import kotlinx.android.synthetic.main.activity_hetzer.*

class HetzerActivity : Adam() {
    enum class RequestCodes {
        GetConditions
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hetzer)
        (oval1.drawable as GradientDrawable).setColor(Color.parseColor("#00CF91"))
        (oval2.drawable as GradientDrawable).setColor(Color.parseColor("#64b5f6"))
        button_hetzer.setOnClickListener {
            startActivityForResult(
                Intent(this, HetzerConditionsActivity::class.java),
                RequestCodes.GetConditions.ordinal
            )
        }
        button_report.setOnClickListener {
            val intent = Intent(this, ReportListActivity::class.java)
            intent.putExtra(ReportListActivity.Parameters.Title.name, getString(R.string.TweetCleaner))
            intent.putExtra(ReportListActivity.Parameters.Description.name, getString(R.string.TweetCleanerDescription))
            intent.putExtra(ReportListActivity.Parameters.NoReportDescription.name, getString(R.string.TweetCleanerNoReport))
            intent.putExtra(ReportListActivity.Parameters.ReportPrefix.name, HetzerReport.prefix)
            intent.putExtra(ReportListActivity.Parameters.ReportActivityName.name, HetzerReportActivity::class.java.name)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RequestCodes.GetConditions.ordinal -> {
                if (resultCode != RESULT_OK) {
                    return
                }
                val intent = Intent(this, HetzerService::class.java)
                val json = data!!.getStringExtra(HetzerService.Parameters.HetzerConditions.name)
                intent.putExtra(HetzerService.Parameters.HetzerConditions.name, json)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                }
                else {
                    startService(intent)
                }
                setResult(RESULT_OK)
                finish()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
