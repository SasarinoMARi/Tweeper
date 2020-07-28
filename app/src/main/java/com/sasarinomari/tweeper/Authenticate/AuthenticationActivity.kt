package com.sasarinomari.tweeper.Authenticate

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.sasarinomari.tweeper.*
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.SimplizatedClass.User
import kotlinx.android.synthetic.main.activity_main.*
import kr.booms.webview.BoomWebView
import kr.booms.webview.BoomWebViewClientInterface
import twitter4j.TwitterException
import twitter4j.auth.AccessToken
import twitter4j.auth.RequestToken
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.*

class AuthenticationActivity : BaseActivity() {
    private val twitterAdapter = TwitterAdapter()
    private var webView: BoomWebView? = null
    private lateinit var requestToken: RequestToken


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeWebView()
        // Generate authentication url
        twitterAdapter.twitter.initialize()
        openAuthPage()
    }

    private fun openAuthPage() {
        Thread(Runnable{
            try {
                requestToken = twitterAdapter.twitter.client.oAuthRequestToken
                runOnUiThread {
                    webView!!.loadUrl(requestToken.authorizationURL)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                this@AuthenticationActivity.onNetworkError {
                    openAuthPage()
                }
            }
        }).start()
    }

    private fun initializeWebView() {
        webView = BoomWebView.createWithContext(Content, "Sasarino", { _, _ -> },
            object : BoomWebViewClientInterface {
                override fun onPageFinished(url: String) {
                    if (url=="https://api.twitter.com/oauth/authorize" ||
                        url == "https://twitter.com/oauth/authorize") {
                            webView!!.loadUrl("javascript:this.document.location.href = 'source://' + encodeURI(document.documentElement.outerHTML);")
                    }
                    else {
                        Content.visibility = View.VISIBLE
                    }
                }

                override fun shouldOverrideUrlLoading(url: String): Boolean {
                    return when {
                        url.startsWith("source://") -> {
                            try {
                                Content.visibility = View.GONE
                                val html = URLDecoder.decode(url, "UTF-8").substring(9)
                                val pin = StringFormatter.extractionString( html, "<code>", "</code>" )
                                if(pin == null) {
                                    da.message(getString(R.string.AuthFailed), getString(R.string.AgreeForTweeper)) {
                                        finish()
                                    }.show()
                                }
                                else Thread(Runnable {
                                    try {
                                        val accessToken = if (!pin.isNullOrEmpty()) {
                                            twitterAdapter.twitter.client.getOAuthAccessToken(requestToken, pin)
                                        } else {
                                            twitterAdapter.twitter.client.getOAuthAccessToken(requestToken)
                                        }
                                        apiTest(accessToken)
                                    } catch (te: TwitterException) {
                                        if (401 == te.statusCode) {
                                            Toast.makeText(this@AuthenticationActivity, "Unable to get the access token.", Toast.LENGTH_LONG).show()
                                        } else {
                                            te.printStackTrace()
                                        }
                                    }
                                }).start()
                            } catch (e: UnsupportedEncodingException) {
                                e.printStackTrace()
                            }
                            true
                        }
                        else -> false
                    }
                }
            })
    }

    private fun apiTest(accessToken: AccessToken) {
        twitterAdapter.initialize(accessToken)
        Thread {
            twitterAdapter.getMe(object : TwitterAdapter.FetchObjectInterface {
                override fun onStart() {}

                override fun onFinished(obj: Any) {
                    val user = obj as twitter4j.User
                    val authData = AuthData()
                    authData.token = accessToken
                    authData.lastLogin = Date()
                    authData.user = User(user)

                    val recorder = AuthData.Recorder(this@AuthenticationActivity)
                    if (!recorder.hasUser(authData)) {
                        recorder.addUser(authData)
                    }
                    recorder.setFocusedUser(authData)
                    setResult(RESULT_OK)
                    finish()
                }

                override fun onRateLimit() {
                    this@AuthenticationActivity.onRateLimit("getMe") {
                        finish()
                    }
                }


                override fun onUncaughtError() {
                    this@AuthenticationActivity.onUncaughtError()
                }

                override fun onNetworkError() {
                    this@AuthenticationActivity.onNetworkError {
                        apiTest(accessToken)
                    }
                }
            })
        }.start()
    }
}
