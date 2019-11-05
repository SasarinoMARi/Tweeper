package com.sasarinomari.tweetcleaner

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

abstract class Adam : AppCompatActivity() {
    fun hideKeyboard() {
        val windowToken = currentFocus?.windowToken
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private val FINISH_INTERVAL_TIME: Long = 2000
    private var backPressedTime: Long = 0

    fun backPressJail(warningText: String): Boolean {
        val tempTime = System.currentTimeMillis()
        val intervalTime = tempTime - backPressedTime

        return when (intervalTime) {
            in 0..FINISH_INTERVAL_TIME -> false
            else -> {
                Toast.makeText(this, warningText, Toast.LENGTH_SHORT).show()
                backPressedTime = tempTime
                true
            }
        }
    }
}