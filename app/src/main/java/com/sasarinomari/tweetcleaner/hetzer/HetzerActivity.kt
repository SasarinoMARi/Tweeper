package com.sasarinomari.tweetcleaner.hetzer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.sasarinomari.tweetcleaner.Adam
import com.sasarinomari.tweetcleaner.R
import com.sasarinomari.tweetcleaner.SharedTwitterProperties
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
            ContextCompat.getColor(this@HetzerActivity, R.color.colorSecondary)
        pDialog.setCancelable(false)
        pDialog.show()

        Thread(Runnable {
            getTweet {
                if(it.isNotEmpty())
                runOnUiThread {
                    pDialog.setTitle(getString(R.string.Hetzer_TweetRemoving))
                }
                for (i in 0 until it.count()) {
                    val item = it[i]
                    val text = item.text
                    val cut = if (text.length > 25) "${text.substring(0,23)}.." else text
                    log(text)
                    when {
                        excludeMyFavorite(item) -> {
                            log("[제외] 내가 마음에 들어한 트윗\n$cut")
                        }
                        excludeRetweetCount(item) -> {
                            log("[제외] 많은 리트윗을 받은 트윗\n$cut")
                        }
                        excludeFavoriteCount(item) -> {
                            log("[제외] 많은 사람이 마음에 들어한 트윗\n$cut")
                        }
                        excludeMinimumCount(item, i) -> {
                            log("[제외] 최근에 한 트윗(개수)\n$cut")
                        }
                        excludeMinimumTick(item) -> {
                            log("[제외] 너무 최근에 한 트윗(시간)\n$cut")
                        }
                        excludeMedia(item) -> {
                            log("[제외됨] 미디어를 포함한 트윗\n$cut")
                        }
                        excludeKeyword(item) -> {
                            log("[제외됨] 키워드를 포함한 트윗\n$cut")
                        }
                        excludeNoMedia(item) -> {
                            log("[제외됨] 미디어를 포함하지 않은 트윗\n$cut")
                        }
                        excludeNoGeo(item) -> {
                            log("[제외됨] 위치 정보를 포함하지 않은 트윗\n$cut")
                        }
                        else -> {
                            runOnUiThread {
                                pDialog.contentText = text
                            }
                            log("[트윗 삭제됨]")
                            // TODO : 이미 트윗이 지워진 경우 등 예외상황에 잘 동작하는지 확인할 필요 있음
                            SharedTwitterProperties.instance().destroyStatus(item.id)
                        }
                    }
                }
                runOnUiThread {
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
        if (!conditions.avoidMyFav) return false
        if (status.isRetweet) return false
        return status.isFavorited
    }

    override fun excludeRetweetCount(status: Status): Boolean {
        if (conditions.avoidRetweetCount == 0) return false
        if (status.isRetweet) return false
        return status.retweetCount >= conditions.avoidRetweetCount
    }

    override fun excludeFavoriteCount(status: Status): Boolean {
        if (conditions.avoidFavCount == 0) return false
        if (status.isRetweet) return false
        return status.favoriteCount >= conditions.avoidFavCount
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

    override fun excludeMedia(status: Status): Boolean {
        if (!conditions.avoidMedia) return false
        if (status.isRetweet) return false
        return status.mediaEntities.isNotEmpty()
    }

    override fun excludeNoMedia(status: Status): Boolean {
        if (!conditions.avoidNoMedia) return false
        if (status.isRetweet) return false
        return status.mediaEntities.isEmpty()
    }

    override fun excludeNoGeo(status: Status): Boolean {
        if (!conditions.avoidNoGeo) return false
        if (status.isRetweet) return false
        return status.geoLocation != null
    }

    override fun excludeKeyword(status: Status): Boolean {
        for (item in conditions.avoidKeywords) {
            if (status.text.contains(item)) return true
        }
        return false
    }

    private fun getTweet(callback: (List<Status>) -> Unit) {
        val list = ArrayList<Status>()
        try {
            // gets Twitter instance with default credentials
            val twitter = SharedTwitterProperties.instance()
            for (i in 1..Int.MAX_VALUE) {
                val paging = Paging(i, 20)
                val statuses = twitter.getUserTimeline(paging)
                list.addAll(statuses)
                if (statuses.size == 0) break
            }
            callback(list)
        } catch (te: TwitterException) {
            te.printStackTrace()
            runOnUiThread {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(R.string.Error))
                .setContentText(getString(R.string.RateLimitError))
                .setConfirmClickListener {
                    callback(list)
                }
                .show()
            }
        }
    }

    private fun log(msg: String) {
        Log.v("Hetzer", msg)
    }
}
