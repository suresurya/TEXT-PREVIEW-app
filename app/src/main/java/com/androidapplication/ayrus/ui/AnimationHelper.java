package com.androidapplication.ayrus.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.androidapplication.ayrus.model.AnimationType;
import com.androidapplication.ayrus.model.DisplayConfig;
import com.androidapplication.ayrus.model.DisplayMode;

public class AnimationHelper {

    public static class ActiveAnimations {
        public ValueAnimator scrollAnimator;
        public AnimatorSet animatorSet;
    }

    @NonNull
    public static ActiveAnimations applyDisplayConfigToTextView(final @NonNull TextView textView,
                                                                final @NonNull DisplayConfig config) {
        ActiveAnimations active = new ActiveAnimations();

        textView.setText(config.getText());
        textView.setTextColor(config.getTextColor());
        textView.setTextSize(config.getTextSizeSp());
        textView.setBackgroundColor(config.getBackgroundColor());
        textView.setTextAlignment(config.getTextAlignment());

        RandomStyleManager.applyFontToTextView(textView, config.getFontFamilyKey());

        textView.setTranslationX(0f);
        textView.setTranslationY(0f);
        textView.setAlpha(1f);
        textView.clearAnimation();

        if (config.getDisplayMode() == DisplayMode.HORIZONTAL_SCROLL
                || config.getDisplayMode() == DisplayMode.VERTICAL_SCROLL) {
            setupScrollAnimation(textView, config, active);
        } else if (config.getDisplayMode() == DisplayMode.ANIMATED
                || config.getDisplayMode() == DisplayMode.PRESENTATION) {
            setupAnimated(textView, config, active);
        }

        return active;
    }

