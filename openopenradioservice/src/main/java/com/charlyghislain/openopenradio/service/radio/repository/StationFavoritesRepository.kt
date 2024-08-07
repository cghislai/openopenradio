package com.charlyghislain.openopenradio.service.radio.repository

import com.charlyghislain.openopenradio.service.client.webradio.WebRadioClient
import com.charlyghislain.openopenradio.service.radio.dao.RadioStationFavoriteDao
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioSource
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioStationFavorite
import com.charlyghislain.openopenradio.service.util.RequestCallback
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

class StationFavoritesRepository(
    private val webRadioClient: WebRadioClient,
    private val radioStationFatoriteDao: RadioStationFavoriteDao
) {
    fun removeFavorite(source: RadioSource, sourceId: String): CompletableFuture<Void?> {
        return CoroutineScope(Dispatchers.IO).future {
            radioStationFatoriteDao.removeFavorite(source, sourceId)
            null
        }
    }

    fun addFavorite(source: RadioSource, sourceId: String): CompletableFuture<Void?> {
        return CoroutineScope(Dispatchers.IO).future {
            radioStationFatoriteDao.addFavorite(RadioStationFavorite(source, sourceId))
            null
        }
    }

}
