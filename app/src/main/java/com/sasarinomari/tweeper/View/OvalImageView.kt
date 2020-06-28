package com.sasarinomari.tweeper.View

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.sasarinomari.tweeper.R
import kotlinx.android.synthetic.main.view_oval_image.view.*

class OvalImageView : RelativeLayout{
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
        LayoutInflater.from(context).inflate(R.layout.view_oval_image, this, true)
        this.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.OvalImageView, defStyle, 0)
        setTypeArray(typedArray)
    }


    private fun setTypeArray(typedArray: TypedArray) {
        val iconAttr = typedArray.getResourceId(R.styleable.OvalImageView_icon, 0)
        val ovalAttr = typedArray.getColor(R.styleable.OvalImageView_ovalColor, 0)

        if(iconAttr!=0) setImageResource(iconAttr)
        if(ovalAttr!=0) setOvalColor(ovalAttr)

        typedArray.recycle()
    }

    fun setImageResource(imageId: Int) { _oi_image.setImageResource(imageId) }

    fun setOvalColor(color: Int) { (_oi_oval.drawable as GradientDrawable).setColor(color) }
}