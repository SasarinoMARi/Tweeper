package com.sasarinomari.tweeper

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.view_dashboard_card.view.*

class DashboardCardView : CardView {
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
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DashboardCardView, defStyle, 0)
        setTypeArray(typedArray)
    }

    private fun setTypeArray(typedArray: TypedArray) {
        val textAttr = typedArray.getString(R.styleable.DashboardCardView_title)
        val descAttr = typedArray.getString(R.styleable.DashboardCardView_description)
        val icon = typedArray.getResourceId(R.styleable.DashboardCardView_icon, 0)
        val ovalColor = typedArray.getColor(R.styleable.DashboardCardView_ovalColor, 0)
 
        text.text = textAttr
        description.text = descAttr
        oval.setImageResource(icon)
        oval.setOvalColor(ovalColor)

        /**
         * 이름 관련 해괴한 버그가 발견되어서 메모를 남긴다.
         * 코틀린 컴파일러 버전 : 1.3.72
         *
         * 위의 oval 변수는 CustomView인 OvalImageView의 인스턴스이다.
         * CustomView in CustomView 상황이기 때문에 어트리뷰트 전달을 위해서 초기화 함수를 만들었다.
         * OvalImageView.setOvalColor(int) 함수는 view_oval_image.xml에서 id가 oval인 View에 색을 입히는 함수로 만들어졌다.
         *
         * 그런데 지금 이 클래스에서도 id가 oval인 View가 있다.
         * 이 CustomView에서 inflate하는 레이아웃인 view_dashboard_card.xml에서도 OvalImageView CustomView의 인스턴스 id를 oval로 정의해 두었다.
         *
         * 자, 이름이 shadowed 된 지금 상황에서 oval.setOvalColor(int)를 호출하자,
         * 해당 함수 안의 oval.drawable as GradientDrawable 코드에서 형변환 에러가 발생했다.
         *
         * 어떻게 이게 가능한지는 잘 모르겠지만 view_oval_image.xml 에서 oval의 이름을 겹치지 않게 변경하는 것으로 수정했다.
         */

        typedArray.recycle()
    }
}