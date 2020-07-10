package com.sasarinomari.tweeper

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseLogger(context: Context) {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun log(eventName: String, vararg parameters: Pair<String, String>) {
        val bundle = Bundle()
        for(param in parameters) {
            bundle.putString(param.first, param.second)
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }
}