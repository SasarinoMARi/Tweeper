package com.sasarinomari.tweeper.Hetzer.New

import android.content.Context
import com.sasarinomari.tweeper.Hetzer.New.Conditions.*
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SimplizatedClass.Status

abstract class ConditionObject(val statement: Boolean,
                               val parameter: Any?) {
    public fun check(tweet:Status): Boolean {
        return statement && checkCondition(tweet)
    }

    abstract fun toString(context: Context): String

    protected abstract fun checkCondition(tweet:Status): Boolean
}

class ConditionCollection {
    var conditions = ArrayList<ConditionObject>()
    var action: Hetzer.Action = Hetzer.Action.Delete // 조건이 모두 참일 때 수행할 액션

    fun check(tweet:Status): Boolean {
        var result = true
        for(condition in conditions) {
            if(!condition.check(tweet)) {
                result = false
            }
        }
        return result
    }
}