package com.sasarinomari.tweetcleaner.hetzer

import twitter4j.Status

interface HetzerInterface {
    fun excludeMyFavorite(status: Status): Boolean
    fun excludeRetweetCount(status: Status): Boolean
    fun excludeFavoriteCount(status: Status): Boolean
    fun excludeMinimunCount(status: Status): Boolean
    fun excludeMinimunTick(status: Status): Boolean
}