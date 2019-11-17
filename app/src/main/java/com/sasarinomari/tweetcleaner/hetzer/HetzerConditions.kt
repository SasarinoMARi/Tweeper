package com.sasarinomari.tweetcleaner.hetzer

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson


class HetzerConditions() : Parcelable {
    var avoidMyFav : Boolean = false
    var avoidFavCount : Int = 0
    var avoidRetweetCount : Int = 0
    var avoidRecentCount : Int = 0
    var avoidRecentMinute : Int = 0
    var avoidKeywords : ArrayList<String> = ArrayList()
    var avoidMedia : Boolean = false
    var avoidNoMedia : Boolean = false
    var avoidNoGeo: Boolean = false

    constructor(parcel: Parcel) : this() {
        avoidMyFav = parcel.readByte().toInt() != 0
        avoidFavCount = parcel.readInt()
        avoidRetweetCount = parcel.readInt()
        avoidRecentCount = parcel.readInt()
        avoidRecentMinute = parcel.readInt()
        val size = parcel.readInt()
        for(i in 0 until size) {
            avoidKeywords.add(parcel.readString())
        }
        avoidMedia = parcel.readByte().toInt() != 0
        avoidNoMedia = parcel.readByte().toInt() != 0
        avoidNoGeo = parcel.readByte().toInt() != 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte((if (avoidMyFav) 1 else 0).toByte())
        parcel.writeInt(avoidFavCount)
        parcel.writeInt(avoidRetweetCount)
        parcel.writeInt(avoidRecentCount)
        parcel.writeInt(avoidRecentMinute)
        parcel.writeInt(avoidKeywords.count())
        for(i in avoidKeywords) {
            parcel.writeString(i)
        }
        parcel.writeByte((if (avoidMedia) 1 else 0).toByte())
        parcel.writeByte((if (avoidNoMedia) 1 else 0).toByte())
        parcel.writeByte((if (avoidNoGeo) 1 else 0).toByte())
    }

    fun toJson() : String {
        return Gson().toJson(this)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HetzerConditions> {
        override fun createFromParcel(parcel: Parcel): HetzerConditions {
            return HetzerConditions(parcel)
        }

        override fun newArray(size: Int): Array<HetzerConditions?> {
            return arrayOfNulls(size)
        }
    }
}