package com.sasarinomari.tweeper.Hetzer.New.Conditions

import com.sasarinomari.tweeper.Hetzer.New.ConditionObject
import com.sasarinomari.tweeper.SimplizatedClass.Status

class RecentByCount(statement: Boolean, minute: Int) : ConditionObject(statement, minute) {
    override fun checkCondition(tweet: Status): Boolean {
        return tweet.id < parameter as Int == statement
    }
}