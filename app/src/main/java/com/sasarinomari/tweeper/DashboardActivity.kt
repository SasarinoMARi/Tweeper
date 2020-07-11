package com.sasarinomari.tweeper

import android.content.Intent
import android.os.Bundle
import com.sasarinomari.tweeper.Hetzer.HetzerActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_dashboard.*
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.ShapeDrawable
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.Authenticate.TokenManagementActivity
import com.sasarinomari.tweeper.ChainBlock.BlockClearActivity
import com.sasarinomari.tweeper.ChainBlock.ChainBlockActivity
import com.sasarinomari.tweeper.Analytics.AnalyticsActivity
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.Billing.BillingActivity
import com.sasarinomari.tweeper.SimplizatedClass.User
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.Circle
import kotlinx.android.synthetic.main.fragment_spotlight.view.*
import twitter4j.TwitterFactory

class DashboardActivity : BaseActivity(), SharedTwitterProperties.ActivityInterface {
    enum class Requests {
        Switch, Hetzer
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        loadUserInformation()
        image_profilePicture.setOnClickListener {
            startActivityForResult(Intent(this, TokenManagementActivity::class.java), Requests.Switch.ordinal)
        }
        button_erase.setOnClickListener {
            startActivityForResult(Intent(this, HetzerActivity::class.java), Requests.Hetzer.ordinal)
        }
        button_tweetReport.setOnClickListener {
            startActivity(Intent(this, AnalyticsActivity::class.java))
        }
        button_removeFriends.setOnClickListener {
            startActivity(Intent(this, ChainBlockActivity::class.java))
        }
        button_blockClear.setOnClickListener {
            startActivity(Intent(this, BlockClearActivity::class.java))
        }
        button_billing.setOnClickListener {
            startActivity(Intent(this, BillingActivity::class.java))
        }
        if(BuildConfig.DEBUG) {
            button_gotoTest.setOnClickListener {
                startActivity(Intent(this, UITestActivity::class.java))
            }
        }
        else {
            button_gotoTest.visibility = View.GONE
        }

        image_profilePicture.background = ShapeDrawable(OvalShape())
        image_profilePicture.clipToOutline = true

        initAds()
        Thread {
            Thread.sleep(100)
            runOnUiThread {
                showTips()
            }
        }.start()
    }


    private fun showTips() {
        val targets = ArrayList<Target>()
        val firstRoot = FrameLayout(this)
        val first = layoutInflater.inflate(R.layout.fragment_spotlight, firstRoot)
        val firstTarget = Target.Builder()
            .setAnchor(image_profilePicture)
            .setShape(Circle(100f))
            .setOverlay(first)
            .build()

        targets.add(firstTarget)

        val spotlight = Spotlight.Builder(this)
            .setTargets(targets)
            .setBackgroundColor(R.color.spotlightBackground)
            .setDuration(1000L)
            .setAnimation(DecelerateInterpolator(2f))
            .build()

        spotlight.start()

        first.column_title.text = "계정 변경하기"
        first.column_description.text = "이곳을 클릭해서 트윗지기에 로그인한 계정을 바꿀 수 있어요!"
        first.isClickable = true
        first.setOnClickListener {
            spotlight.finish()
        }
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
            Requests.Switch.ordinal -> {
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
            Requests.Hetzer.ordinal -> {
                if(resultCode == RESULT_OK) {
                    da.message(getString(R.string.Done), "트윗 청소기가 백그라운드에서 실행됩니다..").show()
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
        SharedTwitterProperties.getMe(this) {
            // 포커스 변경 먼저 반영하고 유저 갱신 후 다시 저장
            authData.user = User(it) 
            AuthData.Recorder(this).setFocusedUser(authData)
        }
        setResult(RESULT_OK)
    }

    override fun onRateLimit(apiPoint: String) {
        runOnUiThread {
            da.error(getString(R.string.Error), getString(R.string.RateLimitError, apiPoint)).show()
        }
    }
}
