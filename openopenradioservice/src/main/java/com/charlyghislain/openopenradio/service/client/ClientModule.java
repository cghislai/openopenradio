package com.charlyghislain.openopenradio.service.client;


import com.charlyghislain.openopenradio.service.client.webradio.WebRadioClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;

@Module
@InstallIn(SingletonComponent.class)
public class ClientModule {

    @Provides
    OkHttpClient provideOkHttpClient() {
        return new OkHttpClient();
    }

    @Provides
    Gson provideGson() {
        return new GsonBuilder().create();
    }

    @Provides
    WebRadioClient provideWebRadioClient(OkHttpClient client, Gson gson) {
        return new WebRadioClient(client, gson);
    }
}
