package com.sasarinomari.tweetcleaner.hetzer

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.sasarinomari.tweetcleaner.R
import kotlinx.android.synthetic.main.activity_hetzer_conditions.*

class HetzerConditionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hetzer_conditions)

        val addDialog = createAddConditionDialog()
        button_addContition.setOnClickListener {
            addDialog.show()
        }
    }

    @SuppressLint("CheckResult")
    private fun createAddConditionDialog(): MaterialDialog {
        val addDialog = MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT))
        val listItems = listOf(
            getString(R.string.Condition_FavMyself),
            getString(R.string.Condition_RetweetCount),
            getString(R.string.Condition_FavCount),
            getString(R.string.Condition_RecentCount),
            getString(R.string.Condition_RecentMinute)
        )
        addDialog.title(R.string.AddCondition)
        addDialog.message(R.string.WhichTweetDoNotRemove)
        addDialog.negativeButton(R.string.Cancel)
        addDialog.positiveButton(R.string.OK) {

        }
        addDialog.listItemsSingleChoice(items = listItems) { _, _, textSeq ->
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
                        positiveButton(R.string.OK) {dialog ->
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
                        positiveButton(R.string.OK) {dialog ->
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
                        positiveButton(R.string.OK) {dialog ->
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
                        positiveButton(R.string.OK) {dialog ->
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
        return addDialog
    }

    private fun addCondition_RetweetCount(number: Int) {
        val newConditionView = HetzerCondition(this)
        newConditionView.setText(getString(R.string.Condition_Display_RetweetCount, number))
        newConditionView.setMoreButtonCallback { createHetzerConditionPopupMenu(this@HetzerConditionsActivity, newConditionView)}
        layout_scrollContent.addView(newConditionView, 0)
    }

    private fun addCondition_FavCount(number: Int) {
        val newConditionView = HetzerCondition(this)
        newConditionView.setText(getString(R.string.Condition_Display_FavCount, number))
        newConditionView.setMoreButtonCallback { createHetzerConditionPopupMenu(this@HetzerConditionsActivity, newConditionView)}
        layout_scrollContent.addView(newConditionView, 0)
    }

    private fun addCondition_RecentCount(number: Int) {
        val newConditionView = HetzerCondition(this)
        newConditionView.setText(getString(R.string.Condition_Display_RecentCount, number))
        newConditionView.setMoreButtonCallback { createHetzerConditionPopupMenu(this@HetzerConditionsActivity, newConditionView)}
        layout_scrollContent.addView(newConditionView, 0)
    }

    private fun addCondition_RecentMinute(number: Int) {
        val newConditionView = HetzerCondition(this)
        newConditionView.setText(getString(R.string.Condition_Display_RecentDate, number))
        newConditionView.setMoreButtonCallback { createHetzerConditionPopupMenu(this@HetzerConditionsActivity, newConditionView)}
        layout_scrollContent.addView(newConditionView, 0)
    }

    private fun addCondition_FavMySelf() {
        val newConditionView = HetzerCondition(this)
        newConditionView.setText(getString(R.string.Condition_FavMyself))
        newConditionView.setMoreButtonCallback { createHetzerConditionPopupMenu(this@HetzerConditionsActivity, newConditionView)}
        layout_scrollContent.addView(newConditionView, 0)
    }

    private fun createHetzerConditionPopupMenu(context: Context, view: View) {
        val menu = PopupMenu(context, view)
        menu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.option_delete -> {
                    layout_scrollContent.removeView(view)
                }
            }
            true }
        menu.inflate(R.menu.hetzer_contition_item_menu)
        menu.gravity = Gravity.END
        menu.show()
    }

}
