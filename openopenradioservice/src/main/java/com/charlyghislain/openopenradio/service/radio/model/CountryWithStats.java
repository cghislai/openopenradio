package com.charlyghislain.openopenradio.service.radio.model;

public class CountryWithStats {

    private String name;
    private int stationCount;

    public CountryWithStats() {
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
