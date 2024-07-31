package com.charlyghislain.openopenradio.service.radio.model;

import androidx.room.Entity;

public class GenreWithStats {

    private String name;
    private int stationCount;

    public GenreWithStats() {
    }

    public String getName() {
        return name;
    }

    public int getStationCount() {
        return stationCount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStationCount(int stationCount) {
        this.stationCount = stationCount;
    }
}
