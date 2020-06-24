package com.sasarinomari.tweeper.fwmanage

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
import twitter4j.User

class FollowerManagerService : Service() {

    companion object {
        val ChannelName = "FollowerManagement"
        var _innerRunningFlag = false

        fun chechServiceRunning(context: Context): Boolean {
            var flag1 = false
            val flag2 = _innerRunningFlag

            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Integer.MAX_VALUE)) { // TODO : 이것 때문에 릴리즈 안될 수도..
                if (FollowerManagerService::class.java.name == service.service.className) {
                    Log.i(ChannelName, "언팔매니저 서비스가 이미 실행중입니다.")
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

        getFriends { friends ->
            getFollowers { followers ->
                Log.i(ChannelName, "Fridnes: ${friends.size},\tFollowers: ${followers.size}")
                val result = compList(friends, followers)
                for(line in result){
                    Log.i(ChannelName, line.screenName)
                }

                // TODO: 완료 후 알림 누르면 결과 페이지로 가게
                // TODO: 알림을 놓쳤을 경우를 대비해서 메뉴로도 진입할 수 있어야 함.
                sendNotification(getString(R.string.Done), getString(R.string.FollowerManagementDone), silent = false, cancelable = true, id = notificationId + 1)
                this.stopForeground(true)
                this.stopSelf()
            }
        }

        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        Log.i(ChannelName, "onDestroy")
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.i(ChannelName, "onBind")
        Companion._innerRunningFlag = true
        return null
    }

    private fun createNotification(
        title: String,
        text: String,
        silent: Boolean = true,
        cancelable: Boolean = false,
        id: Int = notificationId
    ): Notification {
        // Todo: 클릭해도 반응 없게 하기
        val clsIntent = Intent(this, FollowerManagement::class.java)
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


    private fun getFriends(startIndex: Long, callback: (ArrayList<User>)-> Unit) {
        Thread(Runnable {
            val list = ArrayList<User>()
            // gets Twitter instance with default credentials
            var cursor: Long = startIndex
            val twitter = SharedTwitterProperties.instance()
            val me = SharedTwitterProperties.myId!!
            try {
                while (true) {
                    sendNotification(
                        getString(R.string.FriendFetch_PullingTile),
                        getString(R.string.FriendFetch_PullingDesc, list.count())
                    )
                    val users = twitter.getFriendsList(me, cursor, 200, true, true)
                    list.addAll(users)
                    if (users.hasNext()) cursor = users.nextCursor
                    else break
                }
                callback(list)
            } catch (te: TwitterException) {
                sendNotification(getString(R.string.FriendFetch_PullingTile), getString(R.string.WaitingDesc))
                Log.i(ChannelName, "getFriends API 한도에 도달했습니다. 5분 뒤 다시 시도합니다.")
                Log.i(ChannelName, "lastIndex:$cursor")
                Thread.sleep(1000 * 60 * 5)
                getFriends(cursor, callback)
                te.printStackTrace()
            }
        }).start()
    }

    private fun getFriends(callback: (ArrayList<User>) -> Unit) {
        getFriends(-1, callback)
    }

    private fun getFollowers(startIndex: Long, callback: (ArrayList<User>)-> Unit) {
        Thread(Runnable {
            val list = ArrayList<User>()
            // gets Twitter instance with default credentials
            var cursor: Long = startIndex
            val twitter = SharedTwitterProperties.instance()
            val me = SharedTwitterProperties.myId!!
            try {
                while (true) {
                    sendNotification(
                        getString(R.string.FriendFetch_PullingTile),
                        getString(R.string.FriendFetch_PullingDesc, list.count())
                    )
                    val users = twitter.getFollowersList(me, cursor, 200, true, true)
                    list.addAll(users)
                    if (users.hasNext()) cursor = users.nextCursor
                    else break
                }
                callback(list)
            } catch (te: TwitterException) {
                sendNotification(getString(R.string.FollowerFetch_WaitingTitle), getString(R.string.WaitingDesc))
                Log.i(ChannelName, "getFollowers API 한도에 도달했습니다. 5분 뒤 다시 시도합니다.")
                Log.i(ChannelName, "lastIndex:$cursor")
                Thread.sleep(1000 * 60 * 5)
                getFollowers(cursor, callback)
                te.printStackTrace()
            }
        }).start()
    }

    private fun getFollowers(callback: (ArrayList<User>) -> Unit) {
        getFollowers(-1, callback)
    }


    private fun compList(fs: List<User>, fw: List<User>): ArrayList<User> {
        val res = ArrayList<User>()
        for (u1 in fs) {
            var correspond = false
            for (u2 in fw) {
                if (u1.id == u2.id) {
                    correspond = true
                    break
                }
            }
            if (!correspond) {
                res.add(u1)
            }
        }
        return res
    }

}