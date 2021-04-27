package com.sasarinomari.tweeper.Hetzer

import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.Base.BaseService
import com.sasarinomari.tweeper.Billing.AdRemover
import com.sasarinomari.tweeper.BuildConfig
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.Report.ReportInterface
import com.sasarinomari.tweeper.TwitterAdapter
import twitter4j.Status
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class HetzerService : BaseService() {
    private val strServiceName: String by lazy { getString(R.string.TweetCleaner) }
    private val strRateLimitWaiting: String by lazy { getString(R.string.RateLimitWaiting) }

    private val twitterAdapter = TwitterAdapter()
    private lateinit var hetzer: Hetzer

    companion object {
        /**
         * 트윗 청소기 서비스가 이미 실행중인지 확인
         */
        fun checkServiceRunning(context: Context) = BaseService.checkServiceRunning(context, HetzerService::class.java.name)
    }

    enum class Parameters {
        User, Logics
    }

    private val statuses_removed = ArrayList<Status>()      // 삭제된 트윗
    private val statuses_passed = ArrayList<Status>()       // 삭제되지 않은 트윗

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (super.onStartCommand(intent!!, flags, startId) == START_NOT_STICKY) return START_NOT_STICKY

        /**
         * 트위터 어댑터 초기화
         */
        val j = intent.getStringExtra(Parameters.User.name)!!
        val user = Gson().fromJson(j, AuthData::class.java)!!
        twitterAdapter.twitter.initialize(this, user.token!!)

        /**
         * 포그라운드 서비스 붙이기
         */
        startForeground(NotificationId,
            createNotification(getString(R.string.app_name), "Initializing...", false))

        /**
         * 트윗청소기 필터 생성
         */
        val json = intent.getStringExtra(Parameters.Logics.name)!!
        val typeToken = object : TypeToken<ArrayList<LogicPair>>() {}.type
        val conditions = Gson().fromJson(json, typeToken) as ArrayList<LogicPair>
        hetzer = Hetzer(conditions)

        /**
         * 트윗 청소 시작
         */
        getTweets()

        return START_REDELIVER_INTENT
    }

    private fun getTweets(maxId: Long = 0) {
        /**
         * 0번째 페이지만 계속 찾되
         * 가장 최근에 pass 된 트윗 id를 maxId 파라미터로 제공하여
         * 거기서부터 트윗을 불러올 수 있도록 한다.
          */
        runOnManagedThread {
            twitterAdapter.getTweets(object : TwitterAdapter.FetchListStepInterface {
                override fun onStart() {}

                /**
                 * 최근 트윗을 한 뭉텅이 가져왔을 때
                 */
                override fun onFetch(list: List<*>) {
                    restrainedNotification(strServiceName,
                        getString(R.string.TweetPulling, statuses_removed.size))

                    onFetchTweet(hetzer, list)
                }

                /**
                 * API 한도 초과시
                 */
                override fun onRateLimit() {
                    sendNotification(
                        "$strServiceName $strRateLimitWaiting",
                        getString(R.string.TweetPulling, statuses_removed.size)
                    )
                }

                /**
                 * 처리되지 않은 예외 발생시
                */
                override fun onUncaughtError() {
                    this@HetzerService.onUncaughtError(strServiceName)
                }

                /**
                 * 네트워크 오류 발생시
                 */
                override fun onNetworkError(retrySelf: () -> Unit) {
                    this@HetzerService.onNetworkError(strServiceName, { retrySelf() })
                }
            }, maxId)
        }
    }

    private fun onFetchTweet(hetzer: Hetzer, list: List<*>) {
        val tweets = list as List<Status>

        val targets = ArrayList<Status>()

        /**
         * 삭제할 트윗과 남겨둘 트윗 분류
         */
        if (tweets.isNotEmpty()) {
            for (i in 0 until tweets.count()) {
                val item = tweets[i]
                if (hetzer.filter(item, i)) {
                    targets.add(item)
                } else {
                    statuses_passed.add(item)
                }
            }
        }

        /**
         * 실제 트윗 삭제 API 호출
         */
        runOnManagedThread {
            twitterAdapter.destroyStatus(targets, object : TwitterAdapter.IterableInterface {
                override fun onStart() {}

                override fun onFinished() {
                    statuses_removed.addAll(targets)
                    /**
                     * 이번에 Fetch한 트윗 갯수로 이번 Fetch가 마지막인지 판단
                     * 20개 미만이면 마지막 페이지임.
                     */
                    if (BuildConfig.DEBUG && list.size >= 20) {
                        val maxId = list.last().id
                        getTweets(maxId - 1) // 해당 트윗을 포함하지 않도록 1을 뺀다
                    } else if (!BuildConfig.DEBUG && statuses_passed.size > 0) {
                        val maxId = statuses_passed.last().id
                        getTweets(maxId - 1) // 해당 트윗을 포함하지 않도록 1을 뺀다
                    }
                    else {
                        finishService()
                    }
                }

                override fun onIterate(listIndex: Int) {
                    restrainedNotification(strServiceName, getString(R.string.TweetRemoving, statuses_removed.count()))
                }

                override fun onRateLimit(listIndex: Int) {
                    sendNotification(
                        "$strServiceName $strRateLimitWaiting",
                        getString(R.string.TweetRemoving, statuses_removed.count())
                    )
                }

                override fun onUncaughtError() {
                    this@HetzerService.onUncaughtError(strServiceName)
                }

                override fun onNetworkError(retrySelf: () -> Unit) {
                    this@HetzerService.onNetworkError(strServiceName, { retrySelf() })
                }
            })
        }
    }


    /**
     * 트윗 청소가 완료되었을 때 호출되는 함수
     */
    private fun finishService() {
        val reportId = writeReport(statuses_removed, statuses_passed)

        /**
         * Local Notification 송출
         */
        val redirect = Intent(this, HetzerReportActivity::class.java)
        redirect.putExtra(HetzerReportActivity.Parameters.ReportId.name, reportId)
        sendNotification(
            strServiceName, getString(R.string.Hetzer_Done),
            silent = false, cancelable = true, redirect = redirect,
            id = NotificationId + 1
        )

        /**
         * 트윗 청소기 액티비티가 열려있다면 새로 고친다.
         */
        sendActivityRefrashNotification(HetzerActivity::class.java.name)

        /**
         * 과금 여부에 따라 추가 액션 후 동작 종료
         */
        if (!AdRemover(this).isAdRemoved()) publishAdvertiseTweet()
        else finish()
    }

    /**
     * 트윗 청소 완료 후 광고 트윗하는 코드
     */
    private fun publishAdvertiseTweet() {
        twitterAdapter.publish("${getString(R.string.HetzerDoneTweet)} ${getString(R.string.StoreUrl)}", object : TwitterAdapter.PostInterface {
            override fun onStart() {}
            override fun onFinished(obj: Any) {
                finish()
            }

            override fun onRateLimit() {
                finish()
            }

            override fun onUncaughtError() {
                this@HetzerService.onUncaughtError(strServiceName)
            }

            override fun onNetworkError(retrySelf: () -> Unit) {
                this@HetzerService.onNetworkError(strServiceName, { retrySelf() })
            }
        })
    }

    /**
     * 트윗 청소 보고서를 작성하는 함수
     */
    private fun writeReport(targetStatus: ArrayList<Status>, passedStatuses: ArrayList<Status>) : String {
        val ri = ReportInterface<HetzerReport>(twitterAdapter.twitter.id, HetzerReport.prefix)
        val report = HetzerReport(targetStatus, passedStatuses)
        report.id = ri.getReportCount(this) + 1
        report.date = Date()
        val fn = ri.writeReportWithDate(this, report.id, report)
        return fn
    }
}