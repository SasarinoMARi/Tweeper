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
        TargetId, BlockFollowing, BlockFollower
    }

    lateinit var strServiceName: String
    lateinit var strRateLimitWaiting: String

    var followingsCount: Int = 0
    var followersCount: Int = 0
    var blockedCount: Int = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent!!, flags, startId)
        strServiceName = getString(R.string.Chainblock)
        strRateLimitWaiting = getString(R.string.RateLimitWaiting)

        startForeground(NotificationId, createNotification(getString(R.string.app_name), "Initializing...", false))

        val targetUserId = intent.getLongExtra(Parameters.TargetId.name, -1)
        val blockFollowingFlag = intent.getBooleanExtra(Parameters.BlockFollowing.name, false)
        val blockFollowerFlag = intent.getBooleanExtra(Parameters.BlockFollower.name, false)

        val finishedCallback = Runnable {
            // 알림 송출
            sendNotification(
                strServiceName,
                getString(R.string.ChainBlockDone, blockedCount),
                silent = false,
                cancelable = true,
                id = NotificationId + 1
            )

            // 서비스 종료
            this.stopForeground(true)
            this.stopSelf()
        }

        val blockFollower = Runnable {
            getFollowers(targetUserId, { followers ->
                followersCount = followers.count()
                blockUsers(followers, {
                    finishedCallback.run()
                })
            })
        }

        val blockFollowing = Runnable {
            getFriends(targetUserId, { followings ->
                followingsCount = followings.count()
                blockUsers(followings, {
                    if(blockFollowerFlag) blockFollower.run()
                    else finishedCallback.run()
                })
            })
        }

        when {
            blockFollowingFlag -> blockFollowing.run()
            blockFollowerFlag -> blockFollower.run()
            else -> finishedCallback.run()
        }

        return START_REDELIVER_INTENT
    }

    // region API 코드
    private fun blockUsers(list: ArrayList<Long>, callback: () -> Unit, startIndex: Int = 0) {
        Thread(Runnable {
            val twitter = SharedTwitterProperties.instance()
            var cursor = 0
            try {
                for (i in startIndex until list.size) {
                    cursor = i
                    blockedCount += 1
                    restrainedNotification(
                        strServiceName,
                        getString(R.string.Blocking, blockedCount, followingsCount + followersCount)
                    ) // 초기값이 0이라 이거 가능
                    twitter.createBlock(list[i])
                }
                callback()
            } catch (te: TwitterException) {
                object : TwitterExceptionHandler(te, "createBlock") {
                    override fun onRateLimitExceeded() {
                        blockedCount--
                        sendNotification(
                            "$strServiceName $strRateLimitWaiting",
                            getString(R.string.Blocking, followingsCount + cursor + 1, followingsCount + followersCount)
                        )
                    }

                    override fun onRateLimitReset() {
                        blockUsers(list, callback, cursor)
                    }
                }.catch()
            }
        }).start()
    }

    private fun getFriends(targetUserId: Long, callback: (ArrayList<Long>) -> Unit, startIndex: Long = -1, list: ArrayList<Long> = ArrayList()) {
        Thread(Runnable {
            val twitter = SharedTwitterProperties.instance()
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
                        getFriends(targetUserId, callback, cursor, list)
                    }
                }.catch()
            }
        }).start()
    }

    private fun getFollowers(targetUserId: Long, callback: (ArrayList<Long>) -> Unit, startIndex: Long = -1, list: ArrayList<Long> = ArrayList()) {
        Thread(Runnable {
            val twitter = SharedTwitterProperties.instance()
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
                        getFollowers(targetUserId, callback, cursor, list)
                    }
                }.catch()
            }
        }).start()
    }
    // endregion

}