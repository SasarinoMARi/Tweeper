package com.sasarinomari.tweetcleaner.chainb

import android.os.Bundle
import cn.pedant.SweetAlert.SweetAlertDialog
import com.sasarinomari.tweetcleaner.Adam
import com.sasarinomari.tweetcleaner.R
import com.sasarinomari.tweetcleaner.SharedTwitterProperties
import kotlinx.android.synthetic.main.activity_block_clear.*
import twitter4j.TwitterException

class BlockClearActivity : Adam(), SharedTwitterProperties.ActivityInterface {
    private var dp: SweetAlertDialog? = null

    override fun onRateLimit(apiPoint: String) {
        runOnUiThread {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(R.string.Error))
                .setContentText(getString(R.string.RateLimitError, apiPoint))
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_clear)

        button_ok.setOnClickListener {
            val d = SweetAlertDialog(this@BlockClearActivity, SweetAlertDialog.WARNING_TYPE)
                .setTitleText((R.string.AreYouSure))
                .setContentText(getString(R.string.ActionDoNotRestore))
                .setConfirmText(getString(R.string.Yes))

            d.setConfirmClickListener {
                runOnUiThread {
                    d.dismissWithAnimation()
                    fetchBlockedUsers()
                }
            }

            d.show()
        }
    }

    private fun fetchBlockedUsers() {
        dp = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            .setContentText(getString(R.string.FetchBlockedUsers))
        dp?.setCancelable(false)
        dp?.show()

        Thread(Runnable {
            val list = ArrayList<Long>()
            var cursor: Long = -1
            val twitter = SharedTwitterProperties.instance()
            try {
                while (true) {
                    val result = twitter.getBlocksIDs(cursor)
                    list.addAll(result.iDs.toList())
                    if (result.hasNext()) cursor = result.nextCursor
                    else break
                }
                runOnUiThread {
                    unblockUsers(list)
                }
            } catch (te: TwitterException) {
                te.printStackTrace()
                onRateLimit("get/friends")
            }

        }).start()
    }

    private fun unblockUsers(list: java.util.ArrayList<Long>) {
        Thread(Runnable {
            val twitter = SharedTwitterProperties.instance()
            for(u in 0 until list.count()) {
                runOnUiThread {
                    dp?.contentText = getString(R.string.ClearBlockedUsers, u + 1, list.count())
                }
                twitter.destroyBlock(list[u])
            }
            runOnUiThread {
                dp?.dismissWithAnimation()
                finished()
            }
        }).start()

    }

    private fun finished() {
        dp?.dismissWithAnimation()

        val d = SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText(getString(R.string.Done))
            .setContentText(getString(R.string.BlockCleared))
        d.setOnDismissListener {
            finish()
        }
        d.show()
    }
}
