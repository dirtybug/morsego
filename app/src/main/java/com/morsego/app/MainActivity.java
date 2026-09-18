package com.morsego.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.morsego.app.audio.MorseAudioSynthesizer;
import com.morsego.app.audio.MorseSignalDispatcher;
import com.morsego.app.databinding.ActivityMainBinding;
import com.morsego.app.keyer.IambicKeyerEngine;
import com.morsego.app.keyer.KeyerInputManager;
import com.morsego.app.keyer.KeyerSettings;
import com.morsego.app.keyer.MorseDecoder;
import com.morsego.app.keyer.MorseTiming;
import com.morsego.app.ui.AboutDialogFragment;
import com.morsego.app.ui.FreeKeyerFragment;
import com.morsego.app.ui.HardwareFragment;
import com.morsego.app.ui.ReceiveFragment;
import com.morsego.app.ui.SendFragment;
import com.morsego.app.ui.SettingsDialogFragment;
import com.morsego.app.ui.TreeFragment;

public class MainActivity extends AppCompatActivity implements KeyerInputManager.PaddleListener, IambicKeyerEngine.KeyerListener {

    public static final int TAB_TREE = R.id.nav_tree;
    public static final int TAB_SEND = R.id.nav_send;
    public static final int TAB_RECEIVE = R.id.nav_receive;
    public static final int TAB_LEARN = R.id.nav_send;
    public static final int TAB_PRACTICE = R.id.nav_receive;
    public static final int TAB_HARDWARE = R.id.nav_hardware;
    public static final int TAB_KEYER = R.id.nav_keyer;
    public static final int TAB_SETTINGS = R.id.nav_settings;

    private ActivityMainBinding binding;

    private MorseAudioSynthesizer synthesizer;
    private MorseSignalDispatcher signalDispatcher;
    private KeyerSettings settings;
    private KeyerInputManager inputManager;
    private IambicKeyerEngine iambicEngine;
    private MorseDecoder decoder;
    private Vibrator vibrator;
    private BroadcastReceiver silentModeReceiver;
    private ContentObserver volumeObserver;
    private volatile boolean isVibratingTone = false;

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
        signalDispatcher = new MorseSignalDispatcher(this, settings, synthesizer);
        signalDispatcher.setSilentMode(isDeviceInSilentMode());

        inputManager = new KeyerInputManager(settings);
        inputManager.setPaddleListener(this);

        iambicEngine = new IambicKeyerEngine(settings, this);
        decoder = new MorseDecoder(settings);

        initVibrator();

        binding.tvTopWpm.setText(settings.getWpm() + " WPM");
        binding.tvAppVersionBuild.setText("v" + BuildConfig.VERSION_NAME + " • " + BuildConfig.BUILD_TIME);

        View.OnClickListener openAbout = v -> {
            new AboutDialogFragment().show(getSupportFragmentManager(), "about_dialog");
        };
        binding.btnAbout.setOnClickListener(openAbout);
        binding.ivAppLogo.setOnClickListener(openAbout);
        binding.tvAppTitle.setOnClickListener(openAbout);
        binding.tvAppVersionBuild.setOnClickListener(openAbout);

        binding.btnSettings.setOnClickListener(v -> {
            new SettingsDialogFragment().show(getSupportFragmentManager(), "settings_dialog");
        });

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_tree) {
                switchFragment(new TreeFragment());
                return true;
            } else if (itemId == R.id.nav_send || itemId == R.id.nav_learn) {
                SendFragment fragment = new SendFragment();
                if (pendingTargetLevel > 0) {
                    Bundle args = new Bundle();
                    args.putInt("target_level", pendingTargetLevel);
                    fragment.setArguments(args);
                    pendingTargetLevel = -1;
                }
                switchFragment(fragment);
                return true;
            } else if (itemId == R.id.nav_receive || itemId == R.id.nav_practice) {
                ReceiveFragment fragment = new ReceiveFragment();
                if (pendingTargetLevel > 0) {
                    Bundle args = new Bundle();
                    args.putInt("target_level", pendingTargetLevel);
                    fragment.setArguments(args);
                    pendingTargetLevel = -1;
                }
                switchFragment(fragment);
                return true;
            } else if (itemId == R.id.nav_keyer) {
                switchFragment(new FreeKeyerFragment());
                return true;
            } else if (itemId == R.id.nav_hardware) {
                switchFragment(new HardwareFragment());
                return true;
            } else if (itemId == R.id.nav_settings) {
                new SettingsDialogFragment().show(getSupportFragmentManager(), "settings_dialog");
                return false;
            }
            return false;
        });

        // Default start fragment: TreeFragment (Binary Tree view)
        binding.bottomNavigation.setSelectedItemId(R.id.nav_tree);
        checkSilentMode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerSilentModeObserver();
        checkSilentMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterSilentModeObserver();
    }

    private void registerSilentModeObserver() {
        if (silentModeReceiver == null) {
            silentModeReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    checkSilentMode();
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
            filter.addAction("android.media.VOLUME_CHANGED_ACTION");
            filter.addAction("android.media.RINGER_MODE_CHANGED");
            try {
                registerReceiver(silentModeReceiver, filter);
            } catch (Exception ignored) {}
        }

        if (volumeObserver == null) {
            try {
                volumeObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
                    @Override
                    public void onChange(boolean selfChange) {
                        super.onChange(selfChange);
                        checkSilentMode();
                    }
                };
                getContentResolver().registerContentObserver(
                        Settings.System.CONTENT_URI,
                        true,
                        volumeObserver
                );
            } catch (Exception ignored) {}
        }
    }

    private void unregisterSilentModeObserver() {
        if (silentModeReceiver != null) {
            try {
                unregisterReceiver(silentModeReceiver);
            } catch (Exception ignored) {}
            silentModeReceiver = null;
        }
        if (volumeObserver != null) {
            try {
                getContentResolver().unregisterContentObserver(volumeObserver);
            } catch (Exception ignored) {}
            volumeObserver = null;
        }
    }

    private Boolean silentModeOverrideForTesting = null;
    private volatile String lastMorsePlayType = null; // "VIBRATION" or "AUDIO"

    public void setSilentModeForced(Boolean forcedSilent) {
        this.silentModeOverrideForTesting = forcedSilent;
        if (forcedSilent != null && settings != null) {
            settings.setSoundEnabled(!forcedSilent);
        }
        checkSilentMode();
    }

    public Boolean getSilentModeForced() {
        return silentModeOverrideForTesting;
    }

    public String getLastMorsePlayType() {
        return lastMorsePlayType;
    }

    public boolean isSilentBannerVisible() {
        return binding != null && binding.bannerSilentMode.getVisibility() == View.VISIBLE;
    }

    public boolean isDeviceInSilentMode() {
        if (silentModeOverrideForTesting != null) {
            return silentModeOverrideForTesting;
        }
        try {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                int ringerMode = am.getRingerMode();
                int musicVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (ringerMode == AudioManager.RINGER_MODE_SILENT
                        || ringerMode == AudioManager.RINGER_MODE_VIBRATE
                        || musicVol == 0) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return settings != null && !settings.isSoundEnabled();
    }

    public void checkSilentMode() {
        if (binding == null) return;
        runOnUiThread(() -> {
            boolean isSilent = isDeviceInSilentMode();
            if (signalDispatcher != null) {
                signalDispatcher.setSilentMode(isSilent);
            }
            int targetVisibility = isSilent ? View.VISIBLE : View.GONE;
            if (binding.bannerSilentMode.getVisibility() != targetVisibility) {
                binding.bannerSilentMode.setVisibility(targetVisibility);
            }
        });
    }

    public void playMorse(String pattern, int wpm, Runnable onFinished) {
        boolean silent = isDeviceInSilentMode();
        lastMorsePlayType = silent ? "VIBRATION" : "AUDIO";
        if (signalDispatcher != null) {
            signalDispatcher.setSilentMode(silent);
            signalDispatcher.playPattern(pattern, wpm, onFinished);
        } else if (silent) {
            vibrateMorsePattern(pattern, wpm, onFinished);
        } else {
            int letterDits = settings != null ? settings.getLetterSpacingDits() : 3;
            int wordDits = settings != null ? settings.getWordSpacingDits() : 7;
            synthesizer.playMorsePattern(pattern, wpm, letterDits, wordDits, onFinished);
        }
    }

    public void startToneVibration() {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        isVibratingTone = true;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(5000, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(5000);
            }
        } catch (Exception ignored) {}
    }

    public void stopToneVibration() {
        if (!isVibratingTone) return;
        isVibratingTone = false;
        if (vibrator != null) {
            try {
                vibrator.cancel();
            } catch (Exception ignored) {}
        }
    }

    public void vibrate(long ms) {
        if (vibrator == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(ms);
            }
        } catch (Exception ignored) {}
    }

    public void vibrateMorsePattern(String pattern, int wpm) {
        vibrateMorsePattern(pattern, wpm, null);
    }

    public void vibrateMorsePattern(String pattern, int wpm, Runnable onFinished) {
        if (vibrator == null || !vibrator.hasVibrator()) {
            if (onFinished != null) runOnUiThread(onFinished);
            return;
        }
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            int letterDits = settings != null ? settings.getLetterSpacingDits() : 3;
            int wordDits = settings != null ? settings.getWordSpacingDits() : 7;
            long dit = MorseTiming.ditDurationMs(wpm);
            long dah = MorseTiming.dahDurationMs(wpm);
            long intra = MorseTiming.intraCharSpaceMs(wpm);
            long inter = MorseTiming.interCharSpaceMs(wpm, letterDits);
            long word = MorseTiming.wordSpaceMs(wpm, wordDits);
            try {
                for (int i = 0; i < pattern.length(); i++) {
                    char c = pattern.charAt(i);
                    if (c == '.') {
                        vibrate(dit);
                        Thread.sleep(dit + intra);
                    } else if (c == '-') {
                        vibrate(dah);
                        Thread.sleep(dah + intra);
                    } else if (c == ' ') {
                        long extraPause = Math.max(0, inter - intra);
                        if (extraPause > 0) Thread.sleep(extraPause);
                    } else if (c == '/') {
                        long extraPause = Math.max(0, word - intra);
                        if (extraPause > 0) Thread.sleep(extraPause);
                    }
                }
            } catch (InterruptedException ignored) {
                if (vibrator != null) vibrator.cancel();
            }
            if (onFinished != null) {
                runOnUiThread(onFinished);
            }
        });
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
        if (signalDispatcher != null) {
            signalDispatcher.triggerHaptic(15);
        } else {
            if (isDeviceInSilentMode()) return;
            if (!settings.isHapticsEnabled() || vibrator == null) return;
            vibrate(15);
        }
    }

    public void updateTopWpm(int wpm) {
        binding.tvTopWpm.setText(wpm + " WPM");
    }

    public void navigateToTab(int tabId) {
        binding.bottomNavigation.setSelectedItemId(tabId);
    }

    private int pendingTargetLevel = -1;

    public void navigateToSend(int level) {
        runOnUiThread(() -> {
            this.pendingTargetLevel = level;
            if (binding.bottomNavigation.getSelectedItemId() == R.id.nav_send) {
                SendFragment fragment = new SendFragment();
                Bundle args = new Bundle();
                args.putInt("target_level", level);
                fragment.setArguments(args);
                this.pendingTargetLevel = -1;
                switchFragment(fragment);
            } else {
                binding.bottomNavigation.setSelectedItemId(R.id.nav_send);
            }
        });
    }

    public void navigateToReceive(int level) {
        runOnUiThread(() -> {
            this.pendingTargetLevel = level;
            if (binding.bottomNavigation.getSelectedItemId() == R.id.nav_receive) {
                ReceiveFragment fragment = new ReceiveFragment();
                Bundle args = new Bundle();
                args.putInt("target_level", level);
                fragment.setArguments(args);
                this.pendingTargetLevel = -1;
                switchFragment(fragment);
            } else {
                binding.bottomNavigation.setSelectedItemId(R.id.nav_receive);
            }
        });
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
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            if (binding != null && binding.getRoot() != null) {
                binding.getRoot().postDelayed(this::checkSilentMode, 200);
            }
        }
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
        boolean silent = isDeviceInSilentMode();
        lastMorsePlayType = silent ? "VIBRATION" : "AUDIO";
        if (silent) {
            startToneVibration();
        } else if (settings.isSoundEnabled()) {
            synthesizer.startTone();
        }
        decoder.onToneStarted();
        runOnUiThread(() -> {
            if (currentFragment instanceof FreeKeyerFragment) {
                ((FreeKeyerFragment) currentFragment).setLedActive(true);
            }
        });
    }

    @Override
    public void onToneStop() {
        if (isVibratingTone) {
            stopToneVibration();
        }
        synthesizer.stopTone();
        decoder.onToneStopped();
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
    public MorseSignalDispatcher getSignalDispatcher() { return signalDispatcher; }
    public KeyerSettings getSettings() { return settings; }
    public KeyerInputManager getInputManager() { return inputManager; }
    public MorseDecoder getDecoder() { return decoder; }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterSilentModeObserver();
        if (signalDispatcher != null) signalDispatcher.release();
        if (iambicEngine != null) iambicEngine.release();
        if (synthesizer != null) synthesizer.release();
    }
}
