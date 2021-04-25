package com.sasarinomari.tweeper.Hetzer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.RecyclerInjector
import com.sasarinomari.tweeper.Report.ReportInterface
import kotlinx.android.synthetic.main.activity_analytics.*
import kotlinx.android.synthetic.main.activity_hetzer.*
import kotlinx.android.synthetic.main.activity_hetzer.layout_button
import kotlinx.android.synthetic.main.activity_hetzer.layout_column_header
import kotlinx.android.synthetic.main.activity_hetzer.layout_recyclerview
import kotlinx.android.synthetic.main.activity_hetzer.layout_title_and_desc
import kotlinx.android.synthetic.main.fragment_column_header.view.*
import kotlinx.android.synthetic.main.fragment_no_item.view.*
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*
import kotlinx.android.synthetic.main.item_default.view.*
import java.text.SimpleDateFormat
import java.util.*

class HetzerActivity : BaseActivity() {
    enum class RequestCodes {
        GetLogics
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hetzer)

        val reportPrefix = HetzerReport.prefix
        val userId = AuthData.Recorder(this).getFocusedUser()!!.user!!.id
        val reports = ReportInterface<Any>(userId, reportPrefix).getReportsWithDate(this)
        reports.reverse()

        layout_title_and_desc.title_text.text = getString(R.string.TweetCleaner)
        layout_title_and_desc.title_description.text = getString(R.string.TweetCleanerDescription)

        layout_column_header.column_title.text = getString(R.string.TweetCleanerReports)
        layout_column_header.column_description.text = getString(R.string.TouchToDetail)

        val button = layout_button as Button
        button.text = getString(R.string.TweetCleanerRun)
        button.setOnClickListener {
            if(HetzerService.checkServiceRunning((this@HetzerActivity))) {
                da.warning(getString(R.string.Wait), getString(R.string.duplicateService_Hetzer)).show()
            }
            else {
                startActivityForResult(
                    Intent(this@HetzerActivity, LogicPairActivity::class.java),
                    RequestCodes.GetLogics.ordinal
                )
            }
        }

        val adapter = RecyclerInjector()
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.item_default, reports) {
            @SuppressLint("SetTextI18n", "SimpleDateFormat")
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                item as Pair<String, Date?>
                val block = item.first.split("_")
                view.defaultitem_title.text =
                    if(block.size > 1) getString(R.string.TweetCleanerReport) + ' ' + (block[1].toInt() + 1)
                    else item.first
                view.defaultitem_description.text =
                    if(item.second != null) SimpleDateFormat("yyyy년 MM월 dd일 hh시 mm분", Locale.KOREA).format(item.second)
                    else null
            }

            override fun onClickListItem(item: Any?) {
                val intent = Intent(this@HetzerActivity, HetzerReportActivity::class.java)
                intent.putExtra(HetzerReportActivity.Parameters.ReportId.name, (item as Pair<String, Date?>).first)
                startActivity(intent)
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_no_item) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                val f = adapter.getFragment(viewType - 1)
                view.noitem_text.visibility = if(f.visible && f.count == 0) {
                    view.noitem_text.text = getString(R.string.NoHetzerReports)
                    View.VISIBLE
                } else View.GONE
            }
        })

        val recycler = layout_recyclerview as RecyclerView
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RequestCodes.GetLogics.ordinal -> {
                if (resultCode != RESULT_OK) {
                    return
                }
                val intent = Intent(this, HetzerService::class.java)
                val json = data!!.getStringExtra(HetzerService.Parameters.Logics.name)
                intent.putExtra(HetzerService.Parameters.Logics.name, json)
                intent.putExtra(HetzerService.Parameters.User.name,
                    Gson().toJson(AuthData.Recorder(this@HetzerActivity).getFocusedUser()!!))
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
