package com.charlyghislain.openopenradio.service.radio.model;

public class LanguageWithStats {

    private String name;
    private int stationCount;

    public LanguageWithStats() {
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
