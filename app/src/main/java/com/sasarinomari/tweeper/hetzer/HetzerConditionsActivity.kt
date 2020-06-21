package com.sasarinomari.tweeper.hetzer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
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
import cn.pedant.SweetAlert.SweetAlertDialog


class HetzerConditionsActivity : Adam() {
    enum class Results {
        Conditions
    }

    private var conditions = HetzerConditions()

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
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.AreYouSure))
                .setContentText(getString(R.string.TweetCannotRestore))
                .setConfirmText(getString(R.string.YesDeleteIt))
                .setConfirmClickListener {
                    it.dismissWithAnimation()
                    HetzerConditions.Recorder(this).set(conditions)
                    val i = Intent()
                    i.putExtra(Results.Conditions.name, conditions)
                    setResult(RESULT_OK, i)
                    finish()
                }
                .show()
        }
    }

    private fun initializeWithHetzerConditions() {
        conditions = HetzerConditions.Recorder(this).get() ?:return
        if (conditions.avoidMyFav) addCondition_FavMySelf()
        if (conditions.avoidRetweetCount > 0) addCondition_RetweetCount(conditions.avoidRetweetCount)
        if (conditions.avoidFavCount > 0) addCondition_FavCount(conditions.avoidFavCount)
        if (conditions.avoidRecentCount > 0) addCondition_RecentCount(conditions.avoidRecentCount)
        if (conditions.avoidRecentMinute > 0) addCondition_RecentMinute(conditions.avoidRecentMinute)
        if (conditions.avoidMedia) addCondition_IncludedMedia()
        if (conditions.avoidNoMedia) addCondition_ExcludedMedia()
        if (conditions.avoidNoGeo) addCondition_IncludedGeo()
        for (i in conditions.avoidKeywords) {
            addCondition_IncludedKeyword(i)
        }
    }

    private fun createAddConditionDialog(): MaterialDialog {
        val addDialog = MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT))
        addDialog.title(R.string.AddCondition)
        addDialog.message(R.string.WhichTweetDoNotRemove)
        addDialog.negativeButton(R.string.Cancel)
        addDialog.positiveButton(R.string.OK)
        return addDialog
    }

    @SuppressLint("CheckResult")
    private fun attachItemLists(dialog: MaterialDialog) {
        val listItems = ArrayList<String>()
        if (!conditions.avoidMyFav) listItems.add(getString(R.string.Condition_FavMyself))
        if (conditions.avoidRetweetCount == 0) listItems.add(getString(R.string.Condition_RetweetCount))
        if (conditions.avoidFavCount == 0) listItems.add(getString(R.string.Condition_FavCount))
        if (conditions.avoidRecentCount == 0) listItems.add(getString(R.string.Condition_RecentCount))
        if (conditions.avoidRecentMinute == 0) listItems.add(getString(R.string.Condition_RecentMinute))
        listItems.add(getString(R.string.Condition_IncludedKeyword))
        if (!conditions.avoidMedia && !conditions.avoidNoMedia){
            listItems.add(getString(R.string.Condition_IncludedMedia))
            listItems.add(getString(R.string.Condition_ExcludedMedia))
        }
        if (!conditions.avoidNoGeo) listItems.add(getString(R.string.Condition_IncludedGEO))
        dialog.listItemsSingleChoice(items = listItems) { _, _, textSeq ->
            when (val text = textSeq.toString()) {
                getString(R.string.Condition_FavMyself) -> {
                    addCondition_FavMySelf()
                }
                getString(R.string.Condition_RetweetCount) -> {
                    MaterialDialog(this@HetzerConditionsActivity).show {
                        input(waitForPositiveButton = false) { dialog, text ->
                            val inputField = dialog.getInputField()
                            val number = text.toString().toIntOrNull()
                            val isInteger = number != null
                            inputField.error =
                                if (isInteger) null else getString(R.string.OnlyNumberAllowed)
                            dialog.setActionButtonEnabled(WhichButton.POSITIVE, isInteger)
                        }
                        title(text = text)
                        message(R.string.Condition_Description_RetweetCount)
                        positiveButton(R.string.OK) { dialog ->
                            val text = dialog.getInputField().text.toString()
                            val number = text.toIntOrNull()
                            if (number != null) {
                                addCondition_RetweetCount(number)
                            }
                        }
                    }
                }
                getString(R.string.Condition_FavCount) -> {
                    MaterialDialog(this@HetzerConditionsActivity).show {
                        input(waitForPositiveButton = false) { dialog, text ->
                            val inputField = dialog.getInputField()
                            val number = text.toString().toIntOrNull()
                            val isInteger = number != null
                            inputField.error =
                                if (isInteger) null else getString(R.string.OnlyNumberAllowed)
                            dialog.setActionButtonEnabled(WhichButton.POSITIVE, isInteger)
                        }
                        title(text = text)
                        message(R.string.Condition_Description_FavCount)
                        positiveButton(R.string.OK) { dialog ->
                            val text = dialog.getInputField().text.toString()
                            val number = text.toIntOrNull()
                            if (number != null) {
                                addCondition_FavCount(number)
                            }
                        }
                    }
                }
                getString(R.string.Condition_RecentCount) -> {
                    MaterialDialog(this@HetzerConditionsActivity).show {
                        input(waitForPositiveButton = false) { dialog, text ->
                            val inputField = dialog.getInputField()
                            val number = text.toString().toIntOrNull()
                            val isInteger = number != null
                            inputField.error =
                                if (isInteger) null else getString(R.string.OnlyNumberAllowed)
                            dialog.setActionButtonEnabled(WhichButton.POSITIVE, isInteger)
                        }
                        title(text = text)
                        message(R.string.Condition_Description_RecentCount)
                        positiveButton(R.string.OK) { dialog ->
                            val text = dialog.getInputField().text.toString()
                            val number = text.toIntOrNull()
                            if (number != null) {
                                addCondition_RecentCount(number)
                            }
                        }
                    }
                }
                getString(R.string.Condition_RecentMinute) -> {
                    MaterialDialog(this@HetzerConditionsActivity).show {
                        input(
                            waitForPositiveButton = false,
                            allowEmpty = false
                        ) { dialog, text ->
                            val inputField = dialog.getInputField()
                            val number = text.toString().toIntOrNull()
                            val isInteger = number != null
                            inputField.error =
                                if (isInteger) null else getString(R.string.OnlyNumberAllowed)
                            dialog.setActionButtonEnabled(WhichButton.POSITIVE, isInteger)
                        }
                        title(text = text)
                        message(R.string.Condition_Description_RecentMinute)
                        positiveButton(R.string.OK) { dialog ->
                            val text = dialog.getInputField().text.toString()
                            val number = text.toIntOrNull()
                            if (number != null) {
                                addCondition_RecentMinute(number)
                            }
                        }
                    }
                }
                getString(R.string.Condition_IncludedKeyword) -> {
                    MaterialDialog(this@HetzerConditionsActivity).show {
                        title(text = text)
                        message(R.string.Condition_Description_IncludedKeyword)
                        positiveButton(R.string.OK) { dialog ->
                            val text = dialog.getInputField().text.toString()
                            addCondition_IncludedKeyword(text)
                        }
                    }
                }
                getString(R.string.Condition_IncludedMedia)-> {
                    addCondition_IncludedMedia()
                }
                getString(R.string.Condition_ExcludedMedia)-> {
                    addCondition_ExcludedMedia()
                }
                getString(R.string.Condition_IncludedGEO)-> {
                    addCondition_IncludedGeo()
                }
            }
        }
    }

    private fun addCondition_RetweetCount(number: Int) {
        conditions.avoidRetweetCount = number
        val newConditionView = HetzerConditionView(this)
        newConditionView.setText(getString(R.string.Condition_Display_RetweetCount, number))
        newConditionView.setMoreButtonCallback {
            createHetzerConditionPopupMenu(
                this@HetzerConditionsActivity,
                newConditionView
            ) {
                conditions.avoidRetweetCount = 0
            }
        }
        layout_scrollContent.addView(newConditionView, 0)
    }

    private fun addCondition_FavCount(number: Int) {
        conditions.avoidFavCount = number
        val newConditionView = HetzerConditionView(this)
        newConditionView.setText(getString(R.string.Condition_Display_FavCount, number))
        newConditionView.setMoreButtonCallback {
            createHetzerConditionPopupMenu(
                this@HetzerConditionsActivity,
                newConditionView
            ) {
                conditions.avoidFavCount = 0
            }
        }
        layout_scrollContent.addView(newConditionView, 0)
    }

    private fun addCondition_RecentCount(number: Int) {
        conditions.avoidRecentCount = number
        val newConditionView = HetzerConditionView(this)
        newConditionView.setText(getString(R.string.Condition_Display_RecentCount, number))
        newConditionView.setMoreButtonCallback {
            createHetzerConditionPopupMenu(
                this@HetzerConditionsActivity,
                newConditionView
            ) {
                conditions.avoidRecentCount = 0
            }
        }
        layout_scrollContent.addView(newConditionView, 0)
    }

    private fun addCondition_RecentMinute(number: Int) {
        conditions.avoidRecentMinute = number
        val newConditionView = HetzerConditionView(this)
        newConditionView.setText(getString(R.string.Condition_Display_RecentDate, number))
        newConditionView.setMoreButtonCallback {
            createHetzerConditionPopupMenu(
                this@HetzerConditionsActivity,
                newConditionView
            ) {
                conditions.avoidRecentMinute = 0
            }
        }
        layout_scrollContent.addView(newConditionView, 0)
    }

    private fun addCondition_FavMySelf() {
        conditions.avoidMyFav = true
        val newConditionView = HetzerConditionView(this)
        newConditionView.setText(getString(R.string.Condition_FavMyself))
        newConditionView.setMoreButtonCallback {
            createHetzerConditionPopupMenu(
                this@HetzerConditionsActivity,
                newConditionView
            ) {
                conditions.avoidMyFav = false
            }
        }
        layout_scrollContent.addView(newConditionView, 0)
    }

    private fun addCondition_IncludedKeyword(keyword: String) {
        if(conditions.avoidKeywords.contains(keyword)) return
        conditions.avoidKeywords.add(keyword)
        val newConditionView = HetzerConditionView(this)
        newConditionView.setText(getString(R.string.Condition_Display_IncludedKeyword, keyword))
        newConditionView.setMoreButtonCallback {
            createHetzerConditionPopupMenu(
                this@HetzerConditionsActivity,
                newConditionView
            ) {
                conditions.avoidKeywords.remove(keyword)
            }
        }
        layout_scrollContent.addView(newConditionView, 0)
    }

    private fun addCondition_IncludedMedia() {
        conditions.avoidMedia = true
        val newConditionView = HetzerConditionView(this)
        newConditionView.setText(getString(R.string.Condition_IncludedMedia))
        newConditionView.setMoreButtonCallback {
            createHetzerConditionPopupMenu(
                this@HetzerConditionsActivity,
                newConditionView
            ) {
                conditions.avoidMedia = false
            }
        }
        layout_scrollContent.addView(newConditionView, 0)
    }

    private fun addCondition_ExcludedMedia() {
        conditions.avoidNoMedia = true
        val newConditionView = HetzerConditionView(this)
        newConditionView.setText(getString(R.string.Condition_ExcludedMedia))
        newConditionView.setMoreButtonCallback {
            createHetzerConditionPopupMenu(
                this@HetzerConditionsActivity,
                newConditionView
            ) {
                conditions.avoidNoMedia = false
            }
        }
        layout_scrollContent.addView(newConditionView, 0)
    }

    private fun addCondition_IncludedGeo() {
        conditions.avoidNoGeo = true
        val newConditionView = HetzerConditionView(this)
        newConditionView.setText(getString(R.string.Condition_IncludedGEO))
        newConditionView.setMoreButtonCallback {
            createHetzerConditionPopupMenu(
                this@HetzerConditionsActivity,
                newConditionView
            ) {
                conditions.avoidNoGeo = false
            }
        }
        layout_scrollContent.addView(newConditionView, 0)
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
        menu.gravity = Gravity.END
        menu.show()
    }

}
