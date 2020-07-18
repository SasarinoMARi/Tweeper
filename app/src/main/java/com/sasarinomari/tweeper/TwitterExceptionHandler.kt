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
            TwitterStatusCode.OK.code -> {
                when (te.errorCode) {
                    TwitterErrorCode.RateLlimitExceeded.code -> {
                        onRateLimitExceeded()
                        Log.i(LOG_HEADER, "Rate Limit Exceeded:\n\t[API] $apiEndPoint\n\tSeconds Until Reset: ${te.rateLimitStatus.secondsUntilReset}")
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
                            else -> throw te
                        }
                    }
                    else -> throw te
                }
            }
            else -> throw te
        }
    }

    abstract fun onRateLimitExceeded()
    abstract fun onRateLimitReset()
    open fun onNotFound() { }
}