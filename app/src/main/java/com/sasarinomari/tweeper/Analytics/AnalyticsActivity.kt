package com.sasarinomari.tweeper.Analytics

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.Hetzer.HetzerConditionsActivity
import com.sasarinomari.tweeper.Hetzer.HetzerService
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.RecyclerInjector
import com.sasarinomari.tweeper.Report.ReportInterface
import kotlinx.android.synthetic.main.fragment_card_button.view.*
import kotlinx.android.synthetic.main.fragment_column_header.view.*
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*
import kotlinx.android.synthetic.main.full_recycler_view.*
import kotlinx.android.synthetic.main.item_tweet_report.view.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import kotlin.math.abs

class AnalyticsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.full_recycler_view)

        val reportPrefix = AnalyticsReport.prefix
        val reports = ReportInterface<Any>(reportPrefix).getReports(this, AnalyticsReport())
        reports.reverse()

        val adapter = RecyclerInjector()
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_title_with_desc) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.title_text.text = getString(R.string.TweetAnalytics)
                view.title_description.text = getString(R.string.TweetAnalyticsDesc)
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_card_button) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.cardbutton_image.setOvalColor(ContextCompat.getColor(this@AnalyticsActivity, R.color.purple))
                view.cardbutton_image.setImageResource(R.drawable.calendar_edit)
                view.cardbutton_text.text = getString(R.string.TweetAnalyticsRun)
                view.setOnClickListener {
                    if(AnalyticsService.checkServiceRunning((this@AnalyticsActivity))) {
                        da.warning(getString(R.string.Wait), getString(R.string.duplicateService_Analytics)).show()
                    }
                    else {
                        da.warning(getString(R.string.AreYouSure), getString(R.string.AnalyticsRunConfirm))
                            .setConfirmText(getString(R.string.Yes))
                            .setCancelText(getString(R.string.Wait))
                            .setConfirmClickListener {
                                it.dismissWithAnimation()
                                val intent = Intent(this@AnalyticsActivity, AnalyticsService::class.java)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    startForegroundService(intent)
                                }
                                else {
                                    startService(intent)
                                }
                            }.show()
                    }
                }
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_column_header) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.column_title.text = getString(R.string.AnalyticsReports)
                view.column_description.text = getString(R.string.TouchToDetail)
            }
        })
        val df = DecimalFormat("###,###")
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.item_tweet_report, reports) {
            @SuppressLint("SetTextI18n", "SimpleDateFormat")
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                item as AnalyticsReport
                view.text_date.text = SimpleDateFormat(getString(R.string.Format_Date)).format(item.date)
                view.text_time.text = SimpleDateFormat(getString(R.string.Format_Time)).format(item.date)

                setDiffrenceView(item.tweetCount, item.tweetCountVar,
                    view.text_tweetCount, view.image_tweetCount_Arrow, view.text_tweetCount_Value)
                setDiffrenceView(item.followings.count(), item.followingsVar,
                    view.text_friendCount, view.image_friendCount_Arrow, view.text_friendCount_Value)
                setDiffrenceView(item.followers.count(), item.followersVar,
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

            override fun onClickListItem(item: Any?) {
                item as AnalyticsReport
                val intent = Intent(this@AnalyticsActivity, AnalyticsReportActivity::class.java)
                intent.putExtra(AnalyticsReportActivity.Parameters.ReportId.name, item.id)
                // intent.putExtra(AnalyticsReportActivity.Parameters.PreviousReportId.name, item.id - 1)
                startActivityForResult(intent, 0)
            }

        })
        root.layoutManager = LinearLayoutManager(this)
        root.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == RESULT_OK) recreate()
        super.onActivityResult(requestCode, resultCode, data)
    }
}
