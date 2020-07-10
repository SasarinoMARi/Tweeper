package com.sasarinomari.tweeper

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class RewardedAdAdapter {
    interface RewardInterface {
        fun onFinished()
    }

    companion object {
        private const val LOG_TAG = "RewardedAdAdapter"
        private var rewardedAd: RewardedAd? = null

        fun load(context: Context) {
            rewardedAd = RewardedAd(
                context, context.getString(
                    if (BuildConfig.DEBUG) R.string.reward_ad_unit_id_for_test
                    else R.string.reward_ad_unit_id
                )
            )

            val adLoadCallback = object : RewardedAdLoadCallback() {
                override fun onRewardedAdLoaded() {
                    Log.i(LOG_TAG, "onRewardedAdLoaded")
                }

                override fun onRewardedAdFailedToLoad(errorCode: Int) {
                    Log.i(LOG_TAG, "onRewardedAdFailedToLoad $errorCode")
                    FirebaseLogger(context).log("onRewardedAdFailedToLoad",
                        Pair("ErrorCode", errorCode.toString()))
                }
            }
            rewardedAd!!.loadAd(AdRequest.Builder().build(), adLoadCallback)
        }

        fun show(activity: Activity, ri: RewardInterface) {
            when {
                rewardedAd != null && rewardedAd!!.isLoaded -> {
                    val adCallback = object : RewardedAdCallback() {
                        override fun onRewardedAdOpened() {
                            Log.i(LOG_TAG, "onRewardedAdOpened")
                        }

                        override fun onRewardedAdClosed() {
                            Log.i(LOG_TAG, "onRewardedAdClosed")
                            ri.onFinished()
                        }

                        override fun onUserEarnedReward(reward: com.google.android.gms.ads.rewarded.RewardItem) {
                            Log.i(LOG_TAG, "onUserEarnedReward ${reward.type}")
                        }

                        override fun onRewardedAdFailedToShow(errorCode: Int) {
                            Log.i(LOG_TAG, "onRewardedAdFailedToShow $errorCode")
                            FirebaseLogger(activity).log("onRewardedAdFailedToShow",
                                Pair("ErrorCode", errorCode.toString()))
                        }
                    }
                    rewardedAd!!.show(activity, adCallback)
                }
                else -> {
                    Log.d(LOG_TAG, "The rewarded ad wasn't loaded yet.")
                    ri.onFinished()
                }
            }
        }
    }
}