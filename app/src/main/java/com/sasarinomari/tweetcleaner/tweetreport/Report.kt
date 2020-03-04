package com.sasarinomari.tweetcleaner.tweetreport

import com.sasarinomari.tweetcleaner.SimpleUser
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