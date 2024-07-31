package com.charlyghislain.openopenradio.ui.components

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import com.charlyghislain.openopenradio.service.radio.RadioService
import com.charlyghislain.openopenradio.service.radio.model.GenreWithStats

@Composable
fun GenreList(
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isServiceBound = remember { mutableStateOf(false) }
    val genres = remember { mutableStateOf<List<GenreWithStats>>(emptyList()) }
    val radioService = remember { mutableStateOf<RadioService.IWebRadioService?>(null) }

    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as RadioService.IWebRadioService
                radioService.value = binder
                isServiceBound.value = true
            }

            override fun onServiceDisconnected(arg0: ComponentName) {
                isServiceBound.value = false
                radioService.value = null
                genres.value = emptyList()
            }
        }
    }

    LaunchedEffect(key1 = isServiceBound.value) {
        if (isServiceBound.value) {
            radioService.value?.genres?.observe(context as LifecycleOwner, { newGenres ->
                genres.value = newGenres
            })
        }
    }

    // Bind to the service when the composable enters the composition
    DisposableEffect(Unit) {
        val intent = Intent(context, RadioService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        onDispose {
            context.unbindService(serviceConnection)
        }
    }

    // Display the list of genres
    LazyColumn(modifier = modifier) {
        items(genres.value) { genre ->
            GenreItem(genreWithStats = genre, onGenreClick = { onClick(genre.name) })
        }
    }

}


@Composable
fun GenreItem(genreWithStats: GenreWithStats, onGenreClick: () -> Unit) {
    // Display genre information and handle click
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onGenreClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Folder, // Or your preferred folder icon
            contentDescription = "Genre Folder",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp)) // Add spacing between icon and text
        Column {
            Text(text = genreWithStats.name, fontSize = 18.sp) // Large genre name
            Text(
                text = "${genreWithStats.stationCount} stations",
                fontSize = 12.sp
            ) // Smaller count
        }
    }

    HorizontalDivider()
}