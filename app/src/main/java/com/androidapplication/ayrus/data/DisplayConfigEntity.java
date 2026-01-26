package com.androidapplication.ayrus.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "display_config")
public class DisplayConfigEntity {

    @PrimaryKey
    public long id = 1L; // single-row table

    @NonNull
    public String textContent = "";

    @NonNull
    public String displayMode; // DisplayMode.name()

    public int textAlignment; // View.TEXT_ALIGNMENT_*

    @NonNull
    public String fontFamilyKey;

    public float textSizeSp;

    public int textColor;

    public int backgroundColor;

    public int speedLevel;

    @NonNull
    public String animationType; // AnimationType.name()

    public boolean loop;

    public boolean randomStyleEnabled;

    public boolean orientationLocked;

    public long updatedAtMillis;
}
