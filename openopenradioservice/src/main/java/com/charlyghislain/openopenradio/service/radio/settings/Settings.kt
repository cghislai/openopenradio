package com.charlyghislain.openopenradio.service.radio.settings

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val connectTimeoutMs: Int = 1000,
    val readTimeoutMs: Int = 5000,
    val liveTargetOffsetMs: Int = 5000,
    val fallbackMaxPlaybackSpeed: Float = 1.05f,
    val fallbackMinPlaybackSpeed: Float = 0.95f
)