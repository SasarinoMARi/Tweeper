package com.sasarinomari.tweetcleaner.hetzer

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.sasarinomari.tweetcleaner.R
import kotlinx.android.synthetic.main.item_hetzer_condition.view.*

class HetzerConditionView(context: Context) : LinearLayout(context) {

    fun setText(text: String) {
        this.text.text = text
    }

    fun setMoreButtonCallback(callback: () -> Unit) {
        this.button_more.setOnClickListener {
            callback()
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.item_hetzer_condition, this, true)
    }
}