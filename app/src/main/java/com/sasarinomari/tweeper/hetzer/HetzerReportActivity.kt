package com.sasarinomari.tweeper.hetzer

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import com.sasarinomari.tweeper.Adam
import com.sasarinomari.tweeper.DefaultListItem
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.report.ReportInterface
import kotlinx.android.synthetic.main.activity_hetzer_report.*
import java.text.SimpleDateFormat

// TODO: RecyclerView화
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
        val report = ReportInterface<HetzerReport>(HetzerReport.prefix)
            .readReport(this,  reportIndex, HetzerReport()) as HetzerReport
        text_removedCount.text = getString(R.string.DeleteReportCount, report.removedStatuses.count())
        listView.adapter = object: DefaultListItem(report.removedStatuses) {
            @SuppressLint("SimpleDateFormat")
            override fun drawItem(item: Any, title: TextView, description: TextView) {
                item as HetzerReport.Status
                title.text = item.text
                description.text = SimpleDateFormat(getString(R.string.DateFormat)).format(item.createdAt)
            }
        }
    }
}