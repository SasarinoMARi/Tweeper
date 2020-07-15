package com.sasarinomari.tweeper.Billing

import android.content.Context

class AdRemover(private val context: Context) {
    private val key = "ADRemoved"

    fun isAdRemoved() : Boolean {
        val prefs = context.getSharedPreferences(key, Context.MODE_PRIVATE)
        val flag = prefs.getInt("flag", 0)
        return flag == 1
    }
    internal fun removeAd(){
        val prefs = context.getSharedPreferences(key, Context.MODE_PRIVATE).edit()
        prefs.putInt("flag", 1)
        prefs.apply()
    }
}