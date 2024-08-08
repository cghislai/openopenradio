package com.charlyghislain.openopenradio.service.radio.repository;

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
    private final RadioStationFavoriteDao radioStationFavoriteDao;
    private final LiveData<List<RadioStation>> alLFavorites;

    public StationRepository(WebRadioClient webRadioClient,
                             RadioStationDao radioStationDao,
                             RadioStationFavoriteDao radioStationFavoriteDao) {
        this.webRadioClient = webRadioClient;
        this.radioStationDao = radioStationDao;
        this.radioStationFavoriteDao = radioStationFavoriteDao;
        this.alLFavorites = radioStationDao.getAllStationsFavorites();
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
        return Transformations.switchMap(allStationsPage, stations -> {
            List<LiveData<RatedStation>> individualRatedStations = stations.stream()
                    .map(this::getRatedStation)
                    .collect(Collectors.toList());

            MediatorLiveData<List<RatedStation>> mediatorLiveData = new MediatorLiveData<>();

            // Counter to track when all individual LiveData objects have emitted values
            int stationCount = individualRatedStations.size();
            AtomicInteger countDownLatch = new AtomicInteger(stationCount);
            List<RatedStation> currentList = new ArrayList<>(stationCount);
            for (int i = 0; i < stationCount; i++) {
                currentList.add(null);
            }
            if (currentList.isEmpty()) {
                mediatorLiveData.setValue(currentList);
            } else {
                for (int index = 0; index < stationCount; index++) {
                    LiveData<RatedStation> ratedStationLiveData = individualRatedStations.get(index);
                    int finalIndex = index;
                    mediatorLiveData.addSource(ratedStationLiveData, ratedStation -> {
                        currentList.remove(finalIndex);
                        currentList.add(finalIndex, ratedStation);
                        // Update the MediatorLiveData only when all individual LiveData objects have emitted
                        if (countDownLatch.decrementAndGet() == 0) {
                            mediatorLiveData.setValue(currentList);
                        }
                    });
                }
            }
            return mediatorLiveData;
        });
    }

    // Helper function to create LiveData<RatedStation> for each RadioStation
    private LiveData<RatedStation> getRatedStation(RadioStation station) {
        MutableLiveData<RatedStation> ratedStationLiveData = new MutableLiveData<>();
        List<RadioStation> favorites = alLFavorites.getValue();
        if (favorites == null) {
            favorites = Collections.emptyList();
        }
        boolean isFavorite = favorites.contains(station);
        ratedStationLiveData.postValue(new RatedStation(station, isFavorite)); // Use postValue for background threads
        return ratedStationLiveData;
    }

    private LiveData<RatedStation> getRatedStation(LiveData<RadioStation> liveData) {
        return Transformations.switchMap(liveData,
                station -> {
                    MutableLiveData<RatedStation> ratedStationLiveData = new MutableLiveData<>();
                    radioStationFavoriteDao.isStationFavorite(station.getSource(), station.getSourceId())
                            .observeForever(isFavorite -> {
                                ratedStationLiveData.setValue(new RatedStation(station, isFavorite));
                            });
                    return ratedStationLiveData;
                });
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
