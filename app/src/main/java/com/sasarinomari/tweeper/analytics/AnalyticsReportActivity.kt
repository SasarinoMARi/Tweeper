package com.sasarinomari.tweeper.analytics

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.sasarinomari.tweeper.Adam
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.RecyclerInjector
import com.sasarinomari.tweeper.SimplizatedClass.User
import com.sasarinomari.tweeper.report.ReportInterface
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_column_header.view.*
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*
import kotlinx.android.synthetic.main.full_recycler_view.*
import kotlinx.android.synthetic.main.item_simpleuser.view.*
import kotlinx.android.synthetic.main.item_tweet_report.view.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import kotlin.math.abs

class AnalyticsReportActivity : Adam() {
    enum class Parameters {
        ReportId
    }

    private fun checkRequirements(): Boolean {
        for(parameter in Parameters.values())
            if(!intent.hasExtra(parameter.name)) {
                da.error(getString(R.string.Error), getString(R.string.Error_NoParameter)) {
                    finish()
                }
                return false
            }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.full_recycler_view)
        if(!checkRequirements()) return

        val reportIndex = intent.getIntExtra(Parameters.ReportId.name, -1)
        val previousReportIndex = reportIndex - 1
        val ri = ReportInterface<AnalyticsReport>(AnalyticsReport.prefix)
        val _classTemp = AnalyticsReport()
        val report = ri.readReport(this, reportIndex, _classTemp) as AnalyticsReport?
        val previousReport = ri.readReport(this, previousReportIndex, _classTemp) as AnalyticsReport?

        if(report == null) {
            da.error(getString(R.string.Error), getString(R.string.Error_WrongParameter)) { finish() }; return
        }

        // Recycler 어댑터 작성하는 코드
        val adapter = RecyclerInjector()
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_title_with_desc) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.title_text.text = getString(R.string.AnalyticsReport)
                view.title_description.text = getString(R.string.TweetAnalyticsDesc)
            }
        })
        adapter.addSpace(5)
        val df = DecimalFormat("###,###")
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.item_tweet_report) {
            @SuppressLint("SetTextI18n", "SimpleDateFormat")
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.text_date.text = SimpleDateFormat(getString(R.string.Format_Date)).format(report.date)
                view.text_time.text = SimpleDateFormat(getString(R.string.Format_Time)).format(report.date)

                setDiffrenceView(
                    report.tweetCount, report.tweetCountVar,
                    view.text_tweetCount, view.image_tweetCount_Arrow, view.text_tweetCount_Value)
                setDiffrenceView(
                    report.followings.count(), report.followingsVar,
                    view.text_friendCount, view.image_friendCount_Arrow, view.text_friendCount_Value)
                setDiffrenceView(
                    report.followers.count(), report.followersVar,
                    view.text_followerCount, view.image_followerCount_Arrow, view.text_FollowerCount_Value)
            }

            @SuppressLint("SetTextI18n")
            fun setDiffrenceView(totalValue: Int, diffrence: Int?, totalText : TextView, directionImage: ImageView, diffrenceText: TextView) {
                when {
                    diffrence == null || diffrence == 0 -> {
                        totalText.text = df.format(totalValue)
                        directionImage.visibility = View.GONE
                        diffrenceText.visibility = View.GONE
                    }
                    diffrence < 0 -> {
                        totalText.text = "${df.format(totalValue)} ("
                        directionImage.visibility = View.VISIBLE
                        diffrenceText.visibility = View.VISIBLE

                        directionImage.setImageResource(R.drawable.arrow_down_bold_box)
                        diffrenceText.text = "${df.format(abs(diffrence))})"
                    }
                    else -> {
                        totalText.text = "${df.format(totalValue)} ("
                        directionImage.visibility = View.VISIBLE
                        diffrenceText.visibility = View.VISIBLE

                        directionImage.setImageResource(R.drawable.arrow_up_bold_box)
                        diffrenceText.text = "${df.format(abs(diffrence))})"
                    }
                }
            }

        })

        if(previousReport != null) {
            adapter.addSpace(3)

            val newFriends = getDiffrence(report.followings, previousReport.followings)
            val noMoreFriends = getDiffrence(previousReport.followings, report.followings)
            val newFollowers = getDiffrence(report.followers, previousReport.followers)
            val noMoreFollowers = getDiffrence(previousReport.followers, report.followers)

            if(newFriends.isNotEmpty()) {
                adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_column_header) {
                    override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) { drawHeader(view, getString(R.string.NewFriends), adapter, viewType) }
                })
                adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.item_simpleuser, newFriends) {
                override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) { drawUserItem(view,  item, viewType, listItemIndex) }
                override fun onClickListItem(item: Any?) { onClickUserItem(item) }
            })
            }
            if(noMoreFriends.isNotEmpty()) {
                adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_column_header) {
                    override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) { drawHeader(view, getString(R.string.NoMoreFriends), adapter, viewType) }
                })
                adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.item_simpleuser, noMoreFriends) {
                    override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) { drawUserItem(view,  item, viewType, listItemIndex) }
                    override fun onClickListItem(item: Any?) { onClickUserItem(item) }
                })
            }
            if(newFollowers.isNotEmpty()) {
                adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_column_header) {
                    override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) { drawHeader(view, getString(R.string.NewFollowers), adapter, viewType) }
                })
                adapter.add(object : RecyclerInjector.RecyclerFragment(R.layout.item_simpleuser, newFollowers) {
                    override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                        drawUserItem(view, item, viewType, listItemIndex)
                    }

                    override fun onClickListItem(item: Any?) {
                        onClickUserItem(item)
                    }
                })
            }
            if(noMoreFollowers.isNotEmpty()) {
                adapter.add(object : RecyclerInjector.RecyclerFragment(R.layout.fragment_column_header) {
                    override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                        drawHeader(view, getString(R.string.NoMoreFollowers), adapter, viewType)
                    }
                })
                adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.item_simpleuser, noMoreFollowers) {
                    override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) { drawUserItem(view,  item, viewType, listItemIndex) }
                    override fun onClickListItem(item: Any?) { onClickUserItem(item) }
                })
            }
        }

        root.layoutManager = LinearLayoutManager(this)
        root.adapter = adapter
    }

    private fun getDiffrence(list1: ArrayList<User>, list2: ArrayList<User>) : ArrayList<User> {
        val list = ArrayList<User>()
        for (i in list1) {
            if (!list2.contains(i)) {
                list.add(i)
            }
        }
        return list
    }

    private fun drawHeader(view: View, title: String, adapter: RecyclerInjector, viewType: Int) {
        view.column_title.text = title
        view.column_description.text = getString(R.string.TouchToExpend)

        val f = adapter.getFragment(viewType + 1)
        view.setOnClickListener {
            f.visible = !f.visible
            adapter.notifyDataSetChanged()
        }
    }

    fun drawUserItem(view: View, item: Any?, viewType: Int, listItemIndex: Int){
        item as User
        view.simpleuser_id.text = item.id.toString()
        view.simpleuser_name.text = item.name
        view.simpleuser_screenName.text = item.screenName
        Picasso.get()
            .load(item.profileImageUrl.replace("normal.jpg", "200x200.jpg"))
            .into(view.simpleuser_profilePicture)
    }

    fun onClickUserItem(item: Any?) {
        item as User
        detail(item.screenName)
    }

    fun detail(screenName: String) {
        try{
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=$screenName")))
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/#!/$screenName")))
        }
    }
}
