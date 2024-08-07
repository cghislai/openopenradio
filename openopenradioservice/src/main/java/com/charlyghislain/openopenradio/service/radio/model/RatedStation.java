package com.charlyghislain.openopenradio.service.radio.model;

import com.charlyghislain.openopenradio.service.radio.model.entity.RadioStation;

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
}
