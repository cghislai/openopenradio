package com.charlyghislain.openopenradio.service.radio.model.entity;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.TypeConverters;

import com.charlyghislain.openopenradio.service.radio.util.StringListConverter;

import java.util.List;
import java.util.Objects;


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

    @ColumnInfo
    private String logoUri;

    @ColumnInfo
    @TypeConverters(StringListConverter.class)
    private List<String> languages;
    @ColumnInfo
    @TypeConverters(StringListConverter.class)
    private List<String> genres;
    @ColumnInfo
    private String country;
    @ColumnInfo
    private String description;


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

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoUri() {
        return logoUri;
    }

    public void setLogoUri(String logoUri) {
        this.logoUri = logoUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RadioStation that = (RadioStation) o;
        return source == that.source && Objects.equals(sourceId, that.sourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, sourceId);
    }
}
