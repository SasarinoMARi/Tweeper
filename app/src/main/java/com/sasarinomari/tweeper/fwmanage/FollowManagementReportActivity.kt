package com.sasarinomari.tweeper.fwmanage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.sasarinomari.tweeper.Adam
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.RecyclerInjector
import com.sasarinomari.tweeper.SharedTwitterProperties
import com.sasarinomari.tweeper.SimplizatedClass.User
import com.sasarinomari.tweeper.report.ReportInterface
import com.sasarinomari.tweeper.report.ReportListActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_column_header.view.*
import kotlinx.android.synthetic.main.fragment_no_item.view.*
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*
import kotlinx.android.synthetic.main.full_recycler_view.*
import kotlinx.android.synthetic.main.item_user_unfollow.view.*
import twitter4j.StreamController

class FollowManagementReportActivity: Adam() {
    enum class Parameters {
        ReportId
    }

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
        val reportIndex = intent.getIntExtra(Parameters.ReportId.name, -1)
        if(reportIndex == -1)
            da.error(null, getString(R.string.Error_WrongParameter)) { finish() }.show()
        val report = ReportInterface<FollowManagementReport>(FollowManagementReport.prefix)
            .readReport(this, reportIndex, FollowManagementReport()) as FollowManagementReport?

        if(report == null) {
            da.error(getString(R.string.Error), getString(R.string.Error_WrongParameter)) { finish() }; return
        }

        // Recycler 어댑터 작성하는 코드
        val adapter = RecyclerInjector()
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_title_with_desc) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.title_text.text = getString(R.string.FollowManagementReport)
                view.title_description.text = getString(R.string.FollowManagementDesc)
            }
        })
        adapter.addSpace(5)
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_column_header) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.column_title.text = getString(R.string.Traitors)
                view.column_description.text = getString(R.string.TouchToExpend)
                view.setOnClickListener {
                    val f = adapter.getFragment(viewType + 1)
                    f.visible = !f.visible
                    adapter.notifyDataSetChanged()
                }
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.item_user_unfollow, report.traitors) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                val user = item!! as User
                view.text_Name.text = user.name
                view.text_ScreenName.text = user.screenName
                view.text_bio.text = user.bio

                Picasso.get()
                    .load(user.profileImageUrl.replace("normal.jpg", "200x200.jpg"))
                    .into(view.image_profilePicture)

                view.button_detail.setOnClickListener {
                    this@FollowManagementReportActivity.detail(user.screenName)
                }
                view.button_unfollow.setOnClickListener {
                    this@FollowManagementReportActivity.unfollow(user.id, Runnable {
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
        adapter.addSpace(5)
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_column_header) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.column_title.text = getString(R.string.Fans)
                view.column_description.text = getString(R.string.TouchToExpend)
                view.setOnClickListener {
                    val f = adapter.getFragment(viewType + 1)
                    f.visible = !f.visible
                    adapter.notifyDataSetChanged()
                }
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.item_user_unfollow, report.fans) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                val user = item!! as User
                view.text_Name.text = user.name
                view.text_ScreenName.text = user.screenName
                view.text_bio.text = user.bio

                Picasso.get()
                    .load(user.profileImageUrl.replace("normal.jpg", "200x200.jpg"))
                    .into(view.image_profilePicture)

                view.button_detail.setOnClickListener {
                    this@FollowManagementReportActivity.detail(user.screenName)
                }
                (view.button_unfollow.getChildAt(0) as TextView).text = getString(R.string.BlockUnblock)
                view.button_unfollow.setOnClickListener {
                    this@FollowManagementReportActivity.blockUnblock(user.id, Runnable {
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

        root.layoutManager = LinearLayoutManager(this)
        root.adapter = adapter
    }

    fun detail(screenName: String) {
        try{
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=$screenName")))
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/#!/$screenName")))
        }
    }

    fun unfollow(userId: Long, doneCallback: Runnable) {
        da.warning(getString(R.string.AreYouSure), getString(R.string.ActionDoNotRestore))
            .setConfirmText(getString(R.string.Yes))
            .setConfirmClickListener {
                it.dismissWithAnimation()
                Thread(Runnable {
                    val twitter = SharedTwitterProperties.instance()
                    twitter.destroyFriendship(userId)
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
                    runOnUiThread {
                        da.success(getString(R.string.Done), getString(R.string.JobDone)) {
                            it.dismissWithAnimation()
                            doneCallback.run()
                        }.show()
                    }
                }).start()
            }.show()
    }
}