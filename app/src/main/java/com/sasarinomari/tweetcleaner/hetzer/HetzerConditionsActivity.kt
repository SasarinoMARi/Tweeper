package com.sasarinomari.tweetcleaner.hetzer

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
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
                MaterialDialog(this@HetzerConditionsActivity).show{
                    input(waitForPositiveButton = false) { dialog, text ->
                        val inputField = dialog.getInputField()
                        val number = text.toString().toIntOrNull()
                        val isInteger = number != null
                        inputField.error = if (isInteger) null else getString(R.string.OnlyNumberAllowed)
                        dialog.setActionButtonEnabled(WhichButton.POSITIVE, isInteger)
                        if(number!=null) {
                            addCondition_RetweetCount(number)
                        }
                    }
                    title(text = text)
                    message(R.string.Condition_Description_RetweetCount)
                    positiveButton(R.string.OK)
                }
            }
            getString(R.string.Condition_FavCount) -> {
                MaterialDialog(this@HetzerConditionsActivity).show{
                    input(waitForPositiveButton = false) { dialog, text ->
                        val inputField = dialog.getInputField()
                        val number = text.toString().toIntOrNull()
                        val isInteger = number != null
                        inputField.error = if (isInteger) null else getString(R.string.OnlyNumberAllowed)
                        dialog.setActionButtonEnabled(WhichButton.POSITIVE, isInteger)
                        if(number!=null) {
                            addCondition_FavCount(number)
                        }
                    }
                    title(text = text)
                    message(R.string.Condition_Description_FavCount)
                    positiveButton(R.string.OK)
                }
            }
            getString(R.string.Condition_RecentCount) -> {
                MaterialDialog(this@HetzerConditionsActivity).show{
                    input(waitForPositiveButton = false) { dialog, text ->
                        val inputField = dialog.getInputField()
                        val number = text.toString().toIntOrNull()
                        val isInteger = number != null
                        inputField.error = if (isInteger) null else getString(R.string.OnlyNumberAllowed)
                        dialog.setActionButtonEnabled(WhichButton.POSITIVE, isInteger)
                        if(number!=null) {
                            addCondition_RecentCount(number)
                        }
                    }
                    title(text = text)
                    message(R.string.Condition_Description_RecentCount)
                    positiveButton(R.string.OK)
                }
            }
            getString(R.string.Condition_RecentMinute) -> {
                MaterialDialog(this@HetzerConditionsActivity).show{
                    input(waitForPositiveButton = false,
                        allowEmpty = false) { dialog, text ->
                        val inputField = dialog.getInputField()
                        val number = text.toString().toIntOrNull()
                        val isInteger = number != null
                        inputField.error = if (isInteger) null else getString(R.string.OnlyNumberAllowed)
                        dialog.setActionButtonEnabled(WhichButton.POSITIVE, isInteger)
                        if(number!=null) {
                            addCondition_RecentMinute(number)
                        }
                    }
                    title(text = text)
                    message(R.string.Condition_Description_RecentMinute)
                    positiveButton(R.string.OK)
                }
            }
        }
        }
        return addDialog
    }

    private fun addCondition_RetweetCount(number: Int) {

    }

    private fun addCondition_FavCount(number: Int) {

    }

    private fun addCondition_RecentCount(number: Int) {

    }

    private fun addCondition_RecentMinute(number: Int) {

    }

    private fun addCondition_FavMySelf() {

    }


}
