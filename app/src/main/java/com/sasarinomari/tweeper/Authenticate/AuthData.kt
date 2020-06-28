package com.sasarinomari.tweeper.Authenticate

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sasarinomari.tweeper.SimplizatedClass.User
import twitter4j.auth.AccessToken
import java.util.*

class AuthData {
    var token: AccessToken? = null
    var focused: Boolean = false
    var user: User? = null
    var lastLogin: Date? = null

    override operator fun equals(other: Any?): Boolean {
        return if (other is AuthData) this.user == other.user else super.equals(other)
    }

    override fun hashCode(): Int {
        return token?.token.hashCode()
    }

    internal class Recorder(private val context: Context) {
        private var prefId = "record"
        private var key = "auth"

        @SuppressLint("CommitPrefEdits")
        fun addUser(report: AuthData) {
            val prefs = context.getSharedPreferences(prefId, Context.MODE_PRIVATE).edit()
            val authData = getUsers()
            authData.add(0, report)
            val json = Gson().toJson(authData)
            prefs.putString(key, json)
            prefs.apply()
        }

        fun hasUser(authData: AuthData): Boolean {
            return getUsers().contains(authData)
        }

        private fun saveUsers(authData: ArrayList<AuthData>) {
            val prefs = context.getSharedPreferences(prefId, Context.MODE_PRIVATE).edit()
            val json = Gson().toJson(authData)
            prefs.putString(key, json)
            prefs.apply()
        }

        fun getUsers(): ArrayList<AuthData> {
            val prefs = context.getSharedPreferences(prefId, Context.MODE_PRIVATE)
            val json = prefs.getString(key, null) ?: return ArrayList()
            val type = object : TypeToken<ArrayList<AuthData>>() {}.type
            return Gson().fromJson(json, type)
        }

        fun getFocusedUser(): AuthData? {
            val users = getUsers()
            if (users.isEmpty()) return null
            return try {
                users.first { i -> i.focused }
            } catch (e: Exception) {
                setFocusedUser(users[0])
                getFocusedUser()
            }
        }

        fun setFocusedUser(authData: AuthData) {
            val users = getUsers()
            for (u in users) {
                u.focused = u == authData
            }
            saveUsers(users)
        }

        fun deleteUser(authData: AuthData) {
            val users = getUsers()
            users.remove(authData)
            if (authData.focused && users.isNotEmpty()) {
                users[0].focused = true
            }
            saveUsers(users)
        }
    }
}
