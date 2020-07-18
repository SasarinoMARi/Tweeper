package com.sasarinomari.tweeper

enum class TwitterStatusCode(val code: Int) {
    OK(20), NotFound(404)
}
enum class TwitterErrorCode(val code: Int)  {
    RateLlimitExceeded(88), UserNotFound(50)
}