package com.sasarinomari.tweeper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import cn.pedant.SweetAlert.SweetAlertDialog
import com.sasarinomari.tweeper.auth.AuthData
import com.sasarinomari.tweeper.auth.TokenManagementActivity
import twitter4j.TwitterFactory
import java.lang.Exception


class MainActivity : Adam(), SharedTwitterProperties.ActivityInterface {
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
                    SharedTwitterProperties.getMe(this) {
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

    override fun onRateLimit(apiPoint: String) {
        runOnUiThread {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(R.string.Error))
                .setContentText(getString(R.string.RateLimitError, apiPoint))
                .show()
        }
    }
}
