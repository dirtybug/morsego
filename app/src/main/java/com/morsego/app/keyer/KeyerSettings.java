package com.morsego.app.keyer;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.KeyEvent;

public class KeyerSettings {
    private static final String PREF_NAME = "morsego_prefs";
    private static final String KEY_WPM = "wpm";
    private static final String KEY_PITCH = "pitch";
    private static final String KEY_MODE = "mode";
    private static final String KEY_REVERSE = "reverse";
    private static final String KEY_DIT_KEY = "dit_key";
    private static final String KEY_DAH_KEY = "dah_key";
    private static final String KEY_SOUND = "sound";
    private static final String KEY_HAPTICS = "haptics";
    private static final String KEY_LEVEL = "current_level";

    public enum Mode {
        IAMBIC_B("Iambic B", "Modo padrão com inserção alternada ao soltar (Curtis)."),
        IAMBIC_A("Iambic A", "Modo clássico, sem elemento extra ao soltar as pás."),
        STRAIGHT_KEY("Manual / Straight", "Chave manual ou cadência gerada pelo próprio hardware.");

        private final String label;
        private final String description;

        Mode(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
            return description;
        }
    }

    private int wpm = 15;
    private float pitchHz = 700.0f;
    private Mode mode = Mode.IAMBIC_B;
    private boolean reversePaddles = false;
    private int ditKeyCode = KeyEvent.KEYCODE_CTRL_LEFT;
    private int dahKeyCode = KeyEvent.KEYCODE_CTRL_RIGHT;
    private boolean soundEnabled = true;
    private boolean hapticsEnabled = true;
    private int currentUnlockedLevel = 1;

    private final SharedPreferences prefs;

    public KeyerSettings(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        load();
    }

    public void load() {
        this.wpm = prefs.getInt(KEY_WPM, 15);
        this.pitchHz = prefs.getFloat(KEY_PITCH, 700.0f);
        this.reversePaddles = prefs.getBoolean(KEY_REVERSE, false);
        this.ditKeyCode = prefs.getInt(KEY_DIT_KEY, KeyEvent.KEYCODE_CTRL_LEFT);
        this.dahKeyCode = prefs.getInt(KEY_DAH_KEY, KeyEvent.KEYCODE_CTRL_RIGHT);
        this.soundEnabled = prefs.getBoolean(KEY_SOUND, true);
        this.hapticsEnabled = prefs.getBoolean(KEY_HAPTICS, true);
        this.currentUnlockedLevel = prefs.getInt(KEY_LEVEL, 1);

        String modeStr = prefs.getString(KEY_MODE, Mode.IAMBIC_B.name());
        try {
            this.mode = Mode.valueOf(modeStr);
        } catch (Exception e) {
            this.mode = Mode.IAMBIC_B;
        }
    }

    public void save() {
        prefs.edit()
                .putInt(KEY_WPM, wpm)
                .putFloat(KEY_PITCH, pitchHz)
                .putString(KEY_MODE, mode.name())
                .putBoolean(KEY_REVERSE, reversePaddles)
                .putInt(KEY_DIT_KEY, ditKeyCode)
                .putInt(KEY_DAH_KEY, dahKeyCode)
                .putBoolean(KEY_SOUND, soundEnabled)
                .putBoolean(KEY_HAPTICS, hapticsEnabled)
                .putInt(KEY_LEVEL, currentUnlockedLevel)
                .apply();
    }

    public int getWpm() { return wpm; }
    public void setWpm(int wpm) { this.wpm = Math.max(5, Math.min(45, wpm)); save(); }

    public float getPitchHz() { return pitchHz; }
    public void setPitchHz(float pitchHz) { this.pitchHz = pitchHz; save(); }

    public Mode getMode() { return mode; }
    public void setMode(Mode mode) { this.mode = mode; save(); }

    public boolean isReversePaddles() { return reversePaddles; }
    public void setReversePaddles(boolean reverse) { this.reversePaddles = reverse; save(); }

    public int getDitKeyCode() { return ditKeyCode; }
    public void setDitKeyCode(int ditKeyCode) { this.ditKeyCode = ditKeyCode; save(); }

    public int getDahKeyCode() { return dahKeyCode; }
    public void setDahKeyCode(int dahKeyCode) { this.dahKeyCode = dahKeyCode; save(); }

    public boolean isSoundEnabled() { return soundEnabled; }
    public void setSoundEnabled(boolean soundEnabled) { this.soundEnabled = soundEnabled; save(); }

    public boolean isHapticsEnabled() { return hapticsEnabled; }
    public void setHapticsEnabled(boolean hapticsEnabled) { this.hapticsEnabled = hapticsEnabled; save(); }

    public int getCurrentUnlockedLevel() { return currentUnlockedLevel; }
    public void setCurrentUnlockedLevel(int level) {
        this.currentUnlockedLevel = Math.max(1, level);
        save();
    }

    public boolean isLevelUnlocked(int level) {
        return level <= currentUnlockedLevel;
    }

    public boolean unlockNextLevel(int completedLevel) {
        if (completedLevel >= currentUnlockedLevel) {
            currentUnlockedLevel = completedLevel + 1;
            save();
            return true;
        }
        return false;
    }

    public void recordLetterFailure(String letter) {
        if (letter == null || letter.isEmpty()) return;
        String key = "fail_cnt_" + letter.toUpperCase();
        int current = prefs.getInt(key, 0);
        prefs.edit().putInt(key, current + 1).apply();
    }

    public int getLetterFailureCount(String letter) {
        if (letter == null || letter.isEmpty()) return 0;
        return prefs.getInt("fail_cnt_" + letter.toUpperCase(), 0);
    }

    /**
     * Returns up to 'count' letters from 'pool' that have the highest failure rates.
     * If there are ties or letters with 0 failures, fills randomly from pool.
     */
    public java.util.List<String> getMostFailedLetters(java.util.List<String> pool, int count) {
        java.util.List<String> sorted = new java.util.ArrayList<>(pool);
        // Sort by failure count descending
        java.util.Collections.sort(sorted, (a, b) -> Integer.compare(getLetterFailureCount(b), getLetterFailureCount(a)));

        java.util.List<String> result = new java.util.ArrayList<>();
        for (int i = 0; i < count && i < sorted.size(); i++) {
            result.add(sorted.get(i));
        }

        // If count is greater than pool size, repeat randomly from pool
        while (result.size() < count && !pool.isEmpty()) {
            int idx = (int) (Math.random() * pool.size());
            result.add(pool.get(idx));
        }
        return result;
    }
}
