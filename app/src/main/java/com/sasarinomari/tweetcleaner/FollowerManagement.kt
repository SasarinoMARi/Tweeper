package com.sasarinomari.tweetcleaner

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.sasarinomari.tweetcleaner.adapter.UserUnfollowItem
import kotlinx.android.synthetic.main.activity_follower_management.*
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
                    runOnUiThread {
                        pDialog.dismiss()
                        initializeUi(uf)
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

    private fun initializeUi(uf: ArrayList<User>) {
        list.adapter = UserUnfollowItem(uf, object : UserUnfollowItem.ActivityInterface {
            override fun onClickUnfollow(userId: Long, doneCallback: Runnable) {
                val d1 = SweetAlertDialog(this@FollowerManagement, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText((R.string.AreYouSure))
                    .setContentText(getString(R.string.ActionDoNotRestore))
                    .setConfirmText(getString(R.string.Yes))

                d1.setConfirmClickListener {
                    d1.dismiss()
                    Thread(Runnable {
                        val twitter = TwitterFactory.getSingleton()
                        twitter.destroyFriendship(userId)
                        runOnUiThread {
                            val d2 =
                                SweetAlertDialog(
                                    this@FollowerManagement,
                                    SweetAlertDialog.SUCCESS_TYPE
                                )
                                    .setTitleText(getString(R.string.Done))
                                    .setContentText(getString(R.string.JobDone))
                            d2.setConfirmClickListener {
                                d2.dismiss()
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
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("twitter://user?screen_name=$screenName")
                        )
                    )
                } catch (e: Exception) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://twitter.com/#!/$screenName")
                        )
                    )
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
}
