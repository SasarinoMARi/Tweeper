package com.sasarinomari.tweeper.Hetzer.New.Conditions

import com.sasarinomari.tweeper.Hetzer.New.ConditionObject
import com.sasarinomari.tweeper.SimplizatedClass.Status

class RecentByMinute(statement: Boolean, minute: Int) : ConditionObject(statement, minute) {
    override fun checkCondition(tweet: Status): Boolean {
        val min = parameter as Int
        val divider = 1000 * 60
        val tweetMinuteStamp = tweet.createdAt.time / divider + min
        val safeMinuteStamp = System.currentTimeMillis() / divider
        return tweetMinuteStamp >= safeMinuteStamp == statement
    }
}