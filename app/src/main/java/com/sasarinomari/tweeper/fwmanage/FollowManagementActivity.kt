package com.sasarinomari.tweeper.fwmanage

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.sasarinomari.tweeper.Adam
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.RecyclerInjector
import com.sasarinomari.tweeper.report.ReportInterface
import kotlinx.android.synthetic.main.fragment_card_button.view.*
import kotlinx.android.synthetic.main.fragment_column_header.view.*
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*
import kotlinx.android.synthetic.main.full_recycler_view.*
import kotlinx.android.synthetic.main.item_default.view.*
import java.text.SimpleDateFormat
import java.util.*

class FollowManagementActivity : Adam() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.full_recycler_view)

        val reportPrefix = FollowManagementReport.prefix
        val reports = ReportInterface<Any>(reportPrefix).getReportsWithNameAndCreatedDate(this)
        reports.reverse()

        val adapter = RecyclerInjector()
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_title_with_desc) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.title_text.text = getString(R.string.FollowManagement)
                view.title_description.text = getString(R.string.FollowManagementDesc)
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_card_button) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.cardbutton_image.setOvalColor(Color.BLUE)
                view.cardbutton_image.setImageResource(R.drawable.chart_areaspline)
                view.cardbutton_text.text = getString(R.string.FollowManagementRun)
                view.setOnClickListener {
                    if(FollowManagementService.checkServiceRunning((this@FollowManagementActivity))) {
                        da.warning(getString(R.string.Wait), getString(R.string.duplicateService_FollowManager)).show()
                    }
                    else {
                        val intent = Intent(this@FollowManagementActivity, FollowManagementService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent)
                        }
                        else {
                            startService(intent)
                        }
                    }
                }
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_column_header) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.column_title.text = getString(R.string.FollowManagementReports)
                view.column_description.text = getString(R.string.TouchToDetail)
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.item_default, reports) {
            @SuppressLint("SetTextI18n", "SimpleDateFormat")
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                item as Pair<*, *>
                view.defaultitem_title.text = getString(R.string.FollowManagementReport) + ' ' + (item.first.toString().removePrefix(reportPrefix).toInt() + 1)
                view.defaultitem_description.text = SimpleDateFormat(getString(R.string.DateFormat)).format(item.second as Date)
            }

            override fun onClickListItem(item: Any?) {
                val reportIndex = (item as Pair<*, *>).first.toString().removePrefix(reportPrefix).toInt()
                val intent = Intent(this@FollowManagementActivity, FollowManagementReportActivity::class.java)
                intent.putExtra(FollowManagementReportActivity.Parameters.ReportId.name, reportIndex)
                startActivity(intent)
            }

        })
        root.layoutManager = LinearLayoutManager(this)
        root.adapter = adapter
    }
}
