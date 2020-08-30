package com.sasarinomari.tweeper.Hetzer.New

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.sasarinomari.tweeper.Base.BaseActivity
import com.sasarinomari.tweeper.Hetzer.New.Conditions.*
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.RecyclerInjector
import kotlinx.android.synthetic.main.fragment_button_holder.view.*
import kotlinx.android.synthetic.main.fragment_title_with_desc.view.*
import kotlinx.android.synthetic.main.item_checkable.view.*

/**
 * Hetzer에 새 조건을 추가할 때 사용하는 액티비티입니다.
 *
 *  Parameters
 *      - IgnoreClassName (Array<String>) : 목록에 표시하지 않을 클래스 목록입니다.
 *
 *  Returns
 *      - ClassName : 사용자가 선택한 클래스 이름입니다.
 */
class AddNewConditionActivity : BaseActivity() {
    enum class Parameters {
        HetzerAction, IgnoreClassName
    }
    enum class Results {
        ConditionObject
    }
    private class ListItem(val className: String, val description: String, val statement: Boolean) {
        var enabled = false
    }

    private lateinit var buttonHolder: LinearLayout
    private var selectedItem: ListItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = RelativeLayout(this)
        root.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        setContentView(root)

        val recyclerView = RecyclerView(this)
        recyclerView.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        val statement = intent.getStringExtra(Parameters.HetzerAction.name)!!
        val labelAttachment = when(statement) {
            Hetzer.Action.Save.name -> " ${getString(R.string.Condition_Label_Positive_Attachment)}"
            Hetzer.Action.Delete.name -> " ${getString(R.string.Condition_Label_Negative_Attachment)}"
            else -> throw Exception()
        }

