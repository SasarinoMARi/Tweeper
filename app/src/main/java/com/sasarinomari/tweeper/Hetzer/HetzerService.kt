package com.sasarinomari.tweeper.Hetzer

import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sasarinomari.tweeper.Base.BaseService
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.Report.ReportInterface
import com.sasarinomari.tweeper.TwitterAdapter
import twitter4j.Status
import java.lang.Exception

class HetzerService : BaseService() {
    companion object {
        fun checkServiceRunning(context: Context) = BaseService.checkServiceRunning(context, HetzerService::class.java.name)
    }

    enum class Parameters {
        HetzerConditions, UserId
    }

    lateinit var strServiceName: String
    lateinit var strRateLimitWaiting: String

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (super.onStartCommand(intent!!, flags, startId) == START_NOT_STICKY) return START_NOT_STICKY
        strServiceName = getString(R.string.TweetCleaner)
        strRateLimitWaiting = getString(R.string.RateLimitWaiting)

        startForeground(
            NotificationId,
            createNotification(getString(R.string.app_name), "Initializing...", false)
        )

        hetzerLogic(intent)

        return START_REDELIVER_INTENT
    }

    val twitterAdapter = TwitterAdapter()

    private fun hetzerLogic(intent: Intent) {
        if(!intent.hasExtra(Parameters.UserId.name)) throw Exception("User Id is undefined")
        val loggedInUserId = intent.getLongExtra(Parameters.UserId.name, -1)
        val json = intent.getStringExtra(Parameters.HetzerConditions.name)!!
        val typeToken = object : TypeToken<HashMap<Int, Any>>() {}.type
        val conditions = Gson().fromJson(json, typeToken) as HashMap<Int, Any>
        val hetzer = Hetzer(conditions)

        runOnManagedThread {
            twitterAdapter.getTweets(object : TwitterAdapter.FetchListInterface {
                override fun onStart() {}

                override fun onFinished(list: ArrayList<*>) {
                    val tweets = list as ArrayList<Status>
                    val context = this@HetzerService
                    sendNotification(strServiceName, getString(R.string.TweetChecking))
                    val passedStatuses = ArrayList<Status>() // 삭제되지 않은 트윗
                    val targetStatus = ArrayList<Status>() // 삭제된 트윗
                    val tweetCount = tweets.count()
                    if (tweets.isNotEmpty()) {
                        for (i in 0 until tweetCount) {
                            val item = tweets[i]
                            if (hetzer.filter(item, i)) {
                                passedStatuses.add(item)
                            } else {
                                targetStatus.add(item)
                            }
                        }
                    }

                    runOnManagedThread {
                        twitterAdapter.destroyStatus(targetStatus, object : TwitterAdapter.IterableInterface {
                            override fun onStart() {}

                            override fun onFinished() {
                                // 리포트 작성
                                val ri = ReportInterface<HetzerReport>(loggedInUserId, HetzerReport.prefix)
                                val report = HetzerReport(targetStatus, passedStatuses)
                                report.id = ri.getReportCount(context) + 1
                                ri.writeReport(context, report.id, report)

                                // 알림 송출
                                val redirect = Intent(context, HetzerReportActivity::class.java)
                                redirect.putExtra(HetzerReportActivity.Parameters.ReportId.name, report.id)
                                sendNotification(
                                    strServiceName,
                                    getString(R.string.Hetzer_Done),
                                    silent = false,
                                    cancelable = true,
                                    redirect = redirect,
                                    id = NotificationId + 1
                                )
                                context.sendActivityRefrashNotification(HetzerActivity::class.java.name)

                                // 서비스 종료
                                context.stopForeground(true)
                                context.stopSelf()
                            }

                            override fun onIterate(listIndex: Int) {
                                restrainedNotification(strServiceName, getString(R.string.TweetRemoving, listIndex + 1, targetStatus.count()))
                            }

                            override fun onRateLimit(listIndex: Int) {
                                sendNotification(
                                    "$strServiceName $strRateLimitWaiting",
                                    getString(R.string.TweetRemoving, listIndex + 1, targetStatus.count())
                                )
                            }

                        })
                    }
                }

                override fun onFetch(listSize: Int) {
                    restrainedNotification(strServiceName, getString(R.string.TweetPulling, listSize))
                }

                override fun onRateLimit(listSize: Int) {
                    sendNotification(
                        "$strServiceName $strRateLimitWaiting",
                        getString(R.string.TweetPulling, listSize)
                    )
                }

            })
        }
    }


}