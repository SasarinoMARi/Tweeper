package com.sasarinomari.tweeper.ChainBlock

import android.content.Context
import android.content.Intent
import com.sasarinomari.tweeper.Base.BaseService
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SharedTwitterProperties
import com.sasarinomari.tweeper.TwitterExceptionHandler
import twitter4j.TwitterException

class BlockClearService : BaseService() {
    companion object {
        fun checkServiceRunning(context: Context) = BaseService.checkServiceRunning(context)
    }

    lateinit var strServiceName: String
    lateinit var strRateLimitWaiting: String

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        strServiceName = getString(R.string.BlockClear)
        strRateLimitWaiting = getString(R.string.RateLimitWaiting)

        startForeground(NotificationId, createNotification(getString(R.string.app_name), "Initializing...", false))

        getBlockedUsers({ blockedUsers ->
            unblockUsers(blockedUsers, {
                // 알림 송출
                sendNotification(
                    strServiceName,
                    getString(R.string.BlockCleanDone, blockedUsers.count()),
                    silent = false,
                    cancelable = true,
                    id = NotificationId + 1
                )

                // 서비스 종료
                this.stopForeground(true)
                this.stopSelf()
            })
        })

        return START_REDELIVER_INTENT
    }

    // region API 코드
    private fun getBlockedUsers(callback: (ArrayList<Long>) -> Unit, startIndex: Long = -1, list: ArrayList<Long> = ArrayList()) {
        Thread(Runnable {
            val twitter = SharedTwitterProperties.instance()
            var cursor: Long = startIndex
            try {
                while (true) {
                    restrainedNotification(strServiceName, getString(R.string.FetchingUser, list.count()))
                    val users = twitter.getBlocksIDs(cursor)
                    list.addAll(users.iDs.toList())
                    if (users.hasNext()) cursor = users.nextCursor
                    else break
                }
                callback(list)
            } catch (te: TwitterException) {
                object : TwitterExceptionHandler(te, "getBlocksIDs") {
                    override fun onRateLimitExceeded() {
                        sendNotification(
                            "$strServiceName $strRateLimitWaiting",
                            getString(R.string.FetchingUser, list.count())
                        )
                    }

                    override fun onRateLimitReset() {
                        getBlockedUsers(callback, cursor, list)
                    }
                }.catch()
            }
        }).start()
    }

    private fun unblockUsers(list: ArrayList<Long>, callback: () -> Unit, startIndex: Int = 0) {
        Thread(Runnable {
            val twitter = SharedTwitterProperties.instance()
            var cursor = 0
            try {
                for (i in startIndex until list.size) {
                    cursor = i
                    restrainedNotification(
                        strServiceName,
                        getString(R.string.Unblocking, cursor + 1, list.size)
                    ) // 초기값이 0이라 이거 가능
                    twitter.destroyBlock(list[i])
                }
                callback()
            } catch (te: TwitterException) {
                object : TwitterExceptionHandler(te, "createBlock") {
                    override fun onRateLimitExceeded() {
                        sendNotification(
                            "$strServiceName $strRateLimitWaiting",
                            getString(R.string.Unblocking, cursor + 1, list.size)
                        )
                    }

                    override fun onRateLimitReset() {
                        unblockUsers(list, callback, cursor)
                    }
                }.catch()
            }
        }).start()
    }

    // endregion

}