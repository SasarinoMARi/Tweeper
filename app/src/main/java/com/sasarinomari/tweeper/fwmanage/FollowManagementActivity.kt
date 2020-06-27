package com.sasarinomari.tweeper.fwmanage

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
import com.sasarinomari.tweeper.DefaultRecycleAdapter
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.report.ReportInterface
import kotlinx.android.synthetic.main.full_recycler_view.*
import kotlinx.android.synthetic.main.header_follow_management.view.*
import java.text.SimpleDateFormat
import java.util.*

class FollowManagementActivity : Adam() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.full_recycler_view)

        val reportPrefix = FollowManagementReport.prefix
        val reports = ReportInterface<Any>(reportPrefix).getReportsWithNameAndCreatedDate(this)
        val adapter = object : DefaultRecycleAdapter(reports, header = R.layout.header_follow_management) {
            @SuppressLint("SimpleDateFormat", "SetTextI18n")
            override fun drawListItem(item: Any, title: TextView, description: TextView) {
                item as Pair<*, *>
                title.text = getString(R.string.FollowManagementReport) + ' ' + (item.first.toString().removePrefix(reportPrefix).toInt() + 1)
                description.text = SimpleDateFormat(getString(R.string.DateFormat)).format(item.second as Date)
            }

            override fun onClickListItem(item: Any) {
                val reportIndex = (item as Pair<*, *>).first.toString().removePrefix(reportPrefix).toInt()
                val intent = Intent(this@FollowManagementActivity, FollowManagementReportActivity::class.java)
                intent.putExtra(FollowManagementReportActivity.Parameters.ReportId.name, reportIndex)
                startActivity(intent)
            }

            override fun drawHeader(view: View) {
                view.button_run.setOnClickListener {
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
        }
        root.layoutManager = LinearLayoutManager(this)
        root.adapter = adapter

//        var dialog = da.progress(null, getString(R.string.FriendPulling))
//        dialog.show()
//
//        SharedTwitterProperties.getFriends(this) { fs ->
//            runOnUiThread {
//                dialog.dismissWithAnimation()
//                dialog = da.progress(null, getString(R.string.FollowerPulling))
//                dialog.show()
//            }
//            SharedTwitterProperties.getFollowers(this) { fw ->
//                runOnUiThread {
//                    dialog.dismissWithAnimation()
//                    dialog = da.progress(null, getString(R.string.CompareFsFw))
//                    dialog.show()
//                }
//                val uf = comLists(fs, fw)
//                runOnUiThread {
//                    dialog.dismissWithAnimation()
//                    initializeUi(uf)
//                }
//            }
//        }
    }
}
