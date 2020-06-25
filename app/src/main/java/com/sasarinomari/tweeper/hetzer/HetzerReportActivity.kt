package com.sasarinomari.tweeper.hetzer

import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sasarinomari.tweeper.Adam
import com.sasarinomari.tweeper.R
import kotlinx.android.synthetic.main.activity_hetzer_report.*
import twitter4j.Status

class HetzerReportActivity : Adam() {
    enum class Parameters {
        ReportId
    }

    private fun checkRequirement(): Boolean{
        if(!intent.hasExtra(Parameters.ReportId.name)) {
            da.error(null, getString(R.string.Error_NoParameter)) {
                finish()
            }.show()
            return false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hetzer_report)
        if(!checkRequirement()) return

        val reportIndex = intent.getIntExtra(Parameters.ReportId.name, -1)
        if(reportIndex == -1)
            da.error(null, getString(R.string.Error_WrongParameter)) { finish() }.show()
        val removedStatuses = HetzerReport.readReport(this,  reportIndex)
        text_removedCount.text = getString(R.string.DeleteReportCount, removedStatuses.count())
        listView.adapter = HetzerReportItem(removedStatuses)
    }
}