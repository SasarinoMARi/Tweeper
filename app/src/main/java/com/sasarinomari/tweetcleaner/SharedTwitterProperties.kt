package com.sasarinomari.tweetcleaner

import android.content.Context
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

        @Throws(TwitterException::class)
        fun getFriends(ai: ActivityInterface, callback: (List<User>) -> Unit) {
            if (friends != null) {
                callback(friends!!)
                return
            }

            Thread(Runnable {
                val list = ArrayList<User>()
                // gets Twitter instance with default credentials
                var cursor: Long = -1
                val twitter = SharedTwitterProperties.instance()
                getMe(ai) { me ->
                    try {
                        while (true) {
                            val users = twitter.getFriendsList(me.id, cursor, 200, true, true)
                            list.addAll(users)
                            if (users.hasNext()) cursor = users.nextCursor
                            else break
                        }
                        friends = list
                        callback(list)
                    } catch (te: TwitterException) {
                        te.printStackTrace()
                        ai.onRateLimit("get/friends")
                    }
                }
            }).start()
        }

        @Throws(TwitterException::class)
        fun getFollowers(ai: ActivityInterface, callback: (List<User>) -> Unit) {
            if (followers != null) {
                callback(followers!!)
                return
            }

            Thread(Runnable {
                val list = ArrayList<User>()
                // gets Twitter instance with default credentials
                var cursor: Long = -1
                val twitter = SharedTwitterProperties.instance()
                getMe(ai) { me ->
                    try {
                        while (true) {
                            val users = twitter.getFollowersList(me.id, cursor, 200, true, true)
                            list.addAll(users)
                            if (users.hasNext()) cursor = users.nextCursor
                            else break
                        }
                        followers = list
                        callback(list)
                    } catch (te: TwitterException) {
                        te.printStackTrace()
                        ai.onRateLimit("get/followers")
                    }
                }
            }).start()
        }

        fun clear(twitterInstance: Twitter) {
            twitter = twitterInstance
            me = null
            friends = null
            followers = null
            reportWritten = false
        }
    }

    interface ActivityInterface {
        fun onRateLimit(apiPoint: String)
    }
}

