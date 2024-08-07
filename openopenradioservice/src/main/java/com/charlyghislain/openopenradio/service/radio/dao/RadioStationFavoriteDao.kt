package com.charlyghislain.openopenradio.service.radio.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioSource
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioStationFavorite
import java.util.concurrent.CompletableFuture

@Dao
interface RadioStationFavoriteDao {
    @Query("delete from radio_station_favorite where source = :source AND sourceId = :sourceId")
    suspend fun removeFavorite(source: RadioSource, sourceId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: RadioStationFavorite)

    @Query("SELECT EXISTS(SELECT 1 FROM radio_station_favorite WHERE source = :source AND sourceId = :sourceId)")
    fun isStationFavorite(source: RadioSource, sourceId: String): LiveData<Boolean>
}
