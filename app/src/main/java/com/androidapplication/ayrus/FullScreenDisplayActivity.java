package com.androidapplication.ayrus;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.TextView;

import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.widget.TextViewCompat;

import com.androidapplication.ayrus.model.DisplayConfig;
import com.androidapplication.ayrus.ui.AnimationHelper;

public class FullScreenDisplayActivity extends AppCompatActivity {

    public static final String EXTRA_CONFIG = "extra_display_config";

    private TextView textFullScreen;
    private View gestureHintView;
    private AnimationHelper.ActiveAnimations activeAnimations;
    private boolean isPaused = false;

    private GestureDetector gestureDetector;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_display);

        textFullScreen = findViewById(R.id.text_fullscreen);
        gestureHintView = findViewById(R.id.text_gesture_hint);

        final DisplayConfig config = getIntent().getParcelableExtra(EXTRA_CONFIG);
        if (config == null) {
            finish();
            return;
        }

        if (config.isOrientationLocked()) {
            // Lock to landscape for stable fullscreen display
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            // Allow rotation between landscape orientations
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }

        // Let the system auto-size text between a readable minimum and the maximum selected size
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                textFullScreen,
                16, // min sp
                (int) config.getTextSizeSp(), // max sp from config
                1,
                TypedValue.COMPLEX_UNIT_SP
        );

        activeAnimations = AnimationHelper.applyDisplayConfigToTextView(textFullScreen, config);

        setupGestures();
        enterImmersiveMode();
        showGestureHintTemporarily();
    }

    private void setupGestures() {
        View root = findViewById(R.id.root_fullscreen);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 120;
            private static final int SWIPE_VELOCITY_THRESHOLD = 200;

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                togglePauseResume();
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffY) > Math.abs(diffX)
                        && diffY > SWIPE_THRESHOLD
                        && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    finish();
                    return true;
                }
                return false;
            }
        });

        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    showGestureHintTemporarily();
                }
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    private void showGestureHintTemporarily() {
        if (gestureHintView == null) return;
        gestureHintView.setVisibility(View.VISIBLE);
        uiHandler.removeCallbacksAndMessages(null);
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (gestureHintView != null) {
                    gestureHintView.setVisibility(View.GONE);
                }
            }
        }, 3000);
    }

    private void togglePauseResume() {
        if (activeAnimations == null) return;
        if (!isPaused) {
            AnimationHelper.pause(activeAnimations);
            isPaused = true;
        } else {
            AnimationHelper.resume(activeAnimations);
            isPaused = false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            togglePauseResume();
            showGestureHintTemporarily();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void enterImmersiveMode() {
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            int flags = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(flags);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enterImmersiveMode();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (activeAnimations != null) {
            AnimationHelper.cancel(activeAnimations);
        }
    }
}
