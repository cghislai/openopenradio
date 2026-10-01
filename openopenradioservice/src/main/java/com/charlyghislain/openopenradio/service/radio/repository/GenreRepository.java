package com.charlyghislain.openopenradio.service.radio.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.charlyghislain.openopenradio.service.radio.model.entity.RadioSource;
import com.charlyghislain.openopenradio.service.client.webradio.WebRadioClient;
import com.charlyghislain.openopenradio.service.radio.dao.RadioGenreDao;
import com.charlyghislain.openopenradio.service.radio.model.GenreWithStats;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioGenre;
import com.charlyghislain.openopenradio.service.util.RequestCallback;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GenreRepository {
    private final WebRadioClient webRadioClient;
    private final RadioGenreDao radioGenreDao;

    public GenreRepository(WebRadioClient webRadioClient, RadioGenreDao radioGenreDao) {
        this.webRadioClient = webRadioClient;
        this.radioGenreDao = radioGenreDao;
    }

    public LiveData<List<String>> getGenres() {
        return radioGenreDao.getAllGenreNames();
    }

    public LiveData<List<GenreWithStats>> getGenreWithStats() {
        return radioGenreDao.getGenreWithStats();
    }


    public CompletableFuture<Void> fetchGenres() {
        CompletableFuture<Void> done = new CompletableFuture<>();
        webRadioClient.getGenres(createAsyncCallback(done, value -> {
            List<RadioGenre> radioGenreList = value.stream()
                    .map(v -> new RadioGenre(RadioSource.WEBRADIOS, v))
                    .collect(Collectors.toList());
            radioGenreDao.replaceGenres(RadioSource.WEBRADIOS, radioGenreList);
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
        Log.w("GenreRepository", "Error fetching content", error);
    }
}
