package com.music.app.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.music.app.models.Audio

@Dao
interface SongsDao {

    @Query("Select * from Audio")
    fun getAll(): MutableList<Audio>

    @Insert
    fun insertSongs(audioList: MutableList<Audio>)

    @Query("Delete from Audio")
    fun clearDb()

    @Query("update Audio set isSelected = 1 where id = :songId")
    fun updatePlaying(songId: Int)

    @Query("SELECT COUNT(*) FROM Audio")
    fun getSize(): Int

}