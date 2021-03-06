package com.music.app.utils

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.music.app.dialogs.SongInfoDialog
import com.music.app.models.Audio

class DialogUtils {

    companion object {
        fun showSongInfoDialog(listener: SongInfoDialog.SongInfoListener
                               , fragmentManager: FragmentManager, model: Audio
        ) {
            val dialog = SongInfoDialog(listener, model)
            dialog.show(fragmentManager, "song_info")
        }
    }

}