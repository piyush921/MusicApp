package com.music.app

import android.app.Application
import android.graphics.Typeface
import androidx.core.graphics.TypefaceCompatUtil
import com.music.app.utils.TypeFaceUtils

class MusicApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        TypeFaceUtils.overrideFont(this, "recursive", "recursive.ttf")
    }

}