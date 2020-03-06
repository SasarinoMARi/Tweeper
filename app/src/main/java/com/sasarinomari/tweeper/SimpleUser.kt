package com.sasarinomari.tweeper

import twitter4j.User

class SimpleUser {
    var id: Long = -1
    var screenName: String? = null
    var name : String? = null
    var profilePicUrl: String? = null

    companion object CREATOR {
        fun createFromUser(user: User): SimpleUser {
            val me = SimpleUser()
            me.id = user.id
            me.screenName = user.screenName
            me.profilePicUrl = user.profileImageURL
            me.name = user.name
            return me
        }
    }

    override fun equals(other: Any?): Boolean {
        return if (other is SimpleUser) this.id == other.id else super.equals(other)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}