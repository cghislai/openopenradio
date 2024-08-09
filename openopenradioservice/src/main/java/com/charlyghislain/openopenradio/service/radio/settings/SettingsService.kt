package com.charlyghislain.openopenradio.service.radio.settings

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsService : Service() {

    @Inject
    lateinit var dataStore: DataStore<Settings>

    override fun onBind(intent: Intent?): IBinder? {

        TODO("Not yet implemented")
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Settings>,
) : ViewModel() {
    private val saveSettingsChannel = Channel<Settings>(
        capacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val settingsFlow: Flow<Settings> = dataStore.data.catch { _ ->
        // Handle exceptions, e.g., emit default settings
        emit(Settings())
    }

    init {
        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            saveSettingsChannel.consumeAsFlow()
                .debounce(500)
                .collect { newSettings ->
                    dataStore.updateData { currentSettings ->
                        currentSettings.copy(
                            connectTimeoutMs = newSettings.connectTimeoutMs,
                            readTimeoutMs = newSettings.readTimeoutMs,
                            liveTargetOffsetMs = newSettings.liveTargetOffsetMs,
                            fallbackMaxPlaybackSpeed = newSettings.fallbackMaxPlaybackSpeed,
                            fallbackMinPlaybackSpeed = newSettings.fallbackMinPlaybackSpeed
                        )
                    }
                }
        }
    }

    fun saveSettings(newSettings: Settings) {
        viewModelScope.launch {
            saveSettingsChannel.send(newSettings)
        }
    }
}


