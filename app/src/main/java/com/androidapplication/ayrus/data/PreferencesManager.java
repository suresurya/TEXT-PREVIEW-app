package com.androidapplication.ayrus.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

public class PreferencesManager {

    private static final String PREFS_NAME = "text_display_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";

    private final SharedPreferences sharedPreferences;

    public PreferencesManager(@NonNull Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getThemeMode() {
        return sharedPreferences.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setThemeMode(int mode) {
        sharedPreferences.edit().putInt(KEY_THEME_MODE, mode).apply();
    }
}
