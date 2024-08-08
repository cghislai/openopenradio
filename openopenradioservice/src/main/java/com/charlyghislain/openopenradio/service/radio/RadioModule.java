package com.charlyghislain.openopenradio.service.radio;

import android.app.Application;
import android.content.Context;

import androidx.datastore.core.DataStore;
import androidx.datastore.core.DataStoreFactory;
import androidx.room.Room;

import com.charlyghislain.openopenradio.service.radio.dao.RadioCountryDao;
import com.charlyghislain.openopenradio.service.radio.dao.RadioDatabase;
import com.charlyghislain.openopenradio.service.radio.dao.RadioLanguageDao;
import com.charlyghislain.openopenradio.service.client.webradio.WebRadioClient;
import com.charlyghislain.openopenradio.service.radio.dao.RadioGenreDao;
import com.charlyghislain.openopenradio.service.radio.dao.RadioStationDao;
import com.charlyghislain.openopenradio.service.radio.dao.RadioStationFavoriteDao;
import com.charlyghislain.openopenradio.service.radio.repository.CountryRepository;
import com.charlyghislain.openopenradio.service.radio.repository.GenreRepository;
import com.charlyghislain.openopenradio.service.radio.repository.LanguageRepository;
import com.charlyghislain.openopenradio.service.radio.repository.StationFavoritesRepository;
import com.charlyghislain.openopenradio.service.radio.repository.StationRepository;
import com.charlyghislain.openopenradio.service.radio.settings.Settings;
import com.charlyghislain.openopenradio.service.radio.settings.SettingsSerializer;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
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
    RadioStationFavoriteDao provideRadioStationFavoriteDao(RadioDatabase radioDatabase) {
        return radioDatabase.radioStationFavoriteDao();
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
    StationFavoritesRepository provideStationFavoritesRepository(WebRadioClient webRadioClient, RadioStationFavoriteDao radioStationsDaoFavoriteDao) {
        return new StationFavoritesRepository(webRadioClient, radioStationsDaoFavoriteDao);
    }

    @Provides
    @Singleton
    StationRepository provideStationsRepository(WebRadioClient webRadioClient,
                                                RadioStationDao radioStationsDao,
                                                RadioStationFavoriteDao radioStationFavoriteDao) {
        return new StationRepository(webRadioClient, radioStationsDao, radioStationFavoriteDao);
    }


    @Provides
    @Singleton
    DataStore<Settings> provideDataStoreSettings(@ApplicationContext Context appContext) {
        return DataStoreFactory.INSTANCE.create(
                SettingsSerializer.INSTANCE,
                () -> new File(appContext.getFilesDir(), "settings.json")
        );
    }
}
