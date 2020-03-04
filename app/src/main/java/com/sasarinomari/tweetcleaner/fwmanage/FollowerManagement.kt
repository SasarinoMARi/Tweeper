package com.sasarinomari.tweetcleaner.fwmanage

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.sasarinomari.tweetcleaner.Adam
import com.sasarinomari.tweetcleaner.R
import com.sasarinomari.tweetcleaner.SharedTwitterProperties
import kotlinx.android.synthetic.main.activity_follower_management.*
import twitter4j.TwitterException
import twitter4j.User

class FollowerManagement : Adam(), SharedTwitterProperties.ActivityInterface {
    private lateinit var pDialog: SweetAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follower_management)

        pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            .setTitleText(getString(R.string.FriendPulling))

        pDialog.progressHelper.barColor = ContextCompat.getColor(this, R.color.colorSecondary)
        pDialog.setCancelable(false)
        pDialog.show()

        SharedTwitterProperties.getFriends(this) { fs ->
            runOnUiThread {
                pDialog.titleText = getString(R.string.FollowerPulling)
            }
            SharedTwitterProperties.getFollowers(this) { fw ->
                runOnUiThread {
                    pDialog.titleText = getString(R.string.CompareFsFw)
                }
                val uf = comLists(fs, fw)
                runOnUiThread {
                    pDialog.dismissWithAnimation()
                    initializeUi(uf)
                }
            }
        }
    }

    private fun initializeUi(uf: ArrayList<User>) {
        if (uf.isEmpty()) {
            list.visibility = View.GONE
            layout_noApplicable.visibility = View.VISIBLE
            return
        }
        list.adapter = UserUnfollowItem(uf, object : UserUnfollowItem.ActivityInterface {
            override fun onClickUnfollow(userId: Long, doneCallback: Runnable) {
                val d1 = SweetAlertDialog(this@FollowerManagement, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText((R.string.AreYouSure))
                    .setContentText(getString(R.string.ActionDoNotRestore))
                    .setConfirmText(getString(R.string.Yes))

                d1.setConfirmClickListener {
                    d1.dismissWithAnimation()
                    Thread(Runnable {
                        val twitter = SharedTwitterProperties.instance()
                        twitter.destroyFriendship(userId)
                        runOnUiThread {
                            val d2 = SweetAlertDialog(
                                this@FollowerManagement,
                                SweetAlertDialog.SUCCESS_TYPE
                            )
                                .setTitleText(getString(R.string.Done))
                                .setContentText(getString(R.string.JobDone))
                            d2.setConfirmClickListener {
                                d2.dismissWithAnimation()
                                doneCallback.run()
                            }
                            d2.show()
                        }
                    }).start()
                }
                d1.show()
            }

            override fun onclickDetail(screenName: String) {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=$screenName")))
                } catch (e: Exception) {
                    startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://twitter.com/#!/$screenName")))
                }
            }

        })
    }

    private fun comLists(fs: List<User>, fw: List<User>): ArrayList<User> {
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

    override fun onRateLimit(apiPoint: String) {
        runOnUiThread {
            pDialog.dismissWithAnimation()
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(R.string.Error))
                .setContentText(getString(R.string.RateLimitError, apiPoint))
                .setConfirmClickListener {
                    finish()
                }
                .show()
        }
    }
}
