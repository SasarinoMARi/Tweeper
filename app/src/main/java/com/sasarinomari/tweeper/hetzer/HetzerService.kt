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

class HetzerService : Service() {
    companion object {
        val ChannelName = "Hetzer"
    }

    private val notificationId = 4425
    private lateinit var silentChannelBuilder: NotificationCompat.Builder
    private lateinit var defaultChannelBuilder: NotificationCompat.Builder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TODO : 같은 유저에 의해 여러번 수행되는 경우 조치해야 함!

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

        return START_STICKY
    }

    override fun onDestroy() {
        Log.i(HetzerService::class.java.name, "onDestroy")
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.i(HetzerService::class.java.name, "onBind")
        return null
    }

    private fun hetzerLogic(intent: Intent) {
        val json = intent.getStringExtra(HetzerConditionsActivity.Results.Conditions.name)
        val typeToken = object : TypeToken<HashMap<Int, Any>>() {}.type
        val conditions = Gson().fromJson(json, typeToken) as HashMap<Int, Any>
        val hetzer = Hetzer(conditions)

        getTweets { tweets ->
            if (tweets.isNotEmpty())
                for (i in 0 until tweets.count()) {
                    val item = tweets[i]
                    val text = item.text
                    if (hetzer.filter(item, i)) {
                        sendNotification(
                            getString(R.string.Hetzer_TweetRemovingTitle),
                            getString(R.string.Hetzer_TweetRemovingContent, tweets.count(), i + 1)
                        )

                        // TODO : 이미 트윗이 지워진 경우 등 예외상황에 잘 동작하는지 확인할 필요 있음
                        // SharedTwitterProperties.instance().destroyStatus(item.id)
                    }
                }

            sendNotification(getString(R.string.Done), getString(R.string.Hetzer_Done), silent = false, cancelable = true, id = notificationId + 1)
            this.stopForeground(true)
        }
    }

    private fun createNotification(
        title: String,
        text: String,
        silent: Boolean = true,
        cancelable: Boolean = false,
        id: Int = notificationId
    ): Notification {
        val clsIntent = Intent(this, HetzerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, clsIntent, 0)
        val builder = if (silent) silentChannelBuilder else defaultChannelBuilder

        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle(title)
        builder.setContentText(text)
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(cancelable)

        return builder.build()!!
    }

    private fun sendNotification(title: String, text: String, silent: Boolean = true, cancelable: Boolean = false, id: Int = notificationId) {
        val notification = createNotification(title, text, silent, cancelable, id)
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
                    sendNotification(
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
                sendNotification(getString(R.string.Hetzer_WaitingTitle), getString(R.string.Hetzer_WaitingContent))
                Log.i(HetzerService::class.java.name, "API 한도에 도달했습니다. 5분 뒤 다시 시도합니다.")
                Log.i(HetzerService::class.java.name, "lastIndex:$lastIndex")
                Thread.sleep(1000 * 60 * 5)
                getTweets(lastIndex, callback)
                te.printStackTrace()
            }
        }).start()
    }

    private fun getTweets(callback: (List<Status>) -> Unit) {
        getTweets(1, callback)
    }

}