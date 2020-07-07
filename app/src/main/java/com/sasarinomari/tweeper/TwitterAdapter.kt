package com.sasarinomari.tweeper

import android.content.Context
import android.util.Log
import twitter4j.Paging
import twitter4j.Status
import twitter4j.TwitterException
import twitter4j.User

class TwitterAdapter(private val context: Context) {

    companion object {
        private const val LOG_TAG: String = "TwitterAdapter"
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

    fun blockUsers(targetUsersIds: ArrayList<Long>, apiInterface: IterableInterface, startIndex: Int = 0) {
        apiInterface.onStart()
        Thread(Runnable {
            val twitter = SharedTwitterProperties.instance()
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
        }).start()
    }

    fun getFriendsIds(targetUserId: Long, apiInterface: FetchListInterface, startIndex: Long = -1, list: ArrayList<Long> = ArrayList()) {
        apiInterface.onStart()
        Thread(Runnable {
            val twitter = SharedTwitterProperties.instance()
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
        }).start()
    }

    fun getFollowersIds(targetUserId: Long, apiInterface: FetchListInterface, startIndex: Long = -1, list: ArrayList<Long> = ArrayList()) {
        apiInterface.onStart()
        Thread(Runnable {
            val twitter = SharedTwitterProperties.instance()
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
                        getFriendsIds(targetUserId, apiInterface, cursor, list)
                    }
                }.catch()
            }
        }).start()
    }

    fun getMe(activityInterface: FetchObjectInterface) {
        activityInterface.onStart()
        Thread(Runnable {
            try {
                val twitter = SharedTwitterProperties.instance()
                val me = twitter.showUser(twitter.id)
                activityInterface.onFinished(me)
            } catch (te: TwitterException) {
                object: TwitterExceptionHandler(te, "showUser") {
                    override fun onRateLimitExceeded() {
                        activityInterface.onRateLimit()
                    }

                    override fun onRateLimitReset() {
                        getMe(activityInterface)
                    }
                }.catch()
            }
        }).start()
    }

    fun getFriends(targetUserId: Long, apiInterface: FetchListInterface, startIndex: Long = -1, list: ArrayList<User> = ArrayList()) {
        apiInterface.onStart()
        Thread(Runnable {
            var cursor: Long = startIndex
            val twitter = SharedTwitterProperties.instance()
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
                object: TwitterExceptionHandler(te, "getFriendsList") {
                    override fun onRateLimitExceeded() {
                        apiInterface.onRateLimit(list.count())
                    }

                    override fun onRateLimitReset() {
                        getFriends(targetUserId, apiInterface, cursor, list)
                    }
                }.catch()
            }
        }).start()
    }

    fun getFollowers(targetUserId: Long, apiInterface: FetchListInterface, startIndex: Long = -1, list: ArrayList<User> = ArrayList()) {
        apiInterface.onStart()
        Thread(Runnable {
            var cursor: Long = startIndex
            val twitter = SharedTwitterProperties.instance()
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
                object: TwitterExceptionHandler(te, "getFollowersList") {
                    override fun onRateLimitExceeded() {
                        apiInterface.onRateLimit(list.count())
                    }

                    override fun onRateLimitReset() {
                        getFollowers(targetUserId, apiInterface, cursor, list)
                    }
                }.catch()
            }
        }).start()
    }

    fun getTweets(apiInterface: FetchListInterface, startIndex: Int = 1, list: ArrayList<Status> = ArrayList()) {
        apiInterface.onStart()
        Thread(Runnable {
            var lastIndex = startIndex
            try {
                val twitter = SharedTwitterProperties.instance()
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
        }).start()
    }

    fun destroyStatus(statuses: ArrayList<Status>, apiInterface: IterableInterface, startIndex: Int = 0) {
        apiInterface.onStart()
        // TODO : 이미 트윗이 지워진 경우 등 예외상황에 잘 동작하는지 확인할 필요 있음
        Thread(Runnable {
            val twitter = SharedTwitterProperties.instance()
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
        }).start()
    }
}