package com.sasarinomari.tweeper.hetzer

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.sasarinomari.tweeper.Adam
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SharedTwitterProperties
import twitter4j.Paging
import twitter4j.Status
import twitter4j.TwitterException

class HetzerActivity : Adam() {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RequestCodes.GetConditions.ordinal -> {
                if (resultCode != RESULT_OK) {
                    finish()
                    return
                }

                val conditions = data!!.getParcelableExtra(
                    HetzerConditionsActivity.Results.Conditions.name
                ) as HetzerConditions
                val intent = Intent(this, HetzerService::class.java)
                intent.putExtra(HetzerConditionsActivity.Results.Conditions.name, conditions)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                }
                else {
                    startService(intent)
                }

                val d = SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                    .setContentText("백그라운드에서 트윗 청소기가 실행됩니다..")
                d.setOnDismissListener {
                    finish()
                }
                d.show()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
