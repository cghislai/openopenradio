package com.charlyghislain.openopenradio.service.radio.repository;

import androidx.lifecycle.LiveData;

import com.charlyghislain.openopenradio.service.radio.dao.RadioLanguageDao;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioSource;
import com.charlyghislain.openopenradio.service.radio.model.LanguageWithStats;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioLanguage;
import com.charlyghislain.openopenradio.service.util.RequestCallback;
import com.charlyghislain.openopenradio.service.client.webradio.WebRadioClient;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class LanguageRepository {
    private final WebRadioClient webRadioClient;
    private final RadioLanguageDao radioLanguageDao;

    public LanguageRepository(WebRadioClient webRadioClient, RadioLanguageDao radioLanguageDao) {
        this.webRadioClient = webRadioClient;
        this.radioLanguageDao = radioLanguageDao;
    }

    public LiveData<List<String>> getLanguages() {
        return radioLanguageDao.getAllLanguageNames();
    }


    public LiveData<List<LanguageWithStats>> getLanguageWithStats() {
        return radioLanguageDao.getLanguageWithStats();
    }

    public void fetchLanguages() {
        webRadioClient.getLanguages(createAsyncCallback(value -> {
            List<RadioLanguage> radioLanguageList = value.stream()
                    .map(v -> new RadioLanguage(RadioSource.WEBRADIOS, v))
                    .collect(Collectors.toList());
            radioLanguageDao.clearLanguages(RadioSource.WEBRADIOS);
            radioLanguageDao.addLanguages(radioLanguageList);
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
