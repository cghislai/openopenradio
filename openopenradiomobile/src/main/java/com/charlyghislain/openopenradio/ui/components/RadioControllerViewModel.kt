package com.charlyghislain.openopenradio.ui.components

import android.app.Application
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
    player: Player,
    val controller: MediaController,
) : AndroidViewModel(application) {
    val mediaItemFlow = MutableStateFlow(controller.currentMediaItem)
    val mediaPlayingFlow = MutableStateFlow(controller.isPlaying)
    val mediaStatusFlow = MutableStateFlow(getPlayerStatus(controller))
    val backgroundColor = MutableStateFlow(Color.White)
    val foregroundColor = MutableStateFlow(Color.Black)
    val controllerConnected = MutableStateFlow(false)
    val reloadEventFlow = MutableSharedFlow<ReloadEvent>()

    private val imageLoader = ImageLoader.Builder(application).build() // Initialize ImageLoader

    init {
        player.addListener(ControllerPlayerListener {
            viewModelScope.launch {
                mediaItemFlow.value = controller.currentMediaItem
                mediaPlayingFlow.value = controller.isPlaying
                mediaStatusFlow.value = getPlayerStatus(controller)

                backgroundColor.value =
                    getBackgroundColor(controller.currentMediaItem) ?: Color.White
                foregroundColor.value = getComplementaryColor(backgroundColor.value)
            }
        })
        controller.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                viewModelScope.launch {
                    controllerConnected.value = controller.isConnected
                }
            }
        })
    }

    fun onPlayPause() {
        viewModelScope.launch {
            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        }
    }

    fun onSetRating(fav: Boolean) {
        viewModelScope.launch {
            controller.currentMediaItem?.let { item ->
                controller.setRating(HeartRating(fav)).get()
                reloadEventFlow.emit(ReloadEvent(item.mediaId))
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

class RadioControllerViewModelFactory(
    private val application: Application,
    // Add other arguments needed for your ViewModel constructor
    private val player: Player,
    private val controller: MediaController
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RadioControllerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RadioControllerViewModel(application, player, controller) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

