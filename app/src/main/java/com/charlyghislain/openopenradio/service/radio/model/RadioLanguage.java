package com.charlyghislain.openopenradio.service.radio.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "radio_language", primaryKeys = {"source", "name"})
public class RadioLanguage {

    @ColumnInfo(index = true)
    @NonNull
    private RadioSource source;
    @ColumnInfo(index = true)
    @NonNull
    private String name;

    public RadioLanguage(@NonNull RadioSource source, @NonNull String name) {
        this.source = source;
        this.name = name;
    }

    @NonNull
    public RadioSource getSource() {
        return source;
    }

    public void setSource(@NonNull RadioSource source) {
        this.source = source;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }
}
