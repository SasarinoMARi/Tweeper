package com.sasarinomari.tweeper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.sasarinomari.tweeper.fwmanage.FollowManagementService
import com.sasarinomari.tweeper.hetzer.HetzerService

class NotificationChannels {
    fun declaration(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.createNotificationChannel(generalChannel(context))
            mNotificationManager.createNotificationChannel(hetzerChannel(context))
            mNotificationManager.createNotificationChannel(followerManagementChannel(context))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generalChannel(context: Context): NotificationChannel {
        return NotificationChannel( "General", context.getString(R.string.General), NotificationManager.IMPORTANCE_DEFAULT)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun hetzerChannel(context: Context): NotificationChannel {
        return NotificationChannel( HetzerService().ChannelName, context.getString(R.string.TweetCleaner), NotificationManager.IMPORTANCE_LOW)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun followerManagementChannel(context: Context): NotificationChannel {
        return NotificationChannel( FollowManagementService().ChannelName, context.getString(R.string.FollowManagement), NotificationManager.IMPORTANCE_LOW)
    }
}