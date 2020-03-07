package com.sasarinomari.tweeper.danger

import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.sasarinomari.tweeper.Adam
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SharedTwitterProperties
import twitter4j.TwitterException
import twitter4j.TwitterFactory

/*
class RemoveFriends : Adam() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(getString(R.string.AreYouSure))
            .setContentText("이 작업은 돌이킬 수 없습니다.")
            .setConfirmText("넹")
            .setConfirmClickListener {
                start()
            }
            .show()
    }

    private fun start() {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            .setTitleText(getString(R.string.FriendPulling))

        pDialog.progressHelper.barColor =
            ContextCompat.getColor(this, R.color.colorSecondary)
        pDialog.setCancelable(false)
        pDialog.show()

        try {
            val t = SharedTwitterProperties.instance()
            SharedTwitterProperties.getFriends { fs ->
                runOnUiThread {
                    pDialog.titleText = getString(R.string.FollowerPulling)
                }
                for (li in fs) {
                    t.destroyFriendship(li.id)
                    Log.i("RemoveFriends", "Unfollow @${li.screenName}")
                }

                SharedTwitterProperties.getFollowers { fw ->
                    runOnUiThread {
                        pDialog.titleText = getString(R.string.CompareFsFw)
                    }

                    for (li in fw) {
                        t.createBlock(li.id)
                        t.destroyBlock(li.id)
                        Log.i("RemoveFriends", "BnB @1${li.screenName}")
                    }

                    runOnUiThread {
                        val dDialog = SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText(getString(R.string.Done))
                            .setContentText("작업이 완료되었습니다.")
                            .setConfirmClickListener {
                                setResult(RESULT_OK)
                                finish()
                            }
                        dDialog.setCancelable(false)
                        dDialog.show()
                    }
                }
            }
        } catch (te: TwitterException) {
            te.printStackTrace()
            runOnUiThread {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getString(R.string.Error))
                    .setContentText("Rate Limit Exceeded")
                    .show()
            }
        }

    }
}
        */