package com.sasarinomari.tweetcleaner.hetzer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.sasarinomari.tweetcleaner.Adam
import com.sasarinomari.tweetcleaner.R
import kotlinx.android.synthetic.main.activity_hetzer.*
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
        setContentView(R.layout.activity_hetzer)
        text_delete.setOnClickListener {
            layout_button.visibility = View.GONE
            Thread(Runnable {
                getTweet {
                    for (i in 0..it.count()) {
                        val item = it[i]
                        log(
                            "[처리 시작] : ${if (item.text.length > 15) "${item.text.substring(
                                0,
                                15
                            )}.." else item.text}"
                        )
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
                                TwitterFactory.getSingleton().destroyStatus(item.id)
                            }
                        }
                    }
                    runOnUiThread {
                        Toast.makeText(this@HetzerActivity, "트윗 삭제 작업 완료", Toast.LENGTH_LONG).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            }).start()
        }
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
            te.printStackTrace()
            println("Failed to get timeline: " + te.message)
        }
    }

    private fun log(text: String) {
        runOnUiThread { text_output.setText(text_output.text.toString() + "$text\n") }
    }
}
