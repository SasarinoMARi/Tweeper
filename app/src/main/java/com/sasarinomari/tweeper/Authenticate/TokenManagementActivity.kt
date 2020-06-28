package com.sasarinomari.tweeper.Authenticate

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.gson.Gson
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SharedTwitterProperties
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
                    val intent = Intent()
                    intent.putExtra(RESULT_AUTH_DATA, Gson().toJson(authData))
                    setResult(RESULT_OK, intent)
                    finish()
                }

                override fun onDeleteUser(authData: AuthData) {
                    recorder.deleteUser(authData)
                    val focusedUser = recorder.getFocusedUser()
                    if (SharedTwitterProperties.instance().id == authData.user?.id) {
                        val intent = Intent()
                        if (focusedUser != null) {
                            intent.putExtra(RESULT_AUTH_DATA, Gson().toJson(focusedUser))
                        }
                        setResult(RESULT_OK, intent)
                    }
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
