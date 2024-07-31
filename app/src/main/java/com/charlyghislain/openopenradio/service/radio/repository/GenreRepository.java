package com.charlyghislain.openopenradio.service.radio.repository;

import androidx.lifecycle.LiveData;

import com.charlyghislain.openopenradio.service.client.webradio.WebRadioClient;
import com.charlyghislain.openopenradio.service.radio.dao.RadioGenreDao;
import com.charlyghislain.openopenradio.service.radio.model.RadioGenre;
import com.charlyghislain.openopenradio.service.radio.model.RadioSource;
import com.charlyghislain.openopenradio.service.util.RequestCallback;

import java.util.List;
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
        webRadioClient.getGenres(createAsyncCallback(value -> {
            List<RadioGenre> radioGenreList = value.stream()
                    .map(v -> new RadioGenre(RadioSource.WEBRADIOS, v))
                    .collect(Collectors.toList());
            radioGenreDao.clearGenres(RadioSource.WEBRADIOS);
            radioGenreDao.addGenres(radioGenreList);
        }));

        return radioGenreDao.getAllGenreNames();
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
