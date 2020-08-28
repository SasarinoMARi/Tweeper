package com.sasarinomari.tweeper.Hetzer.New.Conditions

import android.content.Context
import com.sasarinomari.tweeper.Hetzer.New.ConditionObject
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SimplizatedClass.Status

class IncludeKeyword(statement: Boolean, keyword: String) : ConditionObject(statement, keyword) {
    override fun checkCondition(tweet: Status): Boolean {
        return tweet.text.contains(parameter as String) == statement
    }

    override fun toString(context: Context): String {
        return context.getString(
            if (statement) R.string.HetzerConditions_11
            else R.string.HetzerConditions_12, parameter as String
        )
    }
}