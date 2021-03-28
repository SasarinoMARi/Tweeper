package com.sasarinomari.tweeper.Analytics

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.Base.BaseService
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.Report.ReportInterface
import com.sasarinomari.tweeper.TwitterAdapter
import java.lang.Exception

class AnalyticsService : BaseService() {
    enum class Parameters {
        User
    }

    companion object {
        fun checkServiceRunning(context: Context) = BaseService.checkServiceRunning(context, AnalyticsService::class.java.name)
    }

    lateinit var strServiceName: String
    lateinit var strRateLimitWaiting: String

    val twitterAdapter = TwitterAdapter()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (super.onStartCommand(intent!!, flags, startId) == START_NOT_STICKY) return START_NOT_STICKY
        strServiceName = getString(R.string.TweetAnalytics)
        strRateLimitWaiting = getString(R.string.RateLimitWaiting)

        val user = Gson().fromJson(intent.getStringExtra(Parameters.User.name), AuthData::class.java)
        twitterAdapter.initialize(this, user.token!!)

        startForeground(NotificationId,
            createNotification(getString(R.string.app_name), "Initializing...", false))

        runOnManagedThread {
            twitterAdapter.getMe(object : TwitterAdapter.FetchObjectInterface {
                override fun onStart() {
                    sendNotification(strServiceName, getString(R.string.PullingMe))
                }

                override fun onFinished(obj: Any) {
                    val me = obj as twitter4j.User
                    runOnManagedThread {
                        twitterAdapter.getFriends(me.id, object : TwitterAdapter.FetchListInterface {
                            override fun onStart() {}
                            override fun onFinished(list: ArrayList<*>) {
                                val followings = list as ArrayList<twitter4j.User>
                                runOnManagedThread {
                                    twitterAdapter.getFollowers(me.id, object : TwitterAdapter.FetchListInterface {
                                        override fun onStart() {}

                                        override fun onFinished(list: ArrayList<*>) {
                                            val context = this@AnalyticsService
                                            val followers = list as ArrayList<twitter4j.User>
                                            Log.i(ChannelName, "Fridnes: ${followings.size},\tFollowers: ${followers.size}")

                                            // 리포트 기록
                                            val ri = ReportInterface<AnalyticsReport>(twitterAdapter.twitter.id, AnalyticsReport.prefix)
                                            val lastReportIndex = ri.getReportCount(context)
                                            val recentReport = if (lastReportIndex >= 0) ri.readReport(
                                                context,
                                                lastReportIndex,
                                                AnalyticsReport()
                                            ) as AnalyticsReport? else null
                                            val report = AnalyticsReport(me, followings, followers, recentReport)
                                            report.id = lastReportIndex + 1

                                            try {
                                                ri.writeReport(context, report.id, report)

                                                // 알림 송출
                                                val redirect = Intent(context, AnalyticsReportActivity::class.java)
                                                redirect.putExtra(AnalyticsReportActivity.Parameters.ReportId.name, report.id)
                                                sendNotification(
                                                    strServiceName,
                                                    getString(R.string.AnalyticsDone),
                                                    silent = false,
                                                    cancelable = true,
                                                    redirect = redirect,
                                                    id = NotificationId + 1
                                                )
                                                context.sendActivityRefrashNotification(AnalyticsActivity::class.java.name)
                                            } catch (ome: OutOfMemoryError) {
                                                sendNotification(strServiceName,
                                                getString(R.string.Error_OutOfMemory),
                                                silent = false,
                                                cancelable = true,
                                                redirect = Intent(),
                                                id = NotificationId + 1)
                                            } catch (e: Exception) {
                                                sendNotification(strServiceName,
                                                    getString(R.string.Error),
                                                    silent = false,
                                                    cancelable = true,
                                                    redirect = Intent(),
                                                    id = NotificationId + 1)
                                            }


                                            /*
                                            if(!AdRemover(context).isAdRemoved()) {
                                                twitterAdapter.publish("${getString(R.string.HetzerDoneTweet)} ${getString(R.string.StoreUrl)}", object: TwitterAdapter.PostInterface {
                                                    override fun onStart() { }
                                                    override fun onFinished(obj: Any) { finish() }
                                                    override fun onRateLimit() { finish() }
                                                })
                                            }
                                            else finish()
                                             */
                                            finish()
                                        }

                                        override fun onFetch(listSize: Int) {
                                            restrainedNotification(strServiceName, getString(R.string.FollowerPulling, listSize))
                                        }

                                        override fun onRateLimit(listSize: Int) {
                                            sendNotification(
                                                "$strServiceName $strRateLimitWaiting",
                                                getString(R.string.FollowerPulling, list.count())
                                            )
                                        }

                                        override fun onUncaughtError() {
                                            this@AnalyticsService.onUncaughtError(strServiceName)
                                        }

                                        override fun onNetworkError(retrySelf: ()->Unit) {
                                            this@AnalyticsService.onNetworkError(strServiceName, { retrySelf() })
                                        }
                                    })
                                }
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
                                this@AnalyticsService.onUncaughtError(strServiceName)
                            }

                            override fun onNetworkError(retrySelf: ()->Unit) {
                                this@AnalyticsService.onNetworkError(strServiceName, { retrySelf() })
                            }
                        })
                    }
                }

                override fun onRateLimit() {
                    sendNotification("$strServiceName $strRateLimitWaiting", "")
                }

                override fun onUncaughtError() {
                    this@AnalyticsService.onUncaughtError(strServiceName)
                }

                override fun onNetworkError(retrySelf: ()->Unit) {
                    this@AnalyticsService.onNetworkError(strServiceName, { retrySelf() })
                }
            })
        }

        return START_REDELIVER_INTENT
    }

    // region API 코드
    // endregion

}