package com.sasarinomari.tweeper

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_default.view.*

@Deprecated("User RecyclerInjector")
abstract class DefaultListItem(private val items: List<*>) : BaseAdapter() {
    var clickEffectVisibility = false

    override fun getCount(): Int {
        return items.size
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val context = parent.context

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.item_default, parent, false)
        }

        convertView!!
        drawItem(items[position]!!, convertView.defaultitem_title, convertView.defaultitem_description)

        if(clickEffectVisibility) { // 이거 없어도 클릭 이펙트 잘 나오는데 왜 씀? ㅋㅋ
            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            convertView.setBackgroundResource(outValue.resourceId)
        }
        else {
            convertView.background = null
        }

        return convertView
    }

    abstract fun drawItem(item: Any, title: TextView, description: TextView)

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any? {
        return items[position]
    }

    fun getItemToJson(position: Int): String? {
        return Gson().toJson(getItem(position))
    }
}