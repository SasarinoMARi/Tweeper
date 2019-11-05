package com.sasarinomari.tweetcleaner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import android.content.Intent
import android.os.LocaleList
import com.sasarinomari.tweetcleaner.hetzer.HetzerActivity
import twitter4j.*


class MainActivity : AppCompatActivity() {

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
        button_tl.setOnClickListener {
            startActivity(Intent(this, HetzerActivity::class.java))
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
