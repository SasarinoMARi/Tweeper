package com.sasarinomari.tweeper.fwmanage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.sasarinomari.tweeper.Adam
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SharedTwitterProperties
import com.sasarinomari.tweeper.hetzer.HetzerReport
import com.sasarinomari.tweeper.report.ReportInterface
import kotlinx.android.synthetic.main.activity_follow_management_report.*
import twitter4j.User

// TODO: RecyclerView화
class FollowManagementReportActivity: Adam() {
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
        setContentView(R.layout.activity_follow_management_report)
        if(!checkRequirement()) return

        // TODO: Traitors와 fans 모두 볼 수 있게 변경
        val reportIndex = intent.getIntExtra(Parameters.ReportId.name, -1)
        if(reportIndex == -1)
            da.error(null, getString(R.string.Error_WrongParameter)) { finish() }.show()
        val report = ReportInterface<FollowManagementReport>(FollowManagementReport.prefix)
            .readReport(this, reportIndex, FollowManagementReport()) as FollowManagementReport
        list.adapter = UserUnfollowItem(report.traitors, object: UserUnfollowItem.ActivityInterface{
            override fun onClickUnfollow(userId: Long, doneCallback: Runnable) {
                da.warning(getString(R.string.AreYouSure), getString(R.string.ActionDoNotRestore))
                    .setConfirmText(getString(R.string.Yes))
                    .setConfirmClickListener {
                        it.dismissWithAnimation()
                        Thread(Runnable {
                            val twitter = SharedTwitterProperties.instance()
                            twitter.destroyFriendship(userId)
                            runOnUiThread {
                                da.success(getString(R.string.Done), getString(R.string.JobDone)) {
                                    it.dismissWithAnimation()
                                    doneCallback.run()
                                }.show()
                            }
                        }).start()
                    }.show()
            }

            override fun onclickDetail(screenName: String) {
                try{
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=$screenName")))
            } catch (e: Exception) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/#!/$screenName")))
            }
            }
        })
    }
}