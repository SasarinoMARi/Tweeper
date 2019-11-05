package com.sasarinomari.tweetcleaner

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_authentication.*
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken

class AuthenticationActivity : Adam() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setConsumerInfo()
        val accessToken = makeToken()
        if (accessToken != null) {
            TwitterFactory.getSingleton().oAuthAccessToken = accessToken
            setResult(RESULT_OK)
            finish()
        }
        setContentView(R.layout.activity_authentication)
        // Generate authentication url
        Thread(Runnable{
            val requestToken = TwitterFactory.getSingleton().oAuthRequestToken
            runOnUiThread {
                webView.loadUrl(requestToken!!.authorizationURL)
            }
        }).start()

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
