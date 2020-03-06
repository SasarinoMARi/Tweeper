package com.sasarinomari.tweeper.hetzer

import twitter4j.Status

interface HetzerInterface {
    fun excludeMyFavorite(status: Status): Boolean
    fun excludeRetweetCount(status: Status): Boolean
    fun excludeFavoriteCount(status: Status): Boolean
    fun excludeMinimumCount(status: Status, index: Int): Boolean
    fun excludeMinimumTick(status: Status): Boolean
    fun excludeMedia(status: Status): Boolean
    fun excludeNoMedia(status: Status): Boolean
    fun excludeNoGeo(status: Status) : Boolean
    fun excludeKeyword(status: Status) : Boolean
}