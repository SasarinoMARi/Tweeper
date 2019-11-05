package com.sasarinomari.tweetcleaner

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_authentication.*
import kr.booms.webview.BoomWebView
import kr.booms.webview.BoomWebViewClientInterface
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import twitter4j.auth.RequestToken
import twitter4j.conf.ConfigurationBuilder
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

class AuthenticationActivity : Adam() {

    private var webView: BoomWebView? = null

    private lateinit var requestToken: RequestToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setConsumerInfo()
        val accessToken = makeToken()
        if (accessToken != null) {
            TwitterFactory.getSingleton().oAuthAccessToken = accessToken
            setResult(RESULT_OK)
            finish()
            return
        }
        setContentView(R.layout.activity_authentication)
        initializeWebView()
        // Generate authentication url
        Thread(Runnable{
            requestToken = TwitterFactory.getSingleton().oAuthRequestToken
            runOnUiThread {
                webView!!.loadUrl(requestToken!!.authorizationURL)
            }
        }).start()
    }

    private fun initializeWebView() {
        webView = BoomWebView.createWithContext(Content, "Sasarino", { _, _ -> },
            object : BoomWebViewClientInterface {
                override fun onPageFinished(url: String) {
                    when (url) {
                        "https://api.twitter.com/oauth/authorize" -> {
                            webView!!.loadUrl("javascript:this.document.location.href = 'source://' + encodeURI(document.documentElement.outerHTML);")
                        }
                    }
                }

                override fun shouldOverrideUrlLoading(url: String): Boolean {
                    return when {
                        url.startsWith("source://") -> {
                            try {
                                Content.visibility = View.GONE
                                val html = URLDecoder.decode(url, "UTF-8").substring(9)
                                val pin = StringFormatter.extractionString(html, "<code>", "</code>")
                                Thread(Runnable {
                                    try {
                                        val accessToken = if (pin.isNotEmpty()) {
                                            TwitterFactory.getSingleton().getOAuthAccessToken(requestToken, pin)
                                        } else {
                                            TwitterFactory.getSingleton().getOAuthAccessToken(requestToken)
                                        }
                                        accessToken!!
                                        SystemPreference.AccessToken.set(this@AuthenticationActivity, accessToken.token)
                                        SystemPreference.AccessTokenSecret.set(this@AuthenticationActivity, accessToken.tokenSecret)
                                        setResult(RESULT_OK)
                                        finish()
                                    } catch (te: TwitterException) {
                                        if (401 == te.statusCode) {
                                            Toast.makeText(this@AuthenticationActivity,
                                                "Unable to get the access token.",
                                                Toast.LENGTH_LONG).show()
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

    private fun makeToken(): AccessToken? {
        val token = SystemPreference.AccessToken.getString(this)
        val secret = SystemPreference.AccessTokenSecret.getString(this)
        return if (token == null || secret == null) null
        else AccessToken(token, secret)
    }

    private fun setConsumerInfo() {
        TwitterFactory.getSingleton()
            .setOAuthConsumer(getString(R.string.consumerKey), getString(R.string.consumerSecret))
    }
}
