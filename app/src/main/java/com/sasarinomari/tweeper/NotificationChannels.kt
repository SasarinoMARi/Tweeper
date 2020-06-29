package com.sasarinomari.tweeper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.sasarinomari.tweeper.Base.BaseService

class NotificationChannels {
    fun declaration(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.createNotificationChannel(generalChannel(context))
            mNotificationManager.createNotificationChannel(serviceChannel(context))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generalChannel(context: Context): NotificationChannel {
        return NotificationChannel("General", context.getString(R.string.General), NotificationManager.IMPORTANCE_DEFAULT)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun serviceChannel(context: Context): NotificationChannel {
        return NotificationChannel("Service", context.getString(R.string.Service), NotificationManager.IMPORTANCE_LOW)
    }

}