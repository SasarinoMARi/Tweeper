package com.sasarinomari.tweeper.Hetzer.New

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.sasarinomari.tweeper.Hetzer.New.Conditions.*
import com.sasarinomari.tweeper.R
import kotlinx.android.synthetic.main.item_hetzer_condition.view.*

internal class ConditionView(context: Context) : LinearLayout(context) {
    fun setText(condition: ConditionObject) {
        this.card_title.text = condition.toString(context)
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