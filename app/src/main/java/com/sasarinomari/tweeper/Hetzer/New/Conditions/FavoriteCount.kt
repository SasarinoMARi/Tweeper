package com.sasarinomari.tweeper.Hetzer.New.Conditions

import com.sasarinomari.tweeper.Hetzer.New.ConditionObject
import com.sasarinomari.tweeper.SimplizatedClass.Status

class FavoriteCount(statement: Boolean, favoriteCount: Int) : ConditionObject(statement, favoriteCount) {
    override fun checkCondition(tweet: Status): Boolean {
        return tweet.favoriteCount >= parameter as Int == statement
    }
}