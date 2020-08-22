package com.sasarinomari.tweeper.Hetzer.New

import com.sasarinomari.tweeper.SimplizatedClass.Status

class Hetzer(private val collections: List<ConditionCollection>,
             private val defaultAction: Action = Action.Delete
) {
    enum class Action { Delete, Save }

    fun filter(status: Status): Action {
        for(collection in collections) {
            if(collection.action == defaultAction) continue
            if(collection.check(status)) return collection.action
        }
        return defaultAction
    }
}