package com.sasarinomari.tweetcleaner.tweetreport

import android.content.Context
import com.sasarinomari.tweetcleaner.SharedUserProperties
import twitter4j.User
import java.util.*
import kotlin.collections.ArrayList

internal class TweetReport(context: Context) {
    private val rec = ReportRecorder(context)

    fun start() {
        fetchUserData()
    }

    private fun fetchUserData() {
        SharedUserProperties.getMe { me ->
            SharedUserProperties.getFollowers { fw ->
                SharedUserProperties.getFriends { fr ->
                    writeReport(me, fr, fw)
                }
            }
        }
    }

    private fun writeReport(me: User, fr: List<User>, fw: List<User>) {
        val recentRep = getReports()[0]

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
        report.tweetCountVar = report.tweetCount - recentRep.tweetCount
        report.friendsVar = report.friends.count() - recentRep.friends.count()
        report.followersVar = report.followers.count() - recentRep.followers.count()

        rec.attachReport(report)
    }

    fun getReports(): ArrayList<Report> {
        return rec.getReports()
    }
}