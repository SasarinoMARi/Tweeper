package com.sasarinomari.tweeper.MediaDownload

import com.google.gson.Gson
import com.sasarinomari.tweeper.TwitterAdapter
import twitter4j.Status

class MediaTool {
    interface LookupInterface {
        fun onMediaEmpty()
        fun onGottenUrls(mediaUrls: Array<String>, mediaType: MediaType?)
        fun onNeedFirebaseLog(title: String, content: Pair<String, String>)
    }

    enum class MediaType {
        Image, Video, Animation
    }

    companion object {
        fun lookup(twitterAdapter: TwitterAdapter, statudId: Long, lookupInterface: LookupInterface) {
            twitterAdapter.lookStatus(statudId, object : TwitterAdapter.FoundObjectInterface {
                override fun onStart() {}

                override fun onFinished(obj: Any) {
                    val status = obj as Status
                    if (status.mediaEntities.isEmpty()) {
                        lookupInterface.onMediaEmpty()
                        return
                    }

                    var type: MediaType? = null
                    val mediaUrls = ArrayList<String>()

                    for (entitie in status.mediaEntities) {
                        when (entitie.type) {
                            "photo" -> {
                                mediaUrls.add(entitie.mediaURLHttps)
                                type = MediaType.Image
                            }
                            "animated_gif" -> {
                                val target = entitie.videoVariants.maxBy { v -> v.bitrate }
                                if (target != null) {
                                    mediaUrls.add(target.url)
                                    type = MediaType.Animation
                                } else {
                                    lookupInterface.onNeedFirebaseLog(
                                        "VideoVariantResultNull",
                                        Pair("entitie.videoVariants", Gson().toJson(entitie.videoVariants))
                                    )
                                }
                            }
                            "video" -> {
                                val target = entitie.videoVariants.maxBy { v -> v.bitrate }
                                if (target != null) {
                                    mediaUrls.add(target.url)
                                    type = MediaType.Video
                                } else {
                                    lookupInterface.onNeedFirebaseLog(
                                        "VideoVariantResultNull",
                                        Pair("entitie.videoVariants", Gson().toJson(entitie.videoVariants))
                                    )
                                }
                            }
                        }
                    }
                    lookupInterface.onGottenUrls(mediaUrls.toTypedArray(), type)
                }

                override fun onRateLimit() {
                    TODO("Not yet implemented")
                }

                override fun onNotFound() {
                    TODO("Not yet implemented")
                }

                override fun onUncaughtError() {
                    TODO("Not yet implemented")
                }

                override fun onNetworkError(retry: () -> Unit) {
                    TODO("Not yet implemented")
                }
            })
        }
    }
}