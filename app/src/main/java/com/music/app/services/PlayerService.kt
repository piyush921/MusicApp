package com.music.app.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.util.Size
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.music.app.Constants
import com.music.app.R
import com.music.app.activities.HomeActivity
import com.music.app.storage.PrefsHelper

open class PlayerService : Service(),
    PlayerNotificationManager.MediaDescriptionAdapter,
    PlayerNotificationManager.NotificationListener,
    Player.Listener {

    companion object {
        const val ACTION_PLAYER = "action_player"
    }

    private lateinit var context: Context
    private lateinit var player: SimpleExoPlayer
    private lateinit var notificationManager: PlayerNotificationManager
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "id_1"
    private lateinit var title: String
    private lateinit var artist: String
    private lateinit var uri: String
    //private lateinit var prefsHelper: PrefsHelper

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        context = this
        //prefsHelper = PrefsHelper(context)
        player = SimpleExoPlayer.Builder(context).build()
        player.addListener(this)

        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (val action = intent?.action) {
            Constants.SERVICE_ACTION_PAUSE -> {
                player.pause()
            }
            Constants.SERVICE_ACTION_PLAY-> {
                player.play()
            }
            else -> {
                title = intent?.getStringExtra(Constants.KEY_TITLE).toString()
                artist = intent?.getStringExtra(Constants.KEY_ARTIST).toString()
                uri = intent?.getStringExtra(Constants.KEY_URI).toString()
                if (action.equals(Constants.SERVICE_ACTION_ALREADY_PLAYING)) {
                    player.stop()
                    player.clearMediaItems()
                }
                val dataSourceFactory = DefaultDataSourceFactory(
                    context, Util.getUserAgent(context, "MusicApp"))
                val mediaSource: MediaSource = ProgressiveMediaSource
                    .Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
                player.setMediaSource(mediaSource)
                player.prepare()
                player.playWhenReady = true

                notificationManager =
                    PlayerNotificationManager.Builder(context, NOTIFICATION_ID, CHANNEL_ID)
                        .setMediaDescriptionAdapter(this).setNotificationListener(this).build()
                notificationManager.setPlayer(player)
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
        return title
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        val intent = Intent(context, HomeActivity::class.java)
        return PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun getCurrentContentText(player: Player): CharSequence? {
        return artist
    }

    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.loadThumbnail(Uri.parse(uri), Size(50, 50), null)
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
                getString(R.string.player_channel_name), NotificationManager.IMPORTANCE_NONE
            )
            channel.lightColor = Color.BLUE
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(channel)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)

        if(playWhenReady) {
            sendPlayerState(PrefsHelper.PLAYER_STATE_PLAYING)
        } else {
            sendPlayerState(PrefsHelper.PLAYER_STATE_PAUSE)
        }
    }

    private fun sendPlayerState(state: String) {
        val intent = Intent()
        intent.action = ACTION_PLAYER
        intent.putExtra(Constants.KEY_PLAYER_STATE, state)
        //prefsHelper.savePref(PrefsHelper.PLAYER_STATE, state)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}