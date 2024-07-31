package com.charlyghislain.openopenradio.service.radio.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.charlyghislain.openopenradio.service.radio.model.RadioSource;
import com.charlyghislain.openopenradio.service.radio.model.RadioStation;

import java.util.Collection;
import java.util.List;

@Dao
public interface RadioStationDao {

    @Query("SELECT * FROM radio_station")
    LiveData<List<RadioStation>> getAllStations();

    @Query("delete from radio_station where source = :source")
    void clearStations(RadioSource source);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addStations(Collection<RadioStation> stations);

}
