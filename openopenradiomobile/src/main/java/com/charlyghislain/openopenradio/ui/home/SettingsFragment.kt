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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.charlyghislain.openopenradio.service.radio.settings.Settings
import com.charlyghislain.openopenradio.service.radio.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.debounce

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
    val settings by viewModel.settingsFlow
        .collectAsState(initial = Settings())

    SettingsScreen(
        settings = settings,
        onSettingChanged = { updatedSettings ->
            viewModel.saveSettings(updatedSettings)
        },
    )
}

@Composable
fun SettingsScreen(
    settings: Settings,
    onSettingChanged: (Settings) -> Unit,
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SettingsIntInput(settings.connectTimeoutMs, "Connect Timeout (ms)",
            onSettingsChange = onSettingChanged,
            settingsCloneFactory = { value ->
                settings.copy(
                    connectTimeoutMs = value ?: settings.connectTimeoutMs
                )
            })
        Spacer(modifier = Modifier.height(8.dp))
        SettingsIntInput(settings.readTimeoutMs, "Read Timeout (ms)",
            onSettingsChange = onSettingChanged,
            settingsCloneFactory = { value ->
                settings.copy(
                    readTimeoutMs = value ?: settings.readTimeoutMs
                )
            })
        Spacer(modifier = Modifier.height(8.dp))
        SettingsIntInput(settings.liveTargetOffsetMs, "Live Target Offset (ms)",
            onSettingsChange = onSettingChanged,
            settingsCloneFactory = { value ->
                settings.copy(
                    liveTargetOffsetMs = value ?: settings.liveTargetOffsetMs
                )
            })
        Spacer(modifier = Modifier.height(8.dp))
        SettingsFloatInput(settings.fallbackMaxPlaybackSpeed,
            "Fallback Max Playback Speed",
            onSettingsChange = onSettingChanged,
            settingsCloneFactory = { value ->
                settings.copy(
                    fallbackMaxPlaybackSpeed = value ?: settings.fallbackMaxPlaybackSpeed
                )
            })
        Spacer(modifier = Modifier.height(8.dp))
        SettingsFloatInput(settings.fallbackMinPlaybackSpeed,
            "Fallback Min Playback Speed",
            onSettingsChange = onSettingChanged,
            settingsCloneFactory = { value ->
                settings.copy(
                    fallbackMinPlaybackSpeed = value ?: settings.fallbackMinPlaybackSpeed
                )
            })
    }
}

@Composable
private fun SettingsIntInput(
    value: Int,
    label: String,
    onSettingsChange: (Settings) -> Unit,
    settingsCloneFactory: (Int?) -> Settings,
) {
    var localValue by remember(value) { mutableStateOf(value.toString()) }

    OutlinedTextField(
        value = localValue,
        onValueChange = { newValue ->
            localValue = newValue
            val intValue = newValue.toIntOrNull()
            val updatedSettings = settingsCloneFactory.invoke(intValue)
            onSettingsChange(updatedSettings)
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}


@Composable
private fun SettingsFloatInput(
    value: Float,
    label: String,
    onSettingsChange: (Settings) -> Unit,
    settingsCloneFactory: (Float?) -> Settings,
) {
    var localValue by remember(value) { mutableStateOf(value.toBigDecimal().setScale(4).toString()) }

    OutlinedTextField(
        value = localValue,
        onValueChange = { newValue ->
            localValue = newValue
            val floatValue = newValue.toFloatOrNull()
            val updatedSettings = settingsCloneFactory.invoke(floatValue)
            onSettingsChange(updatedSettings)
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}