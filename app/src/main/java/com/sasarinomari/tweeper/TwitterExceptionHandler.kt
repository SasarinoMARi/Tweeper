package com.sasarinomari.tweeper

import android.util.Log
import twitter4j.TwitterException

abstract class TwitterExceptionHandler(private val te: TwitterException,
                                       private val apiEndPoint: String) {

    private val LOG_HEADER = "TwitterExceptionHandler"

    fun catch() {
        when (te.statusCode) {
            TwitterStatusCode.NotFound.code -> {
                onNotFound()
            }
            TwitterStatusCode.Unauthrized.code -> {
                onNotFound()
            }
            else -> {
                when (te.errorCode) {
                    TwitterErrorCode.RateLlimitExceeded.code -> {
                        onRateLimitExceeded()
                        Log.i(LOG_HEADER, "Rate Limit Exceeded:\n\t[API] $apiEndPoint\n\tSeconds Until Reset: ${te.rateLimitStatus.secondsUntilReset}")

                        if(te.rateLimitStatus.secondsUntilReset > 0)
                            Thread.sleep((1000 * te.rateLimitStatus.secondsUntilReset).toLong())

                        onRateLimitReset()
                    }
                    TwitterErrorCode.UserNotFound.code -> {
                        onNotFound()
                    }
                    -1 -> {
                        when (te.message) {
                            "thread interrupted" -> {
                                Log.i(LOG_HEADER, "Thread Interrupted!")
                            }
                            "Unable to resolve host \"api.twitter.com\": No address associated with hostname" -> {
                                onNetworkError()
                            }
                            else -> onUncaughtError()
                        }
                    }
                    else -> onUncaughtError()
                }
            }
        }
    }

    abstract fun onNetworkError()
    abstract fun onUncaughtError()
    abstract fun onRateLimitExceeded()
    abstract fun onRateLimitReset()
    open fun onNotFound() { }
}