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
                        Log.i(LOG_HEADER,
                            "Rate Limit Exceeded:\n\t[API] $apiEndPoint\n\tSeconds Until Reset: ${te.rateLimitStatus.secondsUntilReset}"
                        )

                        if (te.rateLimitStatus.secondsUntilReset > 0)
                            try {
                                /**
                                 * 이유는 모르겠으나 sleep 도중 스레드가 종료되어서 Interrupted Exception 발생하는 듯 함.
                                 * 4월 26일 버전에서 우선 이렇게 예외처리함. 이후에도 같은 문제 발생하면 조치 필요
                                 */
                                Thread.sleep((1000 * te.rateLimitStatus.secondsUntilReset).toLong())
                            } catch (ex: InterruptedException) {
                                ex.printStackTrace()
                            } finally {
                                onRateLimitReset()
                            }
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