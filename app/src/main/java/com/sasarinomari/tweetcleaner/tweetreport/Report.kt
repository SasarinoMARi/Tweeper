package com.sasarinomari.tweetcleaner.tweetreport

import android.os.Parcel
import android.os.Parcelable
import twitter4j.User
import java.util.*
import kotlin.collections.ArrayList

internal class Report() : Parcelable {
    var userId: Long = -1
    var tweetCount: Int = -1
    var tweetCountVar: Int? = null
    var friends = ArrayList<SimpleUser>()
    var friendsVar: Int? = null
    var followers = ArrayList<SimpleUser>()
    var followersVar: Int? = null
    var date = Date(0)

    constructor(parcel: Parcel) : this() {
        parcel.run {
            userId = readLong()
            date = readSerializable() as Date
            tweetCount = readInt()
            val friendsCount = readInt()
            for (i in 0 until friendsCount)
                friends.add(SimpleUser(parcel))
            val followersCount = readInt()
            for (i in 0 until followersCount)
                followers.add(SimpleUser(parcel))
        }
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.run {
            writeLong(userId)
            writeSerializable(date)
            writeInt(tweetCount)
            writeInt(friends.count())
            for (i in friends) i.writeToParcel(dest, flags)
            writeInt(followers.count())
            for (i in followers) i.writeToParcel(dest, flags)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Report> {
        override fun createFromParcel(parcel: Parcel): Report {
            return Report(parcel)
        }

        override fun newArray(size: Int): Array<Report?> {
            return arrayOfNulls(size)
        }
    }
}

internal class SimpleUser() : Parcelable {
    var id: Long = -1
    var screenName: String? = null

    constructor(parcel: Parcel) : this() {
        parcel.run {
            id = readLong()
            screenName = readString()
        }
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.run {
            writeLong(id)
            writeString(screenName)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SimpleUser> {
        override fun createFromParcel(parcel: Parcel): SimpleUser {
            return SimpleUser(parcel)
        }

        override fun newArray(size: Int): Array<SimpleUser?> {
            return arrayOfNulls(size)
        }

        fun createFromUser(user: User): SimpleUser {
            val me = SimpleUser()
            me.id = user.id
            me.screenName = me.screenName
            return me
        }
    }
}