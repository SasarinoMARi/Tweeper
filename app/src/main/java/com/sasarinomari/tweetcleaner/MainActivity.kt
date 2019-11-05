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
        startActivity(Intent(this, AuthenticationActivity::class.java))
        main()
    }

    private fun main() {
        setContentView(R.layout.activity_main)
        button_tweet.setOnClickListener {
            val text = text_tweet.text.toString()
            Thread(Runnable {
                updateTweet(text)
            }).start()
        }
    }

    private fun updateTweet(text: String) {
        try {
            val twitter = TwitterFactory.getSingleton()
            val status = twitter.updateStatus(text)
        } catch (te: TwitterException) {
            te.printStackTrace()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
    }
}
