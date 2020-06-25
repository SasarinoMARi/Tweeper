package com.sasarinomari.tweeper.hetzer

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SharedTwitterProperties
import twitter4j.Paging
import twitter4j.Status
import twitter4j.TwitterException
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class HetzerService : Service() {
    enum class Parameters {
        HetzerConditions
    }

    companion object {
        const val ChannelName = "Hetzer"
        private var _innerRunningFlag = false

        fun checkServiceRunning(context: Context): Boolean {
            var flag1 = false
            val flag2 = _innerRunningFlag

            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Integer.MAX_VALUE)) { // TODO : 이것 때문에 릴리즈 안될 수도..
                if (HetzerService::class.java.name == service.service.className) {
                    Log.i("Hetzer", "Hetzer 서비스가 이미 실행중입니다.")
                    flag1 = true
                    break
                }
            }

            return flag1 and flag2
        }

    }

    private val notificationId = 4425
    private lateinit var silentChannelBuilder: NotificationCompat.Builder
    private lateinit var defaultChannelBuilder: NotificationCompat.Builder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Companion._innerRunningFlag = true
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

        startForeground(notificationId, createNotification(getString(R.string.app_name), "Initializing...", false))

        hetzerLogic(intent!!)

        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        Log.i(HetzerService::class.java.name, "onDestroy")
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.i(HetzerService::class.java.name, "onBind")
        Companion._innerRunningFlag = true
        return null
    }

    private fun hetzerLogic(intent: Intent) {
        val json = intent.getStringExtra(Parameters.HetzerConditions.name)
        val typeToken = object : TypeToken<HashMap<Int, Any>>() {}.type
        val conditions = Gson().fromJson(json, typeToken) as HashMap<Int, Any>
        val hetzer = Hetzer(conditions)

        getTweets { tweets ->
            sendNotification(getString(R.string.Hetzer_TweetRemovingTitle), "")
            val savedStatuses = ArrayList<Status>()
            val removedStatuses = ArrayList<Status>()
            if (tweets.isNotEmpty()) {
                for (i in 0 until tweets.count()) {
                    val item = tweets[i]
                    if (hetzer.filter(item, i)) {
                        savedStatuses.add(item)
                    }
                    else {
                        removedStatuses.add(item)
                        // TODO : 이미 트윗이 지워진 경우 등 예외상황에 잘 동작하는지 확인할 필요 있음
                        // SharedTwitterProperties.instance().destroyStatus(item.id)
                    }
                }
            }

            val reportIndex = HetzerReport.getReportCount(this)+1
            HetzerReport.writeReport(this, reportIndex, removedStatuses)

            val redirect = Intent(this, HetzerReportActivity::class.java)
            redirect.putExtra(HetzerReportActivity.Parameters.ReportId.name, reportIndex)
            sendNotification(
                getString(R.string.Done),
                getString(R.string.Hetzer_Done),
                silent = false,
                cancelable = true,
                redirect = redirect,
                id = notificationId + 1
            )

            this.stopForeground(true)
            this.stopSelf()
        }
    }

    private fun createNotification(
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

    private fun sendNotification(
        title: String,
        text: String,
        silent: Boolean = true,
        cancelable: Boolean = false,
        redirect: Intent = Intent(),
        id: Int = notificationId
    ) {
        val notification = createNotification(title, text, silent, cancelable, redirect)
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(id, notification)
    }

    private fun getTweets(startIndex: Int, callback: (List<Status>) -> Unit) {
        Thread(Runnable {
            val list = ArrayList<Status>()
            var lastIndex = startIndex
            try {
                // gets Twitter instance with default credentials
                val twitter = SharedTwitterProperties.instance()
                for (i in startIndex..Int.MAX_VALUE) {
                    sendNotification( // TODO: API 리밋 후? 또는 액티비티 종료 후 여기서 오류
                        getString(R.string.Hetzer_TweetPullingTitle),
                        getString(R.string.Hetzer_TweetPullingContent, list.count())
                    )
                    val paging = Paging(i, 20)
                    val statuses = twitter.getUserTimeline(paging)
                    list.addAll(statuses)
                    lastIndex = i
                    if (statuses.size == 0) break
                }
                callback(list)
            } catch (te: TwitterException) {
                sendNotification(getString(R.string.Hetzer_WaitingTitle), getString(R.string.WaitingDesc))
                Log.i(HetzerService::class.java.name, "API 한도에 도달했습니다. 5분 뒤 다시 시도합니다.")
                Log.i(HetzerService::class.java.name, "lastIndex:$lastIndex")
                Thread.sleep(1000 * 60 * 5)
                getTweets(lastIndex, callback) // TODO: Catch 된 커서가 제대로 마지막 커서 이후인지 확인해봐야함
                te.printStackTrace()
            }
        }).start()
    }

    private fun getTweets(callback: (List<Status>) -> Unit) {
        getTweets(1, callback)
    }

}