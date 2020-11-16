package com.sasarinomari.tweeper.ChainBlock

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.Base.BaseService
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.TwitterAdapter

class ChainBlockService : BaseService() {
    companion object {
        fun checkServiceRunning(context: Context) = BaseService.checkServiceRunning(context, ChainBlockService::class.java.name)
    }

    enum class Parameters {
        User, TargetId, BlockFollowing, BlockFollower, IgnoreMyFollowing
    }

    lateinit var strServiceName: String
    lateinit var strRateLimitWaiting: String

    private var targetUserId: Long = -1
    private var blockFollowingFlag: Boolean = false
    private var blockFollowerFlag: Boolean = false
    private var ignoreMyFollowing: Boolean = false

    private var followingsCount: Int = 0
    private var followersCount: Int = 0
    private var blockedCount: Int = 0

    private var ignoreUserList: ArrayList<Long>? = null

    val twitterAdapter = TwitterAdapter()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (super.onStartCommand(intent!!, flags, startId) == START_NOT_STICKY) return START_NOT_STICKY
        strServiceName = getString(R.string.Chainblock)
        strRateLimitWaiting = getString(R.string.RateLimitWaiting)

        val user = Gson().fromJson(intent.getStringExtra(Parameters.User.name), AuthData::class.java)
        if(user?.token == null) {
            sendNotification(getString(R.string.Chainblock), getString(R.string.AuthFailed),
                silent = false, cancelable = true, id = NotificationId+2)
            return START_NOT_STICKY
        }
        twitterAdapter.initialize(user.token!!)

        startForeground(NotificationId, createNotification(getString(R.string.app_name), "Initializing...", false))

        targetUserId = intent.getLongExtra(Parameters.TargetId.name, -1)
        blockFollowingFlag = intent.getBooleanExtra(Parameters.BlockFollowing.name, false)
        blockFollowerFlag = intent.getBooleanExtra(Parameters.BlockFollower.name, false)
        ignoreMyFollowing = intent.getBooleanExtra(Parameters.IgnoreMyFollowing.name, false)

        doNextStuff()

        return START_REDELIVER_INTENT
    }

    private fun doNextStuff() {
        when {
            ignoreMyFollowing -> getMyFollowing()
            blockFollowingFlag -> blockFollowing()
            blockFollowerFlag -> blockFollowers()
            else -> finish()
        }
    }

    private fun getMyFollowing() {
        runOnManagedThread {
            twitterAdapter.getMe(object : TwitterAdapter.FetchObjectInterface {
                override fun onStart() {
                    sendNotification(strServiceName, getString(R.string.PullingMe))
                }

                override fun onFinished(obj: Any) {
                    ignoreMyFollowing = false
                    val me = obj as twitter4j.User
                    runOnManagedThread {
                        twitterAdapter.getFriendsIds(me.id, object : TwitterAdapter.FetchListInterface {
                            override fun onStart() {}
                            override fun onFinished(list: ArrayList<*>) {
                                ignoreUserList = list as ArrayList<Long>
                                ignoreMyFollowing = false
                                doNextStuff()
                            }

                            override fun onFetch(listSize: Int) {
                                restrainedNotification(strServiceName, getString(R.string.FriendPulling, listSize))
                            }

                            override fun onRateLimit(listSize: Int) {
                                sendNotification(
                                    "$strServiceName $strRateLimitWaiting",
                                    getString(R.string.FollowerPulling, listSize)
                                )
                            }

                            override fun onUncaughtError() {
                                this@ChainBlockService.onUncaughtError(strServiceName)
                            }

                            override fun onNetworkError(retrySelf: ()->Unit) {
                                this@ChainBlockService.onNetworkError(strServiceName, { retrySelf() })
                            }
                        })
                    }
                }

                override fun onRateLimit() {
                    sendNotification("$strServiceName $strRateLimitWaiting", "")
                }

                override fun onUncaughtError() {
                    this@ChainBlockService.onUncaughtError(strServiceName)
                }

                override fun onNetworkError(retrySelf: ()->Unit) {
                    this@ChainBlockService.onNetworkError(strServiceName, { retrySelf() })
                }
            })
        }
    }

    private fun blockFollowing() {
        runOnManagedThread {
            twitterAdapter.getFriendsIds(targetUserId, object : TwitterAdapter.FetchListInterface {
                override fun onStart() {}
                override fun onFinished(list: ArrayList<*>) {
                    val targets = ignoreingUsers(list as ArrayList<Long>, ignoreUserList)
                    followingsCount = targets.count()
                    runOnManagedThread {
                        twitterAdapter.blockUsers(targets, object : TwitterAdapter.IterableInterface {
                            override fun onStart() {}
                            override fun onFinished() {
                                blockFollowingFlag = false
                                doNextStuff()
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

                            override fun onUncaughtError() {
                                this@ChainBlockService.onUncaughtError(strServiceName)
                            }

                            override fun onNetworkError(retrySelf: ()->Unit) {
                                this@ChainBlockService.onNetworkError(strServiceName, { retrySelf() })
                            }
                        })
                    }
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

                override fun onUncaughtError() {
                    this@ChainBlockService.onUncaughtError(strServiceName)
                }

                override fun onNetworkError(retrySelf: ()->Unit) {
                    this@ChainBlockService.onNetworkError(strServiceName, { retrySelf() })
                }
            })
        }
    }

    private fun blockFollowers() {
        runOnManagedThread {
            twitterAdapter.getFollowersIds(targetUserId, object : TwitterAdapter.FetchListInterface {
                override fun onStart() {}
                override fun onFinished(list: ArrayList<*>) {
                    val targets = ignoreingUsers(list as ArrayList<Long>, ignoreUserList)
                    followersCount = targets.count()
                    runOnManagedThread {
                        twitterAdapter.blockUsers(targets, object : TwitterAdapter.IterableInterface {
                            override fun onStart() {}
                            override fun onFinished() {
                                blockFollowerFlag = false
                                doNextStuff()
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

                            override fun onUncaughtError() {
                                this@ChainBlockService.onUncaughtError(strServiceName)
                            }

                            override fun onNetworkError(retrySelf: ()->Unit) {
                                this@ChainBlockService.onNetworkError(strServiceName, { retrySelf() })
                            }
                        })
                    }
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

                override fun onUncaughtError() {
                    this@ChainBlockService.onUncaughtError(strServiceName)
                }

                override fun onNetworkError(retrySelf: ()->Unit) {
                    this@ChainBlockService.onNetworkError(strServiceName, { retrySelf() })
                }
            })
        }
    }

    override fun finish() {
        // 알림 송출
        sendNotification(
            strServiceName,
            getString(R.string.ChainBlockDone, blockedCount),
            silent = false,
            cancelable = true,
            id = NotificationId + 1
        )

        super.finish()
    }

    private fun ignoreingUsers(users: ArrayList<Long>, ignoreUsers: ArrayList<Long>?): ArrayList<Long> {
        if (ignoreUsers == null) return users
        for (ignore in ignoreUsers) {
            if (users.contains(ignore)) {
                Log.i("ChainBlock", "Ignored Block: User $ignore")
                users.remove(ignore)
            }
        }
        return users
    }
}