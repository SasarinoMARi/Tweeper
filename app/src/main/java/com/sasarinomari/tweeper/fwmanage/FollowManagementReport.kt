package com.sasarinomari.tweeper.fwmanage

class FollowManagementReport {
    constructor()
    constructor(traitors: List<twitter4j.User>, fans: List<twitter4j.User>) {
        for(user in traitors) this.traitors.add(User(user))
        for(user in fans) this.fans.add(User(user))
    }

    companion object {
        const val prefix = "fwReport"
    }

    var traitors = ArrayList<User>()
    var fans = ArrayList<User>()

    class User(src: twitter4j.User) {
        val name: String = src.name
        val screenName: String = src.screenName
        val profileImageUrl: String = src.miniProfileImageURL
        val id: Long = src.id
        val bio: String = src.description
    }
}