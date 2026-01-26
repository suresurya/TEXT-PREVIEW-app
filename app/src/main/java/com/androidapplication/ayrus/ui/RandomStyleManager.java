package com.androidapplication.ayrus.ui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.androidapplication.ayrus.model.AnimationType;
import com.androidapplication.ayrus.model.DisplayConfig;
import com.androidapplication.ayrus.model.DisplayMode;

import java.util.Random;

public class RandomStyleManager {

    private static final int[] BACKGROUND_COLORS = new int[]{
            Color.parseColor("#121212"),
            Color.parseColor("#1E1E1E"),
            Color.parseColor("#263238"),
            Color.parseColor("#FAFAFA"),
            Color.parseColor("#ECEFF1"),
            Color.parseColor("#212121"),
            Color.parseColor("#37474F")
    };

    private static final int[] TEXT_COLORS = new int[]{
            Color.WHITE,
            Color.BLACK,
            Color.parseColor("#FFEB3B"),
            Color.parseColor("#FFCDD2"),
            Color.parseColor("#BBDEFB"),
            Color.parseColor("#E1BEE7")
    };

    private static final String[] FONT_KEYS = new String[]{
            "default",
            "sans",
            "serif",
            "mono"
    };

    private static final AnimationType[] ANIMATION_TYPES = new AnimationType[]{
            AnimationType.FADE,
            AnimationType.SLIDE,
            AnimationType.ZOOM,
            AnimationType.BOUNCE,
            AnimationType.COMBINED
    };

    private static final Random RANDOM = new Random();

    @NonNull
    public static DisplayConfig applyRandomStyle(@NonNull DisplayConfig base) {
        // Background & text colors with simple contrast rule
        int bg = BACKGROUND_COLORS[RANDOM.nextInt(BACKGROUND_COLORS.length)];
        int textColor = pickReadableTextColor(bg);

        // Typography pairing: random font + bold-ish size range
        String fontKey = FONT_KEYS[RANDOM.nextInt(FONT_KEYS.length)];
        float textSizeSp = pickRandomTextSize(base.getText());

        // Display mode & animation pairing
        DisplayMode displayMode = pickRandomDisplayMode();
        AnimationType animationType;
        if (displayMode == DisplayMode.ANIMATED || displayMode == DisplayMode.PRESENTATION) {
            animationType = ANIMATION_TYPES[RANDOM.nextInt(ANIMATION_TYPES.length)];
        } else {
            animationType = AnimationType.NONE;
        }

        // Speed: slower for long text, faster for short
        int speedLevel = pickSmartSpeedLevel(base.getText());

        return new DisplayConfig(
                base.getText(),
                displayMode,
                base.getTextAlignment(),
                fontKey,
                textSizeSp,
                textColor,
                bg,
                speedLevel,
                animationType,
                base.isLoop(),
                true,
                base.isOrientationLocked(),
                base.getTimestampMillis()
        );
    }

    private static float pickRandomTextSize(String text) {
        int length = text == null ? 0 : text.length();
        float minSize = 28f;
        float maxSize = 72f;
        if (length > 40) {
            maxSize = 56f;
        } else if (length < 10) {
            minSize = 34f;
        }
        if (maxSize < minSize) {
            maxSize = minSize;
        }
        float range = maxSize - minSize;
        return minSize + RANDOM.nextFloat() * range;
    }

    private static DisplayMode pickRandomDisplayMode() {
        DisplayMode[] modes = DisplayMode.values();
        // Bias slightly towards animated / scrolling modes for a more dynamic feel
        int index = RANDOM.nextInt(modes.length + 2);
        if (index >= modes.length) {
            return DisplayMode.ANIMATED;
        }
        return modes[index];
    }

    private static int pickSmartSpeedLevel(String text) {
        int length = text == null ? 0 : text.length();
        if (length <= 10) {
            return 4 + RANDOM.nextInt(2); // 4-5: faster for short text
        } else if (length <= 30) {
            return 3 + RANDOM.nextInt(2); // 3-4
        } else {
            return 1 + RANDOM.nextInt(3); // 1-3: slower for long text
        }
    }

    private static int pickReadableTextColor(int background) {
        int candidate = TEXT_COLORS[RANDOM.nextInt(TEXT_COLORS.length)];
        for (int i = 0; i < 4; i++) {
            if (!isTooSimilar(candidate, background)) {
                return candidate;
            }
            candidate = TEXT_COLORS[RANDOM.nextInt(TEXT_COLORS.length)];
        }
        return candidate;
    }

    private static boolean isTooSimilar(int color1, int color2) {
        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);
        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);
        int diff = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
        return diff < 120;
    }

    public static void applyFontToTextView(@NonNull TextView textView, @NonNull String fontKey) {
        Typeface typeface;
        switch (fontKey) {
            case "sans":
                typeface = Typeface.SANS_SERIF;
                break;
            case "serif":
                typeface = Typeface.SERIF;
                break;
            case "mono":
                typeface = Typeface.MONOSPACE;
                break;
            case "default":
            default:
                typeface = Typeface.DEFAULT;
                break;
        }
        textView.setTypeface(typeface);
    }

    public static String getDefaultFontKey() {
        return "default";
    }
}
