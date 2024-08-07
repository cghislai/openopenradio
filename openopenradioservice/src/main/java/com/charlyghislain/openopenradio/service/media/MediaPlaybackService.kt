package com.charlyghislain.openopenradio.service.media

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpEngineDataSource
import androidx.media3.exoplayer.DefaultLivePlaybackSpeedControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.hls.playlist.HlsMediaPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsMultivariantPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsPlaylistParser
import androidx.media3.exoplayer.hls.playlist.HlsPlaylistParserFactory
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.ParsingLoadable
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.charlyghislain.openopenradio.service.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MediaPlaybackService : MediaSessionService() {
    companion object {
        private const val NOTIFICATION_ID = 123
        private const val CHANNEL_ID = "session_notification_channel_id"
    }


    /**
     * Returns the single top session activity. It is used by the notification when the app task is
     * active and an activity is in the fore or background.
     *
     * Tapping the notification then typically should trigger a single top activity. This way, the
     * user navigates to the previous activity when pressing back.
     *
     * If null is returned, [MediaSession.setSessionActivity] is not set by the demo service.
     */
    open fun getSingleTopActivity(): PendingIntent? = null

    /**
     * Returns a back stacked session activity that is used by the notification when the service is
     * running standalone as a foreground service. This is typically the case after the app has been
     * dismissed from the recent tasks, or after automatic playback resumption.
     *
     * Typically, a playback activity should be started with a stack of activities underneath. This
     * way, when pressing back, the user doesn't land on the home screen of the device, but on an
     * activity defined in the back stack.
     *
     * See [androidx.core.app.TaskStackBuilder] to construct a back stack.
     *
     * If null is returned, [MediaSession.setSessionActivity] is not set by the demo service.
     */
    open fun getBackStackedActivity(): PendingIntent? = null

    private var mediaSession: MediaLibrarySession? = null

    @Inject
    lateinit var treeService: MediaTreeService;

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_ALL)
            .setFlags(C.FLAG_AUDIBILITY_ENFORCED)
            .build();

        val httpDatasource = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(1000)
            .setReadTimeoutMs(5000)
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(httpDatasource)
                    .setLiveTargetOffsetMs(5000)
            )
            .setLivePlaybackSpeedControl(
                DefaultLivePlaybackSpeedControl.Builder()
                    .setFallbackMaxPlaybackSpeed(1.05f)
                    .setFallbackMinPlaybackSpeed(0.95f)
                    .build()
            )
            .build()
        player.addListener(PlayerListener(player))


        mediaSession = MediaLibrarySession.Builder(this, player, CustomMediaSessionCallback(this))
//            .setPeriodicPositionUpdateEnabled(false)
//            .setSessionActivity()
            .build()
        setListener(MediaSessionServiceListener())
    }

    // The user dismissed the app from the recent tasks
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player!!
        if (!player.playWhenReady
            || player.mediaItemCount == 0
            || player.playbackState == Player.STATE_ENDED
        ) {
            // Stop the service if not playing, continue playing in the background
            // otherwise.
            stopSelf()
        }
    }

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        getBackStackedActivity()?.let { mediaSession?.setSessionActivity(it) }
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        clearListener()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession;
    }


    @OptIn(UnstableApi::class) // MediaSessionService.Listener
    private inner class MediaSessionServiceListener : Listener {

        /**
         * This method is only required to be implemented on Android 12 or above when an attempt is made
         * by a media controller to resume playback when the {@link MediaSessionService} is in the
         * background.
         */
        override fun onForegroundServiceStartNotAllowedException() {
            if (
                Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                // Notification permission is required but not granted
                return
            }
            val notificationManagerCompat =
                NotificationManagerCompat.from(this@MediaPlaybackService)
            ensureNotificationChannel(notificationManagerCompat)
            val builder =
                NotificationCompat.Builder(this@MediaPlaybackService, CHANNEL_ID)
                    .setSmallIcon(androidx.media3.session.R.drawable.media3_notification_small_icon)
                    .setContentTitle(getString(R.string.notification_content_title))
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(getString(R.string.notification_content_text))
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .also { builder -> getBackStackedActivity()?.let { builder.setContentIntent(it) } }
            notificationManagerCompat.notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun ensureNotificationChannel(notificationManagerCompat: NotificationManagerCompat) {
        if (
            Build.VERSION.SDK_INT < 26 ||
            notificationManagerCompat.getNotificationChannel(CHANNEL_ID) != null
        ) {
            return
        }

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        notificationManagerCompat.createNotificationChannel(channel)
    }

}

@UnstableApi
class PlayerListener(val player: ExoPlayer) : Player.Listener {
    private var currentPlaylistUri: String? = null

    @OptIn(UnstableApi::class)
    val dataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
        .setAllowCrossProtocolRedirects(true)

    val playlistParserFactory: HlsPlaylistParserFactory = PlaylistParserFactory()

//    @OptIn(UnstableApi::class)
//    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
//        val newPlaylistUri = mediaItem?.localConfiguration?.uri.toString()
//
//        if (newPlaylistUri.endsWith(".m3u", ignoreCase = true)
//            && newPlaylistUri != currentPlaylistUri
//        ) {
//            currentPlaylistUri = newPlaylistUri
//            val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory)
//                .setPlaylistParserFactory(playlistParserFactory)
//                .createMediaSource(mediaItem!!)
//            player.setMediaSource(hlsMediaSource)
//            player.prepare()
//        } else {
//            currentPlaylistUri = null
//            super.onMediaItemTransition(mediaItem, reason)
//        }
//    }

    override fun onPlayerError(error: PlaybackException) {
        if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
            // Re-initialize player at the live edge.
            player.seekToDefaultPosition()
            player.prepare()
        } else {
            super.onPlayerError(error)
        }

    }
}

@UnstableApi
class PlaylistParserFactory : HlsPlaylistParserFactory {
    override fun createPlaylistParser(): ParsingLoadable.Parser<HlsPlaylist> {
        return WorkaroundHlsPlaylistParser()
    }

    override fun createPlaylistParser(
        multivariantPlaylist: HlsMultivariantPlaylist,
        previousMediaPlaylist: HlsMediaPlaylist?
    ): ParsingLoadable.Parser<HlsPlaylist> {
        return WorkaroundHlsPlaylistParser(multivariantPlaylist, previousMediaPlaylist)
    }

}
