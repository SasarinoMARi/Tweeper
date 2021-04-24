package com.sasarinomari.tweeper.Report

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sasarinomari.tweeper.Tweeper
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 트윗지기 서비스에서 보고서 입출력에 사용하는 클래스.
 */
class ReportInterface<T>(private val userId: Long, private val prefix: String) {
    val df = SimpleDateFormat("yyyymmddhhMM", Locale.KOREA)
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
        val path = File(context.filesDir, "$userId/$prefix")
        if (!path.exists()) {
            val result = path.mkdirs()
            if (!result) throw Exception("$prefix 내부 저장소 디렉터리 생성에 실패했습니다.")
        }
        return path
    }

    fun writeReport(context: Context, reportId: Int, statuses: T) {
        File(getPath(context), "$prefix$reportId").outputStream().use { fos ->
            fos.write(Gson().toJson(statuses).toByteArray(Charsets.UTF_8))
            fos.close()
        }
        // TODO: 해당 메서드를 참조하는 함수에서 메모리 예외처리 하기
    }

    fun writeReportWithDate(context: Context, reportId: Int, content: T) {
        File(getPath(context), "$prefix${reportId}_${df.format(Date())}").outputStream().use { fos ->
            fos.write(Gson().toJson(content).toByteArray(Charsets.UTF_8))
            fos.close()
        }
    }

    fun readReport(context: Context, reportId: Int): T? {
        var report: T? = null
        try{
            val key = "$prefix$reportId"
            if(Tweeper.DataHolder.hasData(key)) report = Tweeper.DataHolder.getData(key) as T?
            val text = File(getPath(context), key).readText(Charsets.UTF_8)
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
            val key = "$prefix$reportId"
            if(Tweeper.DataHolder.hasData(key)) report = Tweeper.DataHolder.getData(key) as T?
            File(getPath(context), key).inputStream().use { fis ->
                val ba = fis.readBytes()
                val str = String(ba)
                report = Gson().fromJson(str, TypeToken.getParameterized(cls::class.java).type)
                Tweeper.DataHolder.loadData(key, report as Any)
            }
        }
        catch (e: Exception) {

        }
        return report
    }

    fun readReport(context: Context, fileName: String, cls: Any): Any? {
        var report: T? = null
        try{
            if(Tweeper.DataHolder.hasData(fileName)) report = Tweeper.DataHolder.getData(fileName) as T?
            File(getPath(context), fileName).inputStream().use { fis ->
                val ba = fis.readBytes()
                val str = String(ba)
                report = Gson().fromJson(str, TypeToken.getParameterized(cls::class.java).type)
                Tweeper.DataHolder.loadData(fileName, report as Any)
            }
        }
        catch (e: Exception) {

        }
        return report
    }

    /**
     * 제너릭스 없이 실행 가능
     */
    @Deprecated("날짜 같이 가져오는놈으로 써")
    fun getReportsWithName(context: Context): ArrayList<String> {
        val list = ArrayList<String>()
        for (item in getPath(context).list()!!) {
            list.add(item)
        }

        /**
         * 리포트 이름이 Report로 끝나지 않으면 정렬이 동작하지 않음.
         */
        list.sortBy { x -> x.substringAfter("Report").toIntOrNull() }
        list.reverse()
        return list
    }

    fun getReportsWithDate(context: Context): ArrayList<Pair<String, Date?>> {
        val list = ArrayList<Pair<String, Date?>>()
        for (item in getPath(context).list()!!) {
            val block = item.split("_")
            if(block.size == 3) list.add(Pair(item, df.parse(block[2])))
            list.add(Pair(item, null))
        }

        list.sortBy { it.second }
        return list
    }

    fun getReports(context: Context): ArrayList<T> {
        val list = ArrayList<T>()
        val names = getReportsWithName(context)
        for(name in names) {
            val report = readReport(context, name.removePrefix(prefix).toInt())
            if(report != null) list.add(report)
        }
        return list
    }
    fun getReports(context: Context, cls: Any, maxCount: Int = 10): ArrayList<Any> {
        val list = ArrayList<Any>()
        val names = getReportsWithName(context)
        val max = if(names.size<maxCount) names.size else maxCount
        for(name in names) {
            val report = readReport(context, name.removePrefix(prefix).toInt(), cls)
            if(report != null) list.add(report)
            if(list.count() >= max) break
        }
        return list
    }
}