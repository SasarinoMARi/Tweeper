package com.sasarinomari.tweeper

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.sasarinomari.tweeper.Analytics.AnalyticsReport
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.Billing.BillingActivity
import com.sasarinomari.tweeper.Hetzer.LogicpairTypeSelectActivity
import com.sasarinomari.tweeper.MediaDownload.MediaDownloadActivity
import com.sasarinomari.tweeper.Report.ReportInterface
import kotlinx.android.synthetic.main.fragment_card_button.view.*
import kotlinx.android.synthetic.main.fragment_column_header.view.*
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*
import kotlinx.android.synthetic.main.full_recycler_view.*


class UITestActivity : BaseActivity() {

    private val LOG_TAG = "UITest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = ListView(this)
        setContentView(root)

        val menus = arrayOf(
            "test Recycler Inject",
            "test Reward Ad",
            "enter Billing Activity",
            "test Firebase Logging",
            "test Media Download",
            "check Connection",
            "load Analytics Reports",
            "LogicPair"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, menus)
        root.adapter = adapter
        root.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->
            when(position) {
                0-> testRecyclerInjector()
                1-> testRewardAd()
                2-> testBillingActivity()
                3-> testFirebaseLogging()
                4-> textMediaDownload()
                5-> connectionCheck()
                6-> reportLoadTest()
                7-> textLogicPair()
            }
        }
    }

    private fun textLogicPair() {
        startActivity(Intent(this, LogicpairTypeSelectActivity::class.java))
    }

    private fun reportLoadTest() {
        val reportPrefix = AnalyticsReport.prefix
        val userId = AuthData.Recorder(this).getFocusedUser()!!.user!!.id
        val reports = ReportInterface<Any>(userId, reportPrefix).getReportsWithName(this)

        val list = ListView(this)
        setContentView(list)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, reports)
        list.adapter = adapter
    }

    private fun connectionCheck() {
        val result = TwitterAdapter.isConnected(this)
        Log.i(LOG_TAG, result.toString())
    }

    private fun textMediaDownload() {
        startActivity(Intent(this, MediaDownloadActivity::class.java))
    }

    private fun testFirebaseLogging() {
        fbLog.log("Test_Event",
            Pair("Param1", "Hello, World!"),
            Pair("Param2", "Second Run"),
            Pair("Param3", "Zi be ga go sip da"))
    }

    private fun testBillingActivity() {
        val intent = Intent(this, BillingActivity::class.java)
        startActivity(intent)
    }

    private fun testRewardAd() {
        RewardedAdAdapter.show(this, object: RewardedAdAdapter.RewardInterface {
            override fun onFinished() {
                finish()
            }
        })
    }

    private fun testRecyclerInjector() {
        setContentView(R.layout.full_recycler_view)

        val adapter = RecyclerInjector()
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_title_with_desc) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.title_text.text = "테스트 제목"
                view.title_description.text = "제목 설명"
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_card_button) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.cardbutton_image.setOvalColor(Color.RED)
                view.cardbutton_image.setImageResource(R.mipmap.ic_launcher)
                view.cardbutton_text.text = "테스트 버튼"
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_column_header) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.column_title.text = "헤더 제목"
                view.column_description.text = "헤더 설명"
            }
        })

        root.layoutManager = LinearLayoutManager(this)
        root.adapter = adapter
    }
}


