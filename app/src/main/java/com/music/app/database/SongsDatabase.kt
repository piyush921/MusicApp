package com.music.app.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.music.app.models.Audio

@Database(entities = [Audio::class], version = 6)
abstract class SongsDatabase : RoomDatabase() {

    abstract fun songsDatabase(): SongsDao

}