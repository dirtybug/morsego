package com.morsego.app.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.morsego.app.MainActivity;
import com.morsego.app.R;
import com.morsego.app.databinding.DialogSettingsBinding;
import com.morsego.app.keyer.KeyerSettings;
import com.morsego.app.keyer.MorseTiming;

public class SettingsDialogFragment extends DialogFragment {

    private DialogSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) requireActivity();
        KeyerSettings settings = activity.getSettings();

        // Speed WPM
        binding.tvSettingsWpmLabel.setText(getString(R.string.settings_wpm_label, settings.getWpm()));
        binding.seekSettingsWpm.setProgress(settings.getWpm() - 5);
        binding.seekSettingsWpm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int wpm = progress + 5;
                    settings.setWpm(wpm);
                    binding.tvSettingsWpmLabel.setText(getString(R.string.settings_wpm_label, wpm));
                    activity.updateTopWpm(wpm);
                    updateSpacingLabels(settings);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Pitch Frequency
        int pitch = (int) settings.getPitchHz();
        binding.tvSettingsPitchLabel.setText(getString(R.string.settings_pitch_label, pitch));
        binding.seekSettingsPitch.setProgress(pitch - 400);
        binding.seekSettingsPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int f = progress + 400;
                    settings.setPitchHz(f);
                    activity.getSynthesizer().setFrequency(f);
                    binding.tvSettingsPitchLabel.setText(getString(R.string.settings_pitch_label, f));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Letter Spacing (Inter-Character: 2 - 8 dits, progress = dits - 2)
        binding.seekSettingsLetterSpacing.setMax(6);
        binding.seekSettingsLetterSpacing.setProgress(settings.getLetterSpacingDits() - 2);
        binding.seekSettingsLetterSpacing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int dits = progress + 2;
                    settings.setLetterSpacingDits(dits);
                    updateLetterSpacingLabel(settings);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Word Spacing (Inter-Word: 5 - 14 dits, progress = dits - 5)
        binding.seekSettingsWordSpacing.setMax(9);
        binding.seekSettingsWordSpacing.setProgress(settings.getWordSpacingDits() - 5);
        binding.seekSettingsWordSpacing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int dits = progress + 5;
                    settings.setWordSpacingDits(dits);
                    updateWordSpacingLabel(settings);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        updateSpacingLabels(settings);

        binding.btnTestTone.setOnClickListener(v -> {
            activity.playMorse("... --- ...", settings.getWpm(), null);
        });

        // Mode
        switch (settings.getMode()) {
            case IAMBIC_A:
                binding.rbIambicA.setChecked(true);
                break;
            case STRAIGHT_KEY:
                binding.rbStraightKey.setChecked(true);
                break;
            case IAMBIC_B:
            default:
                binding.rbIambicB.setChecked(true);
                break;
        }

        binding.rgKeyerMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbIambicA) {
                settings.setMode(KeyerSettings.Mode.IAMBIC_A);
            } else if (checkedId == R.id.rbStraightKey) {
                settings.setMode(KeyerSettings.Mode.STRAIGHT_KEY);
            } else {
                settings.setMode(KeyerSettings.Mode.IAMBIC_B);
            }
        });

        // Sound & Haptics
        binding.switchSound.setChecked(settings.isSoundEnabled());
        binding.switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settings.setSoundEnabled(isChecked);
            activity.checkSilentMode();
        });

        binding.switchHaptics.setChecked(settings.isHapticsEnabled());
        binding.switchHaptics.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settings.setHapticsEnabled(isChecked);
        });

        binding.btnCloseSettings.setOnClickListener(v -> dismiss());

        binding.tvSettingsVersionBuild.setText("MorseGO v" + com.morsego.app.BuildConfig.VERSION_NAME + " • Build: " + com.morsego.app.BuildConfig.BUILD_TIME);
    }

    private void updateSpacingLabels(KeyerSettings settings) {
        updateLetterSpacingLabel(settings);
        updateWordSpacingLabel(settings);
    }

    private void updateLetterSpacingLabel(KeyerSettings settings) {
        int dits = settings.getLetterSpacingDits();
        long ms = MorseTiming.interCharSpaceMs(settings.getWpm(), dits);
        binding.tvSettingsLetterSpacingLabel.setText(getString(R.string.settings_letter_spacing_label, dits, ms));
    }

    private void updateWordSpacingLabel(KeyerSettings settings) {
        int dits = settings.getWordSpacingDits();
        long ms = MorseTiming.wordSpaceMs(settings.getWpm(), dits);
        binding.tvSettingsWordSpacingLabel.setText(getString(R.string.settings_word_spacing_label, dits, ms));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
