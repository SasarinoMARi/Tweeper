package com.sasarinomari.tweetcleaner

import android.content.Context
import android.content.SharedPreferences

enum class SystemPreference {
    AccessToken, AccessTokenSecret,
    HetzerConditions

    ;

    companion object {
        private const val preferenceName = ".system"
    }

    fun getString(context: Context): String? {
        return getPref(context).getString(name, null)
    }
    fun getBoolean(context: Context): Boolean {
        return getPref(context).getBoolean(name, false)
    }
    fun getInt(context: Context): Int {
        return getPref(context).getInt(name, -1)
    }

    fun set(context: Context, value: String) {
        val pref = getPref(context).edit()
        pref.putString(name, value)
        pref.apply()
    }
    fun set(context: Context, value: Boolean) {
        val pref = getPref(context).edit()
        pref.putBoolean(name, value)
        pref.apply()
    }

    private fun getPref(context: Context): SharedPreferences {
        return context.getSharedPreferences(context.packageName + preferenceName, 0)
    }
}