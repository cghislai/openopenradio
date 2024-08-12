package com.charlyghislain.openopenradio.service.radio.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.charlyghislain.openopenradio.service.client.webradio.WebRadioClient;
import com.charlyghislain.openopenradio.service.client.webradio.model.WebRadioStation;
import com.charlyghislain.openopenradio.service.radio.dao.RadioStationDao;
import com.charlyghislain.openopenradio.service.radio.dao.RadioStationFavoriteDao;
import com.charlyghislain.openopenradio.service.radio.model.RatedStation;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioSource;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioStation;
import com.charlyghislain.openopenradio.service.util.RequestCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class StationRepository {
    private final WebRadioClient webRadioClient;
    private final RadioStationDao radioStationDao;
    private final LiveData<List<RadioStation>> allFavoritesLiveData;
    private final List<RadioStation> favorites = new ArrayList<>();

    public StationRepository(WebRadioClient webRadioClient,
                             RadioStationDao radioStationDao,
                             RadioStationFavoriteDao radioStationFavoriteDao) {
        this.webRadioClient = webRadioClient;
        this.radioStationDao = radioStationDao;
        this.allFavoritesLiveData = radioStationDao.getAllStationsFavorites();
    }

    public LiveData<List<RatedStation>> getStations() {
        return getRatedStations(radioStationDao.getAllStations());
    }

    public LiveData<List<RatedStation>> getStationsPage(int offset, int length) {
        return getRatedStations(radioStationDao.getAllStationsPage(offset, length));
    }

    public LiveData<List<RatedStation>> getStationsByGenre(String genre) {
        return getRatedStations(radioStationDao.getStationsByGenre(genre));
    }

    public LiveData<List<RatedStation>> getStationsByContry(String country) {
        return getRatedStations(radioStationDao.getStationsByCountry(country));
    }

    public LiveData<List<RatedStation>> getStationsByLanguage(String language) {
        return getRatedStations(radioStationDao.getStationsByLanguage(language));
    }

    public LiveData<RatedStation> findStationById(RadioSource source, String sourceId) {
        return getRatedStation(radioStationDao.findStationById(source, sourceId));
    }

    public LiveData<List<RatedStation>> searchStations(String query) {
        return getRatedStations(radioStationDao.searchStations(query));
    }

    public LiveData<Integer> countStationsSearch(String query) {
        return radioStationDao.countStationsSearch(query);
    }

    public LiveData<List<RatedStation>> getStationsSearch(String query, int offset, int lenght) {
        return getRatedStations(radioStationDao.getStationsSearch(query, offset, lenght));
    }

    public LiveData<List<RatedStation>> getAllStationsFavorites() {
        return getRatedStations(radioStationDao.getAllStationsFavorites());
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

    private @NonNull LiveData<List<RatedStation>> getRatedStations(LiveData<List<RadioStation>> allStationsPage) {
        MediatorLiveData<List<RatedStation>> mediatorLiveData = new MediatorLiveData<>();

        mediatorLiveData.addSource(allStationsPage, allStations -> {
            List<RatedStation> ratedList = allStations.stream()
                    .map(this::getRatedStationWithFavorites)
                    .collect(Collectors.toList());
            mediatorLiveData.setValue(ratedList);
        });
        mediatorLiveData.addSource(allFavoritesLiveData, allFavorites -> {
            this.favorites.clear();
            this.favorites.addAll(allFavorites);

            List<RatedStation> curStations = mediatorLiveData.getValue();
            if (curStations != null) {
                List<RatedStation> updatedStations = curStations.stream()
                        .map(RatedStation::getStation)
                        .map(this::getRatedStationWithFavorites)
                        .collect(Collectors.toList());
                mediatorLiveData.setValue(updatedStations);
            }
        });
        return mediatorLiveData;
    }


    private LiveData<RatedStation> getRatedStation(LiveData<RadioStation> liveData) {
        MediatorLiveData<RatedStation> mediatorLiveData = new MediatorLiveData<>();

        mediatorLiveData.addSource(liveData, singleStation -> {
            RatedStation ratedStation = getRatedStationWithFavorites(singleStation);
            mediatorLiveData.setValue(ratedStation);
        });
        mediatorLiveData.addSource(allFavoritesLiveData, allFavorites -> {
            this.favorites.clear();
            this.favorites.addAll(allFavorites);

            RatedStation ratedStation = mediatorLiveData.getValue();
            if (ratedStation != null) {
                RatedStation updatedItem = getRatedStationWithFavorites(ratedStation.getStation());
                mediatorLiveData.setValue(updatedItem);
            }
        });
        return mediatorLiveData;
    }

    private @NonNull RatedStation getRatedStationWithFavorites(RadioStation station) {
        boolean fav = this.favorites.contains(station);
        return new RatedStation(station, fav);
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
        Log.w("StationRepository", "Error fetching stations", error);
    }
}
