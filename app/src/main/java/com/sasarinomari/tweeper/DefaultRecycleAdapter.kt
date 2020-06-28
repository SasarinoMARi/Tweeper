package com.sasarinomari.tweeper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_default.view.*

@Deprecated("더 좋은 클래스 RecyclerInjector를 준비해왔어요 ㅎㅎ..")
abstract class DefaultRecycleAdapter(private val items: List<*>,
                                     private val header: Int = 0,
                                     private val footer: Int = 0) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ViewType {
        Header, ListData, Footer
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0-> ViewType.Header.ordinal
            items.size+1 -> ViewType.Footer.ordinal
            else -> ViewType.ListData.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when(viewType) {
            ViewType.Header.ordinal -> {
                view = LayoutInflater.from(parent.context).inflate(header, parent, false)
                HeaderViewHolder(view)
            }
            ViewType.Footer.ordinal  -> {
                view = LayoutInflater.from(parent.context).inflate(footer, parent, false)
                FooterViewHolder(view)
            }
            ViewType.ListData.ordinal  -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.item_default, parent, false)
                ListViewHolder(view)
            }
            else -> throw Exception("ViewType Enum Out of Range.")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> drawHeader(holder.itemView)
            is FooterViewHolder -> drawFooter(holder.itemView)
            is ListViewHolder -> {
                val item = items[position - 1]!!
                drawListItem(item, holder.itemView.defaultitem_title, holder.itemView.defaultitem_description)
                holder.itemView.setOnClickListener{ onClickListItem(item) }
            }
        }
    }

    open fun drawHeader(view: View) { }
    open fun drawFooter(view: View) { }
    abstract fun drawListItem(item: Any, title: TextView, description: TextView)
    abstract fun onClickListItem(item: Any)

    // region List 관련 함수
    private fun existsHeader(): Int {
        return if(header == 0) 0 else 1
    }
    private fun existsFooter(): Int {
        return if(footer == 0) 0 else 1
    }
    override fun getItemCount(): Int {
        return items.size + existsHeader() + existsFooter()
    }
    override fun getItemId(position: Int): Long {
        return position.toLong() +existsHeader()
    }
    fun getItem(position: Int): Any? {
        return items[position+existsHeader()]
    }
    fun getItemToJson(position: Int): String? {
        return Gson().toJson(getItem(position))
    }
    // endregion
}