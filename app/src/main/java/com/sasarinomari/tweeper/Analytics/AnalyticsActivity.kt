package com.sasarinomari.tweeper.Analytics

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.RecyclerInjector
import com.sasarinomari.tweeper.Report.ReportInterface
import com.sasarinomari.tweeper.RewardedAdAdapter
import com.sasarinomari.tweeper.Tweeper
import kotlinx.android.synthetic.main.fragment_card_button.view.*
import kotlinx.android.synthetic.main.fragment_column_header.view.*
import kotlinx.android.synthetic.main.fragment_no_item.view.*
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*
import kotlinx.android.synthetic.main.full_recycler_view.*
import kotlinx.android.synthetic.main.item_tweet_report.view.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

class AnalyticsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.full_recycler_view)

        val reportPrefix = AnalyticsReport.prefix
        val userId = AuthData.Recorder(this).getFocusedUser()!!.user!!.id
        val reports = ReportInterface<Any>(userId, reportPrefix).getReports(this, AnalyticsReport()) as ArrayList<AnalyticsReport>
        reports.sortBy { x -> x.date }
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
                                RewardedAdAdapter.show(this@AnalyticsActivity, object: RewardedAdAdapter.RewardInterface {
                                    override fun onFinished() {
                                        val intent = Intent(this@AnalyticsActivity, AnalyticsService::class.java)
                                        intent.putExtra(
                                            AnalyticsService.Parameters.User.name,
                                            Gson().toJson(AuthData.Recorder(this@AnalyticsActivity).getFocusedUser()!!))
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            startForegroundService(intent)
                                        }
                                        else {
                                            startService(intent)
                                        }
                                    }
                                })
                            }.show()
                    }
                }
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_card_button) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.cardbutton_image.setOvalColor(ContextCompat.getColor(this@AnalyticsActivity, R.color.purple))
                view.cardbutton_image.setImageResource(R.drawable.calendar_edit)
                view.cardbutton_text.text = "아홉 시에 예약 작업 등록하기"
                view.setOnClickListener {
                    val intent = Intent(this@AnalyticsActivity, AnalyticsNotificationReceiver::class.java)
                    val bundle = Bundle()
                    bundle.putString(AnalyticsService.Parameters.User.name,
                        Gson().toJson(AuthData.Recorder(this@AnalyticsActivity).getFocusedUser()!!))
                    intent.putExtra(AnalyticsNotificationReceiver.Parameters.Bundle.name, bundle)
                    val pendingIntent = PendingIntent.getBroadcast(
                        this@AnalyticsActivity, Tweeper.RequestCodes.ScheduledAnalytics.ordinal, intent, PendingIntent.FLAG_UPDATE_CURRENT)

                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                    if (pendingIntent != null && alarmManager != null) {
                        alarmManager.cancel(pendingIntent)
                    }
                    else {
                        Log.e("AnalyricsActivity", "알람 등록에 실패했습니다.")
                        return@setOnClickListener
                    }

                    val calendar: Calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 21)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }

                    alarmManager.setRepeating(AlarmManager.RTC, calendar.timeInMillis, 1000 * 60 * 60 * 24, pendingIntent)
                    Log.i("AnalyticsNotificationReceiver", "알람 설정됨!: {${calendar}}")
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
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_no_item) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                val f = adapter.getFragment(viewType - 1)
                view.noitem_text.visibility = if(f.visible && f.count == 0) {
                    view.noitem_text.text = getString(R.string.NoAnalyticsReports)
                    View.VISIBLE
                } else View.GONE
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
