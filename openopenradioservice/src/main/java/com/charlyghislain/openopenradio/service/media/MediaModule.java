package com.charlyghislain.openopenradio.service.media;


import com.charlyghislain.openopenradio.service.radio.repository.CountryRepository;
import com.charlyghislain.openopenradio.service.radio.repository.GenreRepository;
import com.charlyghislain.openopenradio.service.radio.repository.LanguageRepository;
import com.charlyghislain.openopenradio.service.radio.repository.StationFavoritesRepository;
import com.charlyghislain.openopenradio.service.radio.repository.StationRepository;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class MediaModule {

    @Provides
    MediaTreeService provideMediaTreeService(
            CountryRepository countryRepository,
            GenreRepository genreRepository,
            LanguageRepository languageRepository,
            StationRepository stationRepository,
            StationFavoritesRepository favoriteRepository
    ) {
        return new MediaTreeService(countryRepository, genreRepository,
                languageRepository, stationRepository,favoriteRepository);
    }

}
