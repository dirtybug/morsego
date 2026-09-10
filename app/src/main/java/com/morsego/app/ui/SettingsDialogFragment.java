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
        binding.tvSettingsWpmLabel.setText("VELOCIDADE: " + settings.getWpm() + " WPM");
        binding.seekSettingsWpm.setProgress(settings.getWpm() - 5);
        binding.seekSettingsWpm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int wpm = progress + 5;
                    settings.setWpm(wpm);
                    binding.tvSettingsWpmLabel.setText("VELOCIDADE: " + wpm + " WPM");
                    activity.updateTopWpm(wpm);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Pitch Frequency
        int pitch = (int) settings.getPitchHz();
        binding.tvSettingsPitchLabel.setText("TOM CW: " + pitch + " HZ");
        binding.seekSettingsPitch.setProgress(pitch - 400);
        binding.seekSettingsPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int f = progress + 400;
                    settings.setPitchHz(f);
                    activity.getSynthesizer().setFrequency(f);
                    binding.tvSettingsPitchLabel.setText("TOM CW: " + f + " HZ");
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.btnTestTone.setOnClickListener(v -> {
            activity.getSynthesizer().playMorsePattern("... --- ...", settings.getWpm(), null);
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
        });

        binding.switchHaptics.setChecked(settings.isHapticsEnabled());
        binding.switchHaptics.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settings.setHapticsEnabled(isChecked);
        });

        binding.btnCloseSettings.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
