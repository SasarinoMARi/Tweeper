package com.sasarinomari.tweetcleaner.hetzer

import android.os.Parcel
import android.os.Parcelable
import android.R.id.edit
import com.google.gson.Gson


class HetzerConditions() : Parcelable {
    var avoidMyFav : Boolean = false
    var avoidFavCount : Int = 0
    var avoidRetweetCount : Int = 0
    var avoidRecentCount : Int = 0
    var avoidRecentMinute : Int = 0

    constructor(parcel: Parcel) : this() {
        avoidMyFav = parcel.readByte().toInt() != 0
        avoidFavCount = parcel.readInt()
        avoidRetweetCount = parcel.readInt()
        avoidRecentCount = parcel.readInt()
        avoidRecentMinute = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte((if (avoidMyFav) 1 else 0).toByte())
        parcel.writeInt(avoidFavCount)
        parcel.writeInt(avoidRetweetCount)
        parcel.writeInt(avoidRecentCount)
        parcel.writeInt(avoidRecentMinute)
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