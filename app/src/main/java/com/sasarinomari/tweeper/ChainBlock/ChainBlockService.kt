package com.sasarinomari.tweeper.ChainBlock

import android.content.Context
import android.content.Intent
import android.util.Log
import com.sasarinomari.tweeper.Base.BaseService
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SharedTwitterProperties
import com.sasarinomari.tweeper.TwitterErrorCode
import twitter4j.TwitterException

class ChainBlockService : BaseService() {
    companion object {
        fun checkServiceRunning(context: Context) = BaseService.checkServiceRunning(context)
    }

    enum class Parameters {
        TargetId
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        startForeground(NotificationId, createNotification(getString(R.string.app_name), "Initializing...", false))

        val targetUserId = intent!!.getLongExtra(Parameters.TargetId.name, -1)

        getFriends(targetUserId) { followings ->
            blockUsers(followings) {
                getFollowers(targetUserId) { followers ->
                    blockUsers(followers) {
                        // 알림 송출
                        sendNotification(
                            getString(R.string.Done),
                            getString(R.string.ChainBlockDone),
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
        sendNotification(getString(R.string.Chainblock), getString(R.string.Blocking, list.count()))
        Thread(Runnable {
            val twitter = SharedTwitterProperties.instance()
            var cursor = 0
            try {
                for (i in startIndex until list.size) {
                    cursor = i
                    twitter.createBlock(list[i])
                }
                callback()
            } catch (te: TwitterException) {
                super.onTwitterException(te, "createBlock") {
                    blockUsers(cursor, list, callback)
                }
            }
        }).start()
    }

    private fun blockUsers(list: ArrayList<Long>, callback: () -> Unit) {
        blockUsers(0, list, callback)
    }

    private fun getFriends(startIndex: Long, targetUserId: Long, callback: (ArrayList<Long>) -> Unit) {
        sendNotification(getString(R.string.Chainblock), getString(R.string.FriendPulling))
        Thread(Runnable {
            val twitter = SharedTwitterProperties.instance()
            val list = ArrayList<Long>()
            var cursor: Long = startIndex
            try {
                while (true) {
                    val users = twitter.getFriendsIDs(targetUserId, cursor, 5000)
                    list.addAll(users.iDs.toList())
                    if (users.hasNext()) cursor = users.nextCursor
                    else break
                }
                callback(list)
            } catch (te: TwitterException) {
                super.onTwitterException(te, "getFriendsIDs") {
                    getFriends(cursor, targetUserId, callback)
                }
            }
        }).start()
    }

    private fun getFriends(targetUserId: Long, callback: (ArrayList<Long>) -> Unit) {
        getFriends(-1, targetUserId, callback)
    }

    private fun getFollowers(startIndex: Long, targetUserId: Long, callback: (ArrayList<Long>) -> Unit) {
        sendNotification(getString(R.string.Chainblock), getString(R.string.FollowerPulling))
        Thread(Runnable {
            val twitter = SharedTwitterProperties.instance()
            val list = ArrayList<Long>()
            var cursor: Long = startIndex
            try {
                while (true) {
                    val users = twitter.getFollowersIDs(targetUserId, cursor, 5000)
                    list.addAll(users.iDs.toList())
                    if (users.hasNext()) cursor = users.nextCursor
                    else break
                }
                callback(list)
            } catch (te: TwitterException) {
                super.onTwitterException(te, "getFollowersIDs") {
                    getFollowers(cursor, targetUserId, callback)
                }
            }
        }).start()
    }

    private fun getFollowers(targetUserId: Long, callback: (ArrayList<Long>) -> Unit) {
        getFollowers(-1, targetUserId, callback)
    }

// endregion

}