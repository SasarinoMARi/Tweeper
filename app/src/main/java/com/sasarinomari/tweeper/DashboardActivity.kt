package com.sasarinomari.tweeper

import android.content.Context
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
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.Authenticate.TokenManagementActivity
import com.sasarinomari.tweeper.ChainBlock.BlockClearActivity
import com.sasarinomari.tweeper.ChainBlock.ChainBlockActivity
import com.sasarinomari.tweeper.Analytics.AnalyticsActivity
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.Billing.AdRemover
import com.sasarinomari.tweeper.Billing.BillingActivity
import com.sasarinomari.tweeper.MediaDownload.MediaDownloadActivity
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.Circle
import kotlinx.android.synthetic.main.fragment_spotlight.view.*
import twitter4j.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class DashboardActivity : BaseActivity() {
    enum class Requests {
        Switch, Hetzer, Billing
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
        button_mediaDownload.setOnClickListener {
            startActivity(Intent(this, MediaDownloadActivity::class.java))
        }
        button_removeFriends.setOnClickListener {
            startActivity(Intent(this, ChainBlockActivity::class.java))
        }
        button_blockClear.setOnClickListener {
            startActivity(Intent(this, BlockClearActivity::class.java))
        }
        button_billing.setOnClickListener {
            startActivityForResult(Intent(this, BillingActivity::class.java), Requests.Billing.ordinal)
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
        if (isFirstRunThisActivity()) Thread {
            Thread.sleep(100)
            runOnUiThread {
                showTips()
            }
        }.start() else {
            showNoticeDialog()
        }
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
            .setOnSpotlightListener(object: OnSpotlightListener {
                override fun onEnded() {
                    showNoticeDialog()
                    setNotFirstrun()
                }

                override fun onStarted() { }
            })
            .build()

        spotlight.start()

        first.column_title.text = getString(R.string.tutorial_changeAccount)
        first.column_description.text = getString(R.string.tutorial_changeAccountDesc)
        first.isClickable = true
        first.setOnClickListener {
            spotlight.finish()
        }
    }

    private fun showNoticeDialog() {
        val prefs = getSharedPreferences("fr${this::class.java.name}", Context.MODE_PRIVATE)
        val shownVersion = prefs.getInt("noticeRevision", -1)

        val url = URL("https://gist.githubusercontent.com/SasarinoMARi/1f5c073492b9b07a87d75de34341c3d6/raw")
        val urlConnection = url.openConnection() as HttpURLConnection

        Thread {
            try {
                val json = urlConnection.inputStream.bufferedReader().readText()
                val obj = JSONObject(json)
                val version = obj.getInt("revision")
                if(version > shownVersion) {
                val text = obj.getString("text")
                    runOnUiThread {
                        MaterialDialog(this).show {
                            title(text = getString(R.string.Notice))
                            message(text = text)
                            positiveButton {
                                val edit = prefs.edit()
                                edit.putInt("noticeRevision", version)
                                edit.apply()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                FirebaseLogger(this).log("NoticeDownloadFailed", Pair("StackTrace", e.message!!))
            } finally {
                urlConnection.disconnect()
        }
        }.start()
    }

    private fun initAds() {
        if(AdRemover(this).isAdRemoved()) return

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

        Thread {
            TwitterAdapter().initialize(AuthData.Recorder(this).getFocusedUser()!!.token!!).getMe(object : TwitterAdapter.FetchObjectInterface {
                override fun onStart() { }

                override fun onFinished(obj: Any) {
                    val me = obj as twitter4j.User
                    runOnUiThread {
                        text_Name.text = me.name
                        text_ScreenName.text = me.screenName
                        Picasso.get()
                            .load(me.profileImageURLHttps.replace("normal.jpg", "200x200.jpg"))
                            .into(image_profilePicture)
                    }
                }

                override fun onRateLimit() {
                    da.error(getString(R.string.Error), getString(R.string.RateLimitError, "getMe")).show()
                }

            })
        }.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Requests.Switch.ordinal -> {
                if (resultCode == RESULT_OK) {
                    if(data!=null) {
                        if(data.hasExtra(TokenManagementActivity.RESULT_AUTH_DATA)) {
                            setResult(RESULT_OK)
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
                    da.message(getString(R.string.Done), getString(R.string.HetzerRunning)).show()
                }
            }
            Requests.Billing.ordinal -> {
                if(resultCode == RESULT_OK) {
                    layout_ad.visibility = View.GONE
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
