package com.charlyghislain.openopenradio.service.radio.settings

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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
    private val dataStore: DataStore<Settings>
) : ViewModel() {

    val settingsFlow: Flow<Settings> = dataStore.data.catch { _ ->
        // Handle exceptions, e.g., emit default settings
        emit(Settings())
    }

    fun saveSettings(newSettings: Settings) {
        viewModelScope.launch {
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


