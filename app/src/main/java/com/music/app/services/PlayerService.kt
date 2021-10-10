package com.music.app.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.util.Size
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.music.app.Constants
import com.music.app.activities.HomeActivity
import com.music.app.models.SongsModel
import com.music.app.storage.PrefsHelper

open class PlayerService : Service(),
    PlayerNotificationManager.MediaDescriptionAdapter,
    PlayerNotificationManager.NotificationListener,
    Player.Listener {

    companion object {
        const val PLAYER_ACTION = "player_action"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "player_id"
        private const val CHANNEL_NAME = "Player Channel"
    }

    private lateinit var context: Context
    private lateinit var player: SimpleExoPlayer
    private lateinit var notificationManager: PlayerNotificationManager
    private var position: Int = 0
    private lateinit var songsList: ArrayList<SongsModel.Audio>
    private lateinit var prefsHelper: PrefsHelper
    private lateinit var handler: Handler

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        context = this
        handler = Handler(Looper.getMainLooper())
        prefsHelper = PrefsHelper(context)
        player = SimpleExoPlayer.Builder(context).build()
        player.addListener(this)

        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        handler.removeCallbacksAndMessages(null)

        when (val action = intent?.action) {
            Constants.SERVICE_ACTION_PAUSE -> {
                player.pause()
            }
            Constants.SERVICE_ACTION_PLAY -> {
                player.play()
                updateSeekbarProgress()
            }
            Constants.SERVICE_ACTION_SEEK -> {
                val progress = intent.getIntExtra(Constants.KEY_PROGRESS, 0)
                player.seekTo((progress * 1000).toLong())
                updateSeekbarProgress()
            }
            else -> {
                position = intent?.getIntExtra(Constants.KEY_POSITION, 0)!!
                songsList = intent.getParcelableArrayListExtra(Constants.KEY_LIST)!!

                if (action.equals(Constants.SERVICE_ACTION_ALREADY_PLAYING)) {
                    player.stop()
                    player.clearMediaItems()
                }
                val dataSourceFactory = DefaultDataSourceFactory(
                    context, Util.getUserAgent(context, "MusicApp")
                )

                val concatenatingMediaSource = ConcatenatingMediaSource()
                var i = -1;
                for (model in songsList) {
                    i++;
                    val mediaItem = MediaItem.Builder().setUri(model.uri).setMediaId(i.toString()).build()
                    val mediaSource: MediaSource = ProgressiveMediaSource
                        .Factory(dataSourceFactory).createMediaSource(mediaItem)
                    concatenatingMediaSource.addMediaSource(mediaSource)
                }
                player.setMediaSource(concatenatingMediaSource)
                player.prepare()
                player.seekTo(position, C.TIME_UNSET)
                player.playWhenReady = true

                notificationManager =
                    PlayerNotificationManager.Builder(context, NOTIFICATION_ID, CHANNEL_ID)
                        .setMediaDescriptionAdapter(this).setNotificationListener(this).build()
                notificationManager.setPlayer(player)

                updateSeekbarProgress()
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        notificationManager.setPlayer(null)
        player.release()
        super.onDestroy()
    }

    override fun getCurrentContentTitle(player: Player): CharSequence {
        return songsList[player.currentWindowIndex].title
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        val intent = Intent(context, HomeActivity::class.java)
        return PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun getCurrentContentText(player: Player): CharSequence? {
        return songsList[player.currentWindowIndex].artist
    }

    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.loadThumbnail(
                songsList[player.currentWindowIndex].uri, Size(50, 50), null
            )
        } else {
            null
        }
    }

    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        super.onNotificationCancelled(notificationId, dismissedByUser)
        stopSelf()
    }

    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        super.onNotificationPosted(notificationId, notification, ongoing)
        startForeground(notificationId, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE
            )
            channel.lightColor = Color.BLUE
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(channel)
        }
    }

    private fun updateSeekbarProgress() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (player.playbackState == SimpleExoPlayer.STATE_READY) {
                    val intent = Intent()
                    intent.action = PLAYER_ACTION
                    intent.putExtra(Constants.KEY_PROGRESS, (player.currentPosition / 1000).toInt())
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                }
                handler.postDelayed(this, 1000)
            }
        }, 0)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        prefsHelper.savePref(PrefsHelper.PLAYER_STATE, PrefsHelper.PLAYER_STATE_STOP)
        stopSelf()
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)

        if (playWhenReady) {
            sendPlayerState(PrefsHelper.PLAYER_STATE_PLAYING)
        } else {
            sendPlayerState(PrefsHelper.PLAYER_STATE_PAUSE)
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)

        if(reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {
            if (mediaItem != null) {
                if (mediaItem.playbackProperties != null) {
                    val uri = mediaItem.playbackProperties!!.uri
                    val mediaId = mediaItem.mediaId

                }
            }
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)

        /*
          if play from selecting song
          Playback state: 1, is playing: false, play when ready: true
          Playback state: 2, is playing: false, play when ready: true
          Playback state: 3, is playing: true, play when ready: true

          if press next or previous from notification
          Playback state: 2, is playing: false, play when ready: true
          Playback state: 3, is playing: true, play when ready: true
        */

    }

    private fun sendPlayerState(state: String) {
        val intent = Intent()
        intent.action = PLAYER_ACTION
        intent.putExtra(Constants.KEY_PLAYER_STATE, state)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

        prefsHelper.savePref(PrefsHelper.PLAYER_STATE, state)
    }
}