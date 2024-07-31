package com.charlyghislain.openopenradio.service.radio.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.charlyghislain.openopenradio.service.radio.model.entity.RadioSource;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioStation;

import java.util.Collection;
import java.util.List;

@Dao
public interface RadioStationDao {

    @Query("delete from radio_station where source = :source")
    void clearStations(RadioSource source);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addStations(Collection<RadioStation> stations);

    @Query("SELECT * FROM radio_station")
    LiveData<List<RadioStation>> getAllStations();

    @Query("SELECT count(*) FROM radio_station WHERE genres LIKE '%' || :genre || '%'")
    int countStationsByGenre(String genre);

    @Query("SELECT count(*) FROM radio_station WHERE languages LIKE '%' || :language || '%'")
    int countStationsByLanguage(String language);

    @Query("SELECT count(*) FROM radio_station WHERE country = :country")
    int countStationsByCountry(String country);

    @Query("SELECT * FROM radio_station WHERE genres LIKE '%' || :genre || '%'")
    LiveData<List<RadioStation>> getStationsByGenre(String genre);

    @Query("SELECT * FROM radio_station WHERE languages LIKE '%' || :language || '%'")
    LiveData<List<RadioStation>> getStationsByLanguage(String language);

    @Query("SELECT * FROM radio_station WHERE country = :country")
    LiveData<List<RadioStation>> getStationsByCountry(String country);

}
