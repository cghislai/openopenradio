package com.charlyghislain.openopenradio.service.radio.repository;

import androidx.lifecycle.LiveData;

import com.charlyghislain.openopenradio.service.radio.model.entity.RadioSource;
import com.charlyghislain.openopenradio.service.client.webradio.WebRadioClient;
import com.charlyghislain.openopenradio.service.client.webradio.model.WebRadioStation;
import com.charlyghislain.openopenradio.service.radio.dao.RadioStationDao;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioStation;
import com.charlyghislain.openopenradio.service.util.RequestCallback;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class StationRepository {
    private final WebRadioClient webRadioClient;
    private final RadioStationDao radioStationDao;

    public StationRepository(WebRadioClient webRadioClient, RadioStationDao radioStationDao) {
        this.webRadioClient = webRadioClient;
        this.radioStationDao = radioStationDao;
    }

    public LiveData<List<RadioStation>> getStations() {
        return radioStationDao.getAllStations();
    }

    public LiveData<List<RadioStation>> getStationsByGenre(String genre) {
        return radioStationDao.getStationsByGenre(genre);
    }

    public LiveData<List<RadioStation>> getStationsByContry(String country) {
        return radioStationDao.getStationsByCountry(country);
    }

    public LiveData<List<RadioStation>> getStationsByLanguage(String language) {
        return radioStationDao.getStationsByLanguage(language);
    }

    public void fetchStations() {
        webRadioClient.getStations(createAsyncCallback(value -> {
            List<RadioStation> radioLanguageList = value.stream()
                    .map(this::createRadioStation)
                    .collect(Collectors.toList());
            radioStationDao.clearStations(RadioSource.WEBRADIOS);
            radioStationDao.addStations(radioLanguageList);
        }));
    }

    private RadioStation createRadioStation(WebRadioStation webRadioStation) {
        RadioStation radioStation = new RadioStation(
                RadioSource.WEBRADIOS,
                webRadioStation.getId()
        );
        radioStation.setName(webRadioStation.getName());
        radioStation.setStreamUrl(webRadioStation.getStreamUri());
        radioStation.setCountry(webRadioStation.getCountry());
        radioStation.setLanguages(webRadioStation.getLanguages());
        radioStation.setGenres(webRadioStation.getGenre());
        radioStation.setDescription(webRadioStation.getDescription());
        radioStation.setLogoUri(webRadioStation.getImage());

        return radioStation;
    }

    private <T> RequestCallback<T> createAsyncCallback(Consumer<T> onSuccess) {
        return new RequestCallback<T>() {
            @Override
            public void onSuccess(T value) {
                new Thread(() -> {
                    onSuccess.accept(value);
                }).start();
            }

            @Override
            public void onError(Throwable error) {
                reportError(error);
            }
        };
    }

    private void reportError(Throwable error) {

    }
}
