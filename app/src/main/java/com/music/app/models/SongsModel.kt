package com.music.app.models

import android.net.Uri

open class SongsModel {

    data class Audio(
        val uri: Uri,
        val title: String,
        val duration: Int,
        val size: Int,
        val id: Long,
        val data: String,
        val albumId: Long,
        val album: String,
        val artist: String,
        val dateAdded: String,
        val displayName: String
    )

}