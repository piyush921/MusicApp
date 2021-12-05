package com.music.app.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.room.Room
import com.music.app.database.SongsDao
import com.music.app.database.SongsDatabase
import com.music.app.models.Audio
import com.music.app.utils.PermissionUtils
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

open class SongsRepository(
    var context: Context,
    val listener: GetSongsListener,
    var songsDatabase: SongsDatabase,
    var songsDao: SongsDao
) {

    fun getAllSongs() {
        val taskRunner = TaskRunner()
        taskRunner.executeAsync(LongRunningTask(context, songsDatabase, songsDao),
            object : TaskRunner.Callback<MutableList<Audio>> {
                override fun onComplete(result: MutableList<Audio>) {
                    listener.onGetSongs(result)
                }
            })
    }

    interface GetSongsListener {
        fun onGetSongs(list: MutableList<Audio>)
    }

    open class TaskRunner {
        private val executor = Executors.newSingleThreadExecutor()
        private val handler = Handler(Looper.getMainLooper())

        interface Callback<R> {
            fun onComplete(result: R)
        }

        open fun <R> executeAsync(callable: Callable<R>, callback: Callback<R>) {
            executor.execute {
                val result = callable.call()
                handler.post {
                    callback.onComplete(result)
                }
            }
        }
    }

    open class LongRunningTask(
        private var context: Context, private var songsDatabase: SongsDatabase,
        var songsDao: SongsDao
    ) : Callable<MutableList<Audio>> {

        override fun call(): MutableList<Audio> {
            val audioList = mutableListOf<Audio>()

            if (PermissionUtils.checkReadWritePermission(context)) {

                val proj = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DATE_ADDED,
                    MediaStore.Audio.Media.DISPLAY_NAME
                )

                val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"
                val selection = "${MediaStore.Audio.Media.DURATION} >= ?"
                val selectionArgs = arrayOf(
                    TimeUnit.MILLISECONDS.convert(5, TimeUnit.SECONDS).toString()
                )

                val query = context.contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    proj,
                    selection,
                    selectionArgs,
                    sortOrder
                )

                query?.use { cursor ->

                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val albumIdColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    val durationColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                    val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val dateAddedColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                    val displayNameColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

                    while (cursor.moveToNext()) {
                        // Get values of columns for a given video.
                        val id = cursor.getLong(idColumn)
                        val title = cursor.getString(titleColumn)
                        val duration = cursor.getInt(durationColumn)
                        val size = cursor.getInt(sizeColumn)
                        val albumId = cursor.getLong(albumIdColumn)
                        val album = cursor.getString(albumColumn)
                        val artist = cursor.getString(artistColumn)
                        val dateAdded = cursor.getString(dateAddedColumn)
                        val displayName = cursor.getString(displayNameColumn)

                        val contentUri: Uri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )

                        var albumArt: Uri? = null
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            albumArt = getAlbumUri(albumId)
                        }
                        audioList.add(
                            Audio(
                                contentUri.toString(),
                                title,
                                duration,
                                size,
                                id,
                                albumId,
                                albumArt.toString(),
                                album,
                                artist,
                                dateAdded,
                                displayName,
                                false
                            )
                        )
                    }

                }

            } else {
                PermissionUtils.askReadWritePermission(context)
            }
            if (songsDao.getAll().size != audioList.size) {
                songsDao.clearDb()
                songsDao.insertSongs(audioList)
            }
            return audioList
        }

        private fun getAlbumUri(albumId: Long): Uri {
            val art = Uri.parse("content://media/external/audio/albumart")
            return Uri.withAppendedPath(art, albumId.toString())
        }

    }

}