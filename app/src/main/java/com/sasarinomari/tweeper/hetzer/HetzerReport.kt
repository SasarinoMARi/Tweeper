package com.sasarinomari.tweeper.hetzer

import twitter4j.Status
import java.util.*

class HetzerReport {
    constructor()
    constructor(removedStatuses: List<twitter4j.Status>, savedStatuses: List<twitter4j.Status>) {
        for(status in removedStatuses) this.removedStatuses.add(Status(status))
        for(status in savedStatuses) this.savedStatuses.add(Status(status))
    }

    companion object {
        const val prefix = "hetzerReport"
    }

    var removedStatuses = ArrayList<Status>()
    var savedStatuses = ArrayList<Status>()


    class Status(src: twitter4j.Status) {
        val text: String = src.text
        val createdAt: Date = src.createdAt
    }
}