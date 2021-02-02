package com.sasarinomari.tweeper.Base

import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.sasarinomari.tweeper.DialogAdapter
import com.sasarinomari.tweeper.FirebaseLogger
import com.sasarinomari.tweeper.R

abstract class BaseActivity : AppCompatActivity() {
    fun hideKeyboard() {
        val windowToken = currentFocus?.windowToken
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private val FINISH_INTERVAL_TIME: Long = 2000
    private var backPressedTime: Long = 0
    protected lateinit var da: DialogAdapter
    private lateinit var activityRefrashReceiver: ActivityRefrashReceiver
    protected lateinit var fbLog: FirebaseLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        da = DialogAdapter(this)
        activityRefrashReceiver = ActivityRefrashReceiver(this)
        fbLog = FirebaseLogger(this)
    }

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

    open fun onFinish() { }
    override fun finish() {
        onFinish()
        super.finish()
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver( activityRefrashReceiver,
            IntentFilter(ActivityRefrashReceiver.eventName))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver( activityRefrashReceiver)
    }

    //region FirstRun
    protected fun isFirstRunThisActivity() : Boolean {
        val prefs = getSharedPreferences("fr${this::class.java.name}", Context.MODE_PRIVATE)
        val flag = prefs.getInt("flag", 0)
        return flag == 0
    }
    protected fun setNotFirstrun(){
        val prefs = getSharedPreferences("fr${this::class.java.name}", Context.MODE_PRIVATE).edit()
        prefs.putInt("flag", 1)
        prefs.apply()
    }
    //endregion


    fun onRateLimit(apiName: String, callback: () -> Unit = { }) {
        runOnUiThread {
            da.error(getString(R.string.Error), getString(R.string.RateLimitError, apiName)) {
                callback()
            }.show()
        }
    }

    fun onUncaughtError() {
        runOnUiThread {
            da.error(getString(R.string.Error), getString(R.string.UncaughtError)) {
                finish()
            }.show()
        }
    }

    fun onNoRequirement() {
        runOnUiThread {
            da.error(getString(R.string.Error), getString(R.string.Error_NoParameter)) {
                finish()
            }.show()
        }
    }

    fun onNetworkError(retry: () -> Unit) {
        runOnUiThread {
            val d = da.error(getString(R.string.Error), getString(R.string.NetworkError))
                .setConfirmText(getString(R.string.Yes))
                .setCancelText(getString(R.string.No))
            d.setConfirmClickListener {
                it.dismissWithAnimation()
                retry()
            }
            d.setCancelClickListener {
                it.dismiss()
                finish()
            }
            d.show()
        }
    }
}