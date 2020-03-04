package com.sasarinomari.tweetcleaner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import com.sasarinomari.tweetcleaner.auth.AuthData
import com.sasarinomari.tweetcleaner.auth.TokenManagementActivity
import twitter4j.TwitterFactory
import java.lang.Exception


class MainActivity : Adam() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SharedTwitterProperties.clear(TwitterFactory().instance)
        SharedTwitterProperties.setOAuthConsumer(this, SharedTwitterProperties.instance())
        when (val loggedUser = AuthData.Recorder(this).getFocusedUser()) {
            null -> {
                doAuth()
            }
            else -> {
                SharedTwitterProperties.instance().oAuthAccessToken = loggedUser.token!!
                try {
                    SharedTwitterProperties.getMe {
                        openDashboard()
                        finish()
                    }
                } catch (e: Exception) {
                    doAuth()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            0 -> {
                if(resultCode == RESULT_OK) {
                    openDashboard()
                }
                finish()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun openDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
    }

    private fun doAuth() {
        startActivityForResult(Intent(this, TokenManagementActivity::class.java), 0)
    }
}
