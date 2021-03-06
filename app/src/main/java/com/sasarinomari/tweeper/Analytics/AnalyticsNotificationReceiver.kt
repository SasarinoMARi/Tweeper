package com.sasarinomari.tweeper.Analytics

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.BuildConfig
import com.sasarinomari.tweeper.Tweeper
import java.util.*

class AnalyticsNotificationReceiver : BroadcastReceiver() {
    enum class Parameters {
        Bundle
    }

    override fun onReceive(context: Context, intent: Intent) {
        val b = intent.getBundleExtra(Parameters.Bundle.name)
        val userJson = b?.getString(AnalyticsService.Parameters.User.name)
        if (userJson == null) {
            Log.e("AnalyticsNotificationReceiver", "리시버가 호출되었지만 User가 Null입니다.")
            return
        }
        val i = Intent(context, AnalyticsService::class.java)
        i.putExtra(AnalyticsService.Parameters.User.name, userJson)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(i)
        } else {
            context.startService(i)
        }
    }

    companion object {
        fun isApplied(context: Context) : Boolean {
            val pref = context.getSharedPreferences("AnalyticsNotificationReceiver", Context.MODE_PRIVATE)
            return pref.getBoolean("scheduleEnabled", false)
        }

        fun apply(context: Context, checked: Boolean) : Boolean {
            var checked = checked
            val user = AuthData.Recorder(context).getFocusedUser() ?: return false
            /**
             * getFocusedUser에서 null 반환하는 경우가 들어와서 예외처리함. 대체 왜지?
             */

            val intent = Intent(context, AnalyticsNotificationReceiver::class.java)
            val bundle = Bundle()
            bundle.putString(
                AnalyticsService.Parameters.User.name,
                Gson().toJson(user)
            )
            intent.putExtra(AnalyticsNotificationReceiver.Parameters.Bundle.name, bundle)
            val pendingIntent = PendingIntent.getBroadcast(
                context, Tweeper.RequestCodes.ScheduledAnalytics.ordinal, intent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            // 체크 해제된 경우 알람 해제
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (pendingIntent != null) alarmManager.cancel(pendingIntent)
            if(BuildConfig.DEBUG) Log.d("Schedule", "알람을 해제했습니다.")

            if (checked) {
                val calendar: Calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 21)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                /**
                 * 오늘 이미 9시가 지났다면 내일 아홉시에 실행하기
                 */
                if(calendar.after(Calendar.getInstance())) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, 1000 * 60 * 60 * 24, pendingIntent)
                if(BuildConfig.DEBUG) Log.e("Schedule", "알람을 등록했습니다.")
            } else {
                checked = false

                // 아마 무한재귀 걸릴 듯? 나중에 짬나면 처리하던가 하자
                // checkbox_setScheduled.isChecked = false
            }

            val edit = context.getSharedPreferences("AnalyticsNotificationReceiver", Context.MODE_PRIVATE).edit()
            edit.putBoolean("scheduleEnabled", checked)
            edit.apply()

            return true
        }
    }
}