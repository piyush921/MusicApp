package com.music.app

import com.music.app.models.Audio

class SongSearch(
    private val songList: MutableList<Audio>,
    private val listener: SongSearchListener
) {

    public fun searchSong(query: String) {
        val searchList: MutableList<Audio> = ArrayList()

        for (song in songList) {
            if (song.title.lowercase().contains(query.lowercase())) {
                searchList.add(song)
            }
        }

        listener.onSongSearch(searchList)
    }


    public interface SongSearchListener {
        fun onSongSearch(songList: MutableList<Audio>)
    }

}