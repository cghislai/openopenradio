package com.charlyghislain.openopenradio.service.radio.dao;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.charlyghislain.openopenradio.service.radio.model.RadioCountry;
import com.charlyghislain.openopenradio.service.radio.model.RadioGenre;
import com.charlyghislain.openopenradio.service.radio.model.RadioLanguage;
import com.charlyghislain.openopenradio.service.radio.model.RadioStation;

import javax.inject.Singleton;

@Database(entities = {
        RadioStation.class,
        RadioGenre.class,
        RadioCountry.class,
        RadioLanguage.class
}, version = 1)
public abstract class RadioDatabase extends RoomDatabase {
    public abstract RadioStationDao radioStationDao();

    public abstract RadioGenreDao radioGenreDao();

    public abstract RadioCountryDao radioCountryDao();

    public abstract RadioLanguageDao radioLanguageDao();

}
