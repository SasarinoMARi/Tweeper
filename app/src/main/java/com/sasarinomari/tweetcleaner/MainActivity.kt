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
        setContentView(R.layout.activity_main)

        setConsumerInfo()
        button_req_auth.setOnClickListener {
            Thread(Runnable {
                requestToken = TwitterFactory.getSingleton().oAuthRequestToken
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(requestToken!!.authorizationURL))
                startActivity(intent)
            }).start()
        }
        button_enterpin.setOnClickListener {
            Thread(Runnable {
                val pin = text_pin.text.toString()
                try {
                    accessToken = if (pin.isNotEmpty()) {
                        TwitterFactory.getSingleton().getOAuthAccessToken(requestToken, pin)
                    } else {
                        TwitterFactory.getSingleton().getOAuthAccessToken(requestToken)
                    }
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

    private fun setConsumerInfo() {
        TwitterFactory.getSingleton()
            .setOAuthConsumer(getString(R.string.consumerKey), getString(R.string.consumerSecret))
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
