package com.sasarinomari.tweeper.SimplizatedClass

class User(src: twitter4j.User) {
    val name: String = src.name
    val screenName: String = src.screenName
    val profileImageUrl: String = src.profileImageURL
    val id: Long = src.id
    val bio: String = src.description

    override fun equals(other: Any?): Boolean {
        return if (other is User) this.id == other.id else super.equals(other)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}