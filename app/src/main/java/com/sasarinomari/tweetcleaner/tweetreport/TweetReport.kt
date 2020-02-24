package com.sasarinomari.tweetcleaner.tweetreport

import android.content.Context
import com.sasarinomari.tweetcleaner.SharedUserProperties
import twitter4j.User
import java.util.*
import kotlin.collections.ArrayList

internal class TweetReport(private val context: Context) {

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

        rec.attachReport(report)
    }

    fun getReports(): ArrayList<Report> {
        return rec.getReports()
    }
}