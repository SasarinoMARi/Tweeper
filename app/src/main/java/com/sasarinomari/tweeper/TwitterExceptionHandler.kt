package com.sasarinomari.tweeper

import android.util.Log
import twitter4j.TwitterException

abstract class TwitterExceptionHandler(private val te: TwitterException,
                                       private val apiEndPoint: String) {

    private val LOG_HEADER = "TwitterExceptionHandler"

    fun catch() {
        when (te.errorCode) {
            TwitterErrorCode.RateLlimitExceeded.code -> {
                onRateLimitExceeded()
                Log.i(LOG_HEADER, "Rate Limit Exceeded:\n\t[API] $apiEndPoint\n\tSeconds Until Reset: ${te.rateLimitStatus.secondsUntilReset}")
                Thread.sleep((1000 * te.rateLimitStatus.secondsUntilReset).toLong())
                onRateLimitReset()
            }
            else -> throw te
        }
    }

    abstract fun onRateLimitExceeded()
    abstract fun onRateLimitReset()
}