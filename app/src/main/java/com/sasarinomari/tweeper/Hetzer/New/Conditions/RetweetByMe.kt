package com.sasarinomari.tweeper.Hetzer.New.Conditions

import com.sasarinomari.tweeper.Hetzer.New.ConditionObject
import com.sasarinomari.tweeper.SimplizatedClass.Status

class RetweetByMe(statement: Boolean) : ConditionObject(statement, null) {
    override fun checkCondition(tweet: Status): Boolean {
        return tweet.isRetweeted == statement
    }
}