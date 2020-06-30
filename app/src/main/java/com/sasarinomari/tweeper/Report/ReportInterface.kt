package com.sasarinomari.tweeper.Report

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * 트윗지기 서비스에서 보고서 입출력에 사용하는 클래스.
 */
class ReportInterface<T>(private val prefix: String) {
    fun getReportCount(context: Context): Int {
        val reports = getPath(context).list()!!
        var maxN = -1
        for (name in reports) {
            if (name.startsWith(prefix)) {
                val n = name.removePrefix(prefix).toInt()
                if (n > maxN) maxN = n
            }
        }
        return maxN
    }

    private fun getPath(context: Context): File {
        val path = File(context.filesDir, prefix)
        if (!path.exists()) {
            val result = path.mkdir()
            if (!result) throw Exception("$prefix 내부 저장소 디렉터리 생성에 실패했습니다.")
        }
        return path
    }

    fun writeReport(context: Context, reportId: Int, statuses: T) {
        File(getPath(context), "$prefix$reportId").writeText(Gson().toJson(statuses), Charsets.UTF_8)
    }

    fun readReport(context: Context, reportId: Int): T? {
        var report: T? = null
        try{
            val text = File(getPath(context), "$prefix$reportId").readText(Charsets.UTF_8)
            report = Gson().fromJson(text, object : TypeToken<T>() {}.type)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        return report
    }

    /**
        제너릭스를 포함한 복잡한 클래스에서는 Gson이 오동작.
        구글에 "LinkedTreeMap cannot be cast to" 로 검색하면 대충 나옴.
        더 좋은 해결책 알면 고치셈. 일단 저는 모르겟음 ^^7
     */
    fun readReport(context: Context, reportId: Int, cls: Any): Any? {
        var report: T? = null
        try{
            val text = File(getPath(context), "$prefix$reportId").readText(Charsets.UTF_8)
            report = Gson().fromJson(text, TypeToken.getParameterized(cls::class.java).type)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        return report
    }

    /**
     * 제너릭스 없이 실행 가능
     */
    fun getReportsWithNameAndCreatedDate(context: Context): ArrayList<Pair<String, Date>> {
        val list = ArrayList<Pair<String, Date>>()
        for (item in getPath(context).list()!!) {
            list.add(Pair(item, Date(File(item).lastModified())))
        }
        return list
    }


    fun getReports(context: Context): ArrayList<T> {
        val list = ArrayList<T>()
        val names = getReportsWithNameAndCreatedDate(context)
        for(name in names) {
            val report = readReport(context, name.first.removePrefix(prefix).toInt())
            if(report != null) list.add(report)
        }
        return list
    }
    fun getReports(context: Context, cls: Any): ArrayList<Any> {
        val list = ArrayList<Any>()
        val names = getReportsWithNameAndCreatedDate(context)
        for(name in names) {
            val report = readReport(context, name.first.removePrefix(prefix).toInt(), cls)
            if(report != null) list.add(report)
        }
        return list
    }
}