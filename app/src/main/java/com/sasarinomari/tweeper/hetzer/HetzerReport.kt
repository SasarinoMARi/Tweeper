package com.sasarinomari.tweeper.hetzer

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import twitter4j.Status
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class HetzerReport(status: Status) {
    companion object {
        private const val __filePrifex = "hetzerReport"

        fun getReportCount(context: Context): Int {
            val reports = getPath(context).list()!!
            var maxN = -1
            for (name in reports) {
                if (name.startsWith(__filePrifex)) {
                    val n = name.replace(__filePrifex, "").toInt()
                    if (n > maxN) maxN = n
                }
            }
            return maxN
        }

        private fun getPath(context: Context): File {
            val path = File(context.filesDir, "hetzer")
            if (!path.exists()) {
                val result = path.mkdir()
                if (!result) throw Exception("Hetzer Report를 위한 내부 저장소 디렉터리 생성에 실패했습니다.")
            }
            return path
        }

        fun writeReport(context: Context, reportId: Int, statuses: ArrayList<Status>) {
            File(getPath(context), "$__filePrifex$reportId")
                .writeText(Gson().toJson(createList(statuses)), Charsets.UTF_8)
        }

        fun readReport(context: Context, reportId: Int): ArrayList<HetzerReport> {
            val text = File(getPath(context), "$__filePrifex$reportId")
                .readText(Charsets.UTF_8)
            return Gson().fromJson(text, object : TypeToken<ArrayList<HetzerReport>>() {}.type)
        }

        private fun createList(statuses: ArrayList<Status>): ArrayList<HetzerReport> {
            val list = ArrayList<HetzerReport>()
            for (it in statuses) {
                list.add(HetzerReport(it))
            }
            return list
        }

        fun getReporNames(context: Context): Array<String> { // TODO: 파일 명 말고 보고서 인덱스 및 작성 시간을 돌려주도록. (파일 메타데이터 알아서 읽으셈
            return getPath(context).list()!!
        }
    }

    val text: String = status.text
    val createdAt: Date = status.createdAt
}