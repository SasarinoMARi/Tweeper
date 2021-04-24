package com.sasarinomari.tweeper.Hetzer
import android.util.Log
import twitter4j.Status

internal class Hetzer(private val logicPairs: ArrayList<LogicPair>) {
    private val saveLogics : List<LogicPair> by lazy { logicPairs.filter { it.logicType == LogicPair.LogicType.Save } }
    private val removeLogics : List<LogicPair> by lazy { logicPairs.filter { it.logicType == LogicPair.LogicType.Remove } }

    /**
     * 삭제할 트윗이면 true 반환
     * 아니면 false 반환
     */
    fun filter(status: Status, statusIndex: Int): Boolean {
        for (lp in saveLogics) if(logicCheck(lp, status, statusIndex)) return false
        for (lp in removeLogics) if(logicCheck(lp, status, statusIndex)) return true
        return false
    }

    private fun logicCheck(lp: LogicPair, status: Status, statusIndex: Int): Boolean {
        existsNoHit = true

        judge(everything(lp)) { log(lp, "모든 트윗", status) }
        judge(includeFav(lp, status)) { log(lp, "마음에 들어한 트윗", status) }
        judge(excludeFav(lp, status)) { log(lp, "마음에 들어하지 않은 트윗", status) }
        judge(includeRt(lp, status)) { log(lp, "리트윗한 트윗", status) }
        judge(excludeRt(lp, status)) { log(lp, "리트윗 하지 않은 트윗", status) }
        judge(includeFavOver(lp, status)) { log(lp, "${lp.includeFavoriteOver} 이상 마음받은 트윗", status) }
        judge(includeFavUnder(lp, status)) { log(lp, "${lp.includeFavoriteUnder} 이하 마음받은 트윗", status) }
        judge(includeRtOver(lp, status)) { log(lp, "${lp.includeRetweetOver} 이상 리트윗받은 트윗", status) }
        judge(includeRtUnder(lp, status)) { log(lp, "${lp.includeRetweetUnder} 이하 리트윗받은 트윗", status) }
        judge(includeMedia(lp, status)) { log(lp, "미디어를 포함한 트윗", status) }
        judge(excludeMedia(lp, status)) { log(lp, "미디어를 포함하지 않은 트윗", status) }
        judge(includeKeyword(lp, status)) { log(lp, "키워드를 포함한 트윗", status) }
        judge(excludeKeyword(lp, status)) { log(lp, "키워드를 포함하지 않은 트윗", status) }
        judge(includeGeo(lp, status)) { log(lp, "위치정보를 포함한 트윗", status) }
        judge(excludeGeo(lp, status)) { log(lp, "위치정보를 포함하지 않은 트윗", status) }
        judge(includeRecentCount(lp, statusIndex)) { log(lp, "최근 ${lp.includeRecentTweetNumber}개 내의 트윗", status) }
        judge(includeRecentUntil(lp, status)) { log(lp, "최근 ${lp.includeRecentTweetUntil}분 내의 트윗", status) }

        return existsNoHit
    }

    var existsNoHit = true // 하나라도 불일치하는게 있었을 경우 false

    private fun judge(result: Boolean?, logging: () -> Unit) {
        if (result != null) {
            if(result) logging()
            else existsNoHit = false
        }
    }

    private fun log(lp: LogicPair, text: String, status: Status) {
        val cut = if (status.text.length > 25) "${status.text.substring(0, 23)}.." else status.text
        Log.i("Hetzer", "[${if(lp.logicType==LogicPair.LogicType.Remove) "삭제" else "보호"}] $text: $cut")
    }

    private fun everything(lp: LogicPair): Boolean? {
        return if(lp.everything == null) null else lp.everything!!
    }

    private fun includeFav(lp: LogicPair, status: Status): Boolean? {
        if (lp.includeFavorite == null) return null
        return lp.includeFavorite!! && status.isFavorited
    }

    private fun excludeFav(lp: LogicPair, status: Status): Boolean? {
        if (lp.excludeFavorite == null) return null
        return lp.excludeFavorite!! && !status.isFavorited
    }

    private fun includeRt(lp: LogicPair, status: Status): Boolean? {
        if (lp.includeRetweet == null) return null
        return lp.includeRetweet!! && status.isRetweeted
    }

    private fun excludeRt(lp: LogicPair, status: Status): Boolean? {
        if (lp.excludeRetweet == null) return null
        return lp.excludeRetweet!! && !status.isRetweeted
    }

    private fun includeFavOver(lp: LogicPair, status: Status): Boolean? {
        if (lp.includeFavoriteOver == null) return null
        return status.favoriteCount >= lp.includeFavoriteOver!!
    }

    private fun includeFavUnder(lp: LogicPair, status: Status): Boolean? {
        if (lp.includeFavoriteUnder == null) return null
        return status.favoriteCount <= lp.includeFavoriteUnder!!
    }

    private fun includeRtOver(lp: LogicPair, status: Status): Boolean? {
        if (lp.includeRetweetOver == null) return null
        return status.retweetCount >= lp.includeRetweetOver!!
    }

    private fun includeRtUnder(lp: LogicPair, status: Status): Boolean? {
        if (lp.includeRetweetUnder == null) return null
        return status.retweetCount <= lp.includeRetweetUnder!!
    }

    private fun includeMedia(lp: LogicPair, status: Status): Boolean? {
        if (lp.includeMedia == null) return null
        return lp.includeMedia!! && status.mediaEntities.isNotEmpty()
    }

    private fun excludeMedia(lp: LogicPair, status: Status): Boolean? {
        if (lp.excludeMedia == null) return null
        return lp.excludeMedia!! && status.mediaEntities.isEmpty()
    }

    /**
     * 키워드를 하나라도 포함하면 true
     * 하나도 포함하지 않으면 false
     */
    private fun includeKeyword(lp: LogicPair, status: Status): Boolean? {
        if (lp.includeKeywords.count() == 0) return null
        for (word in lp.includeKeywords) {
            if (status.text.contains(word)) return true
        }
        return false
    }

    /**
     * 키워드를 하나라도 포함하면 false
     * 하나도 포함하지 않으면 true
     */
    private fun excludeKeyword(lp: LogicPair, status: Status): Boolean? {
        if (lp.excludeKeywords.count() == 0) return null
        for (word in lp.excludeKeywords) {
            if (status.text.contains(word)) return false
        }
        return true
    }

    private fun includeGeo(lp: LogicPair, status: Status): Boolean? {
        if (lp.includeGEO == null) return null
        return lp.includeGEO!! && status.place != null
    }

    private fun excludeGeo(lp: LogicPair, status: Status): Boolean? {
        if (lp.excludeGEO == null) return null
        return lp.excludeGEO!! && status.place == null
    }
    
    private fun includeRecentCount(lp: LogicPair, index: Int): Boolean? {
        if (lp.includeRecentTweetNumber == null) return null
        return index < lp.includeRecentTweetNumber!!
    }

    private fun includeRecentUntil(lp: LogicPair, status: Status): Boolean? {
        if (lp.includeRecentTweetUntil == null) return null
        val min = lp.includeRecentTweetUntil!!
        val divider = 1000 * 60
        val tweetMinuteStamp = status.createdAt.time / divider + min
        val safeMinuteStamp = System.currentTimeMillis() / divider
        return tweetMinuteStamp >= safeMinuteStamp
    }
}