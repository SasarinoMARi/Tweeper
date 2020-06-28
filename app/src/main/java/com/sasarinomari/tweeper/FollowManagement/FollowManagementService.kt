package com.sasarinomari.tweeper.FollowManagement

import android.content.Context
import android.content.Intent
import android.util.Log
import com.sasarinomari.tweeper.Base.BaseService
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SharedTwitterProperties
import com.sasarinomari.tweeper.Hetzer.HetzerReportActivity
import com.sasarinomari.tweeper.Report.ReportInterface
import twitter4j.TwitterException
import twitter4j.User

class FollowManagementService : BaseService() {
    companion object {
        fun checkServiceRunning(context: Context) = BaseService.checkServiceRunning(context)
    }
    override val ChannelName: String = "FollowerManagement"
    override val NotificationId: Int = ChannelName.hashCode()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        startForeground(NotificationId,
            createNotification(getString(R.string.app_name), "Initializing...", false))

        getFriends { friends ->
            getFollowers { followers ->
                Log.i(ChannelName, "Fridnes: ${friends.size},\tFollowers: ${followers.size}")
                val traitors = getDifference(friends, followers)
                val fans = getDifference(followers, friends)

                // 리포트 기록
                val ri = ReportInterface<FollowManagementReport>(FollowManagementReport.prefix)
                val report = FollowManagementReport(traitors, fans)
                report.id = ri.getReportCount(this)+1
                ri.writeReport(this, report.id, report)

                // 알림 송출
                val redirect = Intent(this, HetzerReportActivity::class.java)
                redirect.putExtra(HetzerReportActivity.Parameters.ReportId.name, report.id)
                sendNotification(
                    getString(R.string.Done),
                    getString(R.string.FollowerManagementDone),
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

        return START_REDELIVER_INTENT
    }

    private fun getDifference(outer: List<User>, inner: List<User>): ArrayList<User> {
        val res = ArrayList<User>()
        for (u1 in outer) {
            var correspond = false
            for (u2 in inner) {
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

    // region API 코드
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