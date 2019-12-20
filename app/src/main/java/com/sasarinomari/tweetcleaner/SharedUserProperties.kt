package com.sasarinomari.tweetcleaner

import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.User

class SharedUserProperties private constructor() {
    companion object {
        private var me: User? = null
        private var friends: List<User>? = null
        private var followers: List<User>? = null

        @Throws(TwitterException::class)
        fun getMe(callback: (User) -> Unit) {
            if (me != null) {
                callback(me!!)
                return
            }
            Thread(Runnable {
                val twitter = TwitterFactory.getSingleton()
                me = twitter.showUser(twitter.id)
                callback(me!!)
            }).start()
        }

        @Throws(TwitterException::class)
        fun getFriends(callback: (List<User>) -> Unit) {
            if (friends != null) {
                callback(friends!!)
                return
            }

            Thread(Runnable {
                val list = ArrayList<User>()
                // gets Twitter instance with default credentials
                var cursor: Long = -1
                val twitter = TwitterFactory.getSingleton()
                getMe { me ->
                    while (true) {
                        val users = twitter.getFriendsList(me.id, cursor, 100, true, true)
                        list.addAll(users)
                        if (users.hasNext()) cursor = users.nextCursor
                        else break
                    }
                    friends = list
                    callback(list)
                }
            }).start()
        }

        @Throws(TwitterException::class)
        fun getFollowers(callback: (List<User>) -> Unit) {
            if (followers != null) {
                callback(followers!!)
                return
            }

            Thread(Runnable {
                val list = ArrayList<User>()
                // gets Twitter instance with default credentials
                var cursor: Long = -1
                val twitter = TwitterFactory.getSingleton()
                getMe { me ->
                    while (true) {
                        val users = twitter.getFollowersList(me.id, cursor, 100, true, true)
                        list.addAll(users)
                        if (users.hasNext()) cursor = users.nextCursor
                        else break
                    }
                    followers = list
                    callback(list)
                }
            }).start()
        }
    }
}

