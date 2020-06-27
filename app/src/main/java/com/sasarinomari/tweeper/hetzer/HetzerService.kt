package com.sasarinomari.tweeper.hetzer

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sasarinomari.tweeper.BaseService
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SharedTwitterProperties
import com.sasarinomari.tweeper.report.ReportInterface
import twitter4j.Paging
import twitter4j.Status
import twitter4j.TwitterException

class HetzerService : BaseService() {
    companion object {
        fun checkServiceRunning(context: Context) = BaseService.checkServiceRunning(context)
    }
    override val ChannelName= "Hetzer"
    override val NotificationId = ChannelName.hashCode()

    enum class Parameters {
        HetzerConditions
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        startForeground(NotificationId,
            createNotification(getString(R.string.app_name), "Initializing...", false))

        hetzerLogic(intent!!)

        return START_REDELIVER_INTENT
    }

    private fun hetzerLogic(intent: Intent) {
        val json = intent.getStringExtra(Parameters.HetzerConditions.name)
        val typeToken = object : TypeToken<HashMap<Int, Any>>() {}.type
        val conditions = Gson().fromJson(json, typeToken) as HashMap<Int, Any>
        val hetzer = Hetzer(conditions)

        getTweets { tweets ->
            sendNotification(getString(R.string.Hetzer_TweetRemovingTitle), "")
            val savedStatuses = ArrayList<Status>() // 삭제되지 않은 트윗
            val removedStatuses = ArrayList<Status>() // 삭제된 트윗
            if (tweets.isNotEmpty()) {
                for (i in 0 until tweets.count()) {
                    val item = tweets[i]
                    if (hetzer.filter(item, i)) {
                        savedStatuses.add(item)
                    }
                    else {
                        removedStatuses.add(item)
                        // TODO : 이미 트윗이 지워진 경우 등 예외상황에 잘 동작하는지 확인할 필요 있음
                        // SharedTwitterProperties.instance().destroyStatus(item.id)
                    }
                }
            }

            // 리포트 작성
            val ri = ReportInterface<HetzerReport>(HetzerReport.prefix)
            val reportIndex = ri.getReportCount(this)+1
            ri.writeReport(this, reportIndex, HetzerReport(removedStatuses, savedStatuses))

            // 알림 송출
            val redirect = Intent(this, HetzerReportActivity::class.java)
            redirect.putExtra(HetzerReportActivity.Parameters.ReportId.name, reportIndex)
            sendNotification(
                getString(R.string.Done),
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

    // API 코드
    private fun getTweets(startIndex: Int, callback: (List<Status>) -> Unit) {
        Thread(Runnable {
            val list = ArrayList<Status>()
            var lastIndex = startIndex
            try {
                // gets Twitter instance with default credentials
                val twitter = SharedTwitterProperties.instance()
                for (i in startIndex..Int.MAX_VALUE) {
                    sendNotification( // TODO: API 리밋 후? 또는 액티비티 종료 후 여기서 오류
                        getString(R.string.Hetzer_TweetPullingTitle),
                        getString(R.string.Hetzer_TweetPullingContent, list.count())
                    )
                    val paging = Paging(i, 20)
                    val statuses = twitter.getUserTimeline(paging)
                    list.addAll(statuses)
                    lastIndex = i
                    if (statuses.size == 0) break
                }
                callback(list)
            } catch (te: TwitterException) {
                sendNotification(getString(R.string.Hetzer_WaitingTitle), getString(R.string.WaitingDesc))
                Log.i(HetzerService::class.java.name, "API 한도에 도달했습니다. 5분 뒤 다시 시도합니다.")
                Log.i(HetzerService::class.java.name, "lastIndex:$lastIndex")
                Thread.sleep(1000 * 60 * 5)
                getTweets(lastIndex, callback) // TODO: Catch 된 커서가 제대로 마지막 커서 이후인지 확인해봐야함
                te.printStackTrace()
            }
        }).start()
    }

    private fun getTweets(callback: (List<Status>) -> Unit) {
        getTweets(1, callback)
    }
    // endregion

}