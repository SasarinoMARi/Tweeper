package com.sasarinomari.tweeper

import android.content.Intent
import android.os.Bundle
import com.sasarinomari.tweeper.hetzer.HetzerActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_dashboard.*
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.ShapeDrawable
import android.widget.LinearLayout
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import com.sasarinomari.tweeper.auth.AuthData
import com.sasarinomari.tweeper.auth.TokenManagementActivity
import com.sasarinomari.tweeper.chainb.BlockClearActivity
import com.sasarinomari.tweeper.chainb.ChainBlockActivity
import com.sasarinomari.tweeper.fwmanage.FollowerManagement
import com.sasarinomari.tweeper.tweetreport.TweetReportActivity
import twitter4j.TwitterFactory

class DashboardActivity : Adam(), SharedTwitterProperties.ActivityInterface {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        loadUserInformation()
        image_profilePicture.setOnClickListener {
            startActivityForResult(Intent(this, TokenManagementActivity::class.java), 0)
        }
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
            startActivity(Intent(this, ChainBlockActivity::class.java))
        }
        button_blockClear.setOnClickListener {
            startActivity(Intent(this, BlockClearActivity::class.java))
        }

        image_profilePicture.background = ShapeDrawable(OvalShape())
        image_profilePicture.clipToOutline = true

        initAds()
        showTips()
    }

    private fun showTips() {
        // TODO
        // 사용 가이드 팝업 띄워주기.
        // 다시 보지 않기 설정했다면 설정에서 값을 바꾸기 전까지 띄우지 않음.
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
        text_Name.text = ""
        text_ScreenName.text = ""
        image_profilePicture.setImageResource(0)

        SharedTwitterProperties.getMe(this) { me ->
            runOnUiThread {
                text_Name.text = me.name
                text_ScreenName.text = me.screenName
                Picasso.get()
                    .load(me.profileImageURL.replace("normal.jpg", "200x200.jpg"))
                    .into(image_profilePicture)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            0 -> {
                if (resultCode == RESULT_OK) {
                    if(data!=null) {
                        if(data.hasExtra(TokenManagementActivity.RESULT_AUTH_DATA)) {
                            // 계정 스위치
                            val json = data.getStringExtra(TokenManagementActivity.RESULT_AUTH_DATA)
                            val authData = Gson().fromJson(json, AuthData::class.java)
                            setUser(authData)
                        }
                        else {
                            // 전환할 계정이 없는 상태로 종료됨
                            finish()
                            return
                        }
                    }
                    loadUserInformation()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }


    private fun setUser(authData: AuthData) {
        val newTwitter = TwitterFactory().instance
        SharedTwitterProperties.setOAuthConsumer(this, newTwitter)
        newTwitter.oAuthAccessToken = authData.token
        SharedTwitterProperties.clear(newTwitter)
        AuthData.Recorder(this).setFocusedUser(authData)
        setResult(RESULT_OK)
    }

    override fun onRateLimit(apiPoint: String) {
        runOnUiThread {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(R.string.Error))
                .setContentText(getString(R.string.RateLimitError, apiPoint))
                .show()
        }
    }
}
