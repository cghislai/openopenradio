package com.charlyghislain.openopenradio.service.radio.settings

import androidx.datastore.core.IOException
import androidx.datastore.core.Serializer
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.serializer

object SettingsSerializer : Serializer<Settings> {
    override val defaultValue: Settings = Settings()

    override suspend fun readFrom(input: InputStream): Settings {
        return try {
            Json.decodeFromString(
                deserializer = Settings.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: IOException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: Settings, output: OutputStream) {
        output.write(
            Json.encodeToString(
                serializer = Settings.serializer(),
                value = t
            ).encodeToByteArray()
        )
    }
}