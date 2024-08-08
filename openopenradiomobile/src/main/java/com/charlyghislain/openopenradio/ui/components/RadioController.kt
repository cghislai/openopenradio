package com.charlyghislain.openopenradio.ui.components

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.HeartRating
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(UnstableApi::class)
@Composable
fun RadioController(
    player: Player,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val trackSelectionParameters = TrackSelectionParameters.Builder(context)
        .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, true)
        .build()

    val playerView = PlayerView(context).apply {
        this.player = player // Attach the player to the PlayerView
        this.useController = true;
        this.hideController();
        this.setShowFastForwardButton(false)
        this.setShowRewindButton(false)
        this.setShowPreviousButton(false)
        this.setShowNextButton(false)
        this.setShowVrButton(false)
        this.showController();
    }
    LaunchedEffect(player) {
        player.trackSelectionParameters = trackSelectionParameters
    }

    AndroidView(
        modifier = modifier
            .focusable()
            .onKeyEvent { playerView.dispatchKeyEvent(it.nativeKeyEvent) },

        factory = { context ->
            playerView
        },
        update = { playerView ->
            // Update the PlayerView if needed (e.g., when the player changes)
            playerView.player = player
        })

}


@OptIn(UnstableApi::class)
@Composable
fun MediaControlOnly(
    player: Player,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val trackSelectionParameters = TrackSelectionParameters.Builder(context)
        .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, true)
        .build()

    val controlView = PlayerControlView(context).apply {
        this.player = player // Attach the player to the PlayerView
        this.setShowFastForwardButton(false)
        this.setShowRewindButton(false)
        this.setShowPreviousButton(false)
        this.setShowNextButton(false)
    }

    LaunchedEffect(player) {
        player.trackSelectionParameters = trackSelectionParameters
    }

    AndroidView(
        modifier = modifier
            .focusable()
            .onKeyEvent { controlView.dispatchKeyEvent(it.nativeKeyEvent) },

        factory = { context ->
            controlView
        },
        update = { playerView ->
            // Update the PlayerView if needed (e.g., when the player changes)
            playerView.player = player
        })

}


@Composable
fun MyPlayerView(
    player: Player,
    controller: MediaController,
    onSetRating: (MediaItem, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var mediaItem: MediaItem? by remember {
        mutableStateOf(controller.currentMediaItem)
    }
    var mediaPlaying: Boolean? by remember {
        mutableStateOf(controller.isPlaying)
    }
    var mediaStatus: String? by remember {
        mutableStateOf(getPlayerStatus(controller))
    }
    player.addListener(
        ControllerPlayerListener(
            { item ->
                coroutineScope.launch { // Launch a coroutine
                    mediaItem = controller.currentMediaItem
                    mediaPlaying = controller.isPlaying
                    mediaStatus = getPlayerStatus(controller)
                }
            }, player
        )
    )


    var playerViewState: PlayerViewState? by remember {
        mutableStateOf(null)
    }

    mediaItem?.let { item ->
        val imageUri = item.mediaMetadata.artworkUri
        val painterR = rememberAsyncImagePainter(
            model = imageUri,
            contentScale = ContentScale.Fit
        )
        painterR.let { painter ->
            var background = Color.White// Default color
            var foreground = Color.Black // Default color

            coroutineScope.launch { // Use coroutineScope.launch
                val image = painter.imageLoader.execute(
                    ImageRequest.Builder(context)
                        .data(imageUri)
                        .build()
                ).drawable
                if (image is BitmapDrawable) {
                    val softwareBitmap = image.bitmap.copy(Bitmap.Config.ARGB_8888, true)

                    val palette = Palette.from(softwareBitmap).generate()
                    background = Color(palette.getDominantColor(Color.White.toArgb()))
                    foreground = getComplementaryColor(background)
                }
                withContext(Dispatchers.Main) {
                    val state = PlayerViewState(
                        background,
                        foreground,
                    )
                    playerViewState = state
                }
            }
        }
    } ?: run {
        val state = PlayerViewState(
            Color.White,
            Color.Black,
        )
        playerViewState = state
    }


    playerViewState?.let { state ->
        mediaItem?.let { item ->
            val itemRatin = item.mediaMetadata.userRating as HeartRating;

            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.CenterEnd)
                        .background(playerViewState!!.backgroundColor ?: Color.White)
                ) {
                    AsyncImage(
                        item.mediaMetadata.artworkUri,
                        contentDescription = "Background Image",
                        modifier = Modifier
                            .fillMaxHeight()

                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                ) {
                    mediaPlaying?.let { playing ->
                        Column(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Text(
                                text = mediaItem?.mediaMetadata?.title.toString(),
                                color = state.foregroundColor,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                softWrap = false,
                                style = TextStyle(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 24.sp,
                                    background = state.backgroundColor.copy(alpha = 0.8f)
                                ),
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.aligned(Alignment.End)
                            ) {
                                PlayPauseIcon(
                                    isPlaying = playing,
                                    color = state.foregroundColor,
                                    onPlayPauseClick = {
                                        if (controller.isPlaying) {
                                            controller.pause()
                                        } else {
                                            controller.play()
                                        }
                                    }
                                )
                                BinaryHeartRating(
                                    isFavorite = itemRatin.isHeart == true, // Pass the current favorite status
                                    onToggleFavorite = {
                                        val newStatus = itemRatin.isHeart == false
                                        onSetRating(item, newStatus)
                                    },
                                    color = state.foregroundColor
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.aligned(Alignment.End)
                            ) {
                                mediaStatus?.let { status ->
                                    Text(
                                        text = status,
                                        color = state.foregroundColor,
                                        modifier = Modifier
                                            .padding(8.dp),
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            background = state.backgroundColor.copy(alpha = 0.8f)
                                        ),
                                    )
                                }
                            }

                        }

                    }


                    // Your other content here, using playerViewState.foregroundColor for text color, etc.
                }
            }
        } ?: run {
            Text(
                text = "No item",
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)
            )
        }
    } ?: run {
        CircularProgressIndicator()
    }
}

