package com.music.app.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity
data class Audio(
    @ColumnInfo(name = "uri") var uri: String,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "duration") var duration: Int,
    @ColumnInfo(name = "size") var size: Int,
    @PrimaryKey var id: Long,
    @ColumnInfo(name = "albumId") var albumId: Long,
    @ColumnInfo(name = "albumArt") var albumArt: String?,
    @ColumnInfo(name = "album") var album: String,
    @ColumnInfo(name = "artist") var artist: String,
    @ColumnInfo(name = "dateAdded") var dateAdded: String,
    @ColumnInfo(name = "displayName") var displayName: String,
    @ColumnInfo(name = "isSelected") var isSelected: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        uri = parcel.readString()!!,
        title = parcel.readString()!!,
        duration = parcel.readInt(),
        size = parcel.readInt(),
        id = parcel.readLong(),
        albumId = parcel.readLong(),
        albumArt = parcel.readString()!!,
        album = parcel.readString()!!,
        artist = parcel.readString()!!,
        dateAdded = parcel.readString()!!,
        displayName = parcel.readString()!!,
        isSelected = parcel.readByte() != 0.toByte()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(uri)
        dest.writeString(title)
        dest.writeInt(duration)
        dest.writeInt(size)
        dest.writeLong(id)
        dest.writeLong(albumId)
        dest.writeString(albumArt)
        dest.writeString(album)
        dest.writeString(artist)
        dest.writeString(dateAdded)
        dest.writeString(displayName)
        if (isSelected) {
            dest.writeInt(1)
        } else {
            dest.writeInt(0)
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