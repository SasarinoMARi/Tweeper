package com.sasarinomari.tweetcleaner.tweetreport

import android.os.Parcel
import android.os.Parcelable
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
        var result = userId.hashCode()
        result = 31 * result + tweetCount
        result = 31 * result + (tweetCountVar ?: 0)
        result = 31 * result + friends.hashCode()
        result = 31 * result + (friendsVar ?: 0)
        result = 31 * result + followers.hashCode()
        result = 31 * result + (followersVar ?: 0)
        result = 31 * result + date.hashCode()
        return result
    }
}

internal class SimpleUser {
    var id: Long = -1
    var screenName: String? = null

    companion object CREATOR {
        fun createFromUser(user: User): SimpleUser {
            val me = SimpleUser()
            me.id = user.id
            me.screenName = me.screenName
            return me
        }
    }

    override fun equals(other: Any?): Boolean {
        return if (other is SimpleUser) this.id == other.id else super.equals(other)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (screenName?.hashCode() ?: 0)
        return result
    }
}