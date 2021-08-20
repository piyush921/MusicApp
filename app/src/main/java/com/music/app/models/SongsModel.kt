package com.music.app.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

open class SongsModel {

    data class Audio (
        var uri: Uri,
        var title: String,
        var duration: Int,
        var size: Int,
        var id: Long,
        var albumId: Long,
        var albumArt: Uri?,
        var album: String,
        var artist: String,
        var dateAdded: String,
        var displayName: String,
        var isSelected: Boolean
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readParcelable(Uri::class.java.classLoader),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readLong(),
            parcel.readLong(),
            parcel.readParcelable(Uri::class.java.classLoader),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readByte() != 0.toByte()
        ) {
        }

        override fun describeContents(): Int {
            TODO("Not yet implemented")
        }

        override fun writeToParcel(dest: Parcel?, flags: Int) {
            TODO("Not yet implemented")
        }

        companion object CREATOR : Parcelable.Creator<Audio> {
            override fun createFromParcel(parcel: Parcel): Audio {
                return Audio(parcel)
            }

            override fun newArray(size: Int): Array<Audio?> {
                return arrayOfNulls(size)
            }
        }
    }
}