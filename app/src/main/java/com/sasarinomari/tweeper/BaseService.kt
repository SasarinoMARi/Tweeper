package com.sasarinomari.tweeper

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

abstract class BaseService: Service() {
    companion object {
        private var innerRunningFlag = false

        fun checkServiceRunning(context: Context): Boolean {
            var flag1 = false
            val flag2 = innerRunningFlag

            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val className = this::class.java.name
            for (service in manager.getRunningServices(Integer.MAX_VALUE)) { // TODO : 이것 때문에 릴리즈 안될 수도..
                if (className == service.service.className) {
                    Log.i(className, "$className 서비스가 이미 실행중입니다.")
                    flag1 = true
                    break
                }
            }

            return flag1 and flag2
        }
    }

    abstract val ChannelName: String
    abstract val NotificationId: Int

    private lateinit var silentChannelBuilder: NotificationCompat.Builder
    private lateinit var defaultChannelBuilder: NotificationCompat.Builder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        innerRunningFlag = true
        silentChannelBuilder = if (Build.VERSION.SDK_INT >= 26) {
            NotificationCompat.Builder(this, ChannelName)
        } else {
            NotificationCompat.Builder(this)
        }
        silentChannelBuilder.setSound(null)
        defaultChannelBuilder = if (Build.VERSION.SDK_INT >= 26) {
            NotificationCompat.Builder(this, "General")
        } else {
            NotificationCompat.Builder(this)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.i(this::class.java.name, "onDestroy")
        innerRunningFlag = false
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.i(this::class.java.name, "onBind")
        return null
    }

    protected  fun createNotification(
        title: String,
        text: String,
        silent: Boolean = true,
        cancelable: Boolean = false,
        redirect: Intent = Intent()
    ): Notification {
        val pendingIntent = PendingIntent.getActivity(this, 0, redirect, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = if (silent) silentChannelBuilder else defaultChannelBuilder

        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle(title)
        builder.setContentText(text)
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(cancelable)

        return builder.build()!!
    }

    protected fun sendNotification(
        title: String,
        text: String,
        silent: Boolean = true,
        cancelable: Boolean = false,
        redirect: Intent = Intent(),
        id: Int = NotificationId
    ) {
        val notification = createNotification(title, text, silent, cancelable, redirect)
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(id, notification)
    }

}