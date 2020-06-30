package com.sasarinomari.tweeper.Hetzer

import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sasarinomari.tweeper.Analytics.AnalyticsService
import com.sasarinomari.tweeper.Base.BaseService
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SharedTwitterProperties
import com.sasarinomari.tweeper.Report.ReportInterface
import com.sasarinomari.tweeper.TwitterExceptionHandler
import twitter4j.Paging
import twitter4j.Status
import twitter4j.TwitterException
import java.lang.Exception

class HetzerService : BaseService() {
    companion object {
        fun checkServiceRunning(context: Context) = BaseService.checkServiceRunning(context)
    }

    enum class Parameters {
        HetzerConditions, UserId
    }

    lateinit var strServiceName: String
    lateinit var strRateLimitWaiting: String

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent!!, flags, startId)
        strServiceName = getString(R.string.TweetCleaner)
        strRateLimitWaiting = getString(R.string.RateLimitWaiting)

        startForeground(
            NotificationId,
            createNotification(getString(R.string.app_name), "Initializing...", false)
        )

        hetzerLogic(intent)

        return START_REDELIVER_INTENT
    }

    private fun hetzerLogic(intent: Intent) {
        if(!intent.hasExtra(Parameters.UserId.name)) throw Exception("User Id is undefined")
        val loggedInUserId = intent.getLongExtra(Parameters.UserId.name, -1)
        val json = intent.getStringExtra(Parameters.HetzerConditions.name)!!
        val typeToken = object : TypeToken<HashMap<Int, Any>>() {}.type
        val conditions = Gson().fromJson(json, typeToken) as HashMap<Int, Any>
        val hetzer = Hetzer(conditions)

        getTweets { tweets ->
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

            destroyStatus(targetStatus) {
                // 리포트 작성
                val ri = ReportInterface<HetzerReport>(loggedInUserId, HetzerReport.prefix)
                val report = HetzerReport(targetStatus, passedStatuses)
                report.id = ri.getReportCount(this) + 1
                ri.writeReport(this, report.id, report)

                // 알림 송출
                val redirect = Intent(this, HetzerReportActivity::class.java)
                redirect.putExtra(HetzerReportActivity.Parameters.ReportId.name, report.id)
                sendNotification(
                    strServiceName,
                    getString(R.string.Hetzer_Done),
                    silent = false,
                    cancelable = true,
                    redirect = redirect,
                    id = NotificationId + 1
                )

                // 서비스 종료
                this.stopForeground(true)
                this.stopSelf()
            }
        }
    }

    private fun destroyStatus(startIndex: Int, statuses: ArrayList<Status>, callback: () -> Unit) {
        // TODO : 이미 트윗이 지워진 경우 등 예외상황에 잘 동작하는지 확인할 필요 있음
        Thread(Runnable {
            val twitter = SharedTwitterProperties.instance()
            var cursor = 0
            try {
                val statusCount = statuses.count()
                for (i in startIndex until statusCount) {
                    cursor = i
                    val status = statuses[i]
                    restrainedNotification(strServiceName, getString(R.string.TweetRemoving, cursor + 1, statusCount))
                    twitter.destroyStatus(status.id)
                }
                callback()
            } catch (te: TwitterException) {
                object : TwitterExceptionHandler(te, "destroyStatus") {
                    override fun onRateLimitExceeded() {
                        sendNotification(
                            "$strServiceName $strRateLimitWaiting",
                            getString(R.string.TweetRemoving, cursor + 1, statuses.count())
                        )
                    }

                    override fun onRateLimitReset() {
                        destroyStatus(cursor, statuses, callback)
                    }
                }.catch()
            }
        }).start()
    }

    private fun destroyStatus(statuses: ArrayList<Status>, callback: () -> Unit) {
        destroyStatus(0, statuses, callback)
    }

    // API 코드
    private fun getTweets(startIndex: Int, callback: (List<Status>) -> Unit) {
        Thread(Runnable {
            val list = ArrayList<Status>()
            var lastIndex = startIndex
            try {
                val twitter = SharedTwitterProperties.instance()
                for (i in startIndex..Int.MAX_VALUE) {
                    restrainedNotification(strServiceName, getString(R.string.TweetPulling, list.count()))
                    val paging = Paging(i, 20)
                    val statuses = twitter.getUserTimeline(paging)
                    list.addAll(statuses)
                    lastIndex = i
                    if (statuses.size == 0) break
                }
                callback(list)
            } catch (te: TwitterException) {
                object : TwitterExceptionHandler(te, "getUserTimeline") {
                    override fun onRateLimitExceeded() {
                        sendNotification(
                            "$strServiceName $strRateLimitWaiting",
                            getString(R.string.TweetPulling, list.count())
                        )
                    }

                    override fun onRateLimitReset() {
                        getTweets(lastIndex, callback)
                    }
                }.catch()
            }
        }).start()
    }

    private fun getTweets(callback: (List<Status>) -> Unit) {
        getTweets(1, callback)
    }
    // endregion

}