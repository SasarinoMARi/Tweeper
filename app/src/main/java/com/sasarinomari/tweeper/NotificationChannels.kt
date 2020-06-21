package com.sasarinomari.tweeper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.sasarinomari.tweeper.hetzer.HetzerService

class NotificationChannels {
    fun declaration(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.createNotificationChannel(generalChannel())
            mNotificationManager.createNotificationChannel(hetzerChannel())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generalChannel(): NotificationChannel {
        return NotificationChannel( "General", "General", NotificationManager.IMPORTANCE_DEFAULT)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun hetzerChannel(): NotificationChannel {
        return NotificationChannel( HetzerService.ChannelName, "Tweet Cleaner", NotificationManager.IMPORTANCE_LOW)
    }
}