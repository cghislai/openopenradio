package com.charlyghislain.openopenradio.service.radio;

import android.app.Application;

import androidx.room.Room;

import com.charlyghislain.openopenradio.service.client.webradio.WebRadioClient;
import com.charlyghislain.openopenradio.service.radio.dao.RadioCountryDao;
import com.charlyghislain.openopenradio.service.radio.dao.RadioDatabase;
import com.charlyghislain.openopenradio.service.radio.dao.RadioGenreDao;
import com.charlyghislain.openopenradio.service.radio.dao.RadioLanguageDao;
import com.charlyghislain.openopenradio.service.radio.dao.RadioStationDao;
import com.charlyghislain.openopenradio.service.radio.repository.CountryRepository;
import com.charlyghislain.openopenradio.service.radio.repository.GenreRepository;
import com.charlyghislain.openopenradio.service.radio.repository.LanguageRepository;
import com.charlyghislain.openopenradio.service.radio.repository.StationRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class RadioModule {

    @Provides
    @Singleton
    RadioDatabase provideRRadioDatabase(Application application) {
        return Room.databaseBuilder(application, RadioDatabase.class, "radio_database")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @Singleton
    RadioCountryDao provideRadioCountryDao(RadioDatabase radioDatabase) {
        return radioDatabase.radioCountryDao();
    }

    @Provides
    @Singleton
    RadioGenreDao provideRadioGenreDao(RadioDatabase radioDatabase) {
        return radioDatabase.radioGenreDao();
    }

    @Provides
    @Singleton
    RadioLanguageDao provideRadioLanguageDao(RadioDatabase radioDatabase) {
        return radioDatabase.radioLanguageDao();
    }

    @Provides
    @Singleton
    RadioStationDao provideRadioStationDao(RadioDatabase radioDatabase) {
        return radioDatabase.radioStationDao();
    }

    @Provides
    @Singleton
    CountryRepository provideCountryRepository(WebRadioClient webRadioClient, RadioCountryDao radioCountryDao) {
        return new CountryRepository(webRadioClient, radioCountryDao);
    }

    @Provides
    @Singleton
    GenreRepository provideGenreRepository(WebRadioClient webRadioClient, RadioGenreDao radioGenreDao) {
        return new GenreRepository(webRadioClient, radioGenreDao);
    }

    @Provides
    @Singleton
    LanguageRepository provideLanguageRepository(WebRadioClient webRadioClient, RadioLanguageDao radioLanguageDao) {
        return new LanguageRepository(webRadioClient, radioLanguageDao);
    }

    @Provides
    @Singleton
    StationRepository provideStationsRepository(WebRadioClient webRadioClient, RadioStationDao radioStationsDao) {
        return new StationRepository(webRadioClient, radioStationsDao);
    }

}