        val adapter = RecyclerInjector()
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.fragment_title_with_desc) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                view.title_text.text = getString(R.string.AddCondition)
                view.title_description.text = when (statement) {
                    Hetzer.Action.Save.name -> getString(R.string.Hetzer_PlzSelSaveTweets)
                    Hetzer.Action.Delete.name -> getString(R.string.Hetzer_PlzSelDelTweets)
                    else -> ""
                }
            }
        })
        adapter.add(object: RecyclerInjector.RecyclerFragment(R.layout.item_checkable,
            getConditionItems(intent.getStringArrayExtra(Parameters.IgnoreClassName.name)!!)) {
            @SuppressLint("SimpleDateFormat")
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) {
                item as ListItem
                view.checkableItem_text.text = item.description
                view.checkableItem_check.visibility = if(item.enabled) View.VISIBLE else View.INVISIBLE
            }

            override fun onClickListItem(item: Any?) {
                // 다른 모든 리스트 아이템 체크 해제
                for(i in 0 until this.list!!.size) {
                    val it = list!![i] as ListItem
                    if(it == item) it.enabled = true
                    else if(it.enabled) it.enabled = false
                    (list!!as ArrayList<ListItem>)[i] = it
                }
                adapter.notifyDataSetChanged()

                // 하단 버튼 숨겨져 있다면 보이게 설정
                if(buttonHolder.visibility == View.INVISIBLE) {
                    buttonHolder.visibility = View.VISIBLE

                    adapter.addSpace(40)
                    adapter.notifyDataSetChanged()
                }

                selectedItem = item!! as ListItem

                // 파라미터를 입력받아야 하는 경우 여기에서 처리합니다.
                when((item as ListItem).className) {
                    FavoriteCount::class.java.name -> {
                        buttonHolder.buttonHolder_layout_parameter.visibility = View.VISIBLE
                        buttonHolder.buttonHolder_label_parameter.text =
                            if(item.statement) getString(R.string.Condition_FavCount_Positive_Label) + labelAttachment
                            else getString(R.string.Condition_FavCount_Negative_Label) + labelAttachment
                        buttonHolder.buttonHolder_text_parameter.text = null
                    }
                    RetweetCount::class.java.name -> {
                        buttonHolder.buttonHolder_layout_parameter.visibility = View.VISIBLE
                        buttonHolder.buttonHolder_label_parameter.text =
                            if(item.statement) getString(R.string.Condition_RetweetCount_Positive_Label) + labelAttachment
                            else getString(R.string.Condition_RetweetCount_Negative_Label) + labelAttachment
                        buttonHolder.buttonHolder_text_parameter.text = null
                    }
                    IncludeKeyword::class.java.name -> {
                        buttonHolder.buttonHolder_layout_parameter.visibility = View.VISIBLE
                        buttonHolder.buttonHolder_label_parameter.text =
                            if(item.statement) getString(R.string.Condition_Keyword_Positive_Label) + labelAttachment
                            else getString(R.string.Condition_Keyword_Negative_Label) + labelAttachment
                        buttonHolder.buttonHolder_text_parameter.text = null
                    }
                    RecentByMinute::class.java.name -> {
                        buttonHolder.buttonHolder_layout_parameter.visibility = View.VISIBLE
                        buttonHolder.buttonHolder_label_parameter.text =
                            if(item.statement) getString(R.string.Condition_RecentMin_Positive_Label) + labelAttachment
                            else getString(R.string.Condition_RecentMin_Negative_Label) + labelAttachment
                        buttonHolder.buttonHolder_text_parameter.text = null
                    }
                    RecentByCount::class.java.name -> {
                        buttonHolder.buttonHolder_layout_parameter.visibility = View.VISIBLE
                        buttonHolder.buttonHolder_label_parameter.text =
                            if(item.statement) getString(R.string.Condition_RecentCount_Positive_Label) + labelAttachment
                            else getString(R.string.Condition_RecentCount_Negative_Label) + labelAttachment
                        buttonHolder.buttonHolder_text_parameter.text = null
                    }
                    else -> {
                        buttonHolder.buttonHolder_layout_parameter.visibility = View.GONE
                    }
                }
            }
        })
        adapter.addSpace(10)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        root.addView(recyclerView)

        buttonHolder = layoutInflater.inflate(R.layout.fragment_button_holder, LinearLayout(this)) as LinearLayout
        val lParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        lParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        buttonHolder.layoutParams = lParams
        buttonHolder.visibility = View.INVISIBLE
        buttonHolder.buttonHolder_button_ok.setOnClickListener {
            val item = selectedItem!!
            val parameter = buttonHolder.buttonHolder_text_parameter.text.toString()
            try{
                // IncludeKeyword 클래스이며 파라미터가 비어있을 때
                if(item.className == IncludeKeyword::class.java.name && parameter.isEmpty()) throw Exception()
                // IncludeKeyword 이외의 클래스인 경우 문자열을 정수로 파싱
                val converted = if(item.className != IncludeKeyword::class.java.name) parameter.toIntOrNull() else parameter
                val newCondition = ConditionObject.make(item.className, item.statement, converted)
                val i = Intent()
                i.putExtra(Results.ConditionObject.name, Gson().toJson(newCondition))
                setResult(RESULT_OK, i)
                finish()
            }
            catch(e: Exception) {
                // 잘못된 ConditionObject 파라미터
                da.error(getString(R.string.Error), getString(R.string.NeedRequrement)).show()
            }
        }
        root.addView(buttonHolder)
    }

    private fun getConditionItems(ignoreNames: Array<String>): ArrayList<ListItem> {
        val context = this@AddNewConditionActivity
        val list = arrayListOf(
            ListItem(FavoriteByMe::class.java.name, FavoriteByMe(true).toString(context), true),
            ListItem(FavoriteByMe::class.java.name, FavoriteByMe(false).toString(context), false),
            ListItem(RetweetByMe::class.java.name, RetweetByMe(true).toString(context), true),
            ListItem(RetweetByMe::class.java.name, RetweetByMe(false).toString(context), false),
            ListItem(FavoriteCount::class.java.name, FavoriteCount(true, -1).toString(context), true),
            ListItem(FavoriteCount::class.java.name, FavoriteCount(false, -1).toString(context), false),
            ListItem(RetweetCount::class.java.name, RetweetCount(true, -1).toString(context), true),
            ListItem(RetweetCount::class.java.name, RetweetCount(false, -1).toString(context), false),
            ListItem(IncludeKeyword::class.java.name, IncludeKeyword(true, "특정 문구").toString(context), true),
            ListItem(IncludeKeyword::class.java.name, IncludeKeyword(false, "특정 문구").toString(context), false),
            ListItem(IncludeMedia::class.java.name, IncludeMedia(true).toString(context), true),
            ListItem(IncludeMedia::class.java.name, IncludeMedia(false).toString(context), false),
            ListItem(IncludeGeo::class.java.name, IncludeGeo(true).toString(context), true),
            ListItem(IncludeGeo::class.java.name, IncludeGeo(false).toString(context), false),
            ListItem(RecentByMinute::class.java.name, RecentByMinute(true, -1).toString(context), true),
            ListItem(RecentByMinute::class.java.name, RecentByMinute(false, -1).toString(context), false),
            ListItem(RecentByCount::class.java.name, RecentByCount(true, -1).toString(context), true),
            ListItem(RecentByCount::class.java.name, RecentByCount(false, -1).toString(context), false)
        )

        list.removeAll { ignoreNames.contains(it.className) }
        return list
    }
}