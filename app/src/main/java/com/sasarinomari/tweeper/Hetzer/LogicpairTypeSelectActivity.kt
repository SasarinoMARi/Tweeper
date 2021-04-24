package com.sasarinomari.tweeper.Hetzer

import android.content.Intent
import android.os.Bundle
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.R
import kotlinx.android.synthetic.main.activity_logicpair_type_select.*
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*

class LogicpairTypeSelectActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logicpair_type_select)

        layout_title_and_desc.title_text.text = getString(R.string.TweetCleaner)
        layout_title_and_desc.title_description.text = getString(R.string.TweetCleanerDescription)

        button_addSaveRule.setOnClickListener {
            val i = Intent(this@LogicpairTypeSelectActivity, LogicPairEditActivity::class.java)
            i.putExtra(LogicPairEditActivity.Parameters.LogicType.name, LogicPair.LogicType.Save.ordinal)
            startActivityForResult(i, 0)
        }

        button_addRemoveRule.setOnClickListener {
            val i = Intent(this@LogicpairTypeSelectActivity, LogicPairEditActivity::class.java)
            i.putExtra(LogicPairEditActivity.Parameters.LogicType.name, LogicPair.LogicType.Remove.ordinal)
            startActivityForResult(i, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 0) {
            if(resultCode == RESULT_OK) {
                setResult(RESULT_OK, data)
                finish()
            }
        }
        else super.onActivityResult(requestCode, resultCode, data)
    }
}