package com.sasarinomari.tweetcleaner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import cn.pedant.SweetAlert.SweetAlertDialog
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.User

class FollowerManagement : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follower_management)

        Thread(Runnable {
            getFollower {

            }
        }).start()
    }


    private fun getFollower(callback: (List<User>) -> Unit) {
        val list = ArrayList<User>()
        try {
            // gets Twitter instance with default credentials
            val twitter = TwitterFactory.getSingleton()
            val me = twitter.showUser(twitter.id)
            var cursor: Long = -1
            while(true){
                val users = twitter.getFollowersList(me.id, cursor, 100, true, true)
                list.addAll(users)
                if(users.hasNext()) cursor = users.nextCursor
                else break
            }
            callback(list)
        } catch (te: TwitterException) {
            te.printStackTrace()
            runOnUiThread {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getString(R.string.Error))
                    .setContentText(getString(R.string.RateLimitError))
                    .setConfirmClickListener {
                        callback(list)
                    }
                    .show()
            }
        }
    }
}
