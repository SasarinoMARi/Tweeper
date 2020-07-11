package com.sasarinomari.tweeper.Hetzer
import android.util.Log
import twitter4j.Status

internal class Hetzer(private val conditions: HashMap<Int, Any>) {
    /*
        01. 내가 마음에 들어한 트윗
        02. 내가 마음에 들어하지 않은 트윗
        03. 내가 리트윗한 트윗
        04. 내가 리트윗하지 않은 트윗
        05. N회 이상 마음 받은 트윗 (Int)
        06. N회 이하 마음 받은 트윗 (Int)
        07. N회 이상 리트윗 받은 트윗 (Int)
        08. N회 이하 리트윗 받은 트윗 (Int)
        09. 미디어를 포함한 트윗
        10. 미디어를 포함하지 않은 트윗
        11. 키워드를 포함한 트윗 (ArrayList<Sring>)
        12. 키워드를 포함하지 않은 트윗 (ArrayList<Sring>)
        13. 위치 정보를 포함한 트윗 // TODO: 동작 체크
        14. 위치 정보를 포함하지 않은 트윗 // TODO: 동작 체크
        15. 최근 N개 까지의 트윗 (Int)
        16. 최근 N분 이내의 트윗 (Int) // TODO: 동작 체크
        17. N회 이상 인용 받은 트윗 (Int) // 알아낼 방법이 없음
        18. N회 이하 인용 받은 트윗 (Int) // 알아낼 방법이 없음
        19. N회 이상 멘션 받은 트윗 (Int)  // 알아낼 방법이 없음
        20. N회 이하 멘션 받은 트윗 (Int)  // 알아낼 방법이 없음
     */
    fun filter(status: Status, i: Int): Boolean {
        return when {
            내가마음에들어한트윗(status) -> { log("내가마음에들어한트윗", status); true}
            내가마음에들어하지않은트윗(status) -> { log("내가마음에들어하지않은트윗", status); true}
            내가리트윗한트윗(status)  -> { log("내가리트윗한트윗", status); true}
            내가리트윗하지않은트윗(status)  -> { log("내가리트윗하지않은트윗", status); true}
            N회이상마음받은트윗(status)  -> { log("N회이상마음받은트윗", status); true}
            N회이하마음받은트윗(status) -> { log("N회이하마음받은트윗", status); true}
            N회이상리트윗받은트윗(status)  -> { log("N회이상리트윗받은트윗", status); true}
            N회이하리트윗받은트윗(status) -> { log("N회이하리트윗받은트윗", status); true}
            미디어를포함한트윗(status) -> { log("미디어를포함한트윗", status); true}
            미디어를포함하지않은트윗(status) -> { log("미디어를포함하지않은트윗", status); true}
            키워드를포함한트윗(status) -> { log("키워드를포함한트윗", status); true}
            키워드를포함하지않은트윗(status) -> { log("키워드를포함하지않은트윗", status); true}
            위치정보를포함한트윗(status) -> { log("위치정보를포함한트윗", status); true}
            위치정보를포함하지않은트윗(status) -> { log("위치정보를포함하지않은트윗", status); true}
            최근N개까지의트윗(i) -> { log("최근N개까지의트윗", status); true}
            최근N분이내의트윗(status) -> { log("최근N분이내의트윗", status); true}
            N회이상인용받은트윗(status) -> { log("N회이상인용받은트윗", status); true}
            N회이하인용받은트윗(status) -> { log("N회이하인용받은트윗", status); true}
            N회이상멘션받은트윗(status) -> { log("N회이상멘션받은트윗", status); true}
            N회이하멘션받은트윗(status) -> { log("N회이하멘션받은트윗", status); true}
            else -> false
        }
    }

    private fun log(text: String, status: Status) {
        val cut = if (status.text.length > 25) "${status.text.substring(0, 23)}.." else status.text
        Log.i("Hetzer", "$text: $cut")
    }

    private fun 내가마음에들어한트윗(status: Status): Boolean {
        if (!conditions.containsKey(1)) return false
        return status.isFavorited
    }

