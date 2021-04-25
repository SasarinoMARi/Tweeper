package com.sasarinomari.tweeper.Hetzer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.gson.Gson
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.R
import kotlinx.android.synthetic.main.activity_logic_pair_edit.*
import kotlinx.android.synthetic.main.activity_logic_pair_edit.button_ok
import kotlinx.android.synthetic.main.activity_logic_pair_edit.layout_content
import kotlinx.android.synthetic.main.activity_logic_pair_edit.layout_title_and_desc
import kotlinx.android.synthetic.main.activity_logicpair.*
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*

class LogicPairEditActivity : BaseActivity() {
    enum class Parameters {
        LogicType, // 논리쌍 유형
        LogicPair // 편집하는 경우 파라미터로 직렬화된 논리쌍 받음
    }
    enum class Results {
        LogicPair
    }
    private val logicType : Int by lazy {intent.getIntExtra(Parameters.LogicType.name, -1)}
    private val lpView : LogicPairView by lazy {
        // 논리쌍 초기화 과정
        val json = intent.getStringExtra(Parameters.LogicPair.name)
        val logicPair : LogicPair = if(json == null) LogicPair(when(logicType) {
            LogicPair.LogicType.Remove.ordinal -> LogicPair.LogicType.Remove
            LogicPair.LogicType.Save.ordinal -> LogicPair.LogicType.Save
            else -> throw Exception()
        })
        else Gson().fromJson(json, LogicPair::class.java)
        
        // 뷰 초기화 과정
        val v = LogicPairView(this)
        v.initialize(LogicPairView.Mode.Edit, logicPair)
        v
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logic_pair_edit)

        layout_title_and_desc.title_text.text = when(logicType) {
            LogicPair.LogicType.Remove.ordinal -> getString(R.string.LogicPair_RemRule)
            LogicPair.LogicType.Save.ordinal -> getString(R.string.LogicPair_SaveRule)
            else -> throw Exception()
        }
        layout_title_and_desc.title_description.text = when(logicType) {
            LogicPair.LogicType.Remove.ordinal -> getString(R.string.LogicPair_RemRule_Description)
            LogicPair.LogicType.Save.ordinal -> getString(R.string.LogicPair_SaveRule_Description)
            else -> throw Exception()
        }

        layout_content.addView(lpView)

