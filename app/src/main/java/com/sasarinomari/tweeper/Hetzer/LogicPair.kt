package com.sasarinomari.tweeper.Hetzer

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sasarinomari.tweeper.Authenticate.AuthData
import com.sasarinomari.tweeper.R
import kotlinx.android.synthetic.main.activity_logic_pair_edit.*
import kotlinx.android.synthetic.main.item_logic.view.*
import kotlinx.android.synthetic.main.item_logicpair.view.*

class LogicPair(val logicType: LogicType) {
    /**
     * ordinal을 사용할 것
     */
    enum class LogicType {
        Save, Remove
    }

    fun validate(): Boolean {
        return true
    }

    var everything: Boolean? = null                 // 모든 트윗
    var includeFavorite: Boolean? = null            // 내가 마음에 들어한 트윗
    var excludeFavorite: Boolean? = null            // 내가 마음에 들어하지 않은 트윗
    var includeRetweet: Boolean? = null             // 내가 리트윗한 트윗
    var excludeRetweet: Boolean? = null             // 내가 리트윗하지 않은 트윗
    var includeFavoriteOver: Int? = null            // N회 이상 마음 받은 트윗
    var includeFavoriteUnder: Int? = null           // N회 이하 마음 받은 트윗
    var includeRetweetOver: Int? = null             // N회 이상 리트윗 받은 트윗
    var includeRetweetUnder: Int? = null            // N회 이하 리트윗 받은 트윗
    var includeMedia: Boolean? = null               // 미디어를 포함한 트윗
    var excludeMedia: Boolean? = null               // 미디어를 포함하지 않은 트윗
    val includeKeywords = ArrayList<String>()       // 키워드를 포함한 트윗
    val excludeKeywords = ArrayList<String>()       // 키워드를 포함하지 않은 트윗
    var includeGEO: Boolean? = null                 // 위치 정보를 포함한 트윗
    var excludeGEO: Boolean? = null                 // 위치 정보를 포함하지 않은 트윗
    var includeRecentTweetNumber: Int? = null       // 최근 N개 까지의 트윗 (Int)
    var includeRecentTweetUntil: Int? = null        // 최근 N분 이내의 트윗 (Int)

    /**
     * SharedPreference에 LogicPair 리스트를 읽고 쓰는 클래스
     */
    class Recorder(private val context: Context) {
        private var prefId = "logicPairs"

        fun set(logics: List<LogicPair>) {
            val prefs = context.getSharedPreferences(prefId, Context.MODE_PRIVATE).edit()
            val json = Gson().toJson(logics)
            prefs.putString(getKey(), json)
            prefs.apply()
        }

        fun get(): List<LogicPair> {
            val prefs = context.getSharedPreferences(prefId, Context.MODE_PRIVATE)
            val json = prefs.getString(getKey(), null) ?: return ArrayList()
            val type = object : TypeToken<List<LogicPair>>() {}.type
            return try {
                Gson().fromJson(json, type)
            } catch (e: Exception) {
                ArrayList()
            }
        }

        private fun getKey(): String {
            return "lp" + AuthData.Recorder(context).getFocusedUser()!!.user!!.id
        }
    }

    fun isEmpty() : Boolean {
        return  everything==null &&
                includeFavorite==null &&
                excludeFavorite==null &&
                excludeRetweet== null &&
                includeFavoriteOver== null &&
                includeFavoriteUnder== null &&
                includeRetweetOver== null &&
                includeRetweetUnder== null &&
                includeMedia== null &&
                excludeMedia== null &&
                includeKeywords.isEmpty() &&
                excludeKeywords.isEmpty() &&
                includeGEO== null &&
                excludeGEO== null &&
                includeRecentTweetNumber== null &&
                includeRecentTweetUntil== null
    }
}

/**
 * 논리쌍에 대한 뷰 요소
 * 반드시 initialize() 메서드를 한 번 이상 호출한 뒤에 사용하여야 함!
 */
internal class LogicPairView(context: Context) : LinearLayout(context) {
    enum class Mode {
        View, Edit
    }

    private lateinit var lp : LogicPair
    val logicPair : LogicPair get() { return lp }
    private lateinit var mode : Mode

    private var eventListener : EventListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.item_logicpair, this, true)
    }

    /**
     * 전달받은 논리쌍으로
     * 뷰 초기값 지정하는 함수
     */
    fun initialize(mode: Mode, logicPair: LogicPair) {
        this.lp = logicPair
        this.mode = mode

        if(mode == Mode.View) {
            button_logicpair_more.setOnClickListener {
                createLogicPairContextMenu(context,button_logicpair_more)
            }
        }
        else {
            button_logicpair_more.visibility = View.GONE
        }

        text_subject.text = when(lp.logicType) {
            LogicPair.LogicType.Save -> context.getString(R.string.LogicPairSubject_Save)
            LogicPair.LogicType.Remove -> context.getString(R.string.LogicPairSubject_Delete)
        }

        checkEmptyAndDisplayNotice()

        if (lp.everything != null) addLogic_Everything(true)
        if (lp.includeFavorite != null) addLogic_includeFav(true)
        if (lp.excludeFavorite != null) addLogic_excludeFav(true)
        if (lp.includeRetweet != null) addLogic_includeRt(true)
        if (lp.excludeRetweet != null) addLogic_excludeRt(true)
        if (lp.includeFavoriteOver != null) addLogic_favOver(lp.includeFavoriteOver!!, true)
        if (lp.includeFavoriteUnder != null) addLogic_favUnder(lp.includeFavoriteUnder!!, true)
        if (lp.includeRetweetOver != null) addLogic_RtOver(lp.includeRetweetOver!!, true)
        if (lp.includeRetweetUnder != null) addLogic_RtUnder(lp.includeRetweetUnder!!, true)
        if (lp.includeMedia != null) addLogic_includeMedia(true)
        if (lp.excludeMedia != null) addLogic_excludeMedia(true)
        for (keyword in lp.includeKeywords) {
            addLogic_includeKeyword(keyword, true)
        }
        for (keyword in lp.excludeKeywords) {
            addLogic_excludeKeyword(keyword, true)
        }
        if (lp.includeGEO != null) addLogic_includeGeo(true)
        if (lp.excludeGEO != null) addLogic_excludeGeo(true)
        if (lp.includeRecentTweetNumber != null) addLogic_recentNumber(lp.includeRecentTweetNumber!!, true)
        if (lp.includeRecentTweetUntil != null) addLogic_recentMinute(lp.includeRecentTweetUntil!!, true)
    }

    interface EventListener {
        fun onRemove(logicPair: LogicPair)
        fun onEdit(logicPair: LogicPair)
    }
    fun setEventListener(eventListener: EventListener) {
        this.eventListener = eventListener
    }

    /**
     * 트윗 청소기 조건 아이템 뷰 추가
     */
    private fun addLogicview(text: String, callback: () -> Unit) {
        checkEmptyAndDisplayNotice()

        val logic = LogicView(context)
        logic.setText(text)
        if(mode == Mode.Edit) {
            logic.setMoreButtonCallback {
                createLogicContextMenu(
                    context,
                    logic,
                    callback
                )
            }
        }
        else {
            logic.button_logic_more.visibility = View.GONE
        }
        layout_content.addView(logic, 0)
    }

    // region 조건 추가 코드

    // 모든 트윗
    fun addLogic_Everything(viewOnly: Boolean = false) {
        if (!viewOnly) lp.everything = true
        addLogicview(context.getString(R.string.HetzerConditions_0)) { lp.everything = null }
    }

    // 내가 마음에 들어한 트윗
    fun addLogic_includeFav(viewOnly: Boolean = false) {
        if (!viewOnly) lp.includeFavorite = true
        addLogicview(context.getString(R.string.HetzerConditions_1)) { lp.includeFavorite = null }
    }

    // 내가 마음에 들어하지 않은 트윗
    fun addLogic_excludeFav(viewOnly: Boolean = false) {
        if (!viewOnly) lp.excludeFavorite = true
        addLogicview(context.getString(R.string.HetzerConditions_2)){lp.excludeFavorite = null}
    }

    // 내가 리트윗한 트윗
    fun addLogic_includeRt(viewOnly: Boolean = false) {
        if (!viewOnly) lp.includeRetweet = true
        addLogicview(context.getString(R.string.HetzerConditions_3)){lp.includeRetweet = null}
    }

    // 내가 리트윗하지 않은 트윗
    fun addLogic_excludeRt(viewOnly: Boolean = false) {
        if (!viewOnly) lp.excludeRetweet = true
        addLogicview(context.getString(R.string.HetzerConditions_4)){lp.excludeRetweet = null}
    }

    // N회 이상 마음 받은 트윗 (Int)
    fun addLogic_favOver(number: Int, viewOnly: Boolean = false) {
        if (!viewOnly) lp.includeFavoriteOver = number
        addLogicview(context.getString(R.string.HetzerConditions_5, number.toString())){lp.includeFavoriteOver = null}
    }

    // N회 이하 마음 받은 트윗 (Int)
    fun addLogic_favUnder(number: Int, viewOnly: Boolean = false) {
        if (!viewOnly) lp.includeFavoriteUnder = number
        addLogicview(context.getString(R.string.HetzerConditions_6, number.toString())){lp.includeFavoriteUnder = null}
    }

    // N회 이상 리트윗 받은 트윗 (Int)
    fun addLogic_RtOver(number: Int, viewOnly: Boolean = false) {
        if (!viewOnly) lp.includeRetweetOver = number
        addLogicview(context.getString(R.string.HetzerConditions_7, number.toString())){lp.includeRetweetOver = null}
    }

    // N회 이하 리트윗 받은 트윗 (Int)
    fun addLogic_RtUnder(number: Int, viewOnly: Boolean = false) {
        if (!viewOnly) lp.includeRetweetUnder = number
        addLogicview(context.getString(R.string.HetzerConditions_8, number.toString())){lp.includeRetweetUnder = null}
    }

    // 미디어를 포함한 트윗
    fun addLogic_includeMedia(viewOnly: Boolean = false) {
        if (!viewOnly) lp.includeMedia = true
        addLogicview(context.getString(R.string.HetzerConditions_9)){lp.includeMedia = null}
    }

    // 미디어를 포함하지 않은 트윗
    fun addLogic_excludeMedia(viewOnly: Boolean = false) {
        if (!viewOnly) lp.excludeMedia = true
        addLogicview(context.getString(R.string.HetzerConditions_10)){lp.excludeMedia = null}
    }

    // 키워드를 포함한 트윗 (ArrayList<Sring>)
    fun addLogic_includeKeyword(keyword: String, viewOnly: Boolean = false) {
        if(!viewOnly && !lp.includeKeywords.contains(keyword)) lp.includeKeywords.add(keyword)
        addLogicview(context.getString(R.string.HetzerConditions_11, keyword)){lp.includeKeywords.remove(keyword)}
    }

    // 키워드를 포함하지 않은 트윗 (ArrayList<Sring>)
    fun addLogic_excludeKeyword(keyword: String, viewOnly: Boolean = false) {
        if(!viewOnly && !lp.excludeKeywords.contains(keyword)) lp.excludeKeywords.add(keyword)
        addLogicview(context.getString(R.string.HetzerConditions_12, keyword)){lp.excludeKeywords.remove(keyword)}
    }

    // 위치 정보를 포함한 트윗
    fun addLogic_includeGeo(viewOnly: Boolean = false) {
        if (!viewOnly) lp.includeGEO = true
        addLogicview(context.getString(R.string.HetzerConditions_13)){lp.includeGEO = null}
    }

    // 위치 정보를 포함하지 않은 트윗
    fun addLogic_excludeGeo(viewOnly: Boolean = false) {
        if (!viewOnly) lp.excludeGEO = true
        addLogicview(context.getString(R.string.HetzerConditions_14)){lp.excludeGEO = null}
    }

    // 최근 N개 까지의 트윗 (Int)
    fun addLogic_recentNumber(number: Int, viewOnly: Boolean = false) {
        if (!viewOnly) lp.includeRecentTweetNumber = number
        addLogicview(context.getString(R.string.HetzerConditions_15, number.toString())){lp.includeRecentTweetNumber = null}
    }

    // 최근 N분 이내의 트윗 (Int)
    fun addLogic_recentMinute(minute: Int, viewOnly: Boolean = false) {
        if (!viewOnly) lp.includeRecentTweetUntil = minute
        addLogicview(context.getString(R.string.HetzerConditions_16, minute.toString())){lp.includeRecentTweetUntil = null}
    }

    // endregion

    /**
     * Edit 모드에서의
     * 개별 Logic에 대한 팝업 메뉴 생성 코드
     */
    private fun createLogicContextMenu(context: Context, view: View, deleteCallback: () -> Unit) {
        val menu = PopupMenu(context, view)
        menu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.option_delete -> {
                    layout_content.removeView(view)
                    deleteCallback()
                }
            }
            true
        }
        menu.inflate(R.menu.logic_menu)
        menu.gravity = Gravity.END
        menu.show()
    }

    /**
     * View 모드에서의
     * 전체 LogicPair에 대한 팝업 메뉴 생성 코드
     */
    private fun createLogicPairContextMenu(context: Context, view: View) {
        val menu = PopupMenu(context, view)
        menu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.option_edit -> {
                    eventListener?.onEdit(lp)
                }
                R.id.option_delete -> {
                    eventListener?.onRemove(lp)
                }
            }
            true
        }
        menu.inflate(R.menu.logicpair_menu)
        menu.gravity = Gravity.END
        menu.show()
    }

    /**
     * 아이템이 없으면 아이템이 없다는 문구 출력하는 코드
     */
    private fun checkEmptyAndDisplayNotice() {
        if(logicPair.isEmpty()) {
            layout_noItem.visibility = View.VISIBLE
        }
        else {
            layout_noItem.visibility = View.GONE
        }
    }
}

/**
 * 개별 논리에 대한 뷰 요소
 */
internal class LogicView(context: Context) : LinearLayout(context) {
    fun setText(text: String) {
        this.card_title.text = text
    }

    fun setMoreButtonCallback(callback: () -> Unit) {
        this.button_logic_more.setOnClickListener {
            callback()
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.item_logic, this, true)
    }
}