package com.sasarinomari.tweetcleaner.chainb

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.sasarinomari.tweetcleaner.Adam
import com.sasarinomari.tweetcleaner.R
import com.sasarinomari.tweetcleaner.SharedTwitterProperties
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chain_block.*
import twitter4j.TwitterException
import twitter4j.User
import java.text.DecimalFormat

class ChainBlockActivity : Adam(), SharedTwitterProperties.ActivityInterface {
    private var pd: SweetAlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorWarning)
        setContentView(R.layout.activity_chain_block)

        phase1()
    }

    private fun phase1() {
        layout_first.visibility = View.VISIBLE
        layout_second.visibility = View.GONE

        input_ScreenName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.isEmpty()) {
                    button_next.isEnabled = false
                    button_next.drawable.alpha = 128
                } else {
                    button_next.isEnabled = true
                    button_next.drawable.alpha = 255
                }
            }
        })
        button_next.isEnabled = false
        button_next.drawable.alpha = 150

        button_next.setOnClickListener {
            val screenN = input_ScreenName.text.toString()

            pd = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            pd?.setCancelable(false)
            pd?.show()
            lookup(screenN) {
                runOnUiThread {
                    pd?.dismissWithAnimation()
                    phase2(it)
                }
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun phase2(user: User) {
        layout_first.visibility = View.GONE
        layout_second.visibility = View.VISIBLE

        Picasso.get()
            .load(user.biggerProfileImageURL)
            .into(image_ProfilePicture)
        text_Name.text = user.name
        text_ScreenName.text = "@${user.screenName}"
        text_Bio.text = user.description
        val df = DecimalFormat("###,###")
        text_FriendCount.text = df.format(user.friendsCount)
        text_FollowerCount.text = df.format(user.followersCount)

        text_ScreenName.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=${user.screenName}")))
            } catch (e: Exception) {
                startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://twitter.com/#!/${user.screenName}")))
            }
        }
        button_next2.setOnClickListener {
            val d = SweetAlertDialog(this@ChainBlockActivity, SweetAlertDialog.WARNING_TYPE)
                .setTitleText((R.string.AreYouSure))
                .setContentText(getString(R.string.ActionDoNotRestore))
                .setConfirmText(getString(R.string.Yes))

            d.setConfirmClickListener {
                runOnUiThread {
                    d.dismissWithAnimation()
                    phase3(user)
                }
            }

            d.show()
        }
    }

    private fun phase3(user: User) {
        layout_first.visibility = View.GONE
        layout_second.visibility = View.GONE
    }

    fun lookup(screenName: String, callback: (User) -> Unit) {
        Thread(Runnable {
            try {
                val user = SharedTwitterProperties.instance().showUser(screenName)
                callback(user)
            } catch (te: TwitterException) {
                te.printStackTrace()
                when(te.errorCode) {
                    50 -> { // User not found.
                        runOnUiThread {
                            input_ScreenName.text = null
                            pd?.dismissWithAnimation()
                            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText(getString(R.string.Error))
                                .setContentText(getString(R.string.UserNotFoundError))
                                .show()
                        }
                    }
                    else ->{
                        onRateLimit("show/user/me")
                    }
                }
            }
        }).start()
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
