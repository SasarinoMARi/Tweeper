package com.sasarinomari.tweeper.fwmanage

import android.widget.BaseAdapter
import twitter4j.User
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sasarinomari.tweeper.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_user_unfollow.view.*


class UserUnfollowItem(private val users: ArrayList<User>,
                       private val ai:ActivityInterface) : BaseAdapter() {

    override fun getCount(): Int {
        return users.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val context = parent.context

        if (convertView == null) {
            val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.item_user_unfollow, parent, false)
        }

        val user = users[position]

        convertView!!
        convertView.text_Name.text = user.name
        convertView.text_ScreenName.text = user.screenName
        convertView.text_bio.text = user.description

        Picasso.get()
            .load(user.profileImageURL.replace("normal.jpg", "200x200.jpg"))
            .into(convertView.image_profilePicture)

        convertView.button_detail.setOnClickListener {
            ai.onclickDetail(user.screenName)
        }
        convertView.button_unfollow.setOnClickListener {
            ai.onClickUnfollow(user.id, Runnable {
                users.remove(user)
                notifyDataSetChanged()
            })
        }

        return convertView
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return users[position]
    }

    interface ActivityInterface {
        fun onClickUnfollow(userId: Long, doneCallback: Runnable)
        fun onclickDetail(screenName: String)
    }
}