    private static void setupScrollAnimation(final TextView textView,
                                             final DisplayConfig config,
                                             final ActiveAnimations holder) {
        textView.setSingleLine(true);
        textView.setEllipsize(null);

        textView.post(new Runnable() {
            @Override
            public void run() {
                float width = textView.getWidth();
                float textWidth = textView.getPaint().measureText(config.getText());
                float distance = width + textWidth;

                float from, to;
                if (config.getDisplayMode() == DisplayMode.HORIZONTAL_SCROLL) {
                    boolean leftToRight = true; // could later be configurable
                    if (leftToRight) {
                        from = -textWidth;
                        to = width;
                    } else {
                        from = width;
                        to = -textWidth;
                    }
                    holder.scrollAnimator = ValueAnimator.ofFloat(from, to);
                    holder.scrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            float value = (float) valueAnimator.getAnimatedValue();
                            textView.setTranslationX(value);
                        }
                    });
                } else {
                    boolean bottomToTop = true;
                    from = textView.getHeight();
                    to = -textView.getHeight();
                    holder.scrollAnimator = ValueAnimator.ofFloat(from, to);
                    holder.scrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            float value = (float) valueAnimator.getAnimatedValue();
                            textView.setTranslationY(value);
                        }
                    });
                }

                long duration = computeDuration(distance, config.getSpeedLevel());
                holder.scrollAnimator.setDuration(duration);
                holder.scrollAnimator.setInterpolator(new LinearInterpolator());
                holder.scrollAnimator.setRepeatCount(config.isLoop() ? ValueAnimator.INFINITE : 0);
                holder.scrollAnimator.start();
            }
        });
    }

    private static long computeDuration(float distance, int speedLevel) {
        float base = 4000f;
        float factor;
        switch (speedLevel) {
            case 1:
                factor = 1.8f;
                break;
            case 2:
                factor = 1.4f;
                break;
            case 3:
                factor = 1.0f;
                break;
            case 4:
                factor = 0.7f;
                break;
            case 5:
            default:
                factor = 0.5f;
                break;
        }
        float normalized = (distance <= 0) ? base : (base * (distance / 1000f));
        return (long) (normalized * factor);
    }

    private static void setupAnimated(TextView textView,
                                      DisplayConfig config,
                                      ActiveAnimations holder) {
        AnimationType type = config.getAnimationType();
        if (type == AnimationType.NONE) {
            return;
        }

        AnimatorSet set = new AnimatorSet();
        long duration = mapSpeedToDuration(config.getSpeedLevel());

        switch (type) {
            case FADE: {
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f);
                fadeIn.setDuration(duration);
                fadeIn.setRepeatMode(ValueAnimator.REVERSE);
                fadeIn.setRepeatCount(config.isLoop() ? ValueAnimator.INFINITE : 0);
                fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
                set.play(fadeIn);
                break;
            }
            case SLIDE: {
                float fromY = textView.getHeight();
                ObjectAnimator slideIn = ObjectAnimator.ofFloat(textView, "translationY", fromY, 0f);
                slideIn.setDuration(duration);
                slideIn.setInterpolator(new DecelerateInterpolator());
                if (config.isLoop()) {
                    ObjectAnimator slideOut = ObjectAnimator.ofFloat(textView, "translationY", 0f, -fromY);
                    slideOut.setDuration(duration);
                    slideOut.setInterpolator(new AccelerateInterpolator());
                    set.playSequentially(slideIn, slideOut);
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            animation.start();
                        }
                    });
                } else {
                    set.play(slideIn);
                }
                break;
            }
            case ZOOM: {
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(textView, "scaleX", 0.8f, 1f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(textView, "scaleY", 0.8f, 1f);
                scaleX.setDuration(duration);
                scaleY.setDuration(duration);
                scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
                scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
                if (config.isLoop()) {
                    scaleX.setRepeatMode(ValueAnimator.REVERSE);
                    scaleY.setRepeatMode(ValueAnimator.REVERSE);
                    scaleX.setRepeatCount(ValueAnimator.INFINITE);
                    scaleY.setRepeatCount(ValueAnimator.INFINITE);
                }
                set.playTogether(scaleX, scaleY);
                break;
            }
            case BOUNCE: {
                ObjectAnimator bounce = ObjectAnimator.ofFloat(textView, "translationY", 0f, -30f, 0f);
                bounce.setDuration(duration);
                bounce.setInterpolator(new DecelerateInterpolator());
                if (config.isLoop()) {
                    bounce.setRepeatCount(ValueAnimator.INFINITE);
                }
                set.play(bounce);
                break;
            }
            case COMBINED:
            default: {
                ObjectAnimator fade = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f);
                ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(textView, "scaleX", 0.8f, 1f);
                ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(textView, "scaleY", 0.8f, 1f);
                fade.setDuration(duration);
                scaleX2.setDuration(duration);
                scaleY2.setDuration(duration);
                fade.setInterpolator(new AccelerateDecelerateInterpolator());
                scaleX2.setInterpolator(new AccelerateDecelerateInterpolator());
                scaleY2.setInterpolator(new AccelerateDecelerateInterpolator());
                if (config.isLoop()) {
                    fade.setRepeatMode(ValueAnimator.REVERSE);
                    scaleX2.setRepeatMode(ValueAnimator.REVERSE);
                    scaleY2.setRepeatMode(ValueAnimator.REVERSE);
                    fade.setRepeatCount(ValueAnimator.INFINITE);
                    scaleX2.setRepeatCount(ValueAnimator.INFINITE);
                    scaleY2.setRepeatCount(ValueAnimator.INFINITE);
                }
                set.playTogether(fade, scaleX2, scaleY2);
                break;
            }
        }

        set.start();
        holder.animatorSet = set;
    }

    private static long mapSpeedToDuration(int speedLevel) {
        switch (speedLevel) {
            case 1:
                return 3000L;
            case 2:
                return 2200L;
            case 3:
                return 1600L;
            case 4:
                return 1100L;
            case 5:
            default:
                return 800L;
        }
    }

    public static void pause(@NonNull ActiveAnimations active) {
        if (active.scrollAnimator != null && active.scrollAnimator.isRunning()) {
            active.scrollAnimator.pause();
        }
        if (active.animatorSet != null && active.animatorSet.isRunning()) {
            active.animatorSet.pause();
        }
    }

    public static void resume(@NonNull ActiveAnimations active) {
        if (active.scrollAnimator != null && active.scrollAnimator.isPaused()) {
            active.scrollAnimator.resume();
        }
        if (active.animatorSet != null && active.animatorSet.isPaused()) {
            active.animatorSet.resume();
        }
    }

    public static void cancel(@NonNull ActiveAnimations active) {
        if (active.scrollAnimator != null) {
            active.scrollAnimator.cancel();
        }
        if (active.animatorSet != null) {
            active.animatorSet.cancel();
        }
    }
}
