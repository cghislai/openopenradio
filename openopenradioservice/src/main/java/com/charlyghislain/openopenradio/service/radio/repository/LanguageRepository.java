package com.charlyghislain.openopenradio.service.radio.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.charlyghislain.openopenradio.service.radio.dao.RadioLanguageDao;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioSource;
import com.charlyghislain.openopenradio.service.radio.model.LanguageWithStats;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioLanguage;
import com.charlyghislain.openopenradio.service.util.RequestCallback;
import com.charlyghislain.openopenradio.service.client.webradio.WebRadioClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;
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

    public CompletableFuture<Void> fetchLanguages() {
        CompletableFuture<Void> done = new CompletableFuture<>();
        webRadioClient.getLanguages(createAsyncCallback(done, value -> {
            List<RadioLanguage> radioLanguageList = value.stream()
                    .map(v -> new RadioLanguage(RadioSource.WEBRADIOS, v))
                    .collect(Collectors.toList());
            radioLanguageDao.replaceLanguages(RadioSource.WEBRADIOS, radioLanguageList);
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
        Log.w("LanguageRepository", "Error fetching content", error);
    }
}
