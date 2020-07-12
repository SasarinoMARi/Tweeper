package com.sasarinomari.tweeper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import twitter4j.*

class TwitterAdapter {
    companion object {
        private const val LOG_TAG: String = "TwitterAdapter"

        // TODO: 이 병신같은 초기화 코드 언제 좀 고치셈 ㅡㅡ^
         var twitter: Twitter = TwitterFactory().instance
             private set

        fun setOAuthConsumer(context: Context, t: Twitter) {
            t.setOAuthConsumer(
                context.getString(R.string.consumerKey),
                context.getString(R.string.consumerSecret)
            )
        }

        fun initialize(context: Context) {
            val t = TwitterFactory().instance
            setOAuthConsumer(context, t)
            this.twitter = t
        }

        fun initialize(twitter: Twitter) {
            this.twitter = twitter
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

        fun showStatus(context: Context, statusId: Long) {
            try {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("twitter://status?id=$statusId")
                    )
                )
            } catch (e: Exception) {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://twitter.com/i/web/status/$statusId")
                    )
                )
            }
        }
    }

    interface IterableInterface {
        fun onStart()
        fun onFinished()
        fun onIterate(listIndex: Int)
        fun onRateLimit(listIndex: Int)
    }

    interface FetchListInterface {
        fun onStart()
        fun onFinished(list: ArrayList<*>)
        fun onFetch(listSize: Int)
        fun onRateLimit(listSize: Int)
    }

    interface FetchObjectInterface {
        fun onStart()
        fun onFinished(obj: Any)
        fun onRateLimit()
    }
    interface FoundObjectInterface {
        fun onStart()
        fun onFinished(obj: Any)
        fun onRateLimit()
        fun onNotFound()
    }

    fun blockUsers(targetUsersIds: ArrayList<Long>, apiInterface: IterableInterface, startIndex: Int = 0) {
        apiInterface.onStart()
        var cursor = 0
        try {
            for (it in startIndex until targetUsersIds.size) {
                cursor = it
                apiInterface.onIterate(it)
                twitter.createBlock(targetUsersIds[it])
            }
            apiInterface.onFinished()
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "createBlock") {
                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit(cursor)
                }

                override fun onRateLimitReset() {
                    blockUsers(targetUsersIds, apiInterface, cursor)
                }
            }.catch()
        }
    }

    fun getFriendsIds(targetUserId: Long, apiInterface: FetchListInterface, startIndex: Long = -1, list: ArrayList<Long> = ArrayList()) {
        apiInterface.onStart()
        var cursor: Long = startIndex
        try {
            while (true) {
                apiInterface.onFetch(list.count())
                val users = twitter.getFriendsIDs(targetUserId, cursor, 5000)
                list.addAll(users.iDs.toList())
                Log.i(LOG_TAG, "Count of Collected Users: ${list.count()}")
                if (users.hasNext()) cursor = users.nextCursor
                else break
            }
            apiInterface.onFinished(list)
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "getFriendsIDs") {
                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit(list.count())
                }

                override fun onRateLimitReset() {
                    getFriendsIds(targetUserId, apiInterface, cursor, list)
                }
            }.catch()
        }
    }

    fun getFollowersIds(targetUserId: Long, apiInterface: FetchListInterface, startIndex: Long = -1, list: ArrayList<Long> = ArrayList()) {
        apiInterface.onStart()
        var cursor: Long = startIndex
        try {
            while (true) {
                apiInterface.onFetch(list.count())
                val users = twitter.getFollowersIDs(targetUserId, cursor, 5000)
                list.addAll(users.iDs.toList())
                Log.i(LOG_TAG, "Count of Collected Users: ${list.count()}")
                if (users.hasNext()) cursor = users.nextCursor
                else break
            }
            apiInterface.onFinished(list)
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "getFollowersIDs") {
                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit(list.count())
                }

                override fun onRateLimitReset() {
                    getFollowersIds(targetUserId, apiInterface, cursor, list)
                }
            }.catch()
        }
    }

    fun getMe(activityInterface: FetchObjectInterface) {
        activityInterface.onStart()
        try {
            val me = twitter.showUser(twitter.id)
            activityInterface.onFinished(me)
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "showUser") {
                override fun onRateLimitExceeded() {
                    activityInterface.onRateLimit()
                }

                override fun onRateLimitReset() {
                    getMe(activityInterface)
                }
            }.catch()
        }
    }

    fun getFriends(targetUserId: Long, apiInterface: FetchListInterface, startIndex: Long = -1, list: ArrayList<User> = ArrayList()) {
        apiInterface.onStart()
        var cursor: Long = startIndex
        try {
            while (true) {
                apiInterface.onFetch(list.count())
                val users = twitter.getFriendsList(targetUserId, cursor, 200, true, true)
                list.addAll(users)
                if (users.hasNext()) cursor = users.nextCursor
                else break
            }
            apiInterface.onFinished(list)
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "getFriendsList") {
                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit(list.count())
                }

                override fun onRateLimitReset() {
                    getFriends(targetUserId, apiInterface, cursor, list)
                }
            }.catch()
        }
    }

    fun getFollowers(targetUserId: Long, apiInterface: FetchListInterface, startIndex: Long = -1, list: ArrayList<User> = ArrayList()) {
        apiInterface.onStart()
        var cursor: Long = startIndex
        try {
            while (true) {
                apiInterface.onFetch(list.count())
                val users = twitter.getFollowersList(targetUserId, cursor, 200, true, true)
                list.addAll(users)
                if (users.hasNext()) cursor = users.nextCursor
                else break
            }
            apiInterface.onFinished(list)
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "getFollowersList") {
                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit(list.count())
                }

                override fun onRateLimitReset() {
                    getFollowers(targetUserId, apiInterface, cursor, list)
                }
            }.catch()
        }
    }

    fun getTweets(apiInterface: FetchListInterface, startIndex: Int = 1, list: ArrayList<Status> = ArrayList()) {
        apiInterface.onStart()
        var lastIndex = startIndex
        try {
            for (i in startIndex..Int.MAX_VALUE) {
                apiInterface.onFetch(list.count())
                val paging = Paging(i, 20)
                val statuses = twitter.getUserTimeline(paging)
                list.addAll(statuses)
                lastIndex = i
                if (statuses.size == 0) break
            }
            apiInterface.onFinished(list)
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "getUserTimeline") {
                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit(list.count())
                }

                override fun onRateLimitReset() {
                    getTweets(apiInterface, lastIndex, list)
                }
            }.catch()
        }
    }

    fun destroyStatus(statuses: ArrayList<Status>, apiInterface: IterableInterface, startIndex: Int = 0) {
        apiInterface.onStart()
        // TODO : 이미 트윗이 지워진 경우 등 예외상황에 잘 동작하는지 확인할 필요 있음
        var cursor = 0
        try {
            val statusCount = statuses.count()
            for (i in startIndex until statusCount) {
                cursor = i
                val status = statuses[i]
                apiInterface.onIterate(cursor)
//                    twitter.destroyStatus(status.id)
            }
            apiInterface.onFinished()
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "destroyStatus") {
                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit(cursor)
                }

                override fun onRateLimitReset() {
                    destroyStatus(statuses, apiInterface, cursor)
                }
            }.catch()
        }
    }

    fun getBlockedUsers(activityInterface: FetchListInterface, startIndex: Long = -1, list: ArrayList<Long> = ArrayList()) {
        activityInterface.onStart()
        var cursor: Long = startIndex
        try {
            while (true) {
                activityInterface.onFetch(list.count())
                val users = twitter.getBlocksIDs(cursor)
                list.addAll(users.iDs.toList())
                if (users.hasNext()) cursor = users.nextCursor
                else break
            }
            activityInterface.onFinished(list)
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "getBlocksIDs") {
                override fun onRateLimitExceeded() {
                    activityInterface.onRateLimit(list.count())
                }

                override fun onRateLimitReset() {
                    getBlockedUsers(activityInterface, cursor, list)
                }
            }.catch()
        }
    }

    fun unblockUsers(list: ArrayList<Long>, activityInterface: IterableInterface, startIndex: Int = 0) {
        activityInterface.onStart()
        var cursor = 0
        try {
            for (i in startIndex until list.size) {
                cursor = i
                activityInterface.onIterate(i+1)
                twitter.destroyBlock(list[i])
            }
            activityInterface.onFinished()
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "createBlock") {
                override fun onRateLimitExceeded() {
                    activityInterface.onRateLimit(cursor + 1)
                }

                override fun onRateLimitReset() {
                    unblockUsers(list, activityInterface, cursor)
                }
            }.catch()
        }
    }

    fun lookup(screenName: String, activityInterface: FoundObjectInterface) {
        activityInterface.onStart()
        try {
            val user = twitter.showUser(screenName)
            activityInterface.onFinished(user)
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "lookup") {
                override fun onRateLimitExceeded() {
                    activityInterface.onRateLimit()
                }

                override fun onRateLimitReset() {
                    lookup(screenName, activityInterface)
                }

                override fun onUserNotFound() {
                    activityInterface.onNotFound()
                }
            }.catch()
        }
    }

}