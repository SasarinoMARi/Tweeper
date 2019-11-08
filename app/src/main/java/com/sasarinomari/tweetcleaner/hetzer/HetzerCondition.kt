package com.sasarinomari.tweetcleaner.hetzer

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.sasarinomari.tweetcleaner.R
import kotlinx.android.synthetic.main.view_hetzer_condition.view.*

class HetzerCondition(context: Context) : LinearLayout(context) {

    fun setText(text: String) {
        this.text.text = text
    }

    fun setMoreButtonCallback(callback: () -> Unit) {
        this.button_more.setOnClickListener {
            callback()
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_hetzer_condition, this, true)
    }
}