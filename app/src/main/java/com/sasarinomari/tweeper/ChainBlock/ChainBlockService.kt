package com.sasarinomari.tweeper.ChainBlock

import android.content.Context
import android.content.Intent
import android.util.Log
import com.sasarinomari.tweeper.Base.BaseService
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SharedTwitterProperties
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

        sendNotification(getString(R.string.Chainblock), getString(R.string.FriendPulling))
        getFriends(targetUserId) { followings ->
            sendNotification(getString(R.string.Chainblock), getString(R.string.Blocking, followings.count()))
            blockUsers(followings) {
                sendNotification(getString(R.string.Chainblock), getString(R.string.FollowerPulling))
                getFollowers(targetUserId) { followers ->
                    sendNotification(getString(R.string.Chainblock), getString(R.string.Blocking, followings.count()))
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
        Thread(Runnable {
            val twitter = SharedTwitterProperties.instance()
            var cursor: Int = 0
            try {
                for (i in startIndex until list.size) {
                    cursor = i
                    twitter.createBlock(list[i])
                }
                callback()
            } catch (te: TwitterException) {
                te.printStackTrace()
                sendNotification(getString(R.string.API_WaitingTitle), getString(R.string.WaitingDesc))
                Log.i(ChannelName, "createBlock API 한도에 도달했습니다. 5분 뒤 다시 시도합니다.")
                Log.i(ChannelName, "lastIndex:$cursor")
                Thread.sleep(1000 * 60 * 5)
                blockUsers(cursor, list, callback)
                te.printStackTrace()
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
                    val users = twitter.getFriendsIDs(targetUserId, cursor, 5000)
                    list.addAll(users.iDs.toList())
                    if (users.hasNext()) cursor = users.nextCursor
                    else break
                }
                callback(list)
            } catch (te: TwitterException) {
                // 75K명에서 리밋 걸림
                te.printStackTrace()
                sendNotification(getString(R.string.FriendFetch_WaitingTitle), getString(R.string.WaitingDesc))
                Log.i(ChannelName, "getFriendsIDs API 한도에 도달했습니다. 5분 뒤 다시 시도합니다.")
                Log.i(ChannelName, "lastIndex:$cursor")
                Thread.sleep(1000 * 60 * 5)
                getFriends(cursor, targetUserId, callback)
                te.printStackTrace()
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
                    val users = twitter.getFollowersIDs(targetUserId, cursor, 5000)
                    list.addAll(users.iDs.toList())
                    if (users.hasNext()) cursor = users.nextCursor
                    else break
                }
                callback(list)
            } catch (te: TwitterException) {
                // 75K명에서 리밋 걸림
                te.printStackTrace()
                sendNotification(getString(R.string.FollowerFetch_WaitingTitle), getString(R.string.WaitingDesc))
                Log.i(ChannelName, "getFollowersIDs API 한도에 도달했습니다. 5분 뒤 다시 시도합니다.")
                Log.i(ChannelName, "lastIndex:$cursor")
                Thread.sleep(1000 * 60 * 5)
                getFollowers(cursor, targetUserId, callback)
                te.printStackTrace()
            }
        }).start()
    }

    private fun getFollowers(targetUserId: Long, callback: (ArrayList<Long>) -> Unit) {
        getFollowers(-1, targetUserId, callback)
    }

// endregion

}