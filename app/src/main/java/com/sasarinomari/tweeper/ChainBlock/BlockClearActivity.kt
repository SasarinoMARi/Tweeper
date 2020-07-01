package com.sasarinomari.tweeper.ChainBlock

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import cn.pedant.SweetAlert.SweetAlertDialog
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SharedTwitterProperties
import kotlinx.android.synthetic.main.activity_block_clear.*
import twitter4j.TwitterException

class BlockClearActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_clear)

        button_ok.setOnClickListener {
            da.warning(getString(R.string.AreYouSure), getString(R.string.ActionDoNotRestore))
                .setConfirmText(getString(R.string.Yes))
                .setCancelText(getString(R.string.Wait))
                .setConfirmClickListener {
                    it.dismissWithAnimation()
                    val intent = Intent(this, BlockClearService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                    } else {
                        startService(intent)
                    }
                    da.success(getString(R.string.Done), getString(R.string.BlockClearRunning))
                        .setConfirmClickListener { it2 ->
                            it2.dismissWithAnimation()
                            finish()
                        }.show()
                }.show()
        }
    }
}
