package com.sasarinomari.tweeper.ChainBlock

import android.os.Bundle
import android.util.Log
import cn.pedant.SweetAlert.SweetAlertDialog
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SharedTwitterProperties
import kotlinx.android.synthetic.main.activity_block_clear.*
import twitter4j.TwitterException

class BlockClearActivity : BaseActivity(), SharedTwitterProperties.ActivityInterface {
    private lateinit var progress: SweetAlertDialog

    override fun onRateLimit(apiPoint: String) {
        runOnUiThread {
            da.error(getString(R.string.Error), getString(R.string.RateLimitError, apiPoint)).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_clear)

        button_ok.setOnClickListener {
            da.warning(getString(R.string.AreYouSure), getString(R.string.ActionDoNotRestore))
                .setConfirmText(getString(R.string.Yes))
                .setConfirmClickListener {
                    it.dismissWithAnimation()
                    runOnUiThread { fetchBlockedUsers() }
                }.show()
        }
    }

    private fun fetchBlockedUsers() {
        progress = da.progress(null, getString(R.string.FetchBlockedUsers))
        progress.show()

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
                    progress.contentText = getString(R.string.ClearBlockedUsers, u + 1, list.count())
                }
                twitter.destroyBlock(list[u])
            }
            runOnUiThread {
                progress.dismissWithAnimation()
                finished()
            }
        }).start()

    }

    private fun finished() {
        da.success(getString(R.string.Done), getString(R.string.BlockCleared)) { finish() }.show()
    }
}
