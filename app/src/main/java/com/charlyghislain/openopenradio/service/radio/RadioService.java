package com.charlyghislain.openopenradio.service.radio;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.charlyghislain.openopenradio.service.radio.dao.RadioDatabase;
import com.charlyghislain.openopenradio.service.radio.model.GenreWithStats;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioStation;
import com.charlyghislain.openopenradio.service.radio.repository.CountryRepository;
import com.charlyghislain.openopenradio.service.radio.repository.GenreRepository;
import com.charlyghislain.openopenradio.service.radio.repository.LanguageRepository;
import com.charlyghislain.openopenradio.service.radio.repository.StationRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RadioService extends Service {

    private final IWebRadioService binder = new IWebRadioService(this);

    @Inject
    CountryRepository countryRepository;
    @Inject
    GenreRepository genreRepository;
    @Inject
    LanguageRepository languageRepository;
    @Inject
    StationRepository stationRepository;
    @Inject
    RadioDatabase radioDatabase;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        genreRepository.fetchGenres();
        countryRepository.fetchCountries();
        languageRepository.fetchLanguages();
        stationRepository.fetchStations();
        return binder;
    }

    public static class IWebRadioService extends Binder {

        private final RadioService service;

        public IWebRadioService(RadioService service) {
            this.service = service;
        }

        public LiveData<List<GenreWithStats>> getGenres() {
            return service.genreRepository.getGenreWithStats();
        }

        public LiveData<List<String>> getCountries() {
            return service.countryRepository.getCountrys();
        }

        public LiveData<List<String>> getLanguages() {
            return service.languageRepository.getLanguages();
        }

        public LiveData<List<RadioStation>> getStations() {
            return service.stationRepository.getStations();
        }

    }
}
