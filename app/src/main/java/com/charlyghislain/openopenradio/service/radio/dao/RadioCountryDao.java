package com.charlyghislain.openopenradio.service.radio.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.charlyghislain.openopenradio.service.radio.model.CountryWithStats;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioCountry;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioSource;

import java.util.Collection;
import java.util.List;

@Dao
public interface RadioCountryDao {

    @Query("SELECT distinct name FROM radio_country")
    LiveData<List<String>> getAllCountryNames();

    @Query("delete from radio_country where source = :source")
    void clearCountries(RadioSource source);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addCountries(Collection<RadioCountry> counties);

    @Query("WITH CountryWithStats AS (\n" +
            "            SELECT g.name AS name,\n" +
            "               (SELECT COUNT(*) FROM radio_station s WHERE LOWER(s.country) LIKE '%' || LOWER(g.name) ||'%') AS stationCount\n" +
            "    FROM radio_country g\n" +
            "    )\n" +
            "    SELECT name, stationCount FROM CountryWithStats\n")
    LiveData<List<CountryWithStats>> getCountryWithStats();
}
