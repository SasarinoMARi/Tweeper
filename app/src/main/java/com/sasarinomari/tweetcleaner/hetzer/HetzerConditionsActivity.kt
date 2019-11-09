package com.sasarinomari.tweetcleaner.hetzer

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
import com.sasarinomari.tweetcleaner.Adam
import com.sasarinomari.tweetcleaner.R
import kotlinx.android.synthetic.main.activity_hetzer_conditions.*

class HetzerConditionsActivity : Adam() {
    enum class Results {
        Conditions
    }

    var conditions = HetzerConditions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hetzer_conditions)

        val addDialog = createAddConditionDialog()
        button_addContition.setOnClickListener {
            addDialog.show {
                attachItemLists(this)
            }
        }
        button_ok.setOnClickListener {
            val i = Intent()
            i.putExtra(Results.Conditions.name, conditions)
            setResult(RESULT_OK, i)
            finish()
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
        listItems.add(getString(R.string.Condition_FavMyself))
        listItems.add(getString(R.string.Condition_RetweetCount))
        listItems.add(getString(R.string.Condition_FavCount))
        listItems.add(getString(R.string.Condition_RecentCount))
        listItems.add(getString(R.string.Condition_RecentMinute))
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
