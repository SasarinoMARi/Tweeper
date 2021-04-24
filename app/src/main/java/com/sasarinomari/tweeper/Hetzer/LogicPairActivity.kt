package com.sasarinomari.tweeper.Hetzer

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
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
    }

    private fun initViewWithLogicPairs(lps: List<LogicPair>) {
        layout_content.removeAllViews()
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
        if(requestCode == 0) {
            if(resultCode == RESULT_OK) {
                if(data == null) return

                val json = data.getStringExtra(LogicPairEditActivity.Results.LogicPair.name)?: return
                val lp = Gson().fromJson(json, LogicPair::class.java)!!
                logicPairs.add(lp)
                addLogicPairView(lp)
            }
        }
        else super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRemove(logicPair: LogicPair) {
        logicPairs.remove(logicPair)
        initViewWithLogicPairs(logicPairs)
    }

    override fun onEdit(logicPair: LogicPair) {
        val i = Intent(this, LogicPairEditActivity::class.java)
        i.putExtra(LogicPairEditActivity.Parameters.LogicType.name, logicPair.logicType.ordinal)
        i.putExtra(LogicPairEditActivity.Parameters.LogicPair.name, Gson().toJson(logicPair))
        startActivityForResult(i, 0)
    }
}
