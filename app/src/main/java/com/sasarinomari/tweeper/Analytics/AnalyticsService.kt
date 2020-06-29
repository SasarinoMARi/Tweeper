package com.sasarinomari.tweeper.Analytics

import android.content.Context
import android.content.Intent
import android.util.Log
import com.sasarinomari.tweeper.Base.BaseService
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SharedTwitterProperties
import com.sasarinomari.tweeper.Report.ReportInterface
import twitter4j.TwitterException
import twitter4j.User
import java.util.*
import kotlin.collections.ArrayList

class AnalyticsService : BaseService() {
    companion object {
        fun checkServiceRunning(context: Context) = BaseService.checkServiceRunning(context)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        startForeground(NotificationId,
            createNotification(getString(R.string.app_name), "Initializing...", false))

        getMe { me ->
            getFriends { followings ->
                getFollowers { followers ->
                    Log.i(ChannelName, "Fridnes: ${followings.size},\tFollowers: ${followers.size}")

                    // 리포트 기록
                    val ri = ReportInterface<AnalyticsReport>(AnalyticsReport.prefix)
                    val lastReportIndex = ri.getReportCount(this)
                    val recentReport = if(lastReportIndex >= 0) ri.readReport(this, lastReportIndex, AnalyticsReport()) as AnalyticsReport else null
                    val report = AnalyticsReport(me, followings, followers, recentReport)
                    report.id = lastReportIndex + 1
                    ri.writeReport(this, report.id, report)

                    // 알림 송출
                    val redirect = Intent(this, AnalyticsReportActivity::class.java)
                    redirect.putExtra(AnalyticsReportActivity.Parameters.ReportId.name, report.id)
                    sendNotification(
                        getString(R.string.Done),
                        getString(R.string.AnalyticsDone),
                        silent = false,
                        cancelable = true,
                        redirect = redirect,
                        id = NotificationId + 1
                    )

                    // 서비스 종료
                    this.stopForeground(true)
                    this.stopSelf()
                }
            }
        }

        return START_REDELIVER_INTENT
    }

    // region API 코드
    private fun getMe(callback: (User)-> Unit) {
        Thread(Runnable {
            try {
                val twitter = SharedTwitterProperties.instance()
                val me = twitter.showUser(twitter.id)
                callback(me)
            } catch (te: TwitterException) {
                te.printStackTrace()
                sendNotification(getString(R.string.API_WaitingTitle), getString(R.string.WaitingDesc))
                Log.i(ChannelName, "showUser API 한도에 도달했습니다. 5분 뒤 다시 시도합니다.")
                Thread.sleep(1000 * 60 * 5)
                getMe(callback)
                te.printStackTrace()
            }
        }).start()
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

    // endregion

}