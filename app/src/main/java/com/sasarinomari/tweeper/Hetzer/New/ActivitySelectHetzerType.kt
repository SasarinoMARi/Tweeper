package com.sasarinomari.tweeper.Hetzer.New

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sasarinomari.tweeper.Hetzer.New.Conditions.FavoriteByMe
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
                Log.v(LOG_TAG, "Filter Result: [${tweet.text}] $result")
            }
        }
    }

    private fun createDummyTweets(): ArrayList<Status> {
//        val list = ArrayList<Status>()
//        var s : Status
//
//        s = Status()
//        s.id = 1
//        s.isFavorited = true
//        s.isRetweeted = true
//        s.text = "테스트 1"
//        s.favoriteCount = 2
//        s.retweetCount = 5
//        list.add(s)
//
//        s = Status()
//        s.id = 2
//        s.isFavorited = false
//        s.isRetweeted = false
//        s.text = "테스트 2"
//        s.favoriteCount = 10
//        s.retweetCount = 10
//        list.add(s)

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
        condition = FavoriteByMe(true)
        collection.conditions.add(condition)
        collection.action = Hetzer.Action.Save
        collections.add(collection)

        return collections
    }

}