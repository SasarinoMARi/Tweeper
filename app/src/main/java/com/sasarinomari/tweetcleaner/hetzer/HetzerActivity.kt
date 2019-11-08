package com.sasarinomari.tweetcleaner.hetzer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(Intent(this, HetzerConditionsActivity::class.java))

        setContentView(R.layout.activity_hetzer)

        text_delete.setOnClickListener {
            layout_button.visibility = View.GONE
            Thread(Runnable {
                getTweet {
                    for (item in it) {
                        log("[처리 시작] : ${if (item.text.length >15) "${item.text.substring(0, 15)}.." else item.text}")
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
                            excludeMinimunCount(item) -> {
                                log("[트윗 제외됨] : 너무 최근에 한 트윗(개수)")
                            }
                            excludeMinimunTick(item) -> {
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

    override fun excludeMyFavorite(status: Status): Boolean {
        return status.isFavorited
    }

    override fun excludeRetweetCount(status: Status): Boolean {
        return when {
            status.isRetweet -> false
            else -> status.retweetCount >= 1
        }
    }

    override fun excludeFavoriteCount(status: Status): Boolean {
        return when {
            status.isRetweet -> false
            else -> status.favoriteCount >= 1
        }
    }

    override fun excludeMinimunCount(status: Status): Boolean {
        return false
    }

    override fun excludeMinimunTick(status: Status): Boolean {
        return false
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
        runOnUiThread { text_output.text.insert(0, "$text\n") }
    }
}
