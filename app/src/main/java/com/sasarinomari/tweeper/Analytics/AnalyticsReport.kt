package com.sasarinomari.tweeper.Analytics

import com.sasarinomari.tweeper.SimplizatedClass.User
import java.util.*
import kotlin.collections.ArrayList

internal class AnalyticsReport {
    constructor()
    constructor(me: twitter4j.User,
                followings: List<twitter4j.User>,
                followers: List<twitter4j.User>,
                previousReport: AnalyticsReport? = null) {
        this.date = Date()
        this.userId = me.id
        this.tweetCount = me.statusesCount
        for(user in followings) this.followings.add(User(user))
        for(user in followers) this.followers.add(User(user))
        if(previousReport!=null) setDeffrence(previousReport)
    }

    companion object {
        const val prefix = "analyticsReport"
    }

    var id: Int = -1

    var date = Date(0)
    var userId: Long = -1
    var tweetCount: Int = -1
    var followings = ArrayList<User>()
    var followers = ArrayList<User>()

    var tweetCountVar: Int? = null
    var followingsVar: Int? = null
    var followersVar: Int? = null

    fun setDeffrence(previousReport: AnalyticsReport) {
        this.tweetCountVar = this.tweetCount - previousReport.tweetCount
        this.followingsVar = this.followings.count() - previousReport.followings.count()
        this.followersVar = this.followers.count() - previousReport.followers.count()
    }

    override operator fun equals(other: Any?): Boolean {
        return if (other is AnalyticsReport) this.userId == other.userId else super.equals(other)
    }

    override fun hashCode(): Int {
        return userId.hashCode()
    }
}