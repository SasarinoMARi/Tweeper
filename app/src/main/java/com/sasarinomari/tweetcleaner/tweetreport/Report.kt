package com.sasarinomari.tweetcleaner.tweetreport

import twitter4j.User
import java.util.*
import kotlin.collections.ArrayList

internal class Report {
    var userId: Long = -1
    var tweetCount: Int = -1
    var tweetCountVar: Int? = null
    var friends = ArrayList<SimpleUser>()
    var friendsVar: Int? = null
    var followers = ArrayList<SimpleUser>()
    var followersVar: Int? = null
    var date = Date(0)

    override operator fun equals(other: Any?): Boolean {
        return if (other is Report) this.userId == other.userId else super.equals(other)
    }

    override fun hashCode(): Int {
        return userId.hashCode()
    }
}

internal class SimpleUser {
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