package com.androidapplication.ayrus.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface DisplayConfigDao {

    @Query("SELECT * FROM display_config WHERE id = 1 LIMIT 1")
    DisplayConfigEntity getConfig();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(DisplayConfigEntity entity);
}
