package com.sasarinomari.tweeper

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.sasarinomari.tweeper.Authenticate.AuthData
import twitter4j.Twitter
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.User

class SharedTwitterProperties private constructor() {
    companion object {
        fun setOAuthConsumer(context: Context, twitter: Twitter) {
            twitter.setOAuthConsumer(
                context.getString(R.string.consumerKey),
                context.getString(R.string.consumerSecret)
            )
        }

        private var twitter: Twitter = TwitterFactory().instance

        private var me: User? = null
        private var friends: List<User>? = null
        private var followers: List<User>? = null
        var reportWritten = false // 트윗일지에서 사용

        fun instance(): Twitter {
            return twitter
        }

        fun getMe(ai: ActivityInterface, callback: (User) -> Unit) {
            if (me != null) {
                callback(me!!)
                return
            }
            Thread(Runnable {
                try {
                    val twitter = SharedTwitterProperties.instance()
                    me = twitter.showUser(twitter.id)
                } catch (te: TwitterException) {
                    te.printStackTrace()
                    ai.onRateLimit("show/user/me")
                }
                callback(me!!)
            }).start()
        }

        fun clear(twitterInstance: Twitter) {
            twitter = twitterInstance
            me = null
            friends = null
            followers = null
            reportWritten = false
        }

        fun showProfile(context: Context, screenName: String) {
            try {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("twitter://user?screen_name=${screenName}")
                    )
                )
            } catch (e: Exception) {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://twitter.com/#!/${screenName}")
                    )
                )
            }
        }
    }

    interface ActivityInterface {
        fun onRateLimit(apiPoint: String)
    }
}

