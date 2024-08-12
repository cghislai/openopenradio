package com.charlyghislain.openopenradio.service.radio.model;

import com.charlyghislain.openopenradio.service.radio.model.entity.RadioStation;

import java.util.Objects;

public class RatedStation {

    private RadioStation station;
    private Boolean favorite;

    public RatedStation(RadioStation station, Boolean favorite) {
        this.station = station;
        this.favorite = favorite;
    }

    public RadioStation getStation() {
        return station;
    }

    public Boolean getFavorite() {
        return favorite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RatedStation that = (RatedStation) o;
        return Objects.equals(station, that.station);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(station);
    }
}
