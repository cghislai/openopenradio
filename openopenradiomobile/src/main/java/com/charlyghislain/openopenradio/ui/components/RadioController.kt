package com.charlyghislain.openopenradio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.HeartRating
import coil.compose.AsyncImage
import com.charlyghislain.openopenradio.R
import com.charlyghislain.openopenradio.ui.home.ReloadEvent
import kotlinx.coroutines.flow.MutableSharedFlow


@Composable
fun MyPlayerView(
    viewModel: RadioControllerViewModel,
) {
    val status by viewModel.mediaStatusFlow.collectAsState()
    val mediaItem by viewModel.mediaItemFlow.collectAsState()
    val playing by viewModel.mediaPlayingFlow.collectAsState()
    val backgroundColor by viewModel.backgroundColor.collectAsState()
    val foregroundColor by viewModel.foregroundColor.collectAsState()
    val connected by viewModel.controllerConnected.collectAsState()

    if (connected) {
        mediaItem?.let { item ->
            val itemRating = item.mediaMetadata.userRating as HeartRating;

            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.CenterEnd)
                        .background(backgroundColor)
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
                    Column(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Text(
                            text = mediaItem?.mediaMetadata?.title.toString(),
                            color = foregroundColor,
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                            softWrap = false,
                            style = TextStyle(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 24.sp,
                                background = backgroundColor.copy(alpha = 0.8f)
                            ),
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.aligned(Alignment.End)
                        ) {
                            PlayPauseIcon(
                                isPlaying = playing,
                                color = foregroundColor,
                                onPlayPauseClick = { viewModel.onPlayPause() }
                            )
                            BinaryHeartRating(
                                isFavorite = itemRating.isHeart, // Pass the current favorite status
                                onToggleFavorite = {
                                    val newStatus = !itemRating.isHeart
                                    viewModel.onSetRating(newStatus)
                                },
                                color = foregroundColor
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.aligned(Alignment.End)
                        ) {
                            Text(
                                text = status ?: "",
                                color = foregroundColor,
                                modifier = Modifier
                                    .padding(8.dp),
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    background = backgroundColor.copy(alpha = 0.8f)
                                ),
                            )
                        }
                    }
                }
            }
        } ?: run {
            Text(
                text = stringResource(R.string.player_empty_label),
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)
            )
        }
    } else {
        Text(text = "Disconnected")
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



