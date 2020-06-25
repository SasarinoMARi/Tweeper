package com.sasarinomari.tweeper.fwmanage

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import com.sasarinomari.tweeper.Adam
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SharedTwitterProperties
import kotlinx.android.synthetic.main.activity_follower_management.*
import twitter4j.User

class FollowerManagement : Adam(), SharedTwitterProperties.ActivityInterface {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follower_management)


        if(FollowerManagerService.checkServiceRunning((this))) {
            da.warning("잠시만요!", "트윗 청소기가 이미 실행중입니다.\n한 번에 하나의 청소기만 실행될 수 있습니다.").show()
        }
        else {
            val intent = Intent(this, FollowerManagerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            }
            else {
                startService(intent)
            }
        }

//        var dialog = da.progress(null, getString(R.string.FriendPulling))
//        dialog.show()
//
//        SharedTwitterProperties.getFriends(this) { fs ->
//            runOnUiThread {
//                dialog.dismissWithAnimation()
//                dialog = da.progress(null, getString(R.string.FollowerPulling))
//                dialog.show()
//            }
//            SharedTwitterProperties.getFollowers(this) { fw ->
//                runOnUiThread {
//                    dialog.dismissWithAnimation()
//                    dialog = da.progress(null, getString(R.string.CompareFsFw))
//                    dialog.show()
//                }
//                val uf = comLists(fs, fw)
//                runOnUiThread {
//                    dialog.dismissWithAnimation()
//                    initializeUi(uf)
//                }
//            }
//        }
    }

    private fun initializeUi(uf: ArrayList<User>) {
        if (uf.isEmpty()) {
            list.visibility = View.GONE
            layout_noApplicable.visibility = View.VISIBLE
            return
        }
        list.adapter = UserUnfollowItem(uf, object : UserUnfollowItem.ActivityInterface {
            override fun onClickUnfollow(userId: Long, doneCallback: Runnable) {
                da.warning(getString(R.string.AreYouSure), getString(R.string.ActionDoNotRestore))
                    .setConfirmText(getString(R.string.Yes))
                    .setConfirmClickListener {
                        it.dismissWithAnimation()
                        Thread(Runnable {
                            val twitter = SharedTwitterProperties.instance()
                            twitter.destroyFriendship(userId)
                            runOnUiThread {
                                da.success(getString(R.string.Done), getString(R.string.JobDone)) {
                                    it.dismissWithAnimation()
                                    doneCallback.run()
                                }.show()
                            }
                        }).start()
                    }.show()
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
            da.error(getString(R.string.Error), getString(R.string.RateLimitError, apiPoint)) { finish() }.show()
        }
    }
}
