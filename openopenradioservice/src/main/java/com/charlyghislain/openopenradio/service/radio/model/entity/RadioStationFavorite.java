package com.charlyghislain.openopenradio.service.radio.model.entity;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.TypeConverters;

import com.charlyghislain.openopenradio.service.radio.util.StringListConverter;

import java.util.List;


@Entity(tableName = "radio_station_favorite", primaryKeys = {"source", "sourceId"})
public class RadioStationFavorite {

    @ColumnInfo(index = true)
    @NonNull
    private RadioSource source;
    @ColumnInfo(index = true)
    @NonNull
    private String sourceId;

    public RadioStationFavorite(@NonNull RadioSource source, @NonNull String sourceId) {
        this.source = source;
        this.sourceId = sourceId;
    }

    @NonNull
    public RadioSource getSource() {
        return source;
    }

    public void setSource(@NonNull RadioSource source) {
        this.source = source;
    }

    @NonNull
    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(@NonNull String sourceId) {
        this.sourceId = sourceId;
    }

}
