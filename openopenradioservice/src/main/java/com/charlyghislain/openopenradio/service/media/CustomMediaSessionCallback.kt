package com.charlyghislain.openopenradio.service.media

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

private const val COMMAND_SAVE_TO_FAVORITES = "SAVE_TO_FAVORITES"
private const val COMMAND_REMOVE_FROM_FAVORITES = "REMOVE_TO_FAVORITES"

class CustomMediaSessionCallback(val service: MediaPlaybackService) : MediaSession.Callback {

    // Configure commands available to the controller in onConnect()
    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
            .add(SessionCommand(COMMAND_SAVE_TO_FAVORITES, Bundle.EMPTY))
            .add(SessionCommand(COMMAND_REMOVE_FROM_FAVORITES, Bundle.EMPTY))
            .build()
        return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
            .setAvailableSessionCommands(sessionCommands)
            .build()
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        if (customCommand.customAction == COMMAND_SAVE_TO_FAVORITES) {
            // Do custom logic here
            saveToFavorites(session.player.currentMediaItem)
            return Futures.immediateFuture(
                SessionResult(SessionResult.RESULT_SUCCESS)
            )
        } else if (customCommand.customAction == COMMAND_REMOVE_FROM_FAVORITES) {
            removeFromFavorites(session.player.currentMediaItem)
            return Futures.immediateFuture(
                SessionResult(SessionResult.RESULT_SUCCESS)
            )
        } else {
            return Futures.immediateCancelledFuture();
        }
    }


    private fun saveToFavorites(currentMediaItem: MediaItem?) {
        TODO("Not yet implemented")
        updateFavoriteLayout(true)
    }

    private fun removeFromFavorites(currentMediaItem: MediaItem?) {
        TODO("Not yet implemented")
        updateFavoriteLayout(false)
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    private fun updateFavoriteLayout(savedToFavorite: Boolean) {
        if (savedToFavorite) {
            val favoriteButton = CommandButton.Builder()
                .setDisplayName("Remove from favorites")
                .setCustomIconResId(androidx.media3.session.R.drawable.media3_icon_heart_filled)
                .setSessionCommand(SessionCommand(COMMAND_REMOVE_FROM_FAVORITES, Bundle()))
                .build()
            service.mediaSession?.setCustomLayout(ImmutableList.of(favoriteButton))
        } else {
            val favoriteButton = CommandButton.Builder()
                .setDisplayName("Save to favorites")
                .setCustomIconResId(androidx.media3.session.R.drawable.media3_icon_heart_unfilled)
                .setSessionCommand(SessionCommand(COMMAND_SAVE_TO_FAVORITES, Bundle()))
                .build()
            service.mediaSession?.setCustomLayout(ImmutableList.of(favoriteButton))
        }
    }


}