package com.androidapplication.ayrus.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "text_presets")
public class TextPresetEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String textContent;

    @NonNull
    public String displayMode; // maps to DisplayMode.name()

    @NonNull
    public String fontFamilyKey;

    public float textSizeSp;

    public int textColor;

    public int backgroundColor;

    public int speedLevel;

    @NonNull
    public String animationType; // maps to AnimationType.name()

    public boolean loop;

    public boolean favorite;

    public long createdAtMillis;
}
