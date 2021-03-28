package com.sasarinomari.tweeper.Hetzer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.RecyclerInjector
import com.sasarinomari.tweeper.Report.ReportInterface
import kotlinx.android.synthetic.main.fragment_card_button.view.*
import kotlinx.android.synthetic.main.fragment_column_header.view.*
import kotlinx.android.synthetic.main.fragment_no_item.view.*
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*
import kotlinx.android.synthetic.main.full_recycler_view.*
import kotlinx.android.synthetic.main.item_default.*
import kotlinx.android.synthetic.main.item_default.view.*
import kotlinx.android.synthetic.main.item_default.view.defaultitem_description
import java.text.SimpleDateFormat
import java.util.*

class HetzerActivity : BaseActivity() {
    enum class RequestCodes {
        GetConditions
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.full_recycler_view)

        val reportPrefix = HetzerReport.prefix
        val userId = AuthData.Recorder(this).getFocusedUser()!!.user!!.id
        val reports = ReportInterface<Any>(userId, reportPrefix).getReportsWithName(this)
        reports.reverse()

        val adapter = RecyclerInjector()
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_title_with_desc) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.title_text.text = getString(R.string.TweetCleaner)
                view.title_description.text = getString(R.string.TweetCleanerDescription)
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_card_button) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.cardbutton_image.setOvalColor(ContextCompat.getColor(this@HetzerActivity, R.color.mint))
                view.cardbutton_image.setImageResource(R.drawable.comment_remove)
                view.cardbutton_text.text = getString(R.string.TweetCleanerRun)
                view.setOnClickListener {
                    if(HetzerService.checkServiceRunning((this@HetzerActivity))) {
                        da.warning(getString(R.string.Wait), getString(R.string.duplicateService_Hetzer)).show()
                    }
                    else {
                        startActivityForResult(
                            Intent(this@HetzerActivity, HetzerConditionsActivity::class.java),
                            RequestCodes.GetConditions.ordinal
                        )
                    }
                }
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_column_header) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.column_title.text = getString(R.string.TweetCleanerReports)
                view.column_description.text = getString(R.string.TouchToDetail)
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.item_default, reports) {
            @SuppressLint("SetTextI18n", "SimpleDateFormat")
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                item as String
                view.defaultitem_title.text = getString(R.string.TweetCleanerReport) + ' ' + (item.toString().removePrefix(reportPrefix).toInt() + 1)
                // view.defaultitem_description.text = SimpleDateFormat(getString(R.string.Format_DateTime)).format(item.second as Date)
                view.defaultitem_description.visibility = View.GONE // 보고서 객체에 추가 후 수정 필요
            }

            override fun onClickListItem(item: Any?) {
                val reportIndex = (item as Pair<*, *>).first.toString().removePrefix(reportPrefix).toInt()
                val intent = Intent(this@HetzerActivity, HetzerReportActivity::class.java)
                intent.putExtra(HetzerReportActivity.Parameters.ReportId.name, reportIndex)
                startActivity(intent)
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_no_item) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                val f = adapter.getFragment(viewType - 1)
                view.noitem_text.visibility = if(f.visible && f.count == 0) {
                    view.noitem_text.text = getString(R.string.NoHetzerReports)
                    View.VISIBLE
                } else View.GONE
            }
        })
        root.layoutManager = LinearLayoutManager(this)
        root.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RequestCodes.GetConditions.ordinal -> {
                if (resultCode != RESULT_OK) {
                    return
                }
                val intent = Intent(this, HetzerService::class.java)
                val json = data!!.getStringExtra(HetzerService.Parameters.HetzerConditions.name)
                intent.putExtra(HetzerService.Parameters.HetzerConditions.name, json)
                intent.putExtra(HetzerService.Parameters.User.name,
                    Gson().toJson(AuthData.Recorder(this@HetzerActivity).getFocusedUser()!!))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
                setResult(RESULT_OK)
                finish()
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
