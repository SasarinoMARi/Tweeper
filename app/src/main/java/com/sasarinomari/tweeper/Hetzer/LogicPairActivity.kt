package com.sasarinomari.tweeper.Hetzer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.gson.Gson
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.R
import kotlinx.android.synthetic.main.activity_logicpair.*
import com.sasarinomari.tweeper.RewardedAdAdapter
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*

internal class LogicPairActivity : BaseActivity(), LogicPairView.EventListener {
    val logicPairs : ArrayList<LogicPair> by lazy { ArrayList(LogicPair.Recorder(this).get()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logicpair)

        layout_title_and_desc.title_text.text = getString(R.string.DeleteOption)
        layout_title_and_desc.title_description.text = getString(R.string.DeleteOptionPlease)

        initViewWithLogicPairs(logicPairs)

        button_addLP.setOnClickListener {
            val i = Intent(this@LogicPairActivity, LogicpairTypeSelectActivity::class.java)
            startActivityForResult(i, 0)
        }

        button_ok.setOnClickListener {
            // 논리쌍이 비어있을 경우
            if(logicPairs.isEmpty()) {
                da.error(getString(R.string.Error), getString(R.string.NoLogic)).show()
                return@setOnClickListener
            }

            // 확인 다이얼로그 출력
            showConfirmDialog {
                it.dismissWithAnimation()
                RewardedAdAdapter.show(this, object : RewardedAdAdapter.RewardInterface {
                    override fun onFinished() {
                        LogicPair.Recorder(this@LogicPairActivity).set(logicPairs)
                        val i = Intent()
                        i.putExtra(HetzerService.Parameters.Logics.name, Gson().toJson(logicPairs))
                        setResult(RESULT_OK, i)
                        finish()
                    }
                })
            }
        }

        text_warning.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://help.twitter.com/ko/using-twitter/missing-tweets")
                )
            )
        }
    }

    /**
     * 아이템이 없으면 아이템이 없다는 문구 출력하는 코드
     */
    private fun checkEmptyAndDisplayNotice(lps: List<LogicPair>) {
        if(lps.isEmpty()) {
            layout_noItem.visibility = View.VISIBLE
            scrollView.visibility = View.GONE
        }
        else {
            layout_noItem.visibility = View.GONE
            scrollView.visibility = View.VISIBLE
        }
    }

    private fun initViewWithLogicPairs(lps: List<LogicPair>) {
        layout_content.removeAllViews()
        checkEmptyAndDisplayNotice(lps)
        for(lp in lps) addLogicPairView(lp)
    }

    private fun addLogicPairView(lp: LogicPair) {
        val v = LogicPairView(this)
        v.initialize(LogicPairView.Mode.View, lp)
        v.setEventListener(this)
        layout_content.addView(v)
    }

    /**
     * 삭제 확인창 출력
     */
    private fun showConfirmDialog(function: (SweetAlertDialog) -> Unit) {
        val d = da.warning(getString(R.string.AreYouSure), getString(R.string.TweetCannotRestore))
            .setConfirmText(getString(R.string.YesDeleteIt))
            .setCancelText(getString(R.string.Wait))
            .setConfirmClickListener {
                function(it)
            }
        d.setOnShowListener { dialog ->
            dialog as SweetAlertDialog
            val titleView: TextView = dialog.findViewById(R.id.title_text) as TextView
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)
            val contentView: TextView = dialog.findViewById(R.id.content_text) as TextView
            contentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            val confirmView: TextView = dialog.findViewById(R.id.confirm_button) as TextView
            confirmView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            val cancelView: TextView = dialog.findViewById(R.id.cancel_button) as TextView
            cancelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        }
        d.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        /**
         * 새 아이템 추가하고 돌아온 경우
         */
        if(requestCode == 0 || requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (data == null) return

                val json = data.getStringExtra(LogicPairEditActivity.Results.LogicPair.name) ?: return
                val lp = Gson().fromJson(json, LogicPair::class.java)!!
                logicPairs.remove(temp)
                logicPairs.add(lp)
                checkEmptyAndDisplayNotice(logicPairs)
                initViewWithLogicPairs(logicPairs)
            }
        }
        else super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * 아이템 삭제될 때 호출되는 인터페이스
     */
    override fun onRemove(logicPair: LogicPair) {
        logicPairs.remove(logicPair)
        initViewWithLogicPairs(logicPairs)
    }

    /**
     * 아이템 편집할 때 호출되는 인터페이스
     */
    var temp : LogicPair? = null
    override fun onEdit(logicPair: LogicPair) {
        val i = Intent(this, LogicPairEditActivity::class.java)
        i.putExtra(LogicPairEditActivity.Parameters.LogicType.name, logicPair.logicType.ordinal)
        temp = logicPair
        i.putExtra(LogicPairEditActivity.Parameters.LogicPair.name, Gson().toJson(logicPair))
        startActivityForResult(i, 1)
    }
}
