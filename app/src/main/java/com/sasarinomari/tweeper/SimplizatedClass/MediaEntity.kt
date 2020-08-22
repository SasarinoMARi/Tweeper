package com.sasarinomari.tweeper.SimplizatedClass

class MediaEntity(src: twitter4j.MediaEntity) {
    val id = src.id
    val url = src.mediaURLHttps
}