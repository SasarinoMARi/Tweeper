package com.sasarinomari.tweeper.report

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sasarinomari.tweeper.DefaultListItem
import com.sasarinomari.tweeper.R
import com.sasarinomari.tweeper.hetzer.HetzerReportActivity
import kotlinx.android.synthetic.main.fragment_report_list.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 안씀!
 * 기술적인 문제는 없지만 이거 말고 DefualtRecycleAdapter나 쓰셈 ㅋ
 * 사유: 촌티남
 */
/**
 * 트윗지기 서비스에서 생성된 리포트 조회용 공용 프래그먼트.
 * Parameters 전부가 인자로 와야 실행 가능.
 *
 * ReportActivityName은 아이템 클릭 시 redirect할 class 이름이다.
 * Class::class.java.name과 같이 써서 인자로 사용할 수 있다.
 * 
 * 추가적으로 setListViewHeightBasedOnChildren(View) 라는 마법의 함수를 실행하면-
 * 이 프래그먼트를 추가한 액티비티에서 스크롤이 내려가버림(?)(???)
 * 대체 왜이럼?? 전 몰겟구여 암튼 알아서 스크롤 올리는 코드 넣어 쓰세용ㅋㅋ
 */
@Deprecated("DefualtRecycleAdapter")
class ReportListFragment(private val intent: Intent,
                         private val callback: () -> Unit) : Fragment() {
    enum class Parameters {
        NoReportDescription,
        ReportPrefix, ReportActivityName
    }

    private fun checkRequirements(): Boolean {
        for(parameter in Parameters.values())
            if(!intent.hasExtra(parameter.name)) return false
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!checkRequirements()) throw Exception("${this::class.java.name} Requirement not ready.")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_report_list, container, false);

        rootView.text_noReport.text = intent.getStringExtra(Parameters.NoReportDescription.name)

        val reportPrefix = intent.getStringExtra(Parameters.ReportPrefix.name)!!
        val reports = ReportInterface<Any>(reportPrefix).getReportsWithNameAndCreatedDate(this.context!!)
        if (reports.isEmpty()) {
            rootView.layout_noReport.visibility = View.VISIBLE
            rootView.listView.visibility = View.GONE
        } else {
            val adapter = object : DefaultListItem(reports) {
                @SuppressLint("SimpleDateFormat")
                override fun drawItem(item: Any, title: TextView, description: TextView) {
                    item as Pair<*, *>
                    title.text = item.first.toString()
                    description.text = SimpleDateFormat(getString(R.string.DateFormat)).format(item.second as Date)
                }
            }
            rootView.listView.adapter = adapter
            rootView.listView.setOnItemClickListener { _, _, index, _ ->
                val reportIndex = (adapter.getItem(index) as Pair<*, *>).first.toString().removePrefix(reportPrefix).toInt()
                val intent = Intent(this.context,
                    Class.forName(intent.getStringExtra(Parameters.ReportActivityName.name)!!))
                intent.putExtra(HetzerReportActivity.Parameters.ReportId.name, reportIndex)
                startActivity(intent)
            }
            setListViewHeightBasedOnChildren(rootView)
        }
        return rootView
    }

    override fun onResume() {
        callback()
        super.onResume()
    }

    private fun setListViewHeightBasedOnChildren(rootView: View) {
        val listAdapter = rootView.listView.adapter ?: return
        var totalHeight = 0

        /**
         * 모든 listItem의 크기가 같다면 하나만 measure 해도 될 듯함.
         * 근데 내가 무슨 부귀영화를 누리겠다고 그렇게까지 최적화를..?
          */
        for ( i in 0 until listAdapter.count) {
            val listItem = listAdapter.getView(i, null, rootView.listView)
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight
        }

        val params = rootView.listView.layoutParams
        params.height = totalHeight + (rootView.listView.dividerHeight * (listAdapter.count - 1))
        rootView.listView.layoutParams = params
        rootView.listView.requestLayout()
    }


}