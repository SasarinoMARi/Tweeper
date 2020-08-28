package com.sasarinomari.tweeper.Hetzer.New.Conditions

import android.content.Context
import com.sasarinomari.tweeper.Hetzer.New.*
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SimplizatedClass.Status

class RetweetCount(statement: Boolean, retweetCount: Int) : ConditionObject(statement, retweetCount) {
    override fun checkCondition(tweet: Status): Boolean {
        return tweet.retweetCount >= parameter as Int == statement
    }

    override fun toString(context: Context): String {
        var param = (parameter as Int).toString()
        if(param == "-1") param = "N"
        return context.getString(
            if (statement) R.string.HetzerConditions_7
            else R.string.HetzerConditions_8, param
        )
    }
}