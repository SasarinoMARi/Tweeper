package com.sasarinomari.tweeper.fwmanage

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import com.sasarinomari.tweeper.Adam
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.hetzer.HetzerReport
import com.sasarinomari.tweeper.hetzer.HetzerReportActivity
import com.sasarinomari.tweeper.report.ReportListActivity
import kotlinx.android.synthetic.main.activity_follow_management.*

class FollowManagementActivity : Adam() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_management)
        (oval1.drawable as GradientDrawable).setColor(Color.parseColor("#00CF91"))
        (oval2.drawable as GradientDrawable).setColor(Color.parseColor("#64b5f6"))
        button_go.setOnClickListener {
            if(FollowManagementService.checkServiceRunning((this))) {
                da.warning(getString(R.string.Wait), getString(R.string.duplicateService_FollowManager)).show()
            }
            else {
                val intent = Intent(this, FollowManagementService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                }
                else {
                    startService(intent)
                }
            }
        }
        button_report.setOnClickListener {
            val intent = Intent(this, ReportListActivity::class.java)
            intent.putExtra(ReportListActivity.Parameters.Title.name, getString(R.string.FollowManagement))
            intent.putExtra(ReportListActivity.Parameters.Description.name, getString(R.string.FollowManagementDesc))
            intent.putExtra(ReportListActivity.Parameters.NoReportDescription.name, getString(R.string.NoReport))
            intent.putExtra(ReportListActivity.Parameters.ReportPrefix.name, FollowManagementReport.prefix)
            intent.putExtra(ReportListActivity.Parameters.ReportActivityName.name, FollowManagementReportActivity::class.java.name)
            startActivity(intent)
        }


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
