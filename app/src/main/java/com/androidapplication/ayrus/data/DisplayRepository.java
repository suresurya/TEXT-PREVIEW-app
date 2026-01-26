package com.androidapplication.ayrus.data;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.androidapplication.ayrus.model.AnimationType;
import com.androidapplication.ayrus.model.DisplayConfig;
import com.androidapplication.ayrus.model.DisplayMode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DisplayRepository {

    public interface ConfigCallback {
        @MainThread
        void onConfigLoaded(@Nullable DisplayConfig config);
    }

    public interface PresetSaveCallback {
        @MainThread
        void onPresetSaved(long presetId);
    }

    private final TextPresetDao textPresetDao;
    private final DisplayConfigDao displayConfigDao;
    private final ExecutorService ioExecutor;
    private final Handler mainHandler;

    public DisplayRepository(@NonNull AppDatabase database) {
        this.textPresetDao = database.textPresetDao();
        this.displayConfigDao = database.displayConfigDao();
        this.ioExecutor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void getLastConfig(@NonNull final ConfigCallback callback) {
        ioExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final DisplayConfigEntity entity = displayConfigDao.getConfig();
                final DisplayConfig config = entity != null ? mapToConfig(entity) : null;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onConfigLoaded(config);
                    }
                });
            }
        });
    }

    public void saveLastConfig(@NonNull final DisplayConfig config) {
        ioExecutor.execute(new Runnable() {
            @Override
            public void run() {
                DisplayConfigEntity entity = mapFromConfig(config);
                displayConfigDao.upsert(entity);
            }
        });
    }

    public void savePreset(@NonNull final DisplayConfig config,
                           @Nullable final PresetSaveCallback callback) {
        ioExecutor.execute(new Runnable() {
            @Override
            public void run() {
                TextPresetEntity entity = new TextPresetEntity();
                entity.textContent = config.getText();
                entity.displayMode = config.getDisplayMode().name();
                entity.fontFamilyKey = config.getFontFamilyKey();
                entity.textSizeSp = config.getTextSizeSp();
                entity.textColor = config.getTextColor();
                entity.backgroundColor = config.getBackgroundColor();
                entity.speedLevel = config.getSpeedLevel();
                entity.animationType = config.getAnimationType().name();
                entity.loop = config.isLoop();
                entity.favorite = false;
                entity.createdAtMillis = System.currentTimeMillis();

                final long id = textPresetDao.insert(entity);

                if (callback != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onPresetSaved(id);
                        }
                    });
                }
            }
        });
    }

    @NonNull
    private DisplayConfigEntity mapFromConfig(@NonNull DisplayConfig config) {
        DisplayConfigEntity entity = new DisplayConfigEntity();
        entity.id = 1L;
        entity.textContent = config.getText();
        entity.displayMode = config.getDisplayMode().name();
        entity.textAlignment = config.getTextAlignment();
        entity.fontFamilyKey = config.getFontFamilyKey();
        entity.textSizeSp = config.getTextSizeSp();
        entity.textColor = config.getTextColor();
        entity.backgroundColor = config.getBackgroundColor();
        entity.speedLevel = config.getSpeedLevel();
        entity.animationType = config.getAnimationType().name();
        entity.loop = config.isLoop();
        entity.randomStyleEnabled = config.isRandomStyleEnabled();
        entity.orientationLocked = config.isOrientationLocked();
        entity.updatedAtMillis = config.getTimestampMillis();
        return entity;
    }

    @NonNull
    private DisplayConfig mapToConfig(@NonNull DisplayConfigEntity entity) {
        DisplayMode mode;
        AnimationType animationType;
        try {
            mode = DisplayMode.valueOf(entity.displayMode);
        } catch (IllegalArgumentException e) {
            mode = DisplayMode.STATIC;
        }
        try {
            animationType = AnimationType.valueOf(entity.animationType);
        } catch (IllegalArgumentException e) {
            animationType = AnimationType.NONE;
        }

        return new DisplayConfig(
                entity.textContent,
                mode,
                entity.textAlignment,
                entity.fontFamilyKey,
                entity.textSizeSp,
                entity.textColor,
                entity.backgroundColor,
                entity.speedLevel,
                animationType,
                entity.loop,
                entity.randomStyleEnabled,
                entity.orientationLocked,
                entity.updatedAtMillis
        );
    }
}
