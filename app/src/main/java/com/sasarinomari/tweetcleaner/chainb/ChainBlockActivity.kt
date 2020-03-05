package com.sasarinomari.tweetcleaner.chainb

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import twitter4j.api.FriendsFollowersResources
import java.text.DecimalFormat

class ChainBlockActivity : Adam(), SharedTwitterProperties.ActivityInterface {
    private var pd: SweetAlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorWarning)
        setContentView(R.layout.activity_chain_block)

        phase1()
    }

    // 타깃 유저 지정 단계
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
            lookup(screenN) { user ->
                runOnUiThread {
                    pd?.dismissWithAnimation()
                    if (user.isProtected) {
                        input_ScreenName.text = null
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getString(R.string.Error))
                            .setContentText(getString(R.string.UserProtected))
                            .show()
                    } else {
                        phase2(user)
                    }
                }
            }

        }
    }

    // 타깃 유저 확인 단계
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
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("twitter://user?screen_name=${user.screenName}")
                    )
                )
            } catch (e: Exception) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://twitter.com/#!/${user.screenName}")
                    )
                )
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

    // 팔로잉 체인블락 단계
    private fun phase3(user: User) {
        layout_first.visibility = View.GONE
        layout_second.visibility = View.GONE

        pd = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pd?.contentText = getString(R.string.FriendPulling)
        pd?.setCancelable(false)
        pd?.show()

        Thread(Runnable {
            val twitter = SharedTwitterProperties.instance()
            val list = ArrayList<Long>()
            var cursor: Long = -1
            try {
                while (true) {
                    val users = twitter.getFriendsIDs(user.id, cursor, 5000)
                    list.addAll(users.iDs.toList())
                    if (users.hasNext()) cursor = users.nextCursor
                    else break
                }

                runOnUiThread {
                    pd?.dismissWithAnimation()
                    blockUsers(list) {
                        phase4(user)
                    }
                }
            } catch (te: TwitterException) {
                te.printStackTrace()
                val d2 = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.Error))
                    .setContentText(getString(R.string.NotCompletely))
                d2.setOnDismissListener {
                    runOnUiThread {
                        pd?.dismissWithAnimation()
                        blockUsers(list) {
                            phase4(user)
                        }
                    }
                }
                d2.show()

            }
        }).start()
    }

    // 팔로워 체인블락 단계
    private fun phase4(user: User) {
        runOnUiThread {
            pd = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            pd?.contentText = getString(R.string.FollowerPulling)
            pd?.setCancelable(false)
            pd?.show()
        }

        Thread(Runnable {
            val list = ArrayList<Long>()
            var cursor: Long = -1
            try {
                while (true) {
                    val users = SharedTwitterProperties.instance().getFollowersIDs(user.id, cursor, 5000)
                    list.addAll(users.iDs.toList())
                    if (users.hasNext()) cursor = users.nextCursor
                    else break
                }

                runOnUiThread {
                    pd?.dismissWithAnimation()
                    blockUsers(list) {
                        phase5()
                    }
                }
            } catch (te: TwitterException) {
                te.printStackTrace()
                val d2 = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.Error))
                    .setContentText(getString(R.string.NotCompletely))
                d2.setOnDismissListener {
                    runOnUiThread {
                        pd?.dismissWithAnimation()
                        blockUsers(list) {
                            phase5()
                        }
                    }
                }
                d2.show()

            }
        }).start()
    }

    // 마무리 단계
    private fun phase5() {
        runOnUiThread {
            val d = SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(getString(R.string.Done))
                .setContentText(getString(R.string.ChainBlockDone))
            d.setOnDismissListener {
                finish()
            }
            d.show()
        }
    }

    private fun blockUsers(list: ArrayList<Long>, callback: () -> Unit) {
        val d = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        d.contentText = getString(R.string.ChainBlockProcessing)
        d.setCancelable(false)
        d.show()

        Thread(Runnable {
            val twitter = SharedTwitterProperties.instance()
            for (u in list) {
                twitter.createBlock(u)
            }
            runOnUiThread {
                d.dismissWithAnimation()
            }
            callback()
        }).start()
    }

    private fun lookup(screenName: String, callback: (User) -> Unit) {
        Thread(Runnable {
            try {
                val user = SharedTwitterProperties.instance().showUser(screenName)
                callback(user)
            } catch (te: TwitterException) {
                te.printStackTrace()
                when (te.errorCode) {
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
                    else -> {
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
