package com.sasarinomari.tweetcleaner.hetzer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.sasarinomari.tweetcleaner.Adam
import com.sasarinomari.tweetcleaner.R
import twitter4j.Paging
import twitter4j.Status
import twitter4j.TwitterException
import twitter4j.TwitterFactory


class HetzerActivity : Adam(), HetzerInterface {
    private lateinit var conditions: HetzerConditions

    enum class RequestCodes {
        GetConditions
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivityForResult(
            Intent(this, HetzerConditionsActivity::class.java),
            RequestCodes.GetConditions.ordinal
        )
    }

    private fun init() {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            .setTitleText(getString(R.string.Hetzer_TweetPulling))

        pDialog.progressHelper.barColor =
            ContextCompat.getColor(this@HetzerActivity, R.color.colorAccent)
        pDialog.setCancelable(false)
        pDialog.show()

        Thread(Runnable {
            getTweet {
                runOnUiThread {
                    pDialog.setTitle(getString(R.string.Hetzer_TweetRemoving))
                }
                for (i in 0 until it.count()) {
                    val item = it[i]
                    val text = if (item.text.length > 15) "${item.text.substring(0, 15)}.." else item.text
                    log(text)
                    runOnUiThread {
                        pDialog.contentText = text
                    }
                    when {
                        excludeMyFavorite(item) -> {
                            log("[트윗 제외됨] : 본인이 마음에 들어한 트윗")
                        }
                        excludeRetweetCount(item) -> {
                            log("[트윗 제외됨] : 많은 리트윗을 받은 트윗")
                        }
                        excludeFavoriteCount(item) -> {
                            log("[트윗 제외됨] : 많은 사람이 마음에 들어한 트윗")
                        }
                        excludeMinimumCount(item, i) -> {
                            log("[트윗 제외됨] : 너무 최근에 한 트윗(개수)")
                        }
                        excludeMinimumTick(item) -> {
                            log("[트윗 제외됨] : 너무 최근에 한 트윗(시간)")
                        }
                        else -> {
                            log("[트윗 삭제됨]")
                            // TODO : 이미 트윗이 지워진 경우 등 예외상황에 잘 동작하는지 확인할 필요 있음
                            TwitterFactory.getSingleton().destroyStatus(item.id)
                        }
                    }
                }
                runOnUiThread {
                    pDialog.dismiss()

                    val dDialog = SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText(getString(R.string.Done))
                        .setContentText(getString(R.string.Hetzer_Done))
                        .setConfirmClickListener {
                            setResult(RESULT_OK)
                            finish()
                        }
                    dDialog.setCancelable(false)
                    dDialog.show()
                }
            }
        }).start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RequestCodes.GetConditions.ordinal -> {
                if (resultCode != RESULT_OK) {
                    finish()
                    return
                }

                conditions = data!!.getParcelableExtra(
                    HetzerConditionsActivity.Results.Conditions.name
                )

                init()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun excludeMyFavorite(status: Status): Boolean {
        return conditions.avoidMyFav && status.isFavorited

    }

    override fun excludeRetweetCount(status: Status): Boolean {
        return when {
            status.isRetweet -> false
            conditions.avoidRetweetCount == 0 -> false
            else -> status.retweetCount >= conditions.avoidRetweetCount
        }
    }

    override fun excludeFavoriteCount(status: Status): Boolean {
        return when {
            status.isRetweet -> false
            conditions.avoidFavCount == 0 -> false
            else -> status.favoriteCount >= conditions.avoidFavCount
        }
    }

    override fun excludeMinimumCount(status: Status, index: Int): Boolean {
        return when {
            conditions.avoidRecentCount == 0 -> false
            else -> index < conditions.avoidRecentCount
        }
    }

    override fun excludeMinimumTick(status: Status): Boolean {
        val divider = 1000 / 60
        val tweetMinuteStamp = status.createdAt.time / divider + conditions.avoidRecentMinute
        val safeMinuteStamp = System.currentTimeMillis() / divider
        return when {
            conditions.avoidRecentMinute == 0 -> false
            else -> tweetMinuteStamp >= safeMinuteStamp
        }
    }

    private fun getTweet(callback: (List<Status>) -> Unit) {
        try {
            // gets Twitter instance with default credentials
            val twitter = TwitterFactory.getSingleton()
            val list = ArrayList<Status>()
            for (i in 1..Int.MAX_VALUE) {
                val paging = Paging(i, 20)
                val statuses = twitter.getUserTimeline(paging)
                list.addAll(statuses)
                if (statuses.size == 0) break
            }
            callback(list)
        } catch (te: TwitterException) {
            // TODO :리밋이 어떻게 처리되는지 확인할 필요 있음
            te.printStackTrace()
            println("Failed to get timeline: " + te.message)
        }
    }

    private fun log(msg: String) {
        Log.v("Hetzer", msg)
    }
}
