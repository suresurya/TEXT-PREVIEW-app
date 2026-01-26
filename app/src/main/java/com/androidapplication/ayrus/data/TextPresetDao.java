package com.androidapplication.ayrus.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TextPresetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TextPresetEntity entity);

    @Update
    void update(TextPresetEntity entity);

    @Delete
    void delete(TextPresetEntity entity);

    @Query("SELECT * FROM text_presets ORDER BY createdAtMillis DESC")
    List<TextPresetEntity> getAllPresets();

    @Query("SELECT * FROM text_presets WHERE favorite = 1 ORDER BY createdAtMillis DESC")
    List<TextPresetEntity> getFavoritePresets();

    @Query("SELECT DISTINCT textContent FROM text_presets ORDER BY createdAtMillis DESC LIMIT 20")
    List<String> getRecentTextEntries();
}
