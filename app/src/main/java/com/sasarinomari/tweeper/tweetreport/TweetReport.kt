package com.sasarinomari.tweeper.tweetreport

import android.content.Context
import com.sasarinomari.tweeper.SharedTwitterProperties
import com.sasarinomari.tweeper.SimpleUser
import twitter4j.User
import java.util.*

internal class TweetReport(context: Context,
                           private val ai: ActivityInterface) {
    private val rec = Report.Recorder(context)

    fun start() {
        fetchUserData()
    }

    private fun fetchUserData() {
        val ai = object : SharedTwitterProperties.ActivityInterface {
            override fun onRateLimit(apiPoint: String) {
                ai.onRateLimit(apiPoint)
            }
        }
        SharedTwitterProperties.getMe(ai) { me ->
            SharedTwitterProperties.getFollowers(ai) { fw ->
                SharedTwitterProperties.getFriends(ai) { fr ->
                    writeReport(me, fr, fw)
                }
            }
        }
    }

    private fun writeReport(me: User, fr: List<User>, fw: List<User>) {
        val report = Report()
        report.userId = me.id
        report.date = Date()
        report.tweetCount = me.statusesCount
        for (i in fr) {
            report.friends.add(SimpleUser.createFromUser(i))
        }
        for (i in fw) {
            report.followers.add(SimpleUser.createFromUser(i))
        }

        // ㅊㅇ
        val reps = getReports()
        if(reps.isNotEmpty()) {
            val recentRep = reps[0]
            report.tweetCountVar = report.tweetCount - recentRep.tweetCount
            report.friendsVar = report.friends.count() - recentRep.friends.count()
            report.followersVar = report.followers.count() - recentRep.followers.count()
        }

        rec.attachReport(report)
        ai.onFinished()
    }

    fun getReports(): ArrayList<Report> {
        return rec.getReports()
    }

    interface ActivityInterface {
        fun onRateLimit(apiPoint: String)
        fun onFinished()
    }
}