@Composable
fun PlayPauseIcon(
    isPlaying: Boolean,
    color: Color,
    onPlayPauseClick: () -> Unit
) {
    val icon = if (isPlaying) {
        Icons.Filled.PauseCircle // Or your custom pause icon
    } else {
        Icons.Filled.PlayCircle // Or your custom play icon
    }

    Icon(
        imageVector = icon,
        tint = color,
        contentDescription = if (isPlaying) "Pause" else "Play",
        modifier = Modifier
            .size(64.dp)
            .padding(8.dp)
            .clickable { onPlayPauseClick() }
    )
}

@Composable
fun BinaryHeartRating(
    isFavorite: Boolean, // Whether the item is a favorite
    onToggleFavorite: () -> Unit, // Callback to toggle favorite status
    color: Color = Color.Red
) {
    IconButton(
        onClick = { onToggleFavorite() },
        modifier = Modifier
            .size(64.dp)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = color
        )
    }
}

fun getPlayerStatus(controller: MediaController): String? {
    val connected = controller.isConnected
    if (!connected) {
        return "Disconnected"
    }

    val error = controller.playerError
    if (error != null) {
        return "Error: ${error.message}"
    }

    val mediaItem = controller.currentMediaItem ?: return "Stopped"
    val bufferSize = controller.totalBufferedDuration
    if (bufferSize < 100) {
        return "Buffering"
    }
    val playing = controller.isPlaying
    if (!playing) {
        return "Paused"
    }


    return "Playing"
}

class ControllerPlayerListener(
    private val callback: (MediaItem?) -> Unit,
    val player: Player,
) :
    Player.Listener {

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        callback.invoke(mediaItem)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        callback.invoke(player.currentMediaItem)
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        callback.invoke(player.currentMediaItem)
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        super.onMediaMetadataChanged(mediaMetadata)
        callback.invoke(player.currentMediaItem)
    }

}

data class PlayerViewState(
    val backgroundColor: Color,
    val foregroundColor: Color,
)

// For Color objects
fun getComplementaryColor(color: Color): Color {
    val red = (255 - color.red * 255).coerceIn(0F, 255F).toInt()
    val green = (255 - color.green * 255).coerceIn(0F, 255F).toInt()
    val blue = (255 - color.blue * 255).coerceIn(0F, 255F).toInt()
    return Color(red, green, blue)
}

// For integer (ARGB) color values
fun getComplementaryColor(colorInt: Int): Color {
    val color = Color(colorInt) // Convert integer to Color object
    return getComplementaryColor(color) // Call the Color version
}
