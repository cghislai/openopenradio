package com.charlyghislain.openopenradio.service.radio.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.charlyghislain.openopenradio.service.radio.dao.RadioCountryDao;
import com.charlyghislain.openopenradio.service.radio.model.CountryWithStats;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioSource;
import com.charlyghislain.openopenradio.service.client.webradio.WebRadioClient;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioCountry;
import com.charlyghislain.openopenradio.service.util.RequestCallback;

import java.util.List;
import java.util.concurrent.CompletableFuture;
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

    public CompletableFuture<Void> fetchCountries() {
        CompletableFuture<Void> done = new CompletableFuture<>();
        webRadioClient.getCountries(createAsyncCallback(done, value -> {
            List<RadioCountry> radioCountryList = value.stream()
                    .map(v -> new RadioCountry(RadioSource.WEBRADIOS, v))
                    .collect(Collectors.toList());
            radioCountryDao.replaceCountries(RadioSource.WEBRADIOS, radioCountryList);
        }));
        return done;
    }


    private <T> RequestCallback<T> createAsyncCallback(CompletableFuture<Void> done, Consumer<T> onSuccess) {
        return new RequestCallback<T>() {
            @Override
            public void onSuccess(T value) {
                new Thread(() -> {
                    try {
                        onSuccess.accept(value);
                        done.complete(null);
                    } catch (RuntimeException e) {
                        reportError(e);
                        done.completeExceptionally(e);
                    }
                }).start();
            }

            @Override
            public void onError(Throwable error) {
                reportError(error);
                done.completeExceptionally(error);
            }
        };
    }

    private void reportError(Throwable error) {
        Log.w("CountryRepository", "Error fetching content", error);
    }
}
