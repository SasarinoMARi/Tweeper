package com.sasarinomari.tweeper.fwmanage

import com.sasarinomari.tweeper.SimplizatedClass.User

class FollowManagementReport {
    constructor()
    constructor(traitors: List<twitter4j.User>, fans: List<twitter4j.User>) {
        for(user in traitors) this.traitors.add(User(user))
        for(user in fans) this.fans.add(User(user))
    }

    companion object {
        const val prefix = "fwReport"
    }

    var id: Int = -1
    var traitors = ArrayList<User>()
    var fans = ArrayList<User>()
}