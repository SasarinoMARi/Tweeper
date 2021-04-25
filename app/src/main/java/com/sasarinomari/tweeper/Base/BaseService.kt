package com.sasarinomari.tweeper.Base

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.sasarinomari.tweeper.R
import java.io.IOException
import java.io.InterruptedIOException
import java.util.*

abstract class BaseService: Service() {
    companion object {
        private var innerRunningFlag = false
        private var ACTION_STOP_SERVICE = "StopService4425"

        @Suppress("DEPRECATION")
        fun checkServiceRunning(context: Context, serviceName: String): Boolean {
            var flag1 = false
            val flag2 = innerRunningFlag

            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceName == service.service.className) {
                    Log.i(serviceName, "$serviceName 서비스가 이미 실행중입니다.")
                    flag1 = true
                    break
                }
            }

            return flag1 and flag2
        }
    }

    protected val ChannelName: String = "Service"
    protected val NotificationId:Int = Date().time.toInt()

    @Suppress("DEPRECATION")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // region 서비스 중단 분기 코드
        if(intent!=null && intent.action != null && intent.action!! == ACTION_STOP_SERVICE) {
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }
        // endregion
        innerRunningFlag = true
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.i(this::class.java.name, "onDestroy")
        innerRunningFlag = false
        stopAllManagedThreads()
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.i(this::class.java.name, "onBind")
        return null
    }

    // region 알림 관련 코드
    protected  fun createNotification(
        title: String,
        text: String,
        silent: Boolean = true,
        cancelable: Boolean = false,
        redirect: Intent = Intent()
    ): Notification {
        val pendingIntent = PendingIntent.getActivity(this, 0, redirect, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = getBuilder(silent)

        builder.setSmallIcon(R.drawable.ic_stat_icon)
        builder.setContentTitle(title)
        builder.setContentText(text)
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(cancelable)

        if(!cancelable) {
            val cls = this::class.java
            val cancelIntent = Intent(this, cls)
            cancelIntent.action = ACTION_STOP_SERVICE
            val cancelPendingIntent = PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            builder.addAction(0, "중지", cancelPendingIntent)
        }

        return builder.build()!!
    }

    /**
     * 알림 채널 빌드 코드
     */
    private fun getBuilder(silent: Boolean): NotificationCompat.Builder {
        val builder : NotificationCompat.Builder
        if(silent) {
            builder = if (Build.VERSION.SDK_INT >= 26) {
                NotificationCompat.Builder(this, ChannelName)
            } else {
                NotificationCompat.Builder(this)
            }
            builder.setSound(null)
        }
        else {
            builder = if (Build.VERSION.SDK_INT >= 26) {
                NotificationCompat.Builder(this, "General")
            } else {
                NotificationCompat.Builder(this)
            }
        }
        return builder
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

    private var _lastTick: Long = -1
    protected fun restrainedNotification(title: String, desc: String){
        val currentTick: Long = System.currentTimeMillis()
        if (currentTick > _lastTick + (1000 * 3)) {
            sendNotification(title, desc)
            _lastTick = currentTick
        }
    }
    // endregion

    protected fun sendActivityRefrashNotification(targetActivityClassName: String) {
        val intent = Intent(ActivityRefrashReceiver.eventName)
        intent.putExtra(ActivityRefrashReceiver.Parameters.Target.name, targetActivityClassName)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    // region 서비스 종속 스레드 관리 코드
    private var threads = ArrayList<Thread>()
    protected fun runOnManagedThread(runnable: ()-> Unit) {
        val thread = Thread(runnable)
        threads.add(thread)
        thread.start()
    }
    private fun stopAllManagedThreads() {
        for (t in threads) {
            if(t.isAlive) {
                t.interrupt()
                // t.stop()
            }
        }
    }
    // endregion

    protected open fun finish() {
        stopForeground(true)
        stopSelf()
    }

    protected fun onUncaughtError(serviceName: String) {
        sendNotification(serviceName, getString(R.string.UncaughtError),
            silent = false,
            cancelable = true,
            id = NotificationId + 1
        )
        finish()
    }


    private val retryCountMax = 3
    protected fun onNetworkError(serviceName: String, retry: () -> Unit, count: Int = 0) {
        if(count <  retryCountMax) {
            sendNotification("$serviceName ${getString(R.string.ReconnectWaiting)}}",
                getString(R.string.RetrySoon, count+1, retryCountMax))
            Thread {
                Thread.sleep(1000 * 60 * 5)
                retry()
            }.start()
        } else {
            sendNotification(serviceName, getString(R.string.StopWithNetworkError),
                silent = false,
                cancelable = true,
                id = NotificationId + 1
            )
            finish()
        }
    }
}