package com.androidapplication.ayrus.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Immutable configuration object for text display.
 */
public class DisplayConfig implements Parcelable {

    private final String text;
    private final DisplayMode displayMode;
    private final int textAlignment; // View.TEXT_ALIGNMENT_*
    private final String fontFamilyKey; // key used to resolve Typeface
    private final float textSizeSp;
    private final int textColor;
    private final int backgroundColor;
    private final int speedLevel; // 1..5 logical speed level
    private final AnimationType animationType;
    private final boolean loop;
    private final boolean randomStyleEnabled;
    private final boolean orientationLocked;
    private final long timestampMillis;
    private final boolean hasShadow;
    private final boolean hasGlow;

    public DisplayConfig(String text,
                         DisplayMode displayMode,
                         int textAlignment,
                         String fontFamilyKey,
                         float textSizeSp,
                         int textColor,
                         int backgroundColor,
                         int speedLevel,
                         AnimationType animationType,
                         boolean loop,
                         boolean randomStyleEnabled,
                         boolean orientationLocked,
                         long timestampMillis,
                         boolean hasShadow,
                         boolean hasGlow) {
        this.text = text;
        this.displayMode = displayMode;
        this.textAlignment = textAlignment;
        this.fontFamilyKey = fontFamilyKey;
        this.textSizeSp = textSizeSp;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.speedLevel = speedLevel;
        this.animationType = animationType;
        this.loop = loop;
        this.randomStyleEnabled = randomStyleEnabled;
        this.orientationLocked = orientationLocked;
        this.timestampMillis = timestampMillis;
        this.hasShadow = hasShadow;
        this.hasGlow = hasGlow;
    }

    protected DisplayConfig(Parcel in) {
        text = in.readString();
        displayMode = DisplayMode.valueOf(in.readString());
        textAlignment = in.readInt();
        fontFamilyKey = in.readString();
        textSizeSp = in.readFloat();
        textColor = in.readInt();
        backgroundColor = in.readInt();
        speedLevel = in.readInt();
        animationType = AnimationType.valueOf(in.readString());
        loop = in.readByte() != 0;
        randomStyleEnabled = in.readByte() != 0;
        orientationLocked = in.readByte() != 0;
        timestampMillis = in.readLong();
        hasShadow = in.readByte() != 0;
        hasGlow = in.readByte() != 0;
    }

    public static final Creator<DisplayConfig> CREATOR = new Creator<DisplayConfig>() {
        @Override
        public DisplayConfig createFromParcel(Parcel in) {
            return new DisplayConfig(in);
        }

        @Override
        public DisplayConfig[] newArray(int size) {
            return new DisplayConfig[size];
        }
    };

    public String getText() {
        return text;
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public int getTextAlignment() {
        return textAlignment;
    }

    public String getFontFamilyKey() {
        return fontFamilyKey;
    }

    public float getTextSizeSp() {
        return textSizeSp;
    }

    public int getTextColor() {
        return textColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getSpeedLevel() {
        return speedLevel;
    }

    public AnimationType getAnimationType() {
        return animationType;
    }

    public boolean isLoop() {
        return loop;
    }

    public boolean isRandomStyleEnabled() {
        return randomStyleEnabled;
    }

    public boolean isOrientationLocked() {
        return orientationLocked;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    public boolean hasShadow() {
        return hasShadow;
    }

    public boolean hasGlow() {
        return hasGlow;
    }

    public DisplayConfig copyWithTimestamp(long newTimestamp) {
        return new DisplayConfig(text, displayMode, textAlignment, fontFamilyKey, textSizeSp,
                textColor, backgroundColor, speedLevel, animationType, loop,
                randomStyleEnabled, orientationLocked, newTimestamp, hasShadow, hasGlow);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(displayMode.name());
        dest.writeInt(textAlignment);
        dest.writeString(fontFamilyKey);
        dest.writeFloat(textSizeSp);
        dest.writeInt(textColor);
        dest.writeInt(backgroundColor);
        dest.writeInt(speedLevel);
        dest.writeString(animationType.name());
        dest.writeByte((byte) (loop ? 1 : 0));
        dest.writeByte((byte) (randomStyleEnabled ? 1 : 0));
        dest.writeByte((byte) (orientationLocked ? 1 : 0));
        dest.writeLong(timestampMillis);
        dest.writeByte((byte) (hasShadow ? 1 : 0));
        dest.writeByte((byte) (hasGlow ? 1 : 0));
    }
}
