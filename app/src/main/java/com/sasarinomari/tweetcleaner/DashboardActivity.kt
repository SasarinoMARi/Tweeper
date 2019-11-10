package com.sasarinomari.tweetcleaner

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import java.io.IOException
import android.os.Environment.getExternalStorageDirectory
import com.sasarinomari.tweetcleaner.hetzer.HetzerActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_dashboard.*


class DashboardActivity : Adam() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        loadUserInformation()
        button_uiTest.setOnClickListener{
            startActivity(Intent(this, UITestActivity::class.java))
        }
        button_erase.setOnClickListener {
            startActivity(Intent(this, HetzerActivity::class.java))
        }
    }

    private fun loadUserInformation() {
        Thread(Runnable {
            val twitter = TwitterFactory.getSingleton()
            val me = twitter.showUser(twitter.id)
            runOnUiThread {
                text_Name.text = me.name
                text_ScreenName.text = me.screenName
                Picasso.get() // Todo: Resize 작업 필요할 지 확인하기
                    .load(me.biggerProfileImageURL)
                    .into(image_profilePicture)
            }
        }).start()
    }
}
