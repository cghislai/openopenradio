package com.charlyghislain.openopenradio.service.radio.util

import androidx.room.TypeConverter

class StringListConverter {
    @TypeConverter
    fun fromStringList(strings: List<String?>?): String? {
        return strings?.filterNotNull()?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(string: String?): MutableList<String?>? {
        return string?.split(",")?.toMutableList()
    }
}
