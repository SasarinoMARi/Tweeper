package com.sasarinomari.tweeper.tweetreport

import android.annotation.SuppressLint
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.google.gson.Gson
import com.sasarinomari.tweeper.R
import kotlinx.android.synthetic.main.item_tweet_report.view.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat


internal class TweetReportItem: BaseAdapter() {

    var reports = ArrayList<Report>()

    override fun getCount(): Int {
        return reports.size
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val context = parent.context
        val df = DecimalFormat("###,###")

        if (convertView == null) {
            val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.item_tweet_report, parent, false)
        }

        val report = reports[position]

        convertView!!
        convertView.text_date.text =
            SimpleDateFormat(context.getString(R.string.Format_Date)).format(report.date)
        convertView.text_time.text =
            SimpleDateFormat(context.getString(R.string.Format_Time)).format(report.date)

        when {
            report.tweetCountVar == null || report.tweetCountVar == 0 -> { // 값 없음 (첫번째 로그일 경우)
                convertView.text_tweetCount.text = df.format(report.tweetCount)
                convertView.image_tweetCount_Arrow.visibility = View.GONE
                convertView.text_tweetCount_Value.visibility = View.GONE
            }
            report.tweetCountVar!! < 0 -> { // 감소
                convertView.text_tweetCount.text = "${df.format(report.tweetCount)} ("
                convertView.image_tweetCount_Arrow.visibility = View.VISIBLE
                convertView.text_tweetCount_Value.visibility = View.VISIBLE
                convertView.image_tweetCount_Arrow.setImageResource(R.drawable.arrow_down_bold_box)
                convertView.text_tweetCount_Value.text = "${df.format(report.tweetCountVar)})"
            }
            else -> { // 증가
                convertView.text_tweetCount.text = "${df.format(report.tweetCount)} ("
                convertView.image_tweetCount_Arrow.visibility = View.VISIBLE
                convertView.text_tweetCount_Value.visibility = View.VISIBLE
                convertView.image_tweetCount_Arrow.setImageResource(R.drawable.arrow_up_bold_box)
                convertView.text_tweetCount_Value.text = "${df.format(report.tweetCountVar)})"
            }
        }


        when {
            report.friendsVar == null || report.friendsVar == 0 -> {
                convertView.text_friendCount.text = df.format(report.friends.count())
                convertView.image_friendCount_Arrow.visibility = View.GONE
                convertView.text_friendCount_Value.visibility = View.GONE
            }
            report.friendsVar!! < 0 -> {
                convertView.text_friendCount.text = "${df.format(report.friends.count())} ("
                convertView.image_friendCount_Arrow.visibility = View.VISIBLE
                convertView.text_friendCount_Value.visibility = View.VISIBLE
                convertView.image_friendCount_Arrow.setImageResource(R.drawable.arrow_down_bold_box)
                convertView.text_friendCount_Value.text = "${df.format(report.friendsVar)})"
            }
            else -> {
                convertView.text_friendCount.text = "${df.format(report.friends.count())} ("
                convertView.image_friendCount_Arrow.visibility = View.VISIBLE
                convertView.text_friendCount_Value.visibility = View.VISIBLE
                convertView.image_friendCount_Arrow.setImageResource(R.drawable.arrow_up_bold_box)
                convertView.text_friendCount_Value.text = "${df.format(report.friendsVar)})"
            }
        }

        when {
            report.followersVar == null || report.followersVar == 0 -> {
                convertView.text_followerCount.text = df.format(report.followers.count())
                convertView.image_followerCount_Image.visibility = View.GONE
                convertView.text_FollowerCount_Value.visibility = View.GONE
            }
            report.followersVar!! < 0 -> {
                convertView.text_followerCount.text = "${df.format(report.followers.count())} ("
                convertView.image_followerCount_Image.visibility = View.VISIBLE
                convertView.text_FollowerCount_Value.visibility = View.VISIBLE
                convertView.image_followerCount_Image.setImageResource(R.drawable.arrow_down_bold_box)
                convertView.text_FollowerCount_Value.text = "${df.format(report.followersVar)})"
            }
            else -> {
                convertView.text_followerCount.text = "${df.format(report.followers.count())} ("
                convertView.image_followerCount_Image.visibility = View.VISIBLE
                convertView.text_FollowerCount_Value.visibility = View.VISIBLE
                convertView.image_followerCount_Image.setImageResource(R.drawable.arrow_up_bold_box)
                convertView.text_FollowerCount_Value.text = "${df.format(report.followersVar)})"
            }
        }


        return convertView
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Report {
        return reports[position]
    }

    fun getItemToJson(position: Int): String? {
        return Gson().toJson(getItem(position))
    }

}