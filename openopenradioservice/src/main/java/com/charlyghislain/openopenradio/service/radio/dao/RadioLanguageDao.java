package com.charlyghislain.openopenradio.service.radio.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.charlyghislain.openopenradio.service.radio.model.LanguageWithStats;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioLanguage;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioSource;

import java.util.Collection;
import java.util.List;

@Dao
public interface RadioLanguageDao {

    @Query("SELECT distinct name FROM radio_language")
    LiveData<List<String>> getAllLanguageNames();

    @Query("delete from radio_language where source = :source")
    void clearLanguages(RadioSource source);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addLanguages(Collection<RadioLanguage> languages);

    @Query("WITH LanguageWithStats AS (\n" +
            "            SELECT g.name AS name,\n" +
            "               (SELECT COUNT(*) FROM radio_station s WHERE LOWER(s.languages) LIKE '%' || LOWER(g.name) ||'%') AS stationCount\n" +
            "    FROM radio_language g\n" +
            "    )\n" +
            "    SELECT name, stationCount FROM LanguageWithStats\n")
    LiveData<List<LanguageWithStats>> getLanguageWithStats();
}
