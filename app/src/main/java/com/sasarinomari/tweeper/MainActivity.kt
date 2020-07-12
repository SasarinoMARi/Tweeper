package com.sasarinomari.tweeper

import android.os.Bundle
import android.content.Intent
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.Authenticate.TokenManagementActivity
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.SimplizatedClass.User
import java.lang.Exception


class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NotificationChannels().declaration(this)
        TwitterAdapter.initialize(this)
        RewardedAdAdapter.load(this)

        when (val loggedUser = AuthData.Recorder(this).getFocusedUser()) {
            null -> {
                doAuth()
            }
            else -> {
                TwitterAdapter.twitter.oAuthAccessToken = loggedUser.token!!
                Thread {
                    try {
                        TwitterAdapter().getMe(object : TwitterAdapter.FetchObjectInterface {
                            override fun onStart() {}

                            override fun onFinished(obj: Any) {
                                val me = obj as twitter4j.User
                                loggedUser.user = User(me)
                                AuthData.Recorder(this@MainActivity).setFocusedUser(loggedUser)
                                openDashboard()
                                finish()
                            }

                            override fun onRateLimit() {
                                da.error(getString(R.string.Error), getString(R.string.RateLimitError, "getMe")).show()
                            }

                        })
                    } catch (e: Exception) {
                        doAuth()
                    }
                }.start()
            }
        }

        checkNotiPremission()
    }

    private fun checkNotiPremission() {
        // TODO
        // 백그라운드 스레드 실행을 위해 알림을 띄울 수 있는 권한이 있는지 검사.
        // 권한이 없다면 안내 문구와 함께 설정 창으로 연결, 또는 앱 종료료
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
