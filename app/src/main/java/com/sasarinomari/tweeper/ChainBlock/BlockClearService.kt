package com.sasarinomari.tweeper.ChainBlock

import android.content.Context
import android.content.Intent
import com.sasarinomari.tweeper.Base.BaseService
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.TwitterAdapter

class BlockClearService : BaseService() {
    companion object {
        fun checkServiceRunning(context: Context) = BaseService.checkServiceRunning(context, BlockClearService::class.java.name)
    }

    lateinit var strServiceName: String
    lateinit var strRateLimitWaiting: String

    private val twitterAdapter = TwitterAdapter()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (super.onStartCommand(intent!!, flags, startId) == START_NOT_STICKY) return START_NOT_STICKY
        strServiceName = getString(R.string.BlockClear)
        strRateLimitWaiting = getString(R.string.RateLimitWaiting)

        startForeground(NotificationId, createNotification(getString(R.string.app_name), "Initializing...", false))

        runOnManagedThread {
            twitterAdapter.getBlockedUsers(object: TwitterAdapter.FetchListInterface {
                override fun onStart() { }

                override fun onFinished(list: ArrayList<*>) {
                    twitterAdapter.unblockUsers(list as ArrayList<Long>, object: TwitterAdapter.IterableInterface {
                        override fun onStart() { }

                        override fun onFinished() {
                            // 알림 송출
                            sendNotification(
                                strServiceName,
                                getString(R.string.BlockCleanDone, list.count()),
                                silent = false,
                                cancelable = true,
                                id = NotificationId + 1
                            )

                            // 서비스 종료
                            this@BlockClearService.stopForeground(true)
                            this@BlockClearService.stopSelf()
                        }

                        override fun onIterate(listIndex: Int) {
                            restrainedNotification(
                                strServiceName,
                                getString(R.string.Unblocking, listIndex, list.size)
                            ) // 초기값이 0이라 이거 가능
                        }

                        override fun onRateLimit(listIndex: Int) {
                            sendNotification(
                                "$strServiceName $strRateLimitWaiting",
                                getString(R.string.Unblocking, listIndex, list.size)
                            )
                        }
                    })
                }

                override fun onFetch(listSize: Int) {
                    restrainedNotification(strServiceName, getString(R.string.FetchingUser, listSize))
                }

                override fun onRateLimit(listSize: Int) {
                    sendNotification(
                        "$strServiceName $strRateLimitWaiting",
                        getString(R.string.FetchingUser, listSize)
                    )
                }
            })
        }

        return START_REDELIVER_INTENT
    }

}