package com.charlyghislain.openopenradio.service.radio.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.charlyghislain.openopenradio.service.radio.model.RadioGenre;
import com.charlyghislain.openopenradio.service.radio.model.RadioSource;

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

}
