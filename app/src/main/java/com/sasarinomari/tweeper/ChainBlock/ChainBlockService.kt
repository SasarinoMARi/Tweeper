package com.sasarinomari.tweeper.ChainBlock

import android.content.Context
import android.content.Intent
import android.util.Log
import com.sasarinomari.tweeper.Base.BaseService
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SharedTwitterProperties
import com.sasarinomari.tweeper.TwitterExceptionHandler
import twitter4j.TwitterException

class ChainBlockService : BaseService() {
    companion object {
        fun checkServiceRunning(context: Context) = BaseService.checkServiceRunning(context)
    }

    enum class Parameters {
        TargetId
    }

    lateinit var strServiceName: String
    lateinit var strRateLimitWaiting: String

    var followingsCount: Int = 0
    var followersCount: Int = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        strServiceName = getString(R.string.Chainblock)
        strRateLimitWaiting = getString(R.string.RateLimitWaiting)

        startForeground(NotificationId, createNotification(getString(R.string.app_name), "Initializing...", false))

        val targetUserId = intent!!.getLongExtra(Parameters.TargetId.name, -1)

        getFriends(targetUserId) { followings ->
            followingsCount = followings.count()
            blockUsers(followings) {
                getFollowers(targetUserId) { followers ->
                    followersCount = followers.count()
                    blockUsers(followers) {
                        // 알림 송출
                        sendNotification(
                            strServiceName,
                            getString(R.string.ChainBlockDone, followingsCount + followersCount),
                            silent = false,
                            cancelable = true,
                            id = NotificationId + 1
                        )

                        // 서비스 종료
                        this.stopForeground(true)
                        this.stopSelf()
                    }
                }
            }
        }

        return START_REDELIVER_INTENT
    }

    // region API 코드
    private fun blockUsers(startIndex: Int, list: ArrayList<Long>, callback: () -> Unit) {
        Thread(Runnable {
            val twitter = SharedTwitterProperties.instance()
            var cursor = 0
            try {
                for (i in startIndex until list.size) {
                    cursor = i
                    restrainedNotification(
                        strServiceName,
                        getString(R.string.Blocking, followingsCount + cursor + 1, followingsCount + followersCount)
                    ) // 초기값이 0이라 이거 가능
                    twitter.createBlock(list[i])
                }
                callback()
            } catch (te: TwitterException) {
                object : TwitterExceptionHandler(te, "createBlock") {
                    override fun onRateLimitExceeded() {
                        sendNotification(
                            "$strServiceName $strRateLimitWaiting",
                            getString(R.string.Blocking, followingsCount + cursor + 1, followingsCount + followersCount)
                        )
                    }

                    override fun onRateLimitReset() {
                        blockUsers(cursor, list, callback)
                    }
                }.catch()
            }
        }).start()
    }

    private fun blockUsers(list: ArrayList<Long>, callback: () -> Unit) {
        blockUsers(0, list, callback)
    }

    private fun getFriends(startIndex: Long, targetUserId: Long, callback: (ArrayList<Long>) -> Unit) {
        Thread(Runnable {
            val twitter = SharedTwitterProperties.instance()
            val list = ArrayList<Long>()
            var cursor: Long = startIndex
            try {
                while (true) {
                    restrainedNotification(strServiceName, getString(R.string.FetchingUser, list.count()))
                    val users = twitter.getFriendsIDs(targetUserId, cursor, 5000)
                    list.addAll(users.iDs.toList())
                    Log.i(ChannelName, "Count of Collected Users: ${list.count()}")
                    if (users.hasNext()) cursor = users.nextCursor
                    else break
                }
                callback(list)
            } catch (te: TwitterException) {
                object : TwitterExceptionHandler(te, "getFriendsIDs") {
                    override fun onRateLimitExceeded() {
                        sendNotification(
                            "$strServiceName $strRateLimitWaiting",
                            getString(R.string.FetchingUser, list.count())
                        )
                    }

                    override fun onRateLimitReset() {
                        getFriends(cursor, targetUserId, callback)
                    }
                }.catch()
            }
        }).start()
    }

    private fun getFriends(targetUserId: Long, callback: (ArrayList<Long>) -> Unit) {
        getFriends(-1, targetUserId, callback)
    }

    private fun getFollowers(startIndex: Long, targetUserId: Long, callback: (ArrayList<Long>) -> Unit) {
        Thread(Runnable {
            val twitter = SharedTwitterProperties.instance()
            val list = ArrayList<Long>()
            var cursor: Long = startIndex
            try {
                while (true) {
                    restrainedNotification(strServiceName, getString(R.string.FetchingUser, followingsCount + list.count()))
                    val users = twitter.getFollowersIDs(targetUserId, cursor, 5000)
                    list.addAll(users.iDs.toList())
                    if (users.hasNext()) cursor = users.nextCursor
                    else break
                }
                callback(list)
            } catch (te: TwitterException) {
                object : TwitterExceptionHandler(te, "getFollowersIDs") {
                    override fun onRateLimitExceeded() {
                        sendNotification(
                            "$strServiceName $strRateLimitWaiting",
                            getString(R.string.FetchingUser, followingsCount + list.count())
                        )
                    }

                    override fun onRateLimitReset() {
                        getFollowers(cursor, targetUserId, callback)
                    }
                }.catch()
            }
        }).start()
    }

    private fun getFollowers(targetUserId: Long, callback: (ArrayList<Long>) -> Unit) {
        getFollowers(-1, targetUserId, callback)
    }
    // endregion

}