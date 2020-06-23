package com.sasarinomari.tweeper.hetzer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.gson.Gson
import com.sasarinomari.tweeper.Adam
import com.sasarinomari.tweeper.R
import kotlinx.android.synthetic.main.activity_hetzer_conditions.*
import com.google.gson.reflect.TypeToken
import com.sasarinomari.tweeper.SharedTwitterProperties

/*
    이 코드를 들여다보는 자여..
    심연에 온 것을 환영합니다.

    2020년 3월 22일 - 우사긔
 */
class HetzerConditionsActivity : Adam() {
    private var conditions = HashMap<Int, Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hetzer_conditions)

        initializeWithHetzerConditions()
        val addDialog = createAddConditionDialog()
        button_addContition.setOnClickListener {
            addDialog.show {
                attachItemLists(this)
            }
        }
        button_ok.setOnClickListener {
            val d = da.warning(getString(R.string.AreYouSure), getString(R.string.TweetCannotRestore))
                .setConfirmText(getString(R.string.YesDeleteIt))
                .setCancelText(getString(R.string.Wait))
                .setConfirmClickListener {
                    it.dismissWithAnimation()
                    Recorder(this).set(conditions)
                    val i = Intent()
                    i.putExtra(HetzerService.Parameters.HetzerConditions.name, Gson().toJson(conditions))
                    setResult(RESULT_OK, i)
                    finish()
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
    }
    @Suppress("UNCHECKED_CAST")
    private fun initializeWithHetzerConditions() {
        conditions = Recorder(this).get() ?:return
        if (conditions.containsKey(1)) addCondition1()
        if (conditions.containsKey(2)) addCondition2()
        if (conditions.containsKey(3)) addCondition3()
        if (conditions.containsKey(4)) addCondition4()
        if (conditions.containsKey(5)) addCondition5((conditions[5] as Double).toInt())
        if (conditions.containsKey(6)) addCondition6((conditions[6] as Double).toInt())
        if (conditions.containsKey(7)) addCondition7((conditions[7] as Double).toInt())
        if (conditions.containsKey(8)) addCondition8((conditions[8] as Double).toInt())
        if (conditions.containsKey(9)) addCondition9()
        if (conditions.containsKey(10)) addCondition10()
        if (conditions.containsKey(11)) {
            for (keyword in (conditions[11] as ArrayList<String>).toArray()) {
                addCondition11(keyword as String)
            }
        }
        if (conditions.containsKey(12)) {
            for (keyword in (conditions[12] as ArrayList<String>).toArray()) {
                addCondition12(keyword as String)
            }
        }
        if (conditions.containsKey(13)) addCondition13()
        if (conditions.containsKey(14)) addCondition14()
        if (conditions.containsKey(15)) addCondition15((conditions[15] as Double).toInt())
        if (conditions.containsKey(16)) addCondition16((conditions[16] as Double).toInt())
    }

    private fun addView(text: String, callback: ()-> Unit){
        val newConditionView = HetzerConditionView(this)
        newConditionView.setText(text)
        newConditionView.setMoreButtonCallback {
            createHetzerConditionPopupMenu(
                this@HetzerConditionsActivity,
                newConditionView,
                callback
            )
        }
        layout_scrollContent.addView(newConditionView, 0)
    }

    // region 조건 추가 코드

    // 내가 마음에 들어한 트윗
    private fun addCondition1() {
        conditions[1] = true
        addView(getString(R.string.HetzerConditions_1)){conditions.remove(1)}
    }

    // 내가 마음에 들어하지 않은 트윗
    private fun addCondition2() {
        conditions[2] = true
        addView(getString(R.string.HetzerConditions_2)){conditions.remove(2)}
    }

    // 내가 리트윗한 트윗
    private fun addCondition3() {
        conditions[3] = true
        addView(getString(R.string.HetzerConditions_3)){conditions.remove(3)}
    }

    // 내가 리트윗하지 않은 트윗
    private fun addCondition4() {
        conditions[4] = true
        addView(getString(R.string.HetzerConditions_4)){conditions.remove(4)}
    }

    // N회 이상 마음 받은 트윗 (Int)
    private fun addCondition5(number: Int) {
        conditions[5] = number
        addView(getString(R.string.HetzerConditions_5, number.toString())){conditions.remove(5)}
    }

    // N회 이하 마음 받은 트윗 (Int)
    private fun addCondition6(number: Int) {
        conditions[6] = number
        addView(getString(R.string.HetzerConditions_6, number.toString())){conditions.remove(6)}
    }

    // N회 이상 리트윗 받은 트윗 (Int)
    private fun addCondition7(number: Int) {
        conditions[7] = number
        addView(getString(R.string.HetzerConditions_7, number.toString())){conditions.remove(7)}
    }

    // N회 이하 리트윗 받은 트윗 (Int)
    private fun addCondition8(number: Int) {
        conditions[8] = number
        addView(getString(R.string.HetzerConditions_8, number.toString())){conditions.remove(8)}
    }

    // 미디어를 포함한 트윗
    private fun addCondition9() {
        conditions[9] = true
        addView(getString(R.string.HetzerConditions_9)){conditions.remove(9)}
    }

    // 미디어를 포함하지 않은 트윗
    private fun addCondition10() {
        conditions[10] = true
        addView(getString(R.string.HetzerConditions_10)){conditions.remove(10)}
    }

    // 키워드를 포함한 트윗 (ArrayList<Sring>)
    private fun addCondition11(keyword: String) {
        if(!conditions.containsKey(11)){
            conditions[11] = ArrayList<String>()
        }
        (conditions[11] as ArrayList<String>).add(keyword)
        addView(getString(R.string.HetzerConditions_11, keyword)){(conditions[11] as ArrayList<String>).remove(keyword)}
    }

    // 키워드를 포함하지 않은 트윗 (ArrayList<Sring>)
    private fun addCondition12(keyword: String) {
        if(!conditions.containsKey(12)){
            conditions[12] = ArrayList<String>()
        }
        (conditions[12] as ArrayList<String>).add(keyword)
        addView(getString(R.string.HetzerConditions_12, keyword)){(conditions[12] as ArrayList<String>).remove(keyword)}
    }

    // 위치 정보를 포함한 트윗
    private fun addCondition13() {
        conditions[13] = true
        addView(getString(R.string.HetzerConditions_13)){conditions.remove(13)}
    }

    // 위치 정보를 포함하지 않은 트윗
    private fun addCondition14() {
        conditions[14] = true
        addView(getString(R.string.HetzerConditions_14)){conditions.remove(14)}
    }

    // 최근 N개 까지의 트윗 (Int)
    private fun addCondition15(number: Int) {
        conditions[15] = number
        addView(getString(R.string.HetzerConditions_15, number.toString())){conditions.remove(15)}
    }

    // 최근 N분 이내의 트윗 (Int)
    private fun addCondition16(minute: Int) {
        conditions[16] = minute
        addView(getString(R.string.HetzerConditions_16, minute.toString())){conditions.remove(16)}
    }

    // endregion

    private fun createAddConditionDialog(): MaterialDialog {
        val addDialog = MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT))
        addDialog.title(R.string.AddCondition)
        addDialog.message(R.string.WhichTweetDoNotRemove)
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

    @SuppressLint("CheckResult")
    private fun attachItemLists(dialog: MaterialDialog) {
        // region Define Listitems
        val listItems = ArrayList<String>()
        if (!conditions.containsKey(1) && !conditions.containsKey(2)) {
            listItems.add(getString(R.string.HetzerConditions_1))
            listItems.add(getString(R.string.HetzerConditions_2))
        }
        if (!conditions.containsKey(3) && !conditions.containsKey(4)) {
            listItems.add(getString(R.string.HetzerConditions_3))
            listItems.add(getString(R.string.HetzerConditions_4))
        }
        if (!conditions.containsKey(5)) {
            listItems.add(getString(R.string.HetzerConditions_5, "N"))
        }
        if (!conditions.containsKey(6)) {
            listItems.add(getString(R.string.HetzerConditions_6, "N"))
        }
        if (!conditions.containsKey(7)) {
            listItems.add(getString(R.string.HetzerConditions_7, "N"))
        }
        if (!conditions.containsKey(8)) {
            listItems.add(getString(R.string.HetzerConditions_8, "N"))
        }
        if (!conditions.containsKey(9) && !conditions.containsKey(10)) {
            listItems.add(getString(R.string.HetzerConditions_9))
            listItems.add(getString(R.string.HetzerConditions_10))
        }

        listItems.add(getString(R.string.HetzerConditions_11, "특정 단어"))
        listItems.add(getString(R.string.HetzerConditions_12, "특정 단어"))
        if (!conditions.containsKey(13) && !conditions.containsKey(14)) {
            listItems.add(getString(R.string.HetzerConditions_13))
            listItems.add(getString(R.string.HetzerConditions_14))
        }
        if (!conditions.containsKey(15)) {
            listItems.add(getString(R.string.HetzerConditions_15, "N"))
        }
        if (!conditions.containsKey(16)) {
            listItems.add(getString(R.string.HetzerConditions_16, "N"))
        }
        // endregion
        // region ListItem Choice Event
        dialog.listItemsSingleChoice(items = listItems) { _, _, textSeq ->
            when (val text = textSeq.toString()) {
                getString(R.string.HetzerConditions_1) -> addCondition1()
                getString(R.string.HetzerConditions_2) -> addCondition2()
                getString(R.string.HetzerConditions_3) -> addCondition3()
                getString(R.string.HetzerConditions_4) -> addCondition4()
                getString(R.string.HetzerConditions_5, "N") -> {
                    inputNumber(getString(R.string.HetzerConditions_5_Desc)) { number ->
                        addCondition5(number)
                    }
                }
                getString(R.string.HetzerConditions_6, "N") -> {
                    inputNumber(getString(R.string.HetzerConditions_6_Desc)) { number ->
                        addCondition6(number)
                    }
                }
                getString(R.string.HetzerConditions_7, "N") -> {
                    inputNumber(getString(R.string.HetzerConditions_7_Desc)) { number ->
                        addCondition7(number)
                    }
                }
                getString(R.string.HetzerConditions_8, "N") -> {
                    inputNumber(getString(R.string.HetzerConditions_8_Desc)) { number ->
                        addCondition8(number)
                    }
                }
                getString(R.string.HetzerConditions_9) -> addCondition9()
                getString(R.string.HetzerConditions_10) -> addCondition10()
                getString(R.string.HetzerConditions_11, "특정 단어") -> {
                    inputString(getString(R.string.HetzerConditions_11_Desc)) { str ->
                        addCondition11(str)
                    }
                }
                getString(R.string.HetzerConditions_12, "특정 단어") -> {
                    inputString(getString(R.string.HetzerConditions_12_Desc)) { str ->
                        addCondition12(str)
                    }
                }
                getString(R.string.HetzerConditions_13) -> addCondition13()
                getString(R.string.HetzerConditions_14) -> addCondition14()
                getString(R.string.HetzerConditions_15, "N") -> {
                    inputNumber(getString(R.string.HetzerConditions_15_Desc)) { number ->
                        addCondition15(number)
                    }
                }
                getString(R.string.HetzerConditions_16, "N") -> {
                    inputNumber(getString(R.string.HetzerConditions_16_Desc)) { number ->
                        addCondition16(number)
                    }
                }
            }
        }
        // endregion
    }

    private fun createHetzerConditionPopupMenu(
        context: Context,
        view: View,
        deleteCallback: () -> Unit
    ) {
        val menu = PopupMenu(context, view)
        menu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.option_delete -> {
                    layout_scrollContent.removeView(view)
                    deleteCallback()
                }
            }
            true
        }
        menu.inflate(R.menu.hetzer_contition_item_menu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            menu.gravity = Gravity.END
        }
        else {
            // TODO
        }
        menu.show()
    }


    internal class Recorder(private val context: Context) {
        private var prefId = "record"

        @SuppressLint("CommitPrefEdits")
        fun set(con: HashMap<Int, Any>) {
            val prefs = context.getSharedPreferences(prefId, Context.MODE_PRIVATE).edit()
            val json = Gson().toJson(con)
            prefs.putString(getKey(), json)
            prefs.apply()
        }

        fun get(): HashMap<Int, Any>? {
            val prefs = context.getSharedPreferences(prefId, Context.MODE_PRIVATE)
            val json = prefs.getString(getKey(), null) ?: return null
            val type = object : TypeToken<HashMap<Int, Any>>() {}.type
            return try{
                Gson().fromJson(json, type)
            } catch(e: Exception) {
                null
            }
        }

        private fun getKey() : String {
            return "hCon" + SharedTwitterProperties.instance().id
        }
    }
}
