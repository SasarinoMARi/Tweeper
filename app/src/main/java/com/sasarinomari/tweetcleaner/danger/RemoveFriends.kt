package com.sasarinomari.tweetcleaner.danger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.sasarinomari.tweetcleaner.Adam
import com.sasarinomari.tweetcleaner.R
import com.sasarinomari.tweetcleaner.SharedUserProperties
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.User

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
            ContextCompat.getColor(this, R.color.colorAccent)
        pDialog.setCancelable(false)
        pDialog.show()

        try {
            val t = TwitterFactory.getSingleton()
            SharedUserProperties.getFriends { fs ->
                runOnUiThread {
                    pDialog.titleText = getString(R.string.FollowerPulling)
                }
                for (li in fs) {
                    t.destroyFriendship(li.id)
                    Log.i("RemoveFriends", "Unfollow @${li.screenName}")
                }

                SharedUserProperties.getFollowers { fw ->
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