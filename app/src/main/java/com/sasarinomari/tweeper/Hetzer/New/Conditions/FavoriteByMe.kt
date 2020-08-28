package com.sasarinomari.tweeper.Hetzer.New.Conditions

import android.content.Context
import com.sasarinomari.tweeper.Hetzer.New.ConditionObject
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SimplizatedClass.Status

class FavoriteByMe(statement: Boolean) : ConditionObject(statement, null) {
    override fun checkCondition(tweet: Status): Boolean {
        return tweet.isFavorited == statement
    }

    override fun toString(context: Context): String {
        return context.getString(
            if (statement) R.string.HetzerConditions_1
            else R.string.HetzerConditions_2
        )
    }
}