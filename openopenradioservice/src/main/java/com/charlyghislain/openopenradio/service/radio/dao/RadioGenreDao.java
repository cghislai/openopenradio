package com.charlyghislain.openopenradio.service.radio.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.charlyghislain.openopenradio.service.radio.model.entity.RadioSource;
import com.charlyghislain.openopenradio.service.radio.model.GenreWithStats;
import com.charlyghislain.openopenradio.service.radio.model.entity.RadioGenre;

import java.util.Collection;
import java.util.List;

@Dao
public interface RadioGenreDao {

    @Query("SELECT distinct name FROM radio_genre")
    LiveData<List<String>> getAllGenreNames();

    @Query("delete from radio_genre where source = :source")
    void clearGenres(RadioSource source);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addGenres(Collection<RadioGenre> genres);

    @Query("WITH GenreWithStats AS (\n" +
            "            SELECT g.name AS name,\n" +
            "               (SELECT COUNT(*) FROM radio_station s WHERE LOWER(s.genres) LIKE '%' || LOWER(g.name) ||'%') AS stationCount\n" +
            "    FROM radio_genre g\n" +
            "    )\n" +
            "    SELECT name, stationCount FROM GenreWithStats\n")
    LiveData<List<GenreWithStats>> getGenreWithStats();
}
