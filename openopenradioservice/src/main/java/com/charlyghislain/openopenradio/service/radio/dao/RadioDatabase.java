package com.charlyghislain.openopenradio.service.radio.dao;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.charlyghislain.openopenradio.service.radio.model.entity.RadioCountry;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioGenre;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioLanguage;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioStation;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioStationFavorite;

@Database(entities = {
        RadioStation.class,
        RadioStationFavorite.class,
        RadioGenre.class,
        RadioCountry.class,
        RadioLanguage.class
}, version = 1)
public abstract class RadioDatabase extends RoomDatabase {
    public abstract RadioStationDao radioStationDao();

    public abstract RadioStationFavoriteDao radioStationFavoriteDao();

    public abstract RadioGenreDao radioGenreDao();

    public abstract RadioCountryDao radioCountryDao();

    public abstract RadioLanguageDao radioLanguageDao();

}