    private fun 내가마음에들어하지않은트윗(status: Status): Boolean {
        if (!conditions.containsKey(2)) return false
        return !status.isFavorited
    }

    private fun 내가리트윗한트윗(status: Status): Boolean {
        if (!conditions.containsKey(3)) return false
        return status.isRetweeted
    }

    private fun 내가리트윗하지않은트윗(status: Status): Boolean {
        if (!conditions.containsKey(4)) return false
        return !status.isRetweeted
    }

    private fun N회이상마음받은트윗(status: Status): Boolean {
        if (!conditions.containsKey(5)) return false
        return status.favoriteCount >= (conditions[5] as Double).toInt()
    }

    private fun N회이하마음받은트윗(status: Status): Boolean {
        if (!conditions.containsKey(6)) return false
        return status.favoriteCount <= (conditions[6] as Double).toInt()
    }

    private fun N회이상리트윗받은트윗(status: Status): Boolean {
        if (!conditions.containsKey(7)) return false
        return status.retweetCount >= (conditions[7] as Double).toInt()
    }

    private fun N회이하리트윗받은트윗(status: Status): Boolean {
        if (!conditions.containsKey(8)) return false
        return status.retweetCount <= (conditions[8] as Double).toInt()
    }

    private fun 미디어를포함한트윗(status: Status): Boolean {
        if (!conditions.containsKey(9)) return false
        return status.mediaEntities.isNotEmpty()
    }

    private fun 미디어를포함하지않은트윗(status: Status): Boolean {
        if (!conditions.containsKey(10)) return false
        return status.mediaEntities.isEmpty()
    }

    @Suppress("UNCHECKED_CAST")
    private fun 키워드를포함한트윗(status: Status): Boolean {
        if (!conditions.containsKey(11)) return false
        for (word in conditions[11] as ArrayList<String>) {
            if (status.text.contains(word)) return true
        }
        return false
    }

    @Suppress("UNCHECKED_CAST")
    private fun 키워드를포함하지않은트윗(status: Status): Boolean {
        if (!conditions.containsKey(12)) return false
        val list =  conditions[12] as ArrayList<String>
        if(list.count() == 0) return false
        for (word in list) {
            if (status.text.contains(word)) return false
        }
        return true
    }

    private fun 위치정보를포함한트윗(status: Status): Boolean {
        if (!conditions.containsKey(13)) return false
        return status.geoLocation != null
    }

    private fun 위치정보를포함하지않은트윗(status: Status): Boolean {
        if (!conditions.containsKey(14)) return false
        return status.geoLocation == null
    }
    
    private fun 최근N개까지의트윗(index: Int): Boolean {
        if (!conditions.containsKey(15)) return false
        return index < (conditions[15] as Double).toInt()
    }

    private fun 최근N분이내의트윗(status: Status): Boolean {
        if (!conditions.containsKey(16)) return false
        val min = (conditions[16] as Double).toInt()
        val divider = 1000 * 60
        val tweetMinuteStamp = status.createdAt.time / divider + min
        val safeMinuteStamp = System.currentTimeMillis() / divider
        return tweetMinuteStamp >= safeMinuteStamp
    }

    private fun N회이상인용받은트윗(status: Status): Boolean {
        if (!conditions.containsKey(17)) return false
//        return status.retweetCount >= (conditions[17] as Double).toInt()
        return true
    }

    private fun N회이하인용받은트윗(status: Status): Boolean {
        if (!conditions.containsKey(18)) return false
//        return status.retweetCount >= (conditions[18] as Double).toInt()
        return true
    }

    private fun N회이상멘션받은트윗(status: Status): Boolean {
        if (!conditions.containsKey(19)) return false
        //return status.userMentionEntities.count() >= (conditions[19] as Double).toInt()
        return true
    }

    private fun N회이하멘션받은트윗(status: Status): Boolean {
        if (!conditions.containsKey(20)) return false
        //return status.userMentionEntities.count() >= (conditions[20] as Double).toInt()
        return true
    }
}