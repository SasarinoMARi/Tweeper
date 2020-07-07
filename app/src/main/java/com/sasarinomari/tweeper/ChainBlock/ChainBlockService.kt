package com.sasarinomari.tweeper.ChainBlock

import android.content.Context
import android.content.Intent
import android.util.Log
import com.sasarinomari.tweeper.Base.BaseService
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.TwitterAdapter
import twitter4j.User

class ChainBlockService : BaseService() {
    companion object {
        fun checkServiceRunning(context: Context) = BaseService.checkServiceRunning(context)
    }

    enum class Parameters {
        TargetId, BlockFollowing, BlockFollower, IgnoreMyFollowing
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

    val twitterAdapter = TwitterAdapter(this)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent!!, flags, startId)
        strServiceName = getString(R.string.Chainblock)
        strRateLimitWaiting = getString(R.string.RateLimitWaiting)

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
        twitterAdapter.getMe(object : TwitterAdapter.FetchObjectInterface {
            override fun onStart() {
                sendNotification(strServiceName, getString(R.string.PullingMe))
            }

            override fun onFinished(obj: Any) {
                ignoreMyFollowing = false
                val me = obj as twitter4j.User
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
                })
            }

            override fun onRateLimit() {
                sendNotification("$strServiceName $strRateLimitWaiting", "")
            }
        })
    }

    private fun blockFollowing() {
        twitterAdapter.getFriendsIds(targetUserId, object : TwitterAdapter.FetchListInterface {
            override fun onStart() {}
            override fun onFinished(list: ArrayList<*>) {
                val targets = ignoreingUsers(list as ArrayList<Long>, ignoreUserList)
                followingsCount = targets.count()
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

    private fun blockFollowers() {
        twitterAdapter.getFollowersIds(targetUserId, object : TwitterAdapter.FetchListInterface {
            override fun onStart() {}
            override fun onFinished(list: ArrayList<*>) {
                val targets = ignoreingUsers(list as ArrayList<Long>, ignoreUserList)
                followersCount = targets.count()
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

    private fun finish() {
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

    private fun ignoreingUsers(users: ArrayList<Long>, ignoreUsers: ArrayList<Long>?):ArrayList<Long> {
        if(ignoreUsers == null) return users
        for (ignore in ignoreUsers) {
            if(users.contains(ignore)) {
                Log.i("ChainBlock", "Ignored Block: User $ignore")
                users.remove(ignore)
            }
        }
        return users
    }
}