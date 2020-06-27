package com.sasarinomari.tweeper.report

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.sasarinomari.tweeper.Adam
import com.sasarinomari.tweeper.DefaultListItem
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.hetzer.HetzerReport
import com.sasarinomari.tweeper.hetzer.HetzerReportActivity
import kotlinx.android.synthetic.main.activity_report_list.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 안씀!
 * 기술적인 문제는 없지만 이거 말고 ReportListFragment나 쓰셈 ㅋ
 * 사유: 못생김
 */
/**
 * 트윗지기 서비스에서 생성된 리포트 조회용 공용 액티비티.
 * Parameters 전부가 인자로 와야 실행 가능.
 *
 * ReportActivityName은 아이템 클릭 시 redirect할 class 이름임.
 * Class::class.java.name과 같이 써서 인자로 넣을 수 있음.
 */
class ReportListActivity : Adam() {
    enum class Parameters {
        Title, Description, NoReportDescription,
        ReportPrefix, ReportActivityName
    }

    private fun checkRequirements(): Boolean {
        for(parameter in Parameters.values())
            if(!intent.hasExtra(parameter.name)) return false
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_list)
        if(!checkRequirements()) {
            finish(); return
        }

        text_title.text = intent.getStringExtra(Parameters.Title.name)
        text_description.text = intent.getStringExtra(Parameters.Description.name)
        text_noReport.text = intent.getStringExtra(Parameters.NoReportDescription.name)

        val reportPrefix = intent.getStringExtra(Parameters.ReportPrefix.name)!!
        val reports = ReportInterface<Any>(reportPrefix).getReportsWithNameAndCreatedDate(this)
        if (reports.isEmpty()) {
            layout_noReport.visibility = View.VISIBLE
            listView.visibility = View.GONE
        } else {
            val adapter = object : DefaultListItem(reports) {
                @SuppressLint("SimpleDateFormat")
                override fun drawItem(item: Any, title: TextView, description: TextView) {
                    item as Pair<*, *>
                    title.text = item.first.toString()
                    description.text = SimpleDateFormat(getString(R.string.DateFormat)).format(item.second as Date)
                }
            }
            listView.adapter = adapter
            listView.setOnItemClickListener { _, _, index, _ ->
                val reportIndex = (adapter.getItem(index) as Pair<*, *>).first.toString().removePrefix(reportPrefix).toInt()
                val intent = Intent(this@ReportListActivity,
                    Class.forName(intent.getStringExtra(Parameters.ReportActivityName.name)!!))
                intent.putExtra(HetzerReportActivity.Parameters.ReportId.name, reportIndex)
                startActivity(intent)
            }
        }
    }
}