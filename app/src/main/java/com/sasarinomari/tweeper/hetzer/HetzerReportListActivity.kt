package com.sasarinomari.tweeper.hetzer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.google.gson.Gson
import com.sasarinomari.tweeper.Adam
import com.sasarinomari.tweeper.R
import kotlinx.android.synthetic.main.activity_hetzer_report_list.*
import kotlinx.android.synthetic.main.item_hetzer_report.view.*

class HetzerReportListActivity : Adam() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hetzer_report_list)

        val adapter = HetzerReportsItem(HetzerReport.getReporNames(this))
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, index, _ ->
            val intent = Intent(this@HetzerReportListActivity, HetzerReportActivity::class.java)
            intent.putExtra(HetzerReportActivity.Parameters.ReportId.name, adapter.getItem(index).replace("hetzerReport", "").toInt())
            startActivity(intent)
        }
    }

    private class HetzerReportsItem(private val repors: Array<String>) : BaseAdapter() {
        override fun getCount(): Int {
            return repors.size
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val context = parent.context

            if (convertView == null) {
                val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                convertView = inflater.inflate(R.layout.item_hetzer_report, parent, false)
            }

            val report= repors[position]

            convertView!!
            convertView.text.text = report

            return convertView
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): String {
            return repors[position]
        }
    }
}