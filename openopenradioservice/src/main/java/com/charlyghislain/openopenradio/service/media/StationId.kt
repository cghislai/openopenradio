package com.charlyghislain.openopenradio.service.media

import com.charlyghislain.openopenradio.service.radio.model.entity.RadioSource

class StationId(val source: RadioSource, val sourceId: String) {
    override fun toString(): String {
        return source.name + "/" + sourceId
    }

    companion object {
        fun parseString(mediaId: String): StationId {
            val parts = mediaId.split("/")
            return StationId(RadioSource.valueOf(parts[0]), parts[1])
        }
    }
}