package com.sasarinomari.tweeper.ScheduledTask

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.sasarinomari.tweeper.Analytics.AnalyticsService
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.FirebaseLogger
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.RecyclerInjector
import com.sasarinomari.tweeper.TwitterAdapter
import kotlinx.android.synthetic.main.fragment_card_button.view.*
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*
import kotlinx.android.synthetic.main.full_recycler_view.*
import java.lang.Exception
import java.util.*

class ScheduleManageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.full_recycler_view)


        val adapter = RecyclerInjector()
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_title_with_desc) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.title_text.text = "예약 작업 테스트"
                view.title_description.text = "지정된 시간에 자동으로 작업을 시작합니다."
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_card_button) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.cardbutton_text.text = getString(R.string.TweetCleanerRun)
                view.setOnClickListener {

                }
            }
        })
        root.layoutManager = LinearLayoutManager(this)
        root.adapter = adapter

    }

    fun alramTest(context: Context) {
        val loggedUser = AuthData.Recorder(context).getFocusedUser()

        val intent = Intent(context, MAlarmReceiver::class.java)
        intent.putExtra(MAlarmReceiver.Parameters.User.name, Gson().toJson(loggedUser))

        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

        // TwitterAdapter.twitter.oAuthAccessToken = user.token!!

        // Set the alarm to start at 21:32 PM
        val calendar = Calendar.getInstance();
        calendar.timeInMillis = System.currentTimeMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 21)
        calendar.set(Calendar.MINUTE, 32)

        // setRepeating() lets you specify a precise custom interval--in this case,
        // 1 day
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, alarmIntent)
    }

    class MAlarmReceiver : BroadcastReceiver() {
        enum class Parameters {
            User
        }

        override fun onReceive(context: Context, intent: Intent) {
            if(!AnalyticsService.checkServiceRunning(context)) {
                val user = intent.getStringExtra(Parameters.User.name)
                val i = Intent(context, AnalyticsService::class.java)
                i.putExtra(AnalyticsService.Parameters.User.name, user)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(i)
                }
                else {
                    context.startService(i)
                }
            }
        }

        private fun login(context: Context, id: Long, callback: ()-> Unit) {
            Thread {
                try {
                    TwitterAdapter().initialize(AuthData.Recorder(context).getFocusedUser()!!.token!!).getMe(object : TwitterAdapter.FetchObjectInterface {
                        override fun onStart() {}

                        override fun onFinished(obj: Any) {
                            callback()
                        }

                        override fun onRateLimit() {
                            callback()
                        }

                        override fun onUncaughtError() {
                            TODO("Not yet implemented")
                        }

                        override fun onNetworkError(retry: () -> Unit) {
                            TODO("Not yet implemented")
                        }
                    })
                } catch (e: Exception) {
                    Log.i(this::class.java.name, "유저 id [$id]로의 로그인에 실패했습니다.")
                    FirebaseLogger(context).log("AuthFailed", Pair("Message", "유저 id [$id]로의 로그인에 실패했습니다."))
                }
            }.start()
        }
    }
}