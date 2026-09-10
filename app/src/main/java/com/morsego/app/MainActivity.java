package com.morsego.app;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.morsego.app.audio.MorseAudioSynthesizer;
import com.morsego.app.databinding.ActivityMainBinding;
import com.morsego.app.keyer.IambicKeyerEngine;
import com.morsego.app.keyer.KeyerInputManager;
import com.morsego.app.keyer.KeyerSettings;
import com.morsego.app.keyer.MorseDecoder;
import com.morsego.app.ui.FreeKeyerFragment;
import com.morsego.app.ui.HardwareFragment;
import com.morsego.app.ui.LearnFragment;
import com.morsego.app.ui.PracticeFragment;
import com.morsego.app.ui.SettingsDialogFragment;
import com.morsego.app.ui.TreeFragment;

public class MainActivity extends AppCompatActivity implements KeyerInputManager.PaddleListener, IambicKeyerEngine.KeyerListener {

    public static final int TAB_TREE = R.id.nav_tree;
    public static final int TAB_LEARN = R.id.nav_learn;
    public static final int TAB_PRACTICE = R.id.nav_practice;
    public static final int TAB_KEYER = R.id.nav_keyer;
    public static final int TAB_HARDWARE = R.id.nav_hardware;

    private ActivityMainBinding binding;

    private MorseAudioSynthesizer synthesizer;
    private KeyerSettings settings;
    private KeyerInputManager inputManager;
    private IambicKeyerEngine iambicEngine;
    private MorseDecoder decoder;
    private Vibrator vibrator;

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize audio & core keyer components
        synthesizer = new MorseAudioSynthesizer();
        settings = new KeyerSettings(this);
        synthesizer.setFrequency(settings.getPitchHz());

        inputManager = new KeyerInputManager(settings);
        inputManager.setPaddleListener(this);

        iambicEngine = new IambicKeyerEngine(settings, this);
        decoder = new MorseDecoder(settings);

        initVibrator();

        binding.tvTopWpm.setText(settings.getWpm() + " WPM");

        binding.btnSettings.setOnClickListener(v -> {
            new SettingsDialogFragment().show(getSupportFragmentManager(), "settings_dialog");
        });

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_tree) {
                switchFragment(new TreeFragment());
                return true;
            } else if (itemId == R.id.nav_learn) {
                switchFragment(new LearnFragment());
                return true;
            } else if (itemId == R.id.nav_practice) {
                switchFragment(new PracticeFragment());
                return true;
            } else if (itemId == R.id.nav_keyer) {
                switchFragment(new FreeKeyerFragment());
                return true;
            } else if (itemId == R.id.nav_hardware) {
                switchFragment(new HardwareFragment());
                return true;
            }
            return false;
        });

        // Default start fragment: TreeFragment (Binary Tree view)
        switchFragment(new TreeFragment());
    }

    private void initVibrator() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vm = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vm != null) vibrator = vm.getDefaultVibrator();
            } else {
                vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            }
        } catch (Exception ignored) {}
    }

    private void triggerHaptic() {
        if (!settings.isHapticsEnabled() || vibrator == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(15);
            }
        } catch (Exception ignored) {}
    }

    public void updateTopWpm(int wpm) {
        binding.tvTopWpm.setText(wpm + " WPM");
    }

    public void navigateToTab(int tabId) {
        binding.bottomNavigation.setSelectedItemId(tabId);
    }

    private void switchFragment(Fragment fragment) {
        this.currentFragment = fragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    // --- Keyer & Hardware Input Routing ---

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (inputManager.handleKeyEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if (inputManager.handleGenericMotionEvent(event)) {
            return true;
        }
        return super.dispatchGenericMotionEvent(event);
    }

    @Override
    public void onDitStateChanged(boolean isPressed) {
        if (settings.getMode() == KeyerSettings.Mode.STRAIGHT_KEY) {
            decoder.onManualToneState(isPressed);
        }
        iambicEngine.onDitChanged(isPressed);
        if (isPressed) triggerHaptic();

        // Update hardware fragment if visible
        if (currentFragment instanceof HardwareFragment) {
            ((HardwareFragment) currentFragment).updatePaddleVisuals(inputManager.isDitPressed(), inputManager.isDahPressed());
        }
    }

    @Override
    public void onDahStateChanged(boolean isPressed) {
        if (settings.getMode() == KeyerSettings.Mode.STRAIGHT_KEY) {
            decoder.onManualToneState(isPressed);
        }
        iambicEngine.onDahChanged(isPressed);
        if (isPressed) triggerHaptic();

        // Update hardware fragment if visible
        if (currentFragment instanceof HardwareFragment) {
            ((HardwareFragment) currentFragment).updatePaddleVisuals(inputManager.isDitPressed(), inputManager.isDahPressed());
        }
    }

    // --- Iambic Engine Callbacks ---

    @Override
    public void onToneStart() {
        if (settings.isSoundEnabled()) {
            synthesizer.startTone();
        }
        runOnUiThread(() -> {
            if (currentFragment instanceof FreeKeyerFragment) {
                ((FreeKeyerFragment) currentFragment).setLedActive(true);
            }
        });
    }

    @Override
    public void onToneStop() {
        synthesizer.stopTone();
        runOnUiThread(() -> {
            if (currentFragment instanceof FreeKeyerFragment) {
                ((FreeKeyerFragment) currentFragment).setLedActive(false);
            }
        });
    }

    @Override
    public void onElementEmitted(char element) {
        decoder.onElementReceived(element);
    }

    // Getters for fragments
    public MorseAudioSynthesizer getSynthesizer() { return synthesizer; }
    public KeyerSettings getSettings() { return settings; }
    public KeyerInputManager getInputManager() { return inputManager; }
    public MorseDecoder getDecoder() { return decoder; }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (iambicEngine != null) iambicEngine.release();
        if (synthesizer != null) synthesizer.release();
    }
}
