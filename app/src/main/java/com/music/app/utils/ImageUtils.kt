package com.music.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Size
import com.music.app.R
import java.io.FileNotFoundException

class ImageUtils {

    companion object {
        //only for song album art
        fun getBitmapFromUri(context: Context, uri: Uri?): Bitmap? {
            if (uri == null) {
                return BitmapFactory.decodeResource(context.resources, R.drawable.default_album_art)
            }

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    context.contentResolver.loadThumbnail(uri, Size(300, 300), null)
                } catch (e: FileNotFoundException) {
                    BitmapFactory.decodeResource(context.resources, R.drawable.default_album_art)
                }
            } else {
                val stream = context.contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(stream)
            }
        }
    }

}