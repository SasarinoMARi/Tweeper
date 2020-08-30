package com.sasarinomari.tweeper.Hetzer.New

import android.content.Context
import com.sasarinomari.tweeper.Hetzer.New.Conditions.*
import com.sasarinomari.tweeper.SimplizatedClass.Status

abstract class ConditionObject(val statement: Boolean,
                               val parameter: Any?) {
    fun check(tweet:Status): Boolean {
        return statement && checkCondition(tweet)
    }

    abstract fun toString(context: Context): String

    protected abstract fun checkCondition(tweet:Status): Boolean

    companion object Factory{
        fun make(className: String, statement: Boolean, parameter: Any?) : ConditionObject {
            return when(className) {
                FavoriteByMe::class.java.name -> FavoriteByMe(statement)
                FavoriteCount::class.java.name -> FavoriteCount(statement, parameter!! as Int)
                RetweetByMe::class.java.name -> RetweetByMe(statement)
                RetweetCount::class.java.name -> RetweetCount(statement, parameter!! as Int)
                IncludeKeyword::class.java.name -> IncludeKeyword(statement, parameter!! as String)
                IncludeMedia::class.java.name -> IncludeMedia(statement)
                IncludeGeo::class.java.name -> IncludeGeo(statement)
                RecentByCount::class.java.name -> RecentByCount(statement, parameter!! as Int)
                RecentByMinute::class.java.name -> RecentByMinute(statement, parameter!! as Int)
                else -> throw Exception()
            }
        }
    }
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