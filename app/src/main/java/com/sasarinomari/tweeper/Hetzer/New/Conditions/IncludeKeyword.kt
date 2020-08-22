package com.sasarinomari.tweeper.Hetzer.New.Conditions

import com.sasarinomari.tweeper.Hetzer.New.ConditionObject
import com.sasarinomari.tweeper.SimplizatedClass.Status

class IncludeKeyword(statement: Boolean, keyword: String) : ConditionObject(statement, keyword) {
    override fun checkCondition(tweet: Status): Boolean {
        return tweet.text.contains(parameter as String) == statement
    }
}