package com.sasarinomari.tweeper.Hetzer.New.Conditions

import android.content.Context
import com.sasarinomari.tweeper.Hetzer.New.ConditionObject
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SimplizatedClass.Status

class FavoriteCount(statement: Boolean, favoriteCount: Int) : ConditionObject(statement, favoriteCount) {
    override fun checkCondition(tweet: Status): Boolean {
        return tweet.favoriteCount >= parameter as Int == statement
    }

    override fun toString(context: Context): String {
        var param = (parameter as Int).toString()
        if(param == "-1") param = "N"
        return context.getString(
            if (statement) R.string.HetzerConditions_5
            else R.string.HetzerConditions_6, param
        )
    }
}