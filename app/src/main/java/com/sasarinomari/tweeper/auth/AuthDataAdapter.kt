package com.sasarinomari.tweeper.auth

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.PopupMenu
import com.google.gson.Gson
import com.sasarinomari.tweeper.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_usertoken.view.*


internal class AuthDataAdapter(private val users: ArrayList<AuthData>,
                               private val ai: ActivityInterface) : BaseAdapter() {

    override fun getCount(): Int {
        return users.size
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val context = parent.context

        if (convertView == null) {
            val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.item_usertoken, parent, false)
        }

        val authData = users[position]

        convertView!!
        if (authData.focused) convertView.image_focused.visibility = View.VISIBLE
        convertView.text_ScreenName.text = authData.user?.screenName
        convertView.text_Name.text = authData.user?.name
        Picasso.get()
            .load(authData.user?.profilePicUrl)
            .into(convertView.image_profilePicture)

        convertView.button_more.setOnClickListener { v ->
            val menu = PopupMenu(context, v)
            menu.setOnMenuItemClickListener { m ->
                when (m.itemId) {
                    R.id.option_delete -> {
                        ai.onDeleteUser(authData)
                    }
                }
                true
            }
            menu.inflate(R.menu.user_token_item_menu)
            menu.gravity = Gravity.END
            menu.show()
        }

        convertView.setOnClickListener {
            ai.onSelectUser(authData)
        }

        return convertView
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): AuthData {
        return users[position]
    }

    fun getItemToJson(position: Int): String? {
        return Gson().toJson(getItem(position))
    }

    interface ActivityInterface {
        fun onSelectUser(authData: AuthData)
        fun onDeleteUser(authData: AuthData)
    }
}