package com.sasarinomari.tweeper

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log
import twitter4j.*
import twitter4j.auth.AccessToken


class TwitterAdapter {
    companion object {
        private const val LOG_TAG: String = "TwitterAdapter"

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

        @SuppressLint("ObsoleteSdkInt")
        fun isConnected(context: Context): Boolean {
            var result = false
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkCapabilities = connectivityManager.activeNetwork ?: return false
                val actNw =
                    connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
                result = when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } else {
                connectivityManager.run {
                    connectivityManager.activeNetworkInfo?.run {
                        result = when (type) {
                            ConnectivityManager.TYPE_WIFI -> true
                            ConnectivityManager.TYPE_MOBILE -> true
                            ConnectivityManager.TYPE_ETHERNET -> true
                            else -> false
                        }
                    }
                }
            }

            return result
        }
    }

    class TwitterInterface {
        companion object {
            private var consumerKey: String? = null
            private var consumerSecret: String? = null

            fun setOAuthConsumer(context: Context) {
                consumerKey = context.getString(R.string.consumerKey)
                consumerSecret = context.getString(R.string.consumerSecret)
            }
        }
        val id: Long
            get() { return client.id }

        // TODO: 이 병신같은 초기화 코드 언제 좀 고치셈 ㅡㅡ^
        lateinit var client: Twitter
            private set

        fun initialize(context: Context) {
            setOAuthConsumer(context)
            val t = TwitterFactory().instance
            if(t==null) {
-                Log.i("Hello", "Mr.Bug!")
            }
            t.setOAuthConsumer(consumerKey, consumerSecret)
            this.client = t
        }

        fun initialize(twitter: Twitter) {
            this.client = twitter
        }

        fun initialize(context: Context, accessToken: AccessToken) {
            initialize(context)
            client.oAuthAccessToken = accessToken
        }
    }

    var twitter = TwitterInterface()

    fun initialize(context: Context, accessToken: AccessToken) : TwitterAdapter {
        TwitterInterface.setOAuthConsumer(context)
        twitter.initialize(context, accessToken)
        Log.i(LOG_TAG, "Logged in with ${accessToken.screenName}")
        return this
    }

    // region Interfaces
    interface BaseInterface {
        fun onStart()
    }
    interface ErrorInterface {
        fun onUncaughtError()
        fun onNetworkError(retrySelf: ()->Unit)
    }
    interface IterableInterface : BaseInterface, ErrorInterface {
        fun onFinished()
        fun onIterate(listIndex: Int)
        fun onRateLimit(listIndex: Int)
    }
    interface FetchListInterface : BaseInterface, ErrorInterface {
        fun onFinished(list: ArrayList<*>)
        fun onFetch(listSize: Int)
        fun onRateLimit(listSize: Int)
    }
    interface FetchObjectInterface : BaseInterface, ErrorInterface {
        fun onFinished(obj: Any)
        fun onRateLimit()
    }
    interface FoundObjectInterface : BaseInterface, ErrorInterface {
        fun onFinished(obj: Any)
        fun onRateLimit()
        fun onNotFound()
    }
    interface PostInterface : BaseInterface, ErrorInterface{
        fun onFinished(obj: Any)
        fun onRateLimit()
    }

    interface FetchListStepInterface : BaseInterface, ErrorInterface {
        fun onFetch(list: List<*>)
        fun onRateLimit()
    }
    // endregion

    fun blockUsers(targetUsersIds: ArrayList<Long>, apiInterface: IterableInterface, startIndex: Int = 0) {
        apiInterface.onStart()
        var cursor = 0
        try {
            for (it in startIndex until targetUsersIds.size) {
                cursor = it
                apiInterface.onIterate(it)
                twitter.client.createBlock(targetUsersIds[it])
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

                override fun onUncaughtError() {
                    apiInterface.onUncaughtError()
                }

                override fun onNetworkError() {
                    apiInterface.onNetworkError { blockUsers(targetUsersIds, apiInterface, cursor) }
                }
            }.catch()
        }
    }

    /*
    fun getFriendsIdsOnce(targetUserId: Long, apiInterface: FetchListInterface, cursor: Long = -1) {
        apiInterface.onStart()
        try {
            while (true) {
                apiInterface.onFetch(list.count())
                val users = twitter.client.getFriendsIDs(targetUserId, cursor, 5000)
                list.addAll(users.iDs.toList())
                Log.i(LOG_TAG, "Count of Collected Users: ${list.count()}")
                if (users.hasNext()) cursor = users.nextCursor
                else break
            }
            apiInterface.onFinished(list)
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "getFriendsIDs") {
                override fun onUncaughtError() {
                    apiInterface.onUncaughtError()
                }

                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit(list.count())
                }

                override fun onRateLimitReset() {
                    getFriendsIds(targetUserId, apiInterface, cursor, list)
                }

                override fun onNetworkError() {
                    apiInterface.onNetworkError { getFriendsIds(targetUserId, apiInterface, cursor, list) }
                }
            }.catch()
        }
    }
     */

    fun getFriendsIds(targetUserId: Long, apiInterface: FetchListInterface, startIndex: Long = -1, list: ArrayList<Long> = ArrayList()) {
        apiInterface.onStart()
        var cursor: Long = startIndex
        try {
            while (true) {
                apiInterface.onFetch(list.count())
                val users = twitter.client.getFriendsIDs(targetUserId, cursor, 5000)
                list.addAll(users.iDs.toList())
                Log.i(LOG_TAG, "Count of Collected Users: ${list.count()}")
                if (users.hasNext()) cursor = users.nextCursor
                else break
            }
            apiInterface.onFinished(list)
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "getFriendsIDs") {
                override fun onUncaughtError() {
                    apiInterface.onUncaughtError()
                }

                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit(list.count())
                }

                override fun onRateLimitReset() {
                    getFriendsIds(targetUserId, apiInterface, cursor, list)
                }

                override fun onNetworkError() {
                    apiInterface.onNetworkError { getFriendsIds(targetUserId, apiInterface, cursor, list) }
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
                val users = twitter.client.getFollowersIDs(targetUserId, cursor, 5000)
                list.addAll(users.iDs.toList())
                Log.i(LOG_TAG, "Count of Collected Users: ${list.count()}")
                if (users.hasNext()) cursor = users.nextCursor
                else break
            }
            apiInterface.onFinished(list)
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "getFollowersIDs") {
                override fun onUncaughtError() {
                    apiInterface.onUncaughtError()
                }

                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit(list.count())
                }

                override fun onRateLimitReset() {
                    getFollowersIds(targetUserId, apiInterface, cursor, list)
                }

                override fun onNetworkError() {
                    apiInterface.onNetworkError { getFollowersIds(targetUserId, apiInterface, cursor, list) }
                }
            }.catch()
        }
    }

    fun getMe(apiInterface: FetchObjectInterface) {
        apiInterface.onStart()
        try {
            val me = twitter.client.showUser(twitter.client.id)
            apiInterface.onFinished(me)
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "showUser") {
                override fun onUncaughtError() {
                    apiInterface.onUncaughtError()
                }

                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit()
                }

                override fun onRateLimitReset() {
                    getMe(apiInterface)
                }

                override fun onNetworkError() {
                    apiInterface.onNetworkError { getMe(apiInterface) }
                }

                override fun onNotFound() {
                    apiInterface.onUncaughtError()
                }
            }.catch()
        }
    }

    fun getFriends(targetUserId: Long, apiInterface: FetchListInterface, startIndex: Long = -1, list: ArrayList<User> = ArrayList()) {
        apiInterface.onStart()
        /*
        var cursor: Long = startIndex
        try {
            while (true) {
                apiInterface.onFetch(list.count())
                val users = twitter.client.getFriendsList(targetUserId, cursor, 200, true, true)
                list.addAll(users)
                if (users.hasNext()) cursor = users.nextCursor
                else break
            }
            apiInterface.onFinished(list)
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "getFriendsList") {
                override fun onUncaughtError() {
                    apiInterface.onUncaughtError()
                }

                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit(list.count())
                }

                override fun onRateLimitReset() {
                    getFriends(targetUserId, apiInterface, cursor, list)
                }

                override fun onNetworkError() {
                    apiInterface.onNetworkError { getFriends(targetUserId, apiInterface, cursor, list) }
                }
            }.catch()
        }
         */


        var cursor: Long = startIndex
        try {
            while (true) {
                apiInterface.onFetch(list.count())
                val users = twitter.client.getFriendsList(targetUserId, cursor, 200, true, true)
                // for (i in 0 .. 500) list.addAll(users)
                list.addAll(users)
                if (users.hasNext()) cursor = users.nextCursor
                else break
            }
            apiInterface.onFinished(list)
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "getFriendsList") {
                override fun onUncaughtError() {
                    apiInterface.onUncaughtError()
                }

                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit(list.count())
                }

                override fun onRateLimitReset() {
                    getFriends(targetUserId, apiInterface, cursor, list)
                }

                override fun onNetworkError() {
                    apiInterface.onNetworkError { getFriends(targetUserId, apiInterface, cursor, list) }
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
                val users = twitter.client.getFollowersList(targetUserId, cursor, 200, true, true)
                list.addAll(users)
                if (users.hasNext()) cursor = users.nextCursor
                else break
            }
            apiInterface.onFinished(list)
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "getFollowersList") {
                override fun onUncaughtError() {
                    apiInterface.onUncaughtError()
                }

                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit(list.count())
                }

                override fun onRateLimitReset() {
                    getFollowers(targetUserId, apiInterface, cursor, list)
                }

                override fun onNetworkError() {
                    apiInterface.onNetworkError { getFollowers(targetUserId, apiInterface, cursor, list) }
                }
            }.catch()
        }
    }

    /**
     * 가장 최근 트윗 20개를 가져옵니다.
     * 만약 maxId가 지정되어있다면 해당 트윗 이전에 작성한 트윗부터 불러옵니다.
     */
    fun getTweets(apiInterface: FetchListStepInterface, maxId: Long = 0) {
        apiInterface.onStart()

        try {
            /**
             * 페이징
             */
            val numberOfFetch = 20
            val paging = Paging(1, numberOfFetch)
            if(maxId > 0) paging.maxId = maxId

            val statuses = twitter.client.getUserTimeline(paging)

            /**
             * 분명 뒤에 트윗 더 있는데 중간에 못불러오는 트윗이 존재함.
             * 해당 경우의 예외 처리 코드
             *
             * 가져온 트윗이 numberOfFetch와 같으면 ok
             * numberOfFetch보다 적으면 이 페이지가 마지막인지 확인하기 위해 몇 더 호출함
             */
            for(loop_page in 2 until 20) {
                if(statuses.size >= numberOfFetch) {
                    break
                }

                val loop_Paging = Paging(loop_page, numberOfFetch)
                if(maxId > 0) loop_Paging.maxId = maxId
                val loop_Status = twitter.client.getUserTimeline(loop_Paging)
                statuses.addAll(loop_Status)
            }
            apiInterface.onFetch(statuses)

        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "getUserTimeline") {
                override fun onUncaughtError() {
                    apiInterface.onUncaughtError()
                }

                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit()
                }

                override fun onRateLimitReset() {
                    getTweets(apiInterface, maxId)
                }

                override fun onNetworkError() {
                    apiInterface.onNetworkError { getTweets(apiInterface, maxId) }
                }
            }.catch()
        }
    }

    fun destroyStatus(statuses: ArrayList<Status>, apiInterface: IterableInterface, startIndex: Int = 0) {
        apiInterface.onStart()
        var cursor = 0
        try {
            val statusCount = statuses.count()
            for (i in startIndex until statusCount) {
                cursor = i
                val status = statuses[i]
                apiInterface.onIterate(cursor)
                Log.d(LOG_TAG, "destroy status: ${status.text.substring(0, 20)}")
                if(!BuildConfig.DEBUG) twitter.client.destroyStatus(status.id)
            }
            apiInterface.onFinished()
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "destroyStatus") {
                override fun onUncaughtError() {
                    apiInterface.onUncaughtError()
                }

                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit(cursor)
                }

                override fun onRateLimitReset() {
                    destroyStatus(statuses, apiInterface, cursor)
                }

                override fun onNetworkError() {
                    apiInterface.onNetworkError { destroyStatus(statuses, apiInterface, cursor) }
                }

                override fun onNotFound() {
                    /**
                     * 이미 삭제된 등의 이유로 찾을 수 없을 경우
                     * 생략하고 다음 루프로
                     */
                    destroyStatus(statuses, apiInterface, cursor + 1)
                }
            }.catch()
        }
    }

    fun getBlockedUsers(apiInterface: FetchListInterface, startIndex: Long = -1, list: ArrayList<Long> = ArrayList()) {
        apiInterface.onStart()
        var cursor: Long = startIndex
        try {
            while (true) {
                apiInterface.onFetch(list.count())
                val users = twitter.client.getBlocksIDs(cursor)
                list.addAll(users.iDs.toList())
                if (users.hasNext()) cursor = users.nextCursor
                else break
            }
            apiInterface.onFinished(list)
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "getBlocksIDs") {
                override fun onUncaughtError() {
                    apiInterface.onUncaughtError()
                }

                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit(list.count())
                }

                override fun onRateLimitReset() {
                    getBlockedUsers(apiInterface, cursor, list)
                }

                override fun onNetworkError() {
                    apiInterface.onNetworkError { getBlockedUsers(apiInterface, cursor, list) }
                }
            }.catch()
        }
    }

    fun unblockUsers(list: ArrayList<Long>, apiInterface: IterableInterface, startIndex: Int = 0) {
        apiInterface.onStart()
        var cursor = 0
        try {
            for (i in startIndex until list.size) {
                cursor = i
                apiInterface.onIterate(i+1)
                twitter.client.destroyBlock(list[i])
            }
            apiInterface.onFinished()
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "createBlock") {
                override fun onUncaughtError() {
                    apiInterface.onUncaughtError()
                }

                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit(cursor + 1)
                }

                override fun onRateLimitReset() {
                    unblockUsers(list, apiInterface, cursor)
                }

                override fun onNetworkError() {
                    apiInterface.onNetworkError { apiInterface.onRateLimit(cursor + 1) }
                }
            }.catch()
        }
    }

    fun lookup(screenName: String, apiInterface: FoundObjectInterface) {
        apiInterface.onStart()
        try {
            val user = twitter.client.showUser(screenName)
            apiInterface.onFinished(user)
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "lookup") {
                override fun onUncaughtError() {
                    apiInterface.onUncaughtError()
                }

                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit()
                }

                override fun onRateLimitReset() {
                    lookup(screenName, apiInterface)
                }

                override fun onNotFound() {
                    apiInterface.onNotFound()
                }

                override fun onNetworkError() {
                    apiInterface.onNetworkError { lookup(screenName, apiInterface) }
                }
            }.catch()
        }
    }

    fun lookStatus(id: Long, apiInterface: FoundObjectInterface) {
        apiInterface.onStart()
        try {
            val user = twitter.client.showStatus(id)
            apiInterface.onFinished(user)
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "lookStatus") {
                override fun onUncaughtError() {
                    apiInterface.onUncaughtError()
                }

                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit()
                }

                override fun onRateLimitReset() {
                    lookStatus(id, apiInterface)
                }

                override fun onNotFound() {
                    apiInterface.onNotFound()
                }

                override fun onNetworkError() {
                    apiInterface.onNetworkError { lookStatus(id, apiInterface) }
                }
            }.catch()
        }
    }

    fun publish(text: String, apiInterface: PostInterface) {
        apiInterface.onStart()
        try {
            val user = twitter.client.updateStatus(text)
            apiInterface.onFinished(user)
        } catch (te: TwitterException) {
            object : TwitterExceptionHandler(te, "publish") {
                override fun onUncaughtError() {
                    apiInterface.onUncaughtError()
                }

                override fun onRateLimitExceeded() {
                    apiInterface.onRateLimit()
                }

                override fun onRateLimitReset() {
                    publish(text, apiInterface)
                }

                override fun onNetworkError() {
                    apiInterface.onNetworkError { publish(text, apiInterface) }
                }
            }.catch()
        }
    }

}