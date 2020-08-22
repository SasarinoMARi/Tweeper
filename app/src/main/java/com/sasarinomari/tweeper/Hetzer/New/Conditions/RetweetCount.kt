package com.sasarinomari.tweeper.Hetzer.New.Conditions

import com.sasarinomari.tweeper.Hetzer.New.*
import com.sasarinomari.tweeper.SimplizatedClass.Status

class RetweetCount(statement: Boolean, retweetCount: Int) : ConditionObject(statement, retweetCount) {
    override fun checkCondition(tweet: Status): Boolean {
        return tweet.retweetCount >= parameter as Int == statement
    }
}