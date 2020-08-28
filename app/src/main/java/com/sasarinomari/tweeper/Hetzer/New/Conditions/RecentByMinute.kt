package com.sasarinomari.tweeper.Hetzer.New.Conditions

import android.content.Context
import com.sasarinomari.tweeper.Hetzer.New.ConditionObject
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SimplizatedClass.Status

class RecentByMinute(statement: Boolean, minute: Int) : ConditionObject(statement, minute) {
    override fun checkCondition(tweet: Status): Boolean {
        val min = parameter as Int
        val divider = 1000 * 60
        val tweetMinuteStamp = tweet.createdAt.time / divider + min
        val safeMinuteStamp = System.currentTimeMillis() / divider
        return tweetMinuteStamp >= safeMinuteStamp == statement
    }

    override fun toString(context: Context): String {
        var param = (parameter as Int).toString()
        if(param == "-1") param = "N"
        return context.getString(
            if (statement) R.string.HetzerConditions_16
            else R.string.HetzerConditions_22, param
        )
    }
}