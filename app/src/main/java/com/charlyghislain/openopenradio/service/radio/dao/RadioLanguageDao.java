package com.charlyghislain.openopenradio.service.radio.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

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

}
