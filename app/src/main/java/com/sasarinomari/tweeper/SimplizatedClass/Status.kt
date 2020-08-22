package com.sasarinomari.tweeper.SimplizatedClass

import twitter4j.Place
import java.util.*

class Status {
    var id: Long
    var text: String
    var createdAt: Date

    var favoriteCount: Int
    var retweetCount: Int

    var isFavorited: Boolean
    var isRetweeted: Boolean

    var mediaEntities: ArrayList<MediaEntity>
    var place: Any?

    constructor() {
        id = 0
        text = ""
        createdAt = Date()

        favoriteCount = 0
        retweetCount = 0

        isFavorited = false
        isRetweeted = false

        mediaEntities = ArrayList<MediaEntity>()
        place = null
    }
    constructor(src: twitter4j.Status) {
        id = src.id
        text = src.text
        createdAt = src.createdAt

        favoriteCount = src.favoriteCount
        retweetCount = src.retweetCount

        isFavorited = src.isFavorited
        isRetweeted = src.isRetweeted

        mediaEntities = ArrayList<MediaEntity>()
        for(i in mediaEntities.indices) {
            mediaEntities.add(MediaEntity(src.mediaEntities[i]))
        }
        place = src.place
    }
}