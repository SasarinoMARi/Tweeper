package com.sasarinomari.tweetcleaner

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.view_dashboard_card.view.*

class DashboardCard : CardView {
    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        LayoutInflater.from(context).inflate(R.layout.view_dashboard_card, this, true)
        this.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DashboardCard, defStyle, 0)
        setTypeArray(typedArray)
    }

    private fun setTypeArray(typedArray: TypedArray) {
        val iconAttr = typedArray.getResourceId(R.styleable.DashboardCard_icon, 0)
        val textAttr = typedArray.getString(R.styleable.DashboardCard_text)

        icon.setImageResource(iconAttr)
        text.text = textAttr

        typedArray.recycle()
    }
}