package com.charlyghislain.openopenradio.ui.components

import android.app.Application
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.HeartRating
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import com.charlyghislain.openopenradio.ui.home.ReloadEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class RadioControllerViewModel(
    application: Application,
) : AndroidViewModel(application) {

    val controller = MutableStateFlow<MediaController?>(null)
    val mediaItemFlow = MutableStateFlow<MediaItem?>(null)
    val mediaPlayingFlow = MutableStateFlow<Boolean>(false)
    val mediaStatusFlow = MutableStateFlow<String?>(null)
    val backgroundColor = MutableStateFlow(Color.White)
    val foregroundColor = MutableStateFlow(Color.Black)
    val controllerConnected = MutableStateFlow(false)
    val reloadEventFlow = MutableSharedFlow<ReloadEvent>()

    private val imageLoader = ImageLoader.Builder(application).build() // Initialize ImageLoader
    private var listenedController: MediaController? = null
    private var controllerListener: Player.Listener? = null

    fun setController(controller: MediaController?) {
        if (controller === listenedController) {
            return
        }
        // Detach from the previous controller so listeners don't pile up on every resume.
        controllerListener?.let { listenedController?.removeListener(it) }
        listenedController = controller
        controllerListener = null
        this.controller.value = controller
        if (controller == null) {
            controllerConnected.value = false
        }

        controller?.let { c ->
            viewModelScope.launch {
                mediaItemFlow.value = c.currentMediaItem
                mediaPlayingFlow.value = c.isPlaying
                mediaStatusFlow.value = getPlayerStatus(c)
                backgroundColor.value = getBackgroundColor(c.currentMediaItem) ?: Color.White
                foregroundColor.value = getComplementaryColor(backgroundColor.value)
                controllerConnected.value = c.isConnected
            }

            val listener = ControllerPlayerListener {
                viewModelScope.launch {
                    mediaItemFlow.value = controller.currentMediaItem
                    mediaPlayingFlow.value = controller.isPlaying
                    mediaStatusFlow.value = getPlayerStatus(controller)
                    controllerConnected.value = controller.isConnected
                    backgroundColor.value =
                        getBackgroundColor(controller.currentMediaItem) ?: Color.White
                    foregroundColor.value = getComplementaryColor(backgroundColor.value)
                }
            }
            controller.addListener(listener)
            controllerListener = listener
        }
    }

    fun onPlayPause() {
        viewModelScope.launch {
            controller.value?.let { c ->
                if (!c.isConnected) {
                    return@launch
                }
                if (c.isPlaying) {
                    c.pause()
                } else {
                    c.play()
                }
            }
        }
    }

    fun onSetRating(fav: Boolean) {
        viewModelScope.launch {
            controller.value?.let { c ->
                c.currentMediaItem?.let { item ->
                    c.setRating(HeartRating(fav)).awaitResult()
                    reloadEventFlow.emit(ReloadEvent(item.mediaId))
                }
            }
        }
    }

    private suspend fun getBackgroundColor(currentMediaItem: MediaItem?): Color? {
        val imageUri = currentMediaItem?.mediaMetadata?.artworkUri ?: return null
        val image = try {
            imageLoader.execute(ImageRequest.Builder(getApplication()).data(imageUri).build())
                .drawable
        } catch (e: Exception) {
            null
        } ?: return null

        if (image is BitmapDrawable) {
            val softwareBitmap = image.bitmap.copy(Bitmap.Config.ARGB_8888, true)

            val palette = Palette.from(softwareBitmap).generate()
            return Color(palette.getLightVibrantColor(Color.White.toArgb()))
        } else {
            return null;
        }
    }

    private fun getPlayerStatus(controller: MediaController): String {
        if (!controller.isConnected) {
            return "Disconnected"
        }
        return when (controller.playbackState) {
            Player.STATE_IDLE -> "Idle"
            Player.STATE_BUFFERING -> "Buffering"
            Player.STATE_READY -> if (controller.isPlaying) "Playing" else "Paused"
            Player.STATE_ENDED -> "Ended"
            else -> "Unknown"
        }
    }

    // For Color objects
    private fun getComplementaryColor(color: Color): Color {
        val red = (255 - color.red * 255).coerceIn(0F, 255F).toInt()
        val green = (255 - color.green * 255).coerceIn(0F, 255F).toInt()
        val blue = (255 - color.blue * 255).coerceIn(0F, 255F).toInt()
        return Color(red, green, blue)
    }

    // For integer (ARGB) color values
    private fun getComplementaryColor(colorInt: Int): Color {
        val color = Color(colorInt) // Convert integer to Color object
        return getComplementaryColor(color) // Call the Color version
    }


}


class ControllerPlayerListener(
    private val callback: () -> Unit,
) :
    Player.Listener {

    override fun onEvents(player: Player, events: Player.Events) {
        super.onEvents(player, events)
        callback.invoke()
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        super.onMediaMetadataChanged(mediaMetadata)
        callback.invoke()
    }
}

