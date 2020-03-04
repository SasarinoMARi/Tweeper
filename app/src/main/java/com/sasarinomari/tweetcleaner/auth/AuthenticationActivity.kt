package com.sasarinomari.tweetcleaner.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.sasarinomari.tweetcleaner.*
import kotlinx.android.synthetic.main.activity_main.*
import kr.booms.webview.BoomWebView
import kr.booms.webview.BoomWebViewClientInterface
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import twitter4j.auth.RequestToken
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.*

class AuthenticationActivity : Adam() {
    private val twitterInstance = TwitterFactory().instance
    private var webView: BoomWebView? = null
    private lateinit var requestToken: RequestToken


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeWebView()
        // Generate authentication url
        Thread(Runnable{
            SharedTwitterProperties.setOAuthConsumer(this, twitterInstance)
            requestToken = twitterInstance.oAuthRequestToken
            runOnUiThread {
                webView!!.loadUrl(requestToken.authorizationURL)
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
                                Thread(Runnable {
                                    try {
                                        val accessToken = if (pin.isNotEmpty()) {
                                            twitterInstance.getOAuthAccessToken(requestToken, pin)
                                        } else {
                                            twitterInstance.getOAuthAccessToken(requestToken)
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
        SharedTwitterProperties.clear(twitterInstance)
        SharedTwitterProperties.getMe { user ->
            val authData = AuthData()
            authData.token = accessToken
            authData.lastLogin = Date()
            authData.user = SimpleUser.createFromUser(user)

            val recorder = AuthData.Recorder(this@AuthenticationActivity)
            if(!recorder.hasUser(authData)) {
                recorder.addUser(authData)
            }
            recorder.setFocusedUser(authData)
            setResult(RESULT_OK)
            finish()
        }

    }
}
