package com.sasarinomari.tweeper.ChainBlock

import android.content.Context
import android.content.Intent
import com.sasarinomari.tweeper.Base.BaseService
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.TwitterAdapter

class ChainBlockService : BaseService() {
    companion object {
        fun checkServiceRunning(context: Context) = BaseService.checkServiceRunning(context)
    }

    enum class Parameters {
        TargetId, BlockFollowing, BlockFollower, IgnoreMyFollowing
    }

    lateinit var strServiceName: String
    lateinit var strRateLimitWaiting: String

    var followingsCount: Int = 0
    var followersCount: Int = 0
    var blockedCount: Int = 0

    val twitterAdapter = TwitterAdapter(this)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent!!, flags, startId)
        strServiceName = getString(R.string.Chainblock)
        strRateLimitWaiting = getString(R.string.RateLimitWaiting)

        startForeground(NotificationId, createNotification(getString(R.string.app_name), "Initializing...", false))

        val targetUserId = intent.getLongExtra(Parameters.TargetId.name, -1)
        val blockFollowingFlag = intent.getBooleanExtra(Parameters.BlockFollowing.name, false)
        val blockFollowerFlag = intent.getBooleanExtra(Parameters.BlockFollower.name, false)
        val ignoreMyFollowing = intent.getBooleanExtra(Parameters.IgnoreMyFollowing.name, false)

        val getMyFriendsRutin = Runnable {
            // TODO:
        }
        val finishRutin = Runnable {
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
        val blockFollowerRutin = Runnable {
            twitterAdapter.getFollowersIds(targetUserId, object: TwitterAdapter.FetchListInterface{
                override fun onStart() { }
                override fun onFinished(list: ArrayList<*>) {
                    followersCount = list.count()
                    list as ArrayList<Long>
                    twitterAdapter.blockUsers(list , object: TwitterAdapter.IterableInterface{
                        override fun onStart() { }
                        override fun onFinished() {
                            finishRutin.run()
                        }

                        override fun onIterate(listIndex: Int) {
                            blockedCount += 1
                            restrainedNotification(
                                strServiceName,
                                getString(R.string.Blocking, blockedCount, followingsCount + followersCount)
                            )
                        }

                        override fun onRateLimit(listIndex: Int) {
                            blockedCount--
                            sendNotification(
                                "$strServiceName $strRateLimitWaiting",
                                getString(R.string.Blocking, followingsCount + listIndex + 1, followingsCount + followersCount)
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
        val blockFriendsRutin = Runnable {
            twitterAdapter.getFriendsIds(targetUserId, object: TwitterAdapter.FetchListInterface{
                override fun onStart() { }
                override fun onFinished(list: ArrayList<*>) {
                    followingsCount = list.count()
                    list as ArrayList<Long>
                    twitterAdapter.blockUsers(list , object: TwitterAdapter.IterableInterface{
                        override fun onStart() { }
                        override fun onFinished() {
                            if(blockFollowerFlag) blockFollowerRutin.run()
                            else finishRutin.run()
                        }

                        override fun onIterate(listIndex: Int) {
                            blockedCount += 1
                            restrainedNotification(
                                strServiceName,
                                getString(R.string.Blocking, blockedCount, followingsCount + followersCount)
                            )
                        }

                        override fun onRateLimit(listIndex: Int) {
                            blockedCount--
                            sendNotification(
                                "$strServiceName $strRateLimitWaiting",
                                getString(R.string.Blocking, followingsCount + listIndex + 1, followingsCount + followersCount)
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

        when {
            ignoreMyFollowing -> getMyFriendsRutin.run()
            blockFollowingFlag -> blockFriendsRutin.run()
            blockFollowerFlag -> blockFollowerRutin.run()
            else -> finishRutin.run()
        }

        return START_REDELIVER_INTENT
    }

}