package com.music.app.models

import android.net.Uri
import android.os.Build
import android.os.Parcel
import android.os.Parcelable

open class SongsModel {

    data class Audio(
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
            uri = parcel.readParcelable(Uri::class.java.classLoader)!!,
            title = parcel.readString()!!,
            duration = parcel.readInt(),
            size = parcel.readInt(),
            id = parcel.readLong(),
            albumId = parcel.readLong(),
            albumArt = parcel.readParcelable(Uri::class.java.classLoader),
            album = parcel.readString()!!,
            artist = parcel.readString()!!,
            dateAdded = parcel.readString()!!,
            displayName = parcel.readString()!!,
            isSelected = parcel.readByte() != 0.toByte()
        ) {

        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel?, flags: Int) {
            dest?.writeParcelable(uri, 0)
            dest?.writeString(title)
            dest?.writeInt(duration)
            dest?.writeInt(size)
            dest?.writeLong(id)
            dest?.writeLong(albumId)
            dest?.writeParcelable(albumArt, 0)
            dest?.writeString(album)
            dest?.writeString(artist)
            dest?.writeString(dateAdded)
            dest?.writeString(displayName)
            if (isSelected) {
                dest?.writeInt(1)
            } else {
                dest?.writeInt(0)
            }
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