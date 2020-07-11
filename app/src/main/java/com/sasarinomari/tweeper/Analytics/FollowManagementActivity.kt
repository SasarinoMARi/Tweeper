package com.sasarinomari.tweeper.Analytics

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.RecyclerInjector
import com.sasarinomari.tweeper.SharedTwitterProperties
import com.sasarinomari.tweeper.SimplizatedClass.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_column_header.view.*
import kotlinx.android.synthetic.main.fragment_no_item.view.*
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*
import kotlinx.android.synthetic.main.full_recycler_view.*
import kotlinx.android.synthetic.main.item_user_unfollow.view.*

class FollowManagementActivity: BaseActivity() {
    enum class Parameters {
        Followings, Followers
    }

    enum class Results {
        UnfollowedUsers, BlockUnblockedUsers
    }

    private val unfollowedUserIds = ArrayList<Long>()
    private val blockUnblockedUserIds = ArrayList<Long>()

    private fun checkRequirements(): Boolean {
        for(parameter in Parameters.values())
            if(!intent.hasExtra(parameter.name)) {
                da.error(getString(R.string.Error), getString(R.string.Error_NoParameter)) {
                    finish()
                }
                return false
            }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.full_recycler_view)
        if(!checkRequirements()) return

        // Report 읽어오는 코드
        val followings = Gson().fromJson(intent.getStringExtra(Parameters.Followings.name), object : TypeToken<ArrayList<User>>() { }.type) as ArrayList<User>
        val followers = Gson().fromJson(intent.getStringExtra(Parameters.Followers.name), object : TypeToken<ArrayList<User>>() { }.type) as ArrayList<User>

        val traitors = getDifference(followings, followers)
        val fans = getDifference(followers, followings)

        // Recycler 어댑터 작성하는 코드
        val adapter = RecyclerInjector()
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_title_with_desc) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.title_text.text = getString(R.string.FollowManagement)
                view.title_description.text = getString(R.string.FollowManagementDesc)
            }
        })
        if(traitors.isNotEmpty()) {
            adapter.addSpace(5)
            adapter.add(object : RecyclerInjector.RecyclerFragment(R.layout.fragment_column_header) {
                override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                    view.column_title.text = getString(R.string.Traitors)
                    view.column_description.text = getString(R.string.TouchToExpend)
                    view.chevron.visibility = View.VISIBLE
                    view.setOnClickListener {
                        val f = adapter.getFragment(viewType + 1)
                        f.visible = !f.visible
                        view.chevron.setImageResource(if(f.visible) R.drawable.chevron_down else R.drawable.chevron_up)
                        adapter.notifyDataSetChanged()
                    }
                }
            })
            adapter.add(object : RecyclerInjector.RecyclerFragment(R.layout.item_user_unfollow, traitors) {
                override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                    val user = item!! as User
                    view.text_Name.text = user.name
                    view.text_ScreenName.text = user.screenName
                    view.text_bio.text = user.bio

                    Picasso.get()
                        .load(user.profileImageUrl.replace("normal.jpg", "200x200.jpg"))
                        .into(view.image_profilePicture)

                    view.button_detail.setOnClickListener {
                        this@FollowManagementActivity.detail(user.screenName)
                    }
                    view.button_unfollow.setOnClickListener {
                        this@FollowManagementActivity.unfollow(user.id, Runnable {
                            this.removeListItem(listItemIndex)
                            adapter.notifyDataSetChanged()
                        })
                    }
                }
            })
            adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_no_item) {
                override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                    val f = adapter.getFragment(viewType - 1)
                    view.noitem_text.visibility = if(f.visible && f.count == 0) {
                        view.noitem_text.text = getString(R.string.NoMatchedUsers)
                        View.VISIBLE
                    } else View.GONE
                }
            })
        }
        if(fans.isNotEmpty()) {
            adapter.addSpace(5)
            adapter.add(object : RecyclerInjector.RecyclerFragment(R.layout.fragment_column_header) {
                override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                    view.column_title.text = getString(R.string.Fans)
                    view.column_description.text = getString(R.string.TouchToExpend)
                    view.chevron.visibility = View.VISIBLE
                    view.setOnClickListener {
                        val f = adapter.getFragment(viewType + 1)
                        f.visible = !f.visible
                        view.chevron.setImageResource(if(f.visible) R.drawable.chevron_down else R.drawable.chevron_up)
                        adapter.notifyDataSetChanged()
                    }
                }
            })
            adapter.add(object : RecyclerInjector.RecyclerFragment(R.layout.item_user_unfollow, fans) {
                override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                    val user = item!! as User
                    view.text_Name.text = user.name
                    view.text_ScreenName.text = user.screenName
                    view.text_bio.text = user.bio

                    Picasso.get()
                        .load(user.profileImageUrl.replace("normal.jpg", "200x200.jpg"))
                        .into(view.image_profilePicture)

                    view.button_detail.setOnClickListener {
                        this@FollowManagementActivity.detail(user.screenName)
                    }
                    (view.button_unfollow.getChildAt(0) as TextView).text = getString(R.string.BlockUnblock)
                    view.button_unfollow.setOnClickListener {
                        this@FollowManagementActivity.blockUnblock(user.id, Runnable {
                            this.removeListItem(listItemIndex)
                            adapter.notifyDataSetChanged()
                        })
                    }
                }
            })
            adapter.add(object : RecyclerInjector.RecyclerFragment(R.layout.fragment_no_item) {
                override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                    val f = adapter.getFragment(viewType - 1)
                    view.noitem_text.visibility = if (f.visible && f.count == 0) {
                        view.noitem_text.text = getString(R.string.NoMatchedUsers)
                        View.VISIBLE
                    } else View.GONE
                }
            })
        }
        if(traitors.isEmpty() && fans.isEmpty()) {
            adapter.addSpace(5)
            adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_no_item) {
                override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                    view.noitem_text.text = getString(R.string.NoMatchedUsers)
                }
            })
        }

        root.layoutManager = LinearLayoutManager(this)
        root.adapter = adapter
    }

    private fun getDifference(outer: List<User>, inner: List<User>): ArrayList<User> {
        val res = ArrayList<User>()
        for (u1 in outer) {
            var correspond = false
            for (u2 in inner) {
                if (u1.id == u2.id) {
                    correspond = true
                    break
                }
            }
            if (!correspond) {
                res.add(u1)
            }
        }
        return res
    }

    override fun onFinish() {
        if(unfollowedUserIds.isNotEmpty() || blockUnblockedUserIds.isNotEmpty()) {
            val intent = Intent()
            intent.putExtra(Results.UnfollowedUsers.name, Gson().toJson(unfollowedUserIds))
            intent.putExtra(Results.BlockUnblockedUsers.name, Gson().toJson(blockUnblockedUserIds))
            setResult(Activity.RESULT_OK, intent)
        }
    }

    // region API 함수들
    fun detail(screenName: String) {
        SharedTwitterProperties.showProfile(this, screenName)
    }
    fun unfollow(userId: Long, doneCallback: Runnable) {
        da.warning(getString(R.string.AreYouSure), getString(R.string.ActionDoNotRestore))
            .setConfirmText(getString(R.string.Yes))
            .setConfirmClickListener {
                it.dismissWithAnimation()
                Thread(Runnable {
                    val twitter = SharedTwitterProperties.instance()
                    twitter.destroyFriendship(userId)
                    unfollowedUserIds.add(userId)
                    runOnUiThread {
                        da.success(getString(R.string.Done), getString(R.string.JobDone)) {
                            it.dismissWithAnimation()
                            doneCallback.run()
                        }.show()
                    }
                }).start()
            }.show()
    }
    fun blockUnblock(userId: Long, doneCallback: Runnable) {
        da.warning(getString(R.string.AreYouSure), getString(R.string.ActionDoNotRestore))
            .setConfirmText(getString(R.string.Yes))
            .setConfirmClickListener {
                it.dismissWithAnimation()
                Thread(Runnable {
                    val twitter = SharedTwitterProperties.instance()
                    twitter.createBlock(userId)
                    twitter.destroyBlock(userId)
                    blockUnblockedUserIds.add(userId)
                    runOnUiThread {
                        da.success(getString(R.string.Done), getString(R.string.JobDone)) {
                            it.dismissWithAnimation()
                            doneCallback.run()
                        }.show()
                    }
                }).start()
            }.show()
    }
    // endregion
}