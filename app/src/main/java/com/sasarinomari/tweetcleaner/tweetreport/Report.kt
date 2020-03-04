package com.sasarinomari.tweetcleaner.tweetreport

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sasarinomari.tweetcleaner.SharedTwitterProperties
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

    internal class Recorder(private val context: Context) {
        private var prefId = "record"

        @SuppressLint("CommitPrefEdits")
        fun attachReport(report: Report) {
            val prefs = context.getSharedPreferences(prefId, Context.MODE_PRIVATE).edit()
            val reps = getReports()
            reps.add(0, report)
            val json = Gson().toJson(reps)
            prefs.putString(getKey(), json)
            prefs.apply()
        }

        fun getReports(): ArrayList<Report> {
            val prefs = context.getSharedPreferences(prefId, Context.MODE_PRIVATE)
            val json = prefs.getString(getKey(), null) ?: return ArrayList()
            val type = object : TypeToken<ArrayList<Report>>() {}.type
            return Gson().fromJson(json, type)
        }

        private fun getKey() : String {
            return "report" + SharedTwitterProperties.instance().id
        }
    }
}