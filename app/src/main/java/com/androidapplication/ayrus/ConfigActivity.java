package com.androidapplication.ayrus;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.androidapplication.ayrus.data.AppDatabase;
import com.androidapplication.ayrus.data.DisplayRepository;
import com.androidapplication.ayrus.data.PreferencesManager;
import com.androidapplication.ayrus.model.AnimationType;
import com.androidapplication.ayrus.model.DisplayConfig;
import com.androidapplication.ayrus.model.DisplayMode;
import com.androidapplication.ayrus.ui.AnimationHelper;
import com.androidapplication.ayrus.ui.RandomStyleManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class ConfigActivity extends AppCompatActivity {

    private TextInputEditText editTextInput;
    private Spinner spinnerDisplayMode;
    private Spinner spinnerAlignment;
    private Spinner spinnerFont;
    private Spinner spinnerAnimationType;
    private SeekBar seekTextSize;
    private SeekBar seekSpeed;
    private View containerSpeed;
    private View containerAnimationType;
    private View viewTextColor;
    private View viewBackgroundColor;
    private MaterialSwitch switchLoop;
    private MaterialSwitch switchRandom;
    private MaterialSwitch switchOrientationLock;
    private MaterialSwitch switchTextShadow;
    private MaterialSwitch switchTextGlow;
    private TextView textPreview;
    private View rootView;

    private DisplayRepository repository;
    private PreferencesManager preferencesManager;

    private AnimationHelper.ActiveAnimations activePreviewAnimations;

    private int currentTextColor = Color.WHITE;
    private int currentBackgroundColor = Color.BLACK;

    private DisplayMode[] displayModes = DisplayMode.values();

    private static final int[] COLOR_OPTIONS = new int[]{
            Color.WHITE,
            Color.BLACK,
            Color.parseColor("#FFEB3B"), // Yellow
            Color.parseColor("#F44336"), // Red
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#9C27B0")  // Purple
    };

    private static final String[] COLOR_NAMES = new String[]{
            "White",
            "Black",
            "Yellow",
            "Red",
            "Blue",
            "Green",
            "Purple"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferencesManager = new PreferencesManager(this);

        setContentView(R.layout.activity_text_config);

        repository = new DisplayRepository(AppDatabase.getInstance(this));

        initViews();
        setupSpinners();
        setupSeekBars();
        setupColorPickers();
        setupButtons();
        setupLivePreviewListeners();

        loadLastConfig();
    }

    private void initViews() {
        rootView = findViewById(R.id.root_config);
        editTextInput = findViewById(R.id.edit_text_input);
        spinnerDisplayMode = findViewById(R.id.spinner_display_mode);
        spinnerAlignment = findViewById(R.id.spinner_alignment);
        spinnerFont = findViewById(R.id.spinner_font);
        spinnerAnimationType = findViewById(R.id.spinner_animation_type);
        seekTextSize = findViewById(R.id.seek_text_size);
        containerSpeed = findViewById(R.id.container_speed);
        seekSpeed = findViewById(R.id.seek_speed);
        containerAnimationType = findViewById(R.id.container_animation_type);
        viewTextColor = findViewById(R.id.view_text_color);
        viewBackgroundColor = findViewById(R.id.view_background_color);
        switchLoop = findViewById(R.id.switch_loop);
        switchRandom = findViewById(R.id.switch_random);
        switchOrientationLock = findViewById(R.id.switch_orientation_lock);
        switchTextShadow = findViewById(R.id.switch_text_shadow);
        switchTextGlow = findViewById(R.id.switch_text_glow);
        textPreview = findViewById(R.id.text_preview);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> modeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.display_modes,
                android.R.layout.simple_spinner_item
        );
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDisplayMode.setAdapter(modeAdapter);

        ArrayAdapter<CharSequence> alignmentAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.alignments,
                android.R.layout.simple_spinner_item
        );
        alignmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAlignment.setAdapter(alignmentAdapter);

        ArrayAdapter<CharSequence> fontAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.font_families,
                android.R.layout.simple_spinner_item
        );
        fontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFont.setAdapter(fontAdapter);

        ArrayAdapter<CharSequence> animationAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.animation_types,
                android.R.layout.simple_spinner_item
        );
        animationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAnimationType.setAdapter(animationAdapter);

        spinnerDisplayMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSpeedVisibility();
                refreshPreviewIfPossible();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerAnimationType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshPreviewIfPossible();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupSeekBars() {
        seekTextSize.setMax(80);
        seekTextSize.setProgress(32);

        seekSpeed.setMax(4);
        seekSpeed.setProgress(2);
    }

    private void setupColorPickers() {
        viewTextColor.setBackgroundColor(currentTextColor);
        viewBackgroundColor.setBackgroundColor(currentBackgroundColor);

        viewTextColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPickerDialog(true);
            }
        });

        viewBackgroundColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPickerDialog(false);
            }
        });
    }

    private void setupLivePreviewListeners() {
        // Text changes
        editTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                refreshPreviewIfPossible();
            }
        });

        // SeekBars
        seekTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                refreshPreviewIfPossible();
            }
        });

        seekSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                refreshPreviewIfPossible();
            }
        });

        // Switches
        switchLoop.setOnCheckedChangeListener((buttonView, isChecked) -> refreshPreviewIfPossible());
        switchRandom.setOnCheckedChangeListener((buttonView, isChecked) -> refreshPreviewIfPossible());
        switchOrientationLock.setOnCheckedChangeListener((buttonView, isChecked) -> refreshPreviewIfPossible());

        // Extra spinner listeners for alignment & font
        spinnerAlignment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshPreviewIfPossible();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerFont.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshPreviewIfPossible();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupButtons() {
        MaterialButton buttonPreview = findViewById(R.id.button_preview);
        MaterialButton buttonSavePreset = findViewById(R.id.button_save_preset);
        MaterialButton buttonFullScreen = findViewById(R.id.button_full_screen);
        MaterialButton buttonSurpriseMe = findViewById(R.id.button_surprise_me);

        buttonPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisplayConfig config = buildConfigFromUi(false);
                if (config == null) return;
                applyPreview(config);
            }
        });

        buttonSavePreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisplayConfig config = buildConfigFromUi(false);
                if (config == null) return;
                repository.savePreset(config, new DisplayRepository.PresetSaveCallback() {
                    @Override
                    public void onPresetSaved(long presetId) {
                        Snackbar.make(rootView, R.string.message_preset_saved, Snackbar.LENGTH_SHORT).show();
                    }
                });
                repository.saveLastConfig(config);
            }
        });

        buttonFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisplayConfig config = buildConfigFromUi(false);
                if (config == null) return;
                repository.saveLastConfig(config);
                Intent intent = new Intent(ConfigActivity.this, FullScreenDisplayActivity.class);
                intent.putExtra(FullScreenDisplayActivity.EXTRA_CONFIG, config);
                startActivity(intent);
            }
        });

        buttonSurpriseMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean originalRandom = switchRandom.isChecked();
                // Build a base config without auto-randomization
                switchRandom.setChecked(false);
                DisplayConfig base = buildConfigFromUi(false);
                switchRandom.setChecked(originalRandom);
                if (base == null) return;

                DisplayConfig randomized = RandomStyleManager.applyRandomStyle(base);
                // Reflect the randomized style back into the UI and preview
                applyConfigToUi(randomized);
                applyPreview(randomized);
                repository.saveLastConfig(randomized);
            }
        });
    }

    private void updateSpeedVisibility() {
        DisplayMode mode = getSelectedDisplayMode();
        if (mode == DisplayMode.HORIZONTAL_SCROLL
                || mode == DisplayMode.VERTICAL_SCROLL
                || mode == DisplayMode.ANIMATED
                || mode == DisplayMode.PRESENTATION) {
            containerSpeed.setVisibility(View.VISIBLE);
        } else {
            containerSpeed.setVisibility(View.GONE);
        }

        if (mode == DisplayMode.ANIMATED || mode == DisplayMode.PRESENTATION) {
            if (containerAnimationType != null) {
                containerAnimationType.setVisibility(View.VISIBLE);
            }
        } else if (containerAnimationType != null) {
            containerAnimationType.setVisibility(View.GONE);
        }
    }

    private void refreshPreviewIfPossible() {
        String text = editTextInput.getText() != null ? editTextInput.getText().toString() : "";
        if (text.trim().isEmpty()) {
            return;
        }
        DisplayConfig config = buildConfigFromUi(false);
        if (config != null) {
            applyPreview(config);
        }
    }

    @Nullable
    private DisplayConfig buildConfigFromUi(boolean forRandomPreview) {
        String text = editTextInput.getText() != null ? editTextInput.getText().toString() : "";
        if (text.trim().isEmpty()) {
            Snackbar.make(rootView, R.string.error_empty_text, Snackbar.LENGTH_SHORT).show();
            return null;
        }

        DisplayMode mode = getSelectedDisplayMode();
        int alignment = getSelectedAlignment();
        String fontKey = getSelectedFontKey();

        float textSizeSp = 16f + seekTextSize.getProgress();
        int speedLevel = seekSpeed.getProgress() + 1;

        boolean loop = switchLoop.isChecked();
        boolean random = switchRandom.isChecked();
        boolean orientationLocked = switchOrientationLock.isChecked();
        boolean hasShadow = switchTextShadow.isChecked();
        boolean hasGlow = switchTextGlow.isChecked();

        AnimationType animationType;
        if (mode == DisplayMode.ANIMATED || mode == DisplayMode.PRESENTATION) {
            animationType = getSelectedAnimationType();
        } else {
            animationType = AnimationType.NONE;
        }

        DisplayConfig base = new DisplayConfig(
                text,
                mode,
                alignment,
                fontKey,
                textSizeSp,
                currentTextColor,
                currentBackgroundColor,
                speedLevel,
                animationType,
                loop,
                random,
                orientationLocked,
                System.currentTimeMillis(),
                hasShadow,
                hasGlow
        );

        if (random) {
            return RandomStyleManager.applyRandomStyle(base);
        } else {
            return base;
        }
    }

    private DisplayMode getSelectedDisplayMode() {
        int position = spinnerDisplayMode.getSelectedItemPosition();
        if (position < 0 || position >= displayModes.length) {
            return DisplayMode.STATIC;
        }
        return displayModes[position];
    }

    private int getSelectedAlignment() {
        int position = spinnerAlignment.getSelectedItemPosition();
        switch (position) {
            case 0:
                return View.TEXT_ALIGNMENT_TEXT_START;
            case 1:
                return View.TEXT_ALIGNMENT_CENTER;
            case 2:
                return View.TEXT_ALIGNMENT_TEXT_END;
            default:
                return View.TEXT_ALIGNMENT_CENTER;
        }
    }

    private String getSelectedFontKey() {
        int position = spinnerFont.getSelectedItemPosition();
        switch (position) {
            case 0:
                return "default";
            case 1:
                return "sans";
            case 2:
                return "serif";
            case 3:
                return "mono";
            default:
                return RandomStyleManager.getDefaultFontKey();
        }
    }

    private AnimationType getSelectedAnimationType() {
        int position = spinnerAnimationType.getSelectedItemPosition();
        switch (position) {
            case 0:
                return AnimationType.FADE;
            case 1:
                return AnimationType.SLIDE;
            case 2:
                return AnimationType.ZOOM;
            case 3:
                return AnimationType.BOUNCE;
            case 4:
            default:
                return AnimationType.COMBINED;
        }
    }

    private void applyPreview(@NonNull DisplayConfig config) {
        if (activePreviewAnimations != null) {
            AnimationHelper.cancel(activePreviewAnimations);
        }
        activePreviewAnimations = AnimationHelper.applyDisplayConfigToTextView(textPreview, config);
    }

    private void showColorPickerDialog(final boolean isTextColor) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isTextColor ? R.string.color_picker_title_text : R.string.color_picker_title_background);

        CharSequence[] items = new CharSequence[COLOR_OPTIONS.length];
        for (int i = 0; i < COLOR_OPTIONS.length; i++) {
            String name = i < COLOR_NAMES.length ? COLOR_NAMES[i] : ("Color " + (i + 1));
            items[i] = name;
        }

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which < 0 || which >= COLOR_OPTIONS.length) return;
                int chosen = COLOR_OPTIONS[which];
                if (isTextColor) {
                    currentTextColor = chosen;
                    viewTextColor.setBackgroundColor(chosen);
                } else {
                    currentBackgroundColor = chosen;
                    viewBackgroundColor.setBackgroundColor(chosen);
                }
                refreshPreviewIfPossible();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private void loadLastConfig() {
        repository.getLastConfig(new DisplayRepository.ConfigCallback() {
            @Override
            public void onConfigLoaded(@Nullable DisplayConfig config) {
                if (config != null) {
                    DisplayConfig toApply = config;
                    if (config.isRandomStyleEnabled()) {
                        // Re-randomize on each app start when random style is enabled
                        toApply = RandomStyleManager.applyRandomStyle(
                                config.copyWithTimestamp(System.currentTimeMillis()));
                        repository.saveLastConfig(toApply);
                    }
                    applyConfigToUi(toApply);
                    applyPreview(toApply);
                }
            }
        });
    }

    private void applyConfigToUi(@NonNull DisplayConfig config) {
        if (editTextInput.getText() != null) {
            editTextInput.setText(config.getText());
        }

        spinnerDisplayMode.setSelection(config.getDisplayMode().ordinal());

        int alignSelection;
        switch (config.getTextAlignment()) {
            case View.TEXT_ALIGNMENT_TEXT_START:
                alignSelection = 0;
                break;
            case View.TEXT_ALIGNMENT_TEXT_END:
                alignSelection = 2;
                break;
            case View.TEXT_ALIGNMENT_CENTER:
            default:
                alignSelection = 1;
                break;
        }
        spinnerAlignment.setSelection(alignSelection);

        String fontKey = config.getFontFamilyKey();
        int fontSelection = 0;
        if ("sans".equals(fontKey)) fontSelection = 1;
        else if ("serif".equals(fontKey)) fontSelection = 2;
        else if ("mono".equals(fontKey)) fontSelection = 3;
        spinnerFont.setSelection(fontSelection);

        float sizeSp = config.getTextSizeSp();
        int progress = (int) (sizeSp - 16f);
        if (progress < 0) progress = 0;
        if (progress > seekTextSize.getMax()) progress = seekTextSize.getMax();
        seekTextSize.setProgress(progress);

        currentTextColor = config.getTextColor();
        currentBackgroundColor = config.getBackgroundColor();
        viewTextColor.setBackgroundColor(currentTextColor);
        viewBackgroundColor.setBackgroundColor(currentBackgroundColor);

        int speedProgress = config.getSpeedLevel() - 1;
        if (speedProgress < 0) speedProgress = 0;
        if (speedProgress > seekSpeed.getMax()) speedProgress = seekSpeed.getMax();
        seekSpeed.setProgress(speedProgress);

        switchLoop.setChecked(config.isLoop());
        switchRandom.setChecked(config.isRandomStyleEnabled());
        switchOrientationLock.setChecked(config.isOrientationLocked());
        switchTextShadow.setChecked(config.hasShadow());
        switchTextGlow.setChecked(config.hasGlow());

        // Animation type
        int animSelection = 4; // default Combined
        AnimationType type = config.getAnimationType();
        if (type == AnimationType.FADE) animSelection = 0;
        else if (type == AnimationType.SLIDE) animSelection = 1;
        else if (type == AnimationType.ZOOM) animSelection = 2;
        else if (type == AnimationType.BOUNCE) animSelection = 3;
        else if (type == AnimationType.COMBINED) animSelection = 4;
        spinnerAnimationType.setSelection(animSelection);

        updateSpeedVisibility();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (activePreviewAnimations != null) {
            AnimationHelper.cancel(activePreviewAnimations);
        }

        // Persist the latest configuration quietly so it can be restored on next launch
        String text = editTextInput.getText() != null ? editTextInput.getText().toString() : "";
        if (!text.trim().isEmpty()) {
            DisplayConfig config = buildConfigFromUi(false);
            if (config != null) {
                repository.saveLastConfig(config);
            }
        }
    }
}
