package com.sasarinomari.tweeper.hetzer

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.google.gson.Gson
import com.sasarinomari.tweeper.R
import kotlinx.android.synthetic.main.item_hetzer_report.view.*
import twitter4j.Status
import kotlin.collections.ArrayList

internal class HetzerReportItem(private val statuses: ArrayList<HetzerReport>) : BaseAdapter() {
        override fun getCount(): Int {
            return statuses.size
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val context = parent.context

            if (convertView == null) {
                val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                convertView = inflater.inflate(R.layout.item_hetzer_report, parent, false)
            }

            val status= statuses[position]

            convertView!!
            convertView.text.text = status.text
            convertView.createAt.text = status.createdAt.toString()

            return convertView
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): HetzerReport {
            return statuses[position]
        }

        fun getItemToJson(position: Int): String? {
            return Gson().toJson(getItem(position))
        }


}