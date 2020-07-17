package com.sasarinomari.tweeper.ChainBlock

import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.google.gson.Gson
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.RewardedAdAdapter
import kotlinx.android.synthetic.main.activity_block_clear.*

class BlockClearActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_clear)

        button_ok.setOnClickListener {
            if (BlockClearService.checkServiceRunning((this@BlockClearActivity))) {
                da.warning(getString(R.string.Wait), getString(R.string.duplicateService_BlockClear)).show()
                return@setOnClickListener
            }

            da.warning(getString(R.string.AreYouSure), getString(R.string.ActionDoNotRestore))
                .setConfirmText(getString(R.string.Yes))
                .setCancelText(getString(R.string.Wait))
                .setConfirmClickListener {
                    it.dismissWithAnimation()
                    RewardedAdAdapter.show(this@BlockClearActivity, object: RewardedAdAdapter.RewardInterface {
                        override fun onFinished() {
                            val intent = Intent(this@BlockClearActivity, BlockClearService::class.java)
                            intent.putExtra(
                                BlockClearService.Parameters.User.name,
                                Gson().toJson(AuthData.Recorder(this@BlockClearActivity).getFocusedUser()!!))
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
                        }
                    })
                }.show()
        }
    }
}