        val addDialog = createAddConditionDialog()
        button_addContition.setOnClickListener {
            addDialog.show {
                getAvailableItems(this, lpView.logicPair)
            }
        }
        button_ok.setOnClickListener {
            /**
             * 논리쌍이 비어있을 경우
             */
            if(lpView.logicPair.isEmpty()) {
                da.warning(getString(R.string.Error), getString(R.string.NoLogic)).show()
                return@setOnClickListener
            }

            /**
             * 인탠드에 결과 담아서 종료
             */
            val i = Intent()
            i.putExtra(Results.LogicPair.name, Gson().toJson(lpView.logicPair))
            setResult(RESULT_OK, i)
            finish()
        }
    }

    // region UI 관련

    private fun createAddConditionDialog(): MaterialDialog {
        val addDialog = MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT))
        addDialog.title(R.string.AddCondition)
        addDialog.negativeButton(R.string.Cancel)
        addDialog.positiveButton(R.string.OK)
        return addDialog
    }

    private fun inputNumber(dialogText: String, callback: (Int)->Unit){
        MaterialDialog(this).show {
            input(waitForPositiveButton = false) { dialog, text ->
                val inputField = dialog.getInputField()
                val number = text.toString().toIntOrNull()
                val isInteger = number != null
                inputField.error =
                    if (isInteger) null else getString(R.string.OnlyNumberAllowed)
                dialog.setActionButtonEnabled(WhichButton.POSITIVE, isInteger)
            }
            title(text = dialogText)
            message(text = dialogText)
            positiveButton(R.string.OK) { dialog ->
                val text = dialog.getInputField().text.toString()
                val number = text.toIntOrNull()
                if (number != null) {
                    callback(number)
                }
            }
        }
    }

    private fun inputString(dialogText: String, callback: (String)-> Unit){
        MaterialDialog(this).show {
            input(waitForPositiveButton = false)
            title(text = dialogText)
            message(text = dialogText)
            positiveButton(R.string.OK) { dialog ->
                val text = dialog.getInputField().text.toString()
                callback(text)
            }
        }
    }

    /**
     * 현재 추가할 수 있는 아이템을 선택지에 추가
     */
    @SuppressLint("CheckResult")
    private fun getAvailableItems(dialog: MaterialDialog, lp: LogicPair) {
        // region Define Listitems
        val listItems = ArrayList<String>()
        if(lp.everything == null) {
            listItems.add(getString(R.string.HetzerConditions_0))
        }

        if (lp.includeFavorite == null && lp.excludeFavorite == null) {
            listItems.add(getString(R.string.HetzerConditions_1))
            listItems.add(getString(R.string.HetzerConditions_2))
        }
        if (lp.includeRetweet == null && lp.excludeRetweet == null) {
            listItems.add(getString(R.string.HetzerConditions_3))
            listItems.add(getString(R.string.HetzerConditions_4))
        }
        if (lp.includeFavoriteOver == null) {
            listItems.add(getString(R.string.HetzerConditions_5, "N"))
        }
        if (lp.includeFavoriteUnder == null) {
            listItems.add(getString(R.string.HetzerConditions_6, "N"))
        }
        if (lp.includeRetweetOver == null) {
            listItems.add(getString(R.string.HetzerConditions_7, "N"))
        }
        if (lp.includeRetweetUnder == null) {
            listItems.add(getString(R.string.HetzerConditions_8, "N"))
        }
        if (lp.includeMedia == null && lp.excludeMedia == null) {
            listItems.add(getString(R.string.HetzerConditions_9))
            listItems.add(getString(R.string.HetzerConditions_10))
        }

        listItems.add(getString(R.string.HetzerConditions_11, "특정 단어"))
        listItems.add(getString(R.string.HetzerConditions_12, "특정 단어"))
        if (lp.includeGEO == null && lp.excludeGEO == null) {
            listItems.add(getString(R.string.HetzerConditions_13))
            listItems.add(getString(R.string.HetzerConditions_14))
        }
        if (lp.includeRecentTweetNumber == null) {
            listItems.add(getString(R.string.HetzerConditions_15, "N"))
        }
        if (lp.includeRecentTweetUntil == null) {
            listItems.add(getString(R.string.HetzerConditions_16, "N"))
        }
        // endregion
        // region ListItem Choice Event
        dialog.listItemsSingleChoice(items = listItems) { _, _, textSeq ->
            when (textSeq.toString()) {
                getString(R.string.HetzerConditions_0) -> {
                    lpView.addLogic_Everything()
                }
                getString(R.string.HetzerConditions_1) -> lpView.addLogic_includeFav()
                getString(R.string.HetzerConditions_2) -> lpView.addLogic_excludeFav()
                getString(R.string.HetzerConditions_3) -> lpView.addLogic_includeRt()
                getString(R.string.HetzerConditions_4) -> lpView.addLogic_excludeRt()
                getString(R.string.HetzerConditions_5, "N") -> {
                    inputNumber(getString(R.string.HetzerConditions_5_Desc)) { number ->
                        lpView.addLogic_favOver(number)
                    }
                }
                getString(R.string.HetzerConditions_6, "N") -> {
                    inputNumber(getString(R.string.HetzerConditions_6_Desc)) { number ->
                        lpView.addLogic_favUnder(number)
                    }
                }
                getString(R.string.HetzerConditions_7, "N") -> {
                    inputNumber(getString(R.string.HetzerConditions_7_Desc)) { number ->
                        lpView.addLogic_RtOver(number)
                    }
                }
                getString(R.string.HetzerConditions_8, "N") -> {
                    inputNumber(getString(R.string.HetzerConditions_8_Desc)) { number ->
                        lpView.addLogic_RtUnder(number)
                    }
                }
                getString(R.string.HetzerConditions_9) -> lpView.addLogic_includeMedia()
                getString(R.string.HetzerConditions_10) -> lpView.addLogic_excludeMedia()
                getString(R.string.HetzerConditions_11, "특정 단어") -> {
                    inputString(getString(R.string.HetzerConditions_11_Desc)) { str ->
                        lpView.addLogic_includeKeyword(str)
                    }
                }
                getString(R.string.HetzerConditions_12, "특정 단어") -> {
                    inputString(getString(R.string.HetzerConditions_12_Desc)) { str ->
                        lpView.addLogic_excludeKeyword(str)
                    }
                }
                getString(R.string.HetzerConditions_13) -> lpView.addLogic_includeGeo()
                getString(R.string.HetzerConditions_14) -> lpView.addLogic_excludeGeo()
                getString(R.string.HetzerConditions_15, "N") -> {
                    inputNumber(getString(R.string.HetzerConditions_15_Desc)) { number ->
                        lpView.addLogic_recentNumber(number)
                    }
                }
                getString(R.string.HetzerConditions_16, "N") -> {
                    inputNumber(getString(R.string.HetzerConditions_16_Desc)) { number ->
                        lpView.addLogic_recentMinute(number)
                    }
                }
            }
        }
        // endregion
    }

    // endregion
}