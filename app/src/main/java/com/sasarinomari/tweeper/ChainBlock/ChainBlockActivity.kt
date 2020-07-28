package com.sasarinomari.tweeper.ChainBlock

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.RewardedAdAdapter
import com.sasarinomari.tweeper.TwitterAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chain_block.*
import twitter4j.User
import java.text.DecimalFormat

class ChainBlockActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.warning)
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
            if (ChainBlockService.checkServiceRunning((this@ChainBlockActivity))) {
                da.warning(getString(R.string.Wait), getString(R.string.duplicateService_ChainBlock)).show()
                return@setOnClickListener
            }

            val screenN = input_ScreenName.text.toString()
            lookupTarget(screenN)
        }
    }

    private fun lookupTarget(screenN: String) {
        runOnUiThread {
            val p = da.progress(null, getString(R.string.UserFetching))
            p.show()
            Thread {
                TwitterAdapter().initialize(AuthData.Recorder(this@ChainBlockActivity).getFocusedUser()!!.token!!)
                    .lookup(screenN, object : TwitterAdapter.FoundObjectInterface {
                        override fun onStart() {}

                        override fun onFinished(obj: Any) {
                            val user = obj as User
                            runOnUiThread {
                                p.dismissWithAnimation()
                                if (user.isProtected) {
                                    input_ScreenName.text = null
                                    da.error(getString(R.string.Error), getString(R.string.UserProtected)).show()
                                } else {
                                    phase2(user)
                                }
                            }
                        }

                        override fun onRateLimit() {
                            this@ChainBlockActivity.onRateLimit("lookup")
                        }

                        override fun onNotFound() {
                            runOnUiThread {
                                p.dismissWithAnimation()
                                input_ScreenName.text = null
                                da.error(getString(R.string.Error), getString(R.string.UserNotFoundError)).show()
                            }
                        }

                        override fun onUncaughtError() {
                            this@ChainBlockActivity.onUncaughtError()
                        }

                        override fun onNetworkError() {
                            this@ChainBlockActivity.onNetworkError {
                                lookupTarget(screenN)
                            }
                        }
                    })
            }.start()
        }
    }

    // 타깃 유저 확인 단계
    @SuppressLint("SetTextI18n")
    private fun phase2(user: User) {
        layout_first.visibility = View.GONE
        layout_second.visibility = View.VISIBLE

        Picasso.get()
            .load(user.biggerProfileImageURLHttps)
            .into(image_ProfilePicture)
        text_Name.text = user.name
        text_ScreenName.text = "@${user.screenName}"
        text_Bio.text = user.description
        val df = DecimalFormat("###,###")
        text_FriendCount.text = df.format(user.friendsCount)
        text_FollowerCount.text = df.format(user.followersCount)

        layout_following.setOnClickListener { checkbox_following.isChecked = !checkbox_following.isChecked }
        layout_followers.setOnClickListener { checkbox_followers.isChecked = !checkbox_followers.isChecked }
        layout_ignoreMyFollowing.setOnClickListener { checkbox_ignoremyFollowing.isChecked = !checkbox_ignoremyFollowing.isChecked }

        text_ScreenName.setOnClickListener {
            TwitterAdapter.showProfile(this@ChainBlockActivity, user.screenName)
        }
        button_next2.setOnClickListener {
            if(!checkbox_following.isChecked && !checkbox_followers.isChecked) {
                da.error(getString(R.string.Error), getString(R.string.SelectMoreThanOne)).show()
                return@setOnClickListener
            }
            da.warning(getString(R.string.AreYouSure), getString(R.string.ActionDoNotRestore))
                .setConfirmText(getString(R.string.Yes))
                .setCancelText(getString(R.string.Wait))
                .setConfirmClickListener {
                    it.dismissWithAnimation()
                    RewardedAdAdapter.show(this, object: RewardedAdAdapter.RewardInterface {
                        override fun onFinished() {
                            val intent = Intent(this@ChainBlockActivity, ChainBlockService::class.java)
                            intent.putExtra(ChainBlockService.Parameters.TargetId.name, user.id)
                            intent.putExtra(ChainBlockService.Parameters.BlockFollowing.name, checkbox_following.isChecked)
                            intent.putExtra(ChainBlockService.Parameters.BlockFollower.name, checkbox_followers.isChecked)
                            intent.putExtra(ChainBlockService.Parameters.IgnoreMyFollowing.name, checkbox_ignoremyFollowing.isChecked)
                            intent.putExtra(
                                ChainBlockService.Parameters.User.name,
                                Gson().toJson(AuthData.Recorder(this@ChainBlockActivity).getFocusedUser()!!))
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(intent)
                            } else {
                                startService(intent)
                            }
                            da.success(getString(R.string.Done), getString(R.string.ChainBlockRunning))
                                .setConfirmClickListener { it2 ->
                                    it2.dismissWithAnimation()
                                    finish()
                                }.show()
                        }
                    })
                }.show()
        }
    }

}
