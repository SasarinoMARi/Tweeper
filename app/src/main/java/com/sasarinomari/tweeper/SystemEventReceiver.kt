package com.sasarinomari.tweeper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sasarinomari.tweeper.Analytics.AnalyticsActivity
import com.sasarinomari.tweeper.Analytics.AnalyticsNotificationReceiver

class SystemEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action){
            Intent.ACTION_BOOT_COMPLETED -> {
                /**
                 * 재부팅됨
                 */
                Log.d("SystemEventReceiver", "단말기 재부팅됨!")
                val analyticsScheduled = AnalyticsNotificationReceiver.isApplied(context)
                AnalyticsNotificationReceiver.apply(context, analyticsScheduled)
            }
        }
    }
}