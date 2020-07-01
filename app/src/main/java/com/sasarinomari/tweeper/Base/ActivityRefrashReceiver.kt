package com.sasarinomari.tweeper.Base

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ActivityRefrashReceiver(private val activity: Activity): BroadcastReceiver() {
    companion object {
        const val eventName = "Tweeper_Refrash_Activity"
    }
    enum class Parameters {
        Target
    }

    override fun onReceive(p0: Context?, p1: Intent?) {
        val target = p1!!.getStringExtra(Parameters.Target.name)
        if(target == activity::class.java.name) activity.recreate()
    }
}