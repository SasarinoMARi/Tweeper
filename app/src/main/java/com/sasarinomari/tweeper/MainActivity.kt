package com.sasarinomari.tweeper

import android.os.Bundle
import android.content.Intent
import android.widget.Toast
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
        TwitterAdapter.TwitterInterface.setOAuthConsumer(this)
        RewardedAdAdapter.load(this)

        when (val loggedUser = AuthData.Recorder(this).getFocusedUser()) {
            null -> doAuth()
            else -> lookupSelf(loggedUser)
        }

        checkNotiPremission()
    }

    private fun lookupSelf(loggedUser: AuthData) {
        Thread {
            try {
                TwitterAdapter().initialize(loggedUser.token!!).getMe(object : TwitterAdapter.FetchObjectInterface {
                    override fun onStart() {}

                    override fun onFinished(obj: Any) {
                        val me = obj as twitter4j.User
                        loggedUser.user = User(me)
                        AuthData.Recorder(this@MainActivity).setFocusedUser(loggedUser)
                        openDashboard()
                        finish()
                    }

                    override fun onRateLimit() {
                        this@MainActivity.onRateLimit("getMe") { finish() }
                    }

                    override fun onUncaughtError() {
                        runOnUiThread {
                            da.error("로그인에 실패했습니다.", "계정 선택 화면으로 이동합니다.") {
                                doAuth()
                            }.show()
                        }
                    }

                    override fun onNetworkError(retry: () -> Unit) {
                        this@MainActivity.onNetworkError {
                            lookupSelf(loggedUser)
                        }
                    }
                })
            } catch (e: Exception) {
                doAuth()
            }
        }.start()
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
