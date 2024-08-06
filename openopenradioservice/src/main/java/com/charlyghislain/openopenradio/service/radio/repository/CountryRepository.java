package com.charlyghislain.openopenradio.service.radio.repository;

import androidx.lifecycle.LiveData;

import com.charlyghislain.openopenradio.service.radio.dao.RadioCountryDao;
import com.charlyghislain.openopenradio.service.radio.model.CountryWithStats;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioSource;
import com.charlyghislain.openopenradio.service.client.webradio.WebRadioClient;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioCountry;
import com.charlyghislain.openopenradio.service.util.RequestCallback;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CountryRepository {
    private final WebRadioClient webRadioClient;
    private final RadioCountryDao radioCountryDao;

    public CountryRepository(WebRadioClient webRadioClient, RadioCountryDao radioCountryDao) {
        this.webRadioClient = webRadioClient;
        this.radioCountryDao = radioCountryDao;
    }

    public LiveData<List<String>> getCountrys() {
        return radioCountryDao.getAllCountryNames();
    }

    public LiveData<List<CountryWithStats>> getCountryWithStats() {
        return radioCountryDao.getCountryWithStats();
    }

    public void fetchCountries() {
        webRadioClient.getCountries(createAsyncCallback(value -> {
            List<RadioCountry> radioCountryList = value.stream()
                    .map(v -> new RadioCountry(RadioSource.WEBRADIOS, v))
                    .collect(Collectors.toList());
            radioCountryDao.clearCountries(RadioSource.WEBRADIOS);
            radioCountryDao.addCountries(radioCountryList);
        }));
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
