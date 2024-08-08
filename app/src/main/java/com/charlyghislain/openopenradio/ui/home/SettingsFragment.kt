package com.charlyghislain.openopenradio.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.charlyghislain.openopenradio.service.radio.settings.Settings
import com.charlyghislain.openopenradio.service.radio.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                SettingsFragmentScreen(viewModel)
            }
        }
    }


}

@Composable
fun SettingsFragmentScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settingsFlow.collectAsState(initial = Settings())

    SettingsScreen(
        settings = settings,
        onSettingChanged = { updatedSettings ->
            viewModel.saveSettings(updatedSettings)
        },
        onSave = {
        }
    )
}

@Composable
fun SettingsScreen(
    settings: Settings,
    onSettingChanged: (Settings) -> Unit,
    onSave: () -> Unit
) {
    var connectTimeout by remember { mutableStateOf(settings.connectTimeoutMs.toString()) }
    var readTimeout by remember { mutableStateOf(settings.readTimeoutMs.toString()) }
    var liveTargetOffsetMs by remember { mutableStateOf(settings.liveTargetOffsetMs.toString()) }
    var fallbackMaxPlaybackSpeed by remember { mutableStateOf(settings.fallbackMaxPlaybackSpeed.toString()) }
    var fallbackMinPlaybackSpeed by remember { mutableStateOf(settings.fallbackMinPlaybackSpeed.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SettingsIntInput(connectTimeout, "Connect Timeout (ms)",
            onSettingsChange = onSettingChanged,
            settingsCloneFactory = { value ->
                settings.copy(
                    connectTimeoutMs = value.toIntOrNull() ?: settings.connectTimeoutMs
                )
            })
        Spacer(modifier = Modifier.height(8.dp))
        SettingsIntInput(readTimeout, "Read Timeout (ms)",
            onSettingsChange = onSettingChanged,
            settingsCloneFactory = { value ->
                settings.copy(
                    readTimeoutMs = value.toIntOrNull() ?: settings.readTimeoutMs
                )
            })
        Spacer(modifier = Modifier.height(8.dp))
        SettingsIntInput(liveTargetOffsetMs, "Live Target Offset (ms)",
            onSettingsChange = onSettingChanged,
            settingsCloneFactory = { value ->
                settings.copy(
                    liveTargetOffsetMs = value.toIntOrNull() ?: settings.liveTargetOffsetMs
                )
            })
        Spacer(modifier = Modifier.height(8.dp))
        SettingsIntInput(fallbackMaxPlaybackSpeed, "Fallback Max Playback Speed",
            onSettingsChange = onSettingChanged,
            settingsCloneFactory = { value ->
                settings.copy(
                    fallbackMaxPlaybackSpeed = value.toFloatOrNull()
                        ?: settings.fallbackMaxPlaybackSpeed
                )
            })
        Spacer(modifier = Modifier.height(8.dp))
        SettingsIntInput(fallbackMinPlaybackSpeed, "Fallback Min Playback Speed",
            onSettingsChange = onSettingChanged,
            settingsCloneFactory = { value ->
                settings.copy(
                    fallbackMinPlaybackSpeed = value.toFloatOrNull()
                        ?: settings.fallbackMinPlaybackSpeed
                )
            })

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                onSave()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Save Settings")
        }
    }
}

@Composable
private fun SettingsIntInput(
    value: String,
    label: String,
    onSettingsChange: (Settings) -> Unit,
    settingsCloneFactory: (String) -> Settings,
) {
    var valueRemember by remember { mutableStateOf(value) }

    OutlinedTextField(
        value = valueRemember,
        onValueChange = { newValue ->
            val updatedSettings = settingsCloneFactory.invoke(newValue)
            onSettingsChange(updatedSettings)
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
}