package com.sasarinomari.tweeper.Authenticate

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.gson.Gson
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.TwitterAdapter
import kotlinx.android.synthetic.main.activity_token_management.*

class TokenManagementActivity : BaseActivity() {
    companion object {
        val RESULT_AUTH_DATA = "authData"
    }

    private val recorder = AuthData.Recorder(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_token_management)

        getAuthData()
        button_addContition.setOnClickListener {
            startActivityForResult(Intent(this, AuthenticationActivity::class.java), 0)
        }
    }

    private fun getAuthData() {
        val users = recorder.getUsers()
        if (users.isEmpty()) {
            listView.visibility = View.GONE
        } else {
            val adapter = AuthDataAdapter(users, object : AuthDataAdapter.ActivityInterface {
                override fun onSelectUser(authData: AuthData) {
                    recorder.setFocusedUser(authData)
                    setResult(RESULT_OK)
                    finish()
                }

                override fun onDeleteUser(authData: AuthData) {
                    // 저장된 유저를 삭제하고 액티비티를 닫을 때 유저를 갱신하도록 설정
                    // focused user가 삭제될 경우에 대비한 것임
                    recorder.deleteUser(authData)
                    setResult(RESULT_OK)
                    getAuthData()
                }
            })
            listView.adapter = adapter
            listView.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            0 -> {
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK)
                    finish()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }


}
