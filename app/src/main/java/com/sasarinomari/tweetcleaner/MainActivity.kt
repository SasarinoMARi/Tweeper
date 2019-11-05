package com.sasarinomari.tweetcleaner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import twitter4j.auth.OAuth2Token
import twitter4j.auth.RequestToken
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import android.content.Intent
import android.net.Uri


class MainActivity : AppCompatActivity() {

    private var requestToken: RequestToken? = null
    private var accessToken: AccessToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        main()
        startActivity(Intent(this, AuthenticationActivity::class.java))
    }

    private fun main() {
        setContentView(R.layout.activity_main)
        button_enterpin.setOnClickListener {
            Thread(Runnable {
                val pin = text_pin.text.toString()
                try {
                    accessToken = if (pin.isNotEmpty()) {
                        TwitterFactory.getSingleton().getOAuthAccessToken(requestToken, pin)
                    } else {
                        TwitterFactory.getSingleton().getOAuthAccessToken(requestToken)
                    }
                    SystemPreference.AccessToken.set(this, accessToken!!.token)
                    SystemPreference.AccessTokenSecret.set(this, accessToken!!.tokenSecret)
                } catch (te: TwitterException) {
                    if (401 == te.statusCode) {
                        System.out.println("Unable to get the access token.")
                    } else {
                        te.printStackTrace()
                    }
                }
            }).start()
        }
        button_req_tweet.setOnClickListener {
            Thread(Runnable {
                updateTweet("OAuth and Tweet update Test (via Twitter4j)")
            }).start()
        }
    }

    private fun updateTweet(text: String) {
        try {
            val twitter = TwitterFactory.getSingleton()
            val status = twitter.updateStatus(text)
            System.out.println("Successfully updated the status to [" + status.text + "].")
            System.exit(0)
        } catch (te: TwitterException) {
            te.printStackTrace()
            System.out.println("Failed to get timeline: " + te.message)
            System.exit(-1)
        } catch (ioe: IOException) {
            ioe.printStackTrace()
            System.out.println("Failed to read the system input.")
            System.exit(-1)
        }
    }
}
