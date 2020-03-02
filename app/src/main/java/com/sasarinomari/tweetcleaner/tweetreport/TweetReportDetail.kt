package com.sasarinomari.tweetcleaner.tweetreport

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.gson.Gson
import com.sasarinomari.tweetcleaner.Adam
import com.sasarinomari.tweetcleaner.R
import kotlinx.android.synthetic.main.activity_tweet_report_detail.*
import kotlinx.android.synthetic.main.item_tweet_report.view.*
import java.text.SimpleDateFormat

class TweetReportDetail : Adam() {
    companion object {
        const val currentData = "current"
        const val previousData = "previous"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentReport = getReport(intent.getStringExtra(currentData)!!)!!
        val previousReport = getReport(intent.getStringExtra(previousData)!!)!!

        setContentView(R.layout.activity_tweet_report_detail)

        initHeader(currentReport)
        initBody(currentReport, previousReport)

    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun initHeader(report: Report) {
        view_report.text_date.text =
            SimpleDateFormat(getString(R.string.Format_Date)).format(report.date)
        view_report.text_time.text =
            SimpleDateFormat(getString(R.string.Format_Time)).format(report.date)

        when {
            report.tweetCountVar == null || report.tweetCountVar == 0 -> { // 값 없음 (첫번째 로그일 경우)
                view_report.text_tweetCount.text = report.tweetCount.toString()
                view_report.image_tweetCount_Arrow.visibility = View.GONE
                view_report.text_tweetCount_Value.visibility = View.GONE
            }
            report.tweetCountVar!! < 0 -> { // 감소
                view_report.text_tweetCount.text = "${report.tweetCount} ("
                view_report.image_tweetCount_Arrow.visibility = View.VISIBLE
                view_report.text_tweetCount_Value.visibility = View.VISIBLE
                view_report.image_tweetCount_Arrow.setImageResource(R.drawable.arrow_down_bold_box)
                view_report.text_tweetCount_Value.text = "${report.tweetCountVar})"
            }
            else -> { // 증가
                view_report.text_tweetCount.text = "${report.tweetCount} ("
                view_report.image_tweetCount_Arrow.visibility = View.VISIBLE
                view_report.text_tweetCount_Value.visibility = View.VISIBLE
                view_report.image_tweetCount_Arrow.setImageResource(R.drawable.arrow_up_bold_box)
                view_report.text_tweetCount_Value.text = "${report.tweetCountVar})"
            }
        }


        when {
            report.friendsVar == null || report.friendsVar == 0 -> {
                view_report.text_friendCount.text = report.friends.count().toString()
                view_report.image_friendCount_Arrow.visibility = View.GONE
                view_report.text_friendCount_Value.visibility = View.GONE
            }
            report.friendsVar!! < 0 -> {
                view_report.text_friendCount.text = "${report.friends.count()} ("
                view_report.image_friendCount_Arrow.visibility = View.VISIBLE
                view_report.text_friendCount_Value.visibility = View.VISIBLE
                view_report.image_friendCount_Arrow.setImageResource(R.drawable.arrow_down_bold_box)
                view_report.text_friendCount_Value.text = "${report.friendsVar})"
            }
            else -> {
                view_report.text_friendCount.text = "${report.friends.count()} ("
                view_report.image_friendCount_Arrow.visibility = View.VISIBLE
                view_report.text_friendCount_Value.visibility = View.VISIBLE
                view_report.image_friendCount_Arrow.setImageResource(R.drawable.arrow_up_bold_box)
                view_report.text_friendCount_Value.text = "${report.friendsVar})"
            }
        }

        when {
            report.followersVar == null || report.followersVar == 0 -> {
                view_report.text_followerCount.text = report.followers.count().toString()
                view_report.image_followerCount_Image.visibility = View.GONE
                view_report.text_FollowerCount_Value.visibility = View.GONE
            }
            report.followersVar!! < 0 -> {
                view_report.text_followerCount.text = "${report.followers.count()} ("
                view_report.image_followerCount_Image.visibility = View.VISIBLE
                view_report.text_FollowerCount_Value.visibility = View.VISIBLE
                view_report.image_followerCount_Image.setImageResource(R.drawable.arrow_down_bold_box)
                view_report.text_FollowerCount_Value.text = "${report.followersVar})"
            }
            else -> {
                view_report.text_followerCount.text = "${report.followers.count()} ("
                view_report.image_followerCount_Image.visibility = View.VISIBLE
                view_report.text_FollowerCount_Value.visibility = View.VISIBLE
                view_report.image_followerCount_Image.setImageResource(R.drawable.arrow_up_bold_box)
                view_report.text_FollowerCount_Value.text = "${report.followersVar})"
            }
        }
    }

    private fun initBody(currentReport: Report, previousReport: Report) {
        val newFriends = ArrayList<SimpleUser>()
        val noMoreFriends = ArrayList<SimpleUser>()
        val newFollowers = ArrayList<SimpleUser>()
        val noMoreFollowers = ArrayList<SimpleUser>()

        for (i in currentReport.friends) {
            if (!previousReport.friends.contains(i)) {
                newFriends.add(i)
            }
        }
        for (i in previousReport.friends) {
            if (!currentReport.friends.contains(i)) {
                noMoreFriends.add(i)
            }
        }
        for (i in currentReport.followers) {
            if (!previousReport.followers.contains(i)) {
                newFollowers.add(i)
            }
        }
        for (i in previousReport.followers) {
            if (!currentReport.followers.contains(i)) {
                noMoreFollowers.add(i)
            }
        }

        if (newFriends.count() > 0)
            for (i in newFriends) {
                layout_newFriends.addView(addReportView(i))
            }
        else layout_newFriends.visibility = View.GONE

        if (noMoreFriends.count() > 0)
            for (i in noMoreFriends) {
                layout_noMoreFriends.addView(addReportView(i))
            }
        else layout_noMoreFriends.visibility = View.GONE

        if (newFollowers.count() > 0)
            for (i in newFollowers) {
                layout_newFollowers.addView(addReportView(i))
            }
        else layout_newFollowers.visibility = View.GONE

        if (noMoreFollowers.count() > 0)
            for (i in noMoreFollowers) {
                layout_noMoreFollowers.addView(addReportView(i))
            }
        else layout_noMoreFollowers.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun addReportView(user: SimpleUser): View {
        val tv = TextView(this)
        tv.textSize = 15f
        tv.setTextColor(getColor(R.color.white))
        tv.text = "@${user.screenName}(${user.id})"
        return tv
    }

    private fun getReport(json: String): Report? {
        return Gson().fromJson(json, Report::class.java)
    }
}