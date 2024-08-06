package com.charlyghislain.openopenradio.service.client.webradio;

import com.charlyghislain.openopenradio.service.client.webradio.model.WebRadioStation;
import com.charlyghislain.openopenradio.service.util.RequestCallback;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebRadioClient {
    // https://github.com/jcorporation/webradiodb?tab=readme-ov-file
    private final String baseUri = "https://jcorporation.github.io/webradiodb/db/index";
    private final String bitratesPath = "/bitrates.min.json";
    private final String codecsPath = "/codecs.min.json";
    private final String countriesPath = "/countries.min.json";
    private final String statesPath = "/states.min.json";
    private final String regionsPath = "/regions.min.json";
    private final String genresPath = "/genres.min.json";
    private final String languagesPath = "/languages.min.json";
    private final String statusPath = "/status.min.json";
    private final String stationsPath = "/webradios.min.json";


    private OkHttpClient client;
    private Gson gson;

    public WebRadioClient(OkHttpClient client, Gson gson) {
        this.client = client;
        this.gson = gson;
    }

    public void getBitRates(RequestCallback<List<Integer>> callback) {
        Request request = new Request.Builder()
                .url(Objects.requireNonNull(HttpUrl.parse(baseUri + bitratesPath)))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (InputStream inputStream = Objects.requireNonNull(response.body()).byteStream()) {
                    TypeToken<List<Integer>> collectionType = new TypeToken<List<Integer>>() {
                    };
                    List<Integer> bitRates = gson.fromJson(new InputStreamReader(inputStream), collectionType.getType());
                    callback.onSuccess(bitRates);
                } catch (RuntimeException e) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getCountries(RequestCallback<List<String>> callback) {
        Request request = new Request.Builder()
                .url(Objects.requireNonNull(HttpUrl.parse(baseUri + countriesPath)))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (InputStream inputStream = Objects.requireNonNull(response.body()).byteStream()) {
                    TypeToken<List<String>> collectionType = new TypeToken<List<String>>() {
                    };
                    List<String> countries = gson.fromJson(new InputStreamReader(inputStream), collectionType.getType());
                    callback.onSuccess(countries);
                } catch (RuntimeException e) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getGenres(RequestCallback<List<String>> callback) {
        Request request = new Request.Builder()
                .url(Objects.requireNonNull(HttpUrl.parse(baseUri + genresPath)))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (InputStream inputStream = Objects.requireNonNull(response.body()).byteStream()) {
                    TypeToken<List<String>> collectionType = new TypeToken<List<String>>() {
                    };
                    List<String> genres = gson.fromJson(new InputStreamReader(inputStream), collectionType.getType());
                    callback.onSuccess(genres);
                } catch (RuntimeException e) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getLanguages(RequestCallback<List<String>> callback) {
        Request request = new Request.Builder()
                .url(Objects.requireNonNull(HttpUrl.parse(baseUri + languagesPath)))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (InputStream inputStream = Objects.requireNonNull(response.body()).byteStream()) {
                    TypeToken<List<String>> collectionType = new TypeToken<List<String>>() {
                    };
                    List<String> languges = gson.fromJson(new InputStreamReader(inputStream), collectionType.getType());
                    callback.onSuccess(languges);
                } catch (RuntimeException e) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getStations(RequestCallback<List<WebRadioStation>> callback) {
        Request request = new Request.Builder()
                .url(Objects.requireNonNull(HttpUrl.parse(baseUri + stationsPath)))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (InputStream inputStream = Objects.requireNonNull(response.body()).byteStream()) {
                    TypeToken<Map<String, WebRadioStation>> collectionType = new TypeToken<Map<String, WebRadioStation>>() {
                    };
                    Map<String, WebRadioStation> webRadioStations = gson.fromJson(new InputStreamReader(inputStream), collectionType.getType());
                    List<WebRadioStation> stationList = webRadioStations.entrySet().stream()
                            .map(e -> {
                                String stationId = e.getKey();
                                WebRadioStation station = e.getValue();
                                station.setId(stationId);
                                return station;
                            })
                            .collect(Collectors.toList());
                    callback.onSuccess(stationList);
                } catch (RuntimeException e) {
                    callback.onError(e);
                }
            }
        });
    }
}
