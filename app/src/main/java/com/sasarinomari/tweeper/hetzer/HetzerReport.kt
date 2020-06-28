package com.sasarinomari.tweeper.hetzer

import java.util.*
import com.sasarinomari.tweeper.SimplizatedClass.Status

class HetzerReport {
    constructor()
    constructor(removedStatuses: List<twitter4j.Status>, savedStatuses: List<twitter4j.Status>) {
        for(status in removedStatuses) this.removedStatuses.add(Status(status))
        for(status in savedStatuses) this.savedStatuses.add(Status(status))
    }

    companion object {
        const val prefix = "hetzerReport"
    }

    var id: Int = -1
    var removedStatuses = ArrayList<Status>()
    var savedStatuses = ArrayList<Status>()
}