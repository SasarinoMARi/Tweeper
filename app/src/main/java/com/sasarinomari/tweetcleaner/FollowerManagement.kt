package com.sasarinomari.tweetcleaner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.User

class FollowerManagement : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follower_management)


        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            .setTitleText(getString(R.string.FriendPulling))

        pDialog.progressHelper.barColor =
            ContextCompat.getColor(this, R.color.colorAccent)
        pDialog.setCancelable(false)
        pDialog.show()

        try {
            SharedUserProperties.getFriends { fs ->
                runOnUiThread {
                    pDialog.titleText = getString(R.string.FollowerPulling)
                }
                SharedUserProperties.getFollowers { fw ->
                    runOnUiThread {
                        pDialog.titleText = getString(R.string.CompareFsFw)
                    }
                    val uf = comLists(fs, fw)
                    for (u in uf) {
                        Log.i("Ufu", u.screenName)
                    }
                    pDialog.dismiss()
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

    private fun comLists(fs: List<User>, fw: List<User>): List<User> {
        val res = ArrayList<User>()
        for (u1 in fs) {
            var correspond = false
            for (u2 in fw) {
                if (u1.id == u2.id) {
                    correspond = true
                    break
                }
            }
            if (!correspond) {
                res.add(u1)
            }
        }
        return res
    }
}
