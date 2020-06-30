package com.sasarinomari.tweeper.Hetzer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.RecyclerInjector
import com.sasarinomari.tweeper.SimplizatedClass.Status
import com.sasarinomari.tweeper.Report.ReportInterface
import com.sasarinomari.tweeper.SharedTwitterProperties
import kotlinx.android.synthetic.main.fragment_column_header.view.*
import kotlinx.android.synthetic.main.fragment_no_item.view.*
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*
import kotlinx.android.synthetic.main.full_recycler_view.*
import kotlinx.android.synthetic.main.item_default.view.*
import java.text.SimpleDateFormat

class HetzerReportActivity : BaseActivity() {
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

        val reportIndex = intent.getIntExtra(Parameters.ReportId.name, -1)
        if(reportIndex == -1)
            da.error(null, getString(R.string.Error_WrongParameter)) { finish() }.show()
        val userId = AuthData.Recorder(this).getFocusedUser()!!.user!!.id
        val report = ReportInterface<HetzerReport>(userId, HetzerReport.prefix)
            .readReport(this,  reportIndex, HetzerReport()) as HetzerReport?

        if(report == null) {
            da.error(getString(R.string.Error), getString(R.string.Error_WrongParameter)) { finish() }; return
        }

        // Recycler 어댑터 작성하는 코드
        val adapter = RecyclerInjector()
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_title_with_desc) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.title_text.text = getString(R.string.TweetCleanerReport)
                view.title_description.text = getString(R.string.TweetCleanerReportDesc)
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_column_header) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.column_title.text = getString(R.string.RemovedTweets)
                view.column_description.text = getString(R.string.DeletedTweetCount, report.removedStatuses.count())
                view.setOnClickListener {
                    val f = adapter.getFragment(viewType + 1)
                    f.visible = !f.visible
                    adapter.notifyDataSetChanged()
                }
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.item_default, report.removedStatuses) {
            @SuppressLint("SimpleDateFormat")
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                item as Status
                view.defaultitem_title.text = item.text
                view.defaultitem_description.text = SimpleDateFormat(getString(R.string.Format_DateTime)).format(item.createdAt)
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_no_item) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                val f = adapter.getFragment(viewType - 1)
                view.noitem_text.visibility = if(f.visible && f.count == 0) {
                    view.noitem_text.text = getString(R.string.NoMatchedTweets)
                    View.VISIBLE
                } else View.GONE
            }
        })
        adapter.addSpace(5)
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_column_header) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.column_title.text = getString(R.string.SavedTweets)
                view.column_description.text = getString(R.string.SavedTweetCount, report.savedStatuses.count())
                view.setOnClickListener {
                    val f = adapter.getFragment(viewType + 1)
                    f.visible = !f.visible
                    adapter.notifyDataSetChanged()
                }
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.item_default, report.savedStatuses) {
                @SuppressLint("SimpleDateFormat")
                override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                    item as Status
                    view.defaultitem_title.text = item.text
                    view.defaultitem_description.text = SimpleDateFormat(getString(R.string.Format_DateTime)).format(item.createdAt)
                }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_no_item) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                val f = adapter.getFragment(viewType - 1)
                view.noitem_text.visibility = if(f.visible && f.count == 0) {
                    view.noitem_text.text = getString(R.string.NoMatchedTweets)
                    View.VISIBLE
                } else View.GONE
            }
        })

        root.layoutManager = LinearLayoutManager(this)
        root.adapter = adapter
    }
}