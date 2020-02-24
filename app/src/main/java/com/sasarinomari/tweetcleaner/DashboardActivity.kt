package com.sasarinomari.tweetcleaner

import android.content.Intent
import android.os.Bundle
import com.sasarinomari.tweetcleaner.hetzer.HetzerActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_dashboard.*
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.ShapeDrawable
import android.widget.LinearLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.sasarinomari.tweetcleaner.danger.RemoveFriends
import com.sasarinomari.tweetcleaner.tweetreport.TweetReportActivity

class DashboardActivity : Adam() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        loadUserInformation()
        button_erase.setOnClickListener {
            startActivity(Intent(this, HetzerActivity::class.java))
        }
        button_followerManagement.setOnClickListener {
            startActivity(Intent(this, FollowerManagement::class.java))
        }
        button_tweetReport.setOnClickListener {
            startActivity(Intent(this, TweetReportActivity::class.java))
        }

        button_removeFriends.setOnClickListener {
            startActivity(Intent(this, RemoveFriends::class.java))
        }

        image_profilePicture.background = ShapeDrawable(OvalShape())
        image_profilePicture.clipToOutline = true

        initAds()
    }

    private fun initAds() {
        MobileAds.initialize(this)
        val adView = AdView(this)
        layout_ad.addView(adView)
        adView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        adView.adSize = AdSize.BANNER
        adView.adUnitId = if (BuildConfig.DEBUG) getString(R.string.banner_ad_unit_id_for_test)
        else getString(R.string.banner_ad_unit_id)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun loadUserInformation() {
        SharedUserProperties.getMe { me ->
            runOnUiThread {
                text_Name.text = me.name
                text_ScreenName.text = me.screenName
                Picasso.get()
                    .load(me.profileImageURL.replace("normal.jpg", "200x200.jpg"))
                    .into(image_profilePicture)
            }
        }
    }
}
