package com.sasarinomari.tweeper.chainb

import android.os.Bundle
import android.util.Log
import cn.pedant.SweetAlert.SweetAlertDialog
import com.sasarinomari.tweeper.Adam
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SharedTwitterProperties
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
                Log.i(this.localClassName, "불러온 유저 수 : ${list.count()}")
                runOnUiThread {
                    unblockUsers(list)
                }
            } catch (te: TwitterException) {
                te.printStackTrace()
                onRateLimit("get/friends")
            }

        }).start()
    }

    // 1,345 명까지 한 번에 처리하는것을 확인
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
