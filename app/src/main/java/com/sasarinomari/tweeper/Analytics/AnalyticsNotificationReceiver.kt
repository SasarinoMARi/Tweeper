package com.sasarinomari.tweeper.Analytics

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class AnalyticsNotificationReceiver : BroadcastReceiver() {
    enum class Parameters {
        Bundle
    }
    override fun onReceive(context: Context, intent: Intent) {
        val b = intent.getBundleExtra(Parameters.Bundle.name)
        val userJson = b?.getString(AnalyticsService.Parameters.User.name)
        if(userJson == null) {
            Log.e("AnalyticsNotificationReceiver", "리시버가 호출되었지만 User가 Null입니다.")
            return
        }
        val i = Intent(context, AnalyticsService::class.java)
        i.putExtra(AnalyticsService.Parameters.User.name, userJson)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(i)
        }
        else {
            context.startService(i)
        }
    }
}