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

        fun apply(context: Context, checked: Boolean) {
            var checked = checked

            val intent = Intent(context, AnalyticsNotificationReceiver::class.java)
            val bundle = Bundle()
            bundle.putString(
                AnalyticsService.Parameters.User.name,
                Gson().toJson(AuthData.Recorder(context).getFocusedUser()!!)
            )
            intent.putExtra(AnalyticsNotificationReceiver.Parameters.Bundle.name, bundle)
            val pendingIntent = PendingIntent.getBroadcast(
                context, Tweeper.RequestCodes.ScheduledAnalytics.ordinal, intent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            // 체크 해제된 경우 알람 해제
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (pendingIntent != null) alarmManager.cancel(pendingIntent)
            Log.d("Schedule", "알람을 해제했습니다.")

            if (checked) {
                val calendar: Calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 21)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, 1000 * 60 * 60 * 24, pendingIntent)
                Log.e("Schedule", "알람을 등록했습니다.")
            } else {
                Log.e("Schedule", "알람 등록에 실패했습니다.")
                checked = false

                // 아마 무한재귀 걸릴 듯? 나중에 짬나면 처리하던가 하자
                // checkbox_setScheduled.isChecked = false
            }

            val edit = context.getSharedPreferences("AnalyticsNotificationReceiver", Context.MODE_PRIVATE).edit()
            edit.putBoolean("scheduleEnabled", checked)
            edit.apply()
        }
    }
}