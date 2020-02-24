package com.sasarinomari.tweetcleaner.tweetreport

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal class ReportRecorder(private val context: Context) {
    var prefId = "record"
    var KEY_report = "report"

    @SuppressLint("CommitPrefEdits")
    fun attachReport(report: Report) {
        val prefs = context.getSharedPreferences(prefId, Context.MODE_PRIVATE).edit()
        val reps = getReports()
        reps.add(report)
        val json = Gson().toJson(reps)
        prefs.putString(KEY_report, json)
        prefs.apply()
    }

    fun getReports(): ArrayList<Report> {
        val prefs = context.getSharedPreferences(prefId, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_report, null) ?: return ArrayList()
        val type = object : TypeToken<ArrayList<Report>>() {}.type
        return Gson().fromJson(json, type)
    }
}