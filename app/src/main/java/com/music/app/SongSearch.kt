package com.music.app

import com.music.app.models.SongsModel

class SongSearch(
    private val songList: MutableList<SongsModel.Audio>,
    private val listener: SongSearchListener
) {

    public fun searchSong(query: String) {
        val searchList: MutableList<SongsModel.Audio> = ArrayList()

        for (song in songList) {
            if (song.title.contains(query)) {
                searchList.add(song)
            }
        }

        listener.onSongSearch(searchList)
    }


    public interface SongSearchListener {
        fun onSongSearch(songList: MutableList<SongsModel.Audio>)
    }

}