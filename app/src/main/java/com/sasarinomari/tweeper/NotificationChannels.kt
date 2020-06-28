package com.sasarinomari.tweeper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.sasarinomari.tweeper.Analytics.AnalyticsService
import com.sasarinomari.tweeper.Hetzer.HetzerService

class NotificationChannels {
    fun declaration(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.createNotificationChannel(generalChannel(context))
            mNotificationManager.createNotificationChannel(hetzerChannel(context))
            mNotificationManager.createNotificationChannel(analyticsChannel(context))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generalChannel(context: Context): NotificationChannel {
        return NotificationChannel("General", context.getString(R.string.General), NotificationManager.IMPORTANCE_DEFAULT)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun hetzerChannel(context: Context): NotificationChannel {
        return NotificationChannel(HetzerService().ChannelName, context.getString(R.string.TweetCleaner), NotificationManager.IMPORTANCE_LOW)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun analyticsChannel(context: Context): NotificationChannel {
        return NotificationChannel(AnalyticsService().ChannelName, context.getString(R.string.TweetAnalytics), NotificationManager.IMPORTANCE_LOW)
    }
}