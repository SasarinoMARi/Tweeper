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
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.ShapeDrawable




class DashboardActivity : Adam() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        loadUserInformation()
        button_erase.setOnClickListener {
            startActivity(Intent(this, HetzerActivity::class.java))
        }
        button_followingManagement.setOnClickListener {
            startActivity(Intent(this, FollowingManagement::class.java))
        }
        button_followerManagement.setOnClickListener {
            startActivity(Intent(this, FollowerManagement::class.java))
        }

        image_profilePicture.background = ShapeDrawable(OvalShape())
        image_profilePicture.clipToOutline = true
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
