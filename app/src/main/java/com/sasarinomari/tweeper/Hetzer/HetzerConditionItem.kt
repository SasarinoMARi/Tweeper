package com.sasarinomari.tweeper.Hetzer

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.sasarinomari.tweeper.R
import kotlinx.android.synthetic.main.item_hetzer_condition.view.*

internal class HetzerConditionItem(context: Context) : LinearLayout(context) {
    fun setText(text: String) {
        this.card_title.text = text
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