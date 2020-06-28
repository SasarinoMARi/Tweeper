package com.sasarinomari.tweeper

import android.app.ActionBar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView

/**
 * 와, 이건 진짜 내가 만들었는데 잘 만든거같음
 * 우사긔. 그는 천재인가??
 *                           - 2020.06.28
 */
/**
 * RecyclerView에 동적으로 ViewHolder를 할당시켜서 사용할 수 있게 해주는 어댑터.
 * 일반 뷰와 리스트 모두 삽입 가능.
 */
class RecyclerInjector : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    abstract class RecyclerFragment {
        internal var layoutId: Int = 0
        private var _list : ArrayList<*>? = null
        constructor(layoutId: Int) { this.layoutId = layoutId}
        constructor(layoutId: Int, list: ArrayList<*>): this(layoutId) { bindList(list) }

        // region List Methods
        fun bindList(list: ArrayList<*>) {
            this._list = list
        }
        val isList: Boolean get() { return _list != null }
        val count: Int get() { return if (isList) _list!!.size else 1 }
        fun getListItem(index: Int): Any? {
            return if(isList) this._list!![index]
            else null
        }
        fun removeListItem(listItemIndex: Int) {
            if(!isList) return
            this._list!!.removeAt(listItemIndex)
        }
        // endregion

        open fun createViewHolder(view: View) : RecyclerView.ViewHolder {
            return object: RecyclerView.ViewHolder(view) { }
        }
        abstract fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int)
        open fun onClickListItem(item: Any?) { }
        open fun inflate(parent: ViewGroup): View {
            return LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        }
    }

    private val fragments = ArrayList<RecyclerFragment>()

    fun add(rFragment: RecyclerFragment) {
        this.fragments.add(rFragment)
    }

    fun addSpace(space: Int) {
        this.fragments.add(object: RecyclerFragment(0) {
            override fun draw(view: View, item: Any?, viewType: Int, listItemIndex: Int) { }

            override fun inflate(parent: ViewGroup): View {
                val view = FrameLayout(parent.context)
                val params = FrameLayout.LayoutParams( FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT )
                params.setMargins(0,space * 10,0, 0)
                view.layoutParams = params
                return view
            }
        })
    }

    override fun getItemViewType(position: Int): Int {
        var counter = 0
        for(i in 0 until fragments.count()) {
            val f = fragments[i]
            val startIndex = counter
            val endIndex = counter + f.count
            if(position in startIndex until endIndex) {
                return i
            }
            counter = endIndex
        }
        throw Exception("Fragment Out Of Range")
    }
    private fun getItemListIndex(position: Int): Int {
        var counter = 0
        for(i in 0 until fragments.count()) {
            val f = fragments[i]
            val startIndex = counter
            val endIndex = counter + f.count
            if(position in startIndex until endIndex) {
                return position - startIndex
            }
            counter = endIndex
        }
        throw Exception("Fragment Out Of Range")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val fragment = fragments[viewType]
        val view = fragment.inflate(parent)
        return fragment.createViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        val fragment = fragments[viewType]
        val listItemIndex = getItemListIndex(position)
        val item = fragment.getListItem(listItemIndex)
        fragment.draw(holder.itemView, item, viewType, listItemIndex)
        if(fragment.isList) holder.itemView.setOnClickListener { fragment.onClickListItem(item!!) }
    }

    fun getItemCount(viewType: Int): Int {
        return fragments[viewType].count
    }
    override fun getItemCount(): Int {
        var counter = 0
        for(f in fragments) { counter += f.count }
        return counter
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun removeListItem(viewType: Int, listItemIndex: Int) {
        val f = fragments[viewType]
        f.removeListItem(listItemIndex)
    }
}