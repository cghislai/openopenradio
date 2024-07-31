package com.charlyghislain.openopenradio.service.radio.model;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "radio_station", primaryKeys = {"source", "sourceId"})
public class RadioStation {

    @ColumnInfo(index = true)
    @NonNull
    private RadioSource source;
    @ColumnInfo(index = true)
    @NonNull
    private String sourceId;

    @ColumnInfo(index = true)
    @NonNull
    private String name;

    @ColumnInfo
    @NonNull
    private String streamUrl;


    public RadioStation(@NonNull RadioSource source, @NonNull String sourceId) {
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

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(@NonNull String streamUrl) {
        this.streamUrl = streamUrl;
    }
}
