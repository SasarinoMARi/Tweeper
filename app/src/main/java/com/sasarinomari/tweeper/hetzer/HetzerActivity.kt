package com.sasarinomari.tweeper.hetzer

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.sasarinomari.tweeper.Adam
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.DefaultRecycleAdapter
import com.sasarinomari.tweeper.report.ReportInterface
import kotlinx.android.synthetic.main.full_recycler_view.*
import kotlinx.android.synthetic.main.header_hetzer.view.*
import java.text.SimpleDateFormat
import java.util.*

class HetzerActivity : Adam() {
    enum class RequestCodes {
        GetConditions
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.full_recycler_view)

        val reportPrefix = HetzerReport.prefix
        val reports = ReportInterface<Any>(reportPrefix).getReportsWithNameAndCreatedDate(this)
        val adapter = object : DefaultRecycleAdapter(reports, header = R.layout.header_hetzer) {
            @SuppressLint("SimpleDateFormat", "SetTextI18n")
            override fun drawListItem(item: Any, title: TextView, description: TextView) {
                item as Pair<*, *>
                title.text = getString(R.string.TweetCleanerReport) + ' ' + (item.first.toString().removePrefix(reportPrefix).toInt() + 1)
                description.text = SimpleDateFormat(getString(R.string.DateFormat)).format(item.second as Date)
            }

            override fun onClickListItem(item: Any) {
                val reportIndex = (item as Pair<*, *>).first.toString().removePrefix(reportPrefix).toInt()
                val intent = Intent(this@HetzerActivity, HetzerReportActivity::class.java)
                intent.putExtra(HetzerReportActivity.Parameters.ReportId.name, reportIndex)
                startActivity(intent)
            }

            override fun drawHeader(view: View) {
                view.button_run.setOnClickListener {
                    startActivityForResult(
                        Intent(this@HetzerActivity, HetzerConditionsActivity::class.java),
                        RequestCodes.GetConditions.ordinal
                    )
                }
            }
        }
        root.layoutManager = LinearLayoutManager(this)
        root.adapter = adapter
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
                } else {
                    startService(intent)
                }
                setResult(RESULT_OK)
                finish()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
