package com.sasarinomari.tweeper.hetzer
import android.util.Log
import twitter4j.Status

public class Hetzer(private val conditions: HetzerConditions) {
    private  fun excludeMyFavorite(status: Status): Boolean {
        if (!conditions.avoidMyFav) return false
        if (status.isRetweet) return false
        return status.isFavorited
    }

    private  fun excludeRetweetCount(status: Status): Boolean {
        if (conditions.avoidRetweetCount == 0) return false
        if (status.isRetweet) return false
        return status.retweetCount >= conditions.avoidRetweetCount
    }

    private  fun excludeFavoriteCount(status: Status): Boolean {
        if (conditions.avoidFavCount == 0) return false
        if (status.isRetweet) return false
        return status.favoriteCount >= conditions.avoidFavCount
    }

    private fun excludeMinimumCount(status: Status, index: Int): Boolean {
        return when {
            conditions.avoidRecentCount == 0 -> false
            else -> index < conditions.avoidRecentCount
        }
    }

    private  fun excludeMinimumTick(status: Status): Boolean {
        val divider = 1000 / 60
        val tweetMinuteStamp = status.createdAt.time / divider + conditions.avoidRecentMinute
        val safeMinuteStamp = System.currentTimeMillis() / divider
        return when {
            conditions.avoidRecentMinute == 0 -> false
            else -> tweetMinuteStamp >= safeMinuteStamp
        }
    }

    private  fun excludeMedia(status: Status): Boolean {
        if (!conditions.avoidMedia) return false
        if (status.isRetweet) return false
        return status.mediaEntities.isNotEmpty()
    }

    private fun excludeNoMedia(status: Status): Boolean {
        if (!conditions.avoidNoMedia) return false
        if (status.isRetweet) return false
        return status.mediaEntities.isEmpty()
    }

    private fun excludeNoGeo(status: Status): Boolean {
        if (!conditions.avoidNoGeo) return false
        if (status.isRetweet) return false
        return status.geoLocation != null
    }

     private fun excludeKeyword(status: Status): Boolean {
        for (item in conditions.avoidKeywords) {
            if (status.text.contains(item)) return true
        }
        return false
    }

    private fun log(text: String, status: Status) {
        val cut = if (text.length > 25) "${text.substring(0, 23)}.." else text
        Log.i("Hetzer", "${text}${cut}")
    }

    fun filter(status: Status, i: Int): Boolean {
        when {
            excludeMyFavorite(status) -> {
                log("[제외] 내가 마음에 들어한 트윗\n", status)
                return true
            }
            excludeRetweetCount(status) -> {
                log("[제외] 많은 리트윗을 받은 트윗\n", status)
                return true
            }
            excludeFavoriteCount(status) -> {
                log("[제외] 많은 사람이 마음에 들어한 트윗\n", status)
                return true
            }
            excludeMinimumCount(status, i) -> {
                log("[제외] 최근에 한 트윗(개수)\n", status)
                return true
            }
            excludeMinimumTick(status) -> {
                log("[제외] 너무 최근에 한 트윗(시간)\n", status)
                return true
            }
            excludeMedia(status) -> {
                log("[제외됨] 미디어를 포함한 트윗\n", status)
                return true
            }
            excludeKeyword(status) -> {
                log("[제외됨] 키워드를 포함한 트윗\n", status)
                return true
            }
            excludeNoMedia(status) -> {
                log("[제외됨] 미디어를 포함하지 않은 트윗\n", status)
                return true
            }
            excludeNoGeo(status) -> {
                log("[제외됨] 위치 정보를 포함하지 않은 트윗\n", status)
                return true
            }
            else -> return false
        }
    }
}
