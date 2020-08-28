package com.sasarinomari.tweeper.Hetzer.New

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sasarinomari.tweeper.Hetzer.New.Conditions.FavoriteByMe
import com.sasarinomari.tweeper.Hetzer.New.Conditions.RetweetByMe
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.SimplizatedClass.Status
import kotlinx.android.synthetic.main.activity_select_hetzer_type.*
import java.util.*

class ActivitySelectHetzerType : AppCompatActivity() {
    private val LOG_TAG = "HetzerTest"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_hetzer_type)
        val tweets = createDummyTweets()
        val conditions = createDummyConditions()
        button_whitelist.setOnClickListener {
            val hetzer = Hetzer(
                conditions, Hetzer.Action.Delete)

            for (tweet in tweets) {
                val result = hetzer.filter(tweet)
                Log.v(LOG_TAG, "Filter Result: [${tweet.text}] $result")
            }
        }
        button_blacklist.setOnClickListener {
            val hetzer = Hetzer(
                conditions, Hetzer.Action.Save)

            for (tweet in tweets) {
                val result = hetzer.filter(tweet)
                if(result == Hetzer.Action.Delete) {
                    Log.v(LOG_TAG, "Filter Result: [${tweet.text}] $result")
                }
            }
        }

        for(collection in conditions) {
            initConditionView(collection.conditions)
        }

        button_addCondition.setOnClickListener {
            startActivity(Intent(this@ActivitySelectHetzerType, AddNewConditionActivity::class.java))
        }
    }

    private fun initConditionView(conditions: ArrayList<ConditionObject>) {
        for(condition in conditions) {
            val newConditionView = ConditionView(this)
            newConditionView.setText(condition)
            temp_container.addView(newConditionView, 0)
        }
    }

    private fun createDummyTweets(): ArrayList<Status> {
        val pref = getSharedPreferences("tempStatuses", Context.MODE_PRIVATE)
        val json = pref.getString("tweets", "")
        val type = object: TypeToken<ArrayList<Status>>(){}.type
        return Gson().fromJson(json, type)
    }

    private fun createDummyConditions(): ArrayList<ConditionCollection> {
        val collections = ArrayList<ConditionCollection>()
        var collection : ConditionCollection
        var condition : ConditionObject

        collection = ConditionCollection()
        collection.action = Hetzer.Action.Delete

        condition = RetweetByMe(true)
        collection.conditions.add(condition)

        collections.add(collection)

        return collections
    }

}