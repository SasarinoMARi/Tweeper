package com.sasarinomari.tweeper.Hetzer.New

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.sasarinomari.tweeper.Hetzer.New.Conditions.*
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.RecyclerInjector
import com.sasarinomari.tweeper.SimplizatedClass.Status
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*
import kotlinx.android.synthetic.main.full_recycler_view.*
import kotlinx.android.synthetic.main.item_checkable.view.*
import kotlinx.android.synthetic.main.item_default.view.*
import java.text.SimpleDateFormat

class AddNewConditionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.full_recycler_view)

        val statement = Hetzer.Action.Save
        val adapter = RecyclerInjector()
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_title_with_desc) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.title_text.text = getString(R.string.AddCondition)
                view.title_description.text = when (statement) {
                    Hetzer.Action.Save -> "지우지 않고 남겨둘 트윗을 지정하세요."
                    Hetzer.Action.Delete -> "지울 트윗을 지정하세요."
                }
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.item_checkable, getConditionItems()) {
            @SuppressLint("SimpleDateFormat")
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                item as Pair<String, Boolean>
                view.checkableItem_text.text = item.first
                view.checkableItem_check.visibility = if(item.second) View.VISIBLE else View.INVISIBLE
            }

            override fun onClickListItem(item: Any?) {
                for(i in 0 until this.list!!.size) {
                    val it = list!![i] as Pair<String, Boolean>
                    if(it == item) (list!!as ArrayList<Pair<String, Boolean>>)[i] = it.copy(second = true)
                    else if(it.second) (list!!as ArrayList<Pair<String, Boolean>>)[i] = it.copy(second = false)
                }
                adapter.notifyDataSetChanged()
            }
        })
        adapter.addSpace(20)
        root.layoutManager = LinearLayoutManager(this)
        root.adapter = adapter
    }

    private fun addConfirmView(): MaterialDialog {
        val addDialog = MaterialDialog(this, BottomSheet(LayoutMode.MATCH_PARENT))
        addDialog.positiveButton(R.string.OK)
        return addDialog
    }

    private fun getConditionItems(): ArrayList<Pair<String, Boolean>> {
        val context = this@AddNewConditionActivity
        return arrayListOf(
            Pair(FavoriteByMe(true).toString(context), false),
            Pair(FavoriteByMe(false).toString(context), false),
            Pair(RetweetByMe(true).toString(context), false),
            Pair(RetweetByMe(false).toString(context), false),
            Pair(FavoriteCount(true, -1).toString(context), false),
            Pair(FavoriteCount(false, -1).toString(context), false),
            Pair(RetweetCount(true, -1).toString(context), false),
            Pair(RetweetCount(false, -1).toString(context), false),
            Pair(IncludeKeyword(true, "특정 문구").toString(context), false),
            Pair(IncludeKeyword(false, "특정 문구").toString(context), false),
            Pair(IncludeMedia(true).toString(context), false),
            Pair(IncludeMedia(false).toString(context), false),
            Pair(IncludeGeo(true).toString(context), false),
            Pair(IncludeGeo(false).toString(context), false),
            Pair(RecentByMinute(true, -1).toString(context), false),
            Pair(RecentByMinute(false, -1).toString(context), false),
            Pair(RecentByCount(true, -1).toString(context), false),
            Pair(RecentByCount(false, -1).toString(context), false)
        )
    }
}