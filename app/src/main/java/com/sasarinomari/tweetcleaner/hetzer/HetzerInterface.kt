package com.sasarinomari.tweetcleaner.hetzer

import twitter4j.Status

interface HetzerInterface {
    fun excludeMyFavorite(status: Status): Boolean
    fun excludeRetweetCount(status: Status): Boolean
    fun excludeFavoriteCount(status: Status): Boolean
    fun excludeMinimumCount(status: Status, index: Int): Boolean
    fun excludeMinimumTick(status: Status): Boolean
}