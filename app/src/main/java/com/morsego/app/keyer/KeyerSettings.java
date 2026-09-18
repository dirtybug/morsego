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
    private static final String KEY_LETTER_SPACING = "letter_spacing_dits";
    private static final String KEY_WORD_SPACING = "word_spacing_dits";

    public enum Mode {
        IAMBIC_B("Iambic B", "Standard mode with alternate insertion on release (Curtis)."),
        IAMBIC_A("Iambic A", "Classic mode, no extra element on paddle release."),
        STRAIGHT_KEY("Manual / Straight", "Manual straight key or hardware-generated cadence.");

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
    private int letterSpacingDits = 3;
    private int wordSpacingDits = 7;

    private static final String KEY_BACKUP_LEVEL = "highest_unlocked_level_backup";

    private final SharedPreferences prefs;
    private final java.util.Map<String, Integer> inMemoryFailures = new java.util.HashMap<>();
    private final java.util.Set<Integer> inMemoryReceivePassed = new java.util.HashSet<>();
    private final java.util.Set<Integer> inMemorySendPassed = new java.util.HashSet<>();

    public KeyerSettings() {
        this.prefs = null;
    }

    public KeyerSettings(Context context) {
        this.prefs = context != null ? context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) : null;
        if (this.prefs != null) {
            load();
        }
    }

    public void load() {
        if (prefs == null) return;
        this.wpm = prefs.getInt(KEY_WPM, 15);
        this.pitchHz = prefs.getFloat(KEY_PITCH, 700.0f);
        this.reversePaddles = prefs.getBoolean(KEY_REVERSE, false);
        this.ditKeyCode = prefs.getInt(KEY_DIT_KEY, KeyEvent.KEYCODE_CTRL_LEFT);
        this.dahKeyCode = prefs.getInt(KEY_DAH_KEY, KeyEvent.KEYCODE_CTRL_RIGHT);
        this.soundEnabled = prefs.getBoolean(KEY_SOUND, true);
        this.hapticsEnabled = prefs.getBoolean(KEY_HAPTICS, true);
        this.letterSpacingDits = prefs.getInt(KEY_LETTER_SPACING, 3);
        this.wordSpacingDits = prefs.getInt(KEY_WORD_SPACING, 7);

        // Persistent level restoration with backup redundancy
        int savedLevel = prefs.getInt(KEY_LEVEL, 1);
        int backupLevel = prefs.getInt(KEY_BACKUP_LEVEL, 1);
        this.currentUnlockedLevel = Math.max(1, Math.max(savedLevel, backupLevel));

        String modeStr = prefs.getString(KEY_MODE, Mode.IAMBIC_B.name());
        try {
            this.mode = Mode.valueOf(modeStr);
        } catch (Exception e) {
            this.mode = Mode.IAMBIC_B;
        }
    }

    public void save() {
        if (prefs == null) return;
        prefs.edit()
                .putInt(KEY_WPM, wpm)
                .putFloat(KEY_PITCH, pitchHz)
                .putString(KEY_MODE, mode.name())
                .putBoolean(KEY_REVERSE, reversePaddles)
                .putInt(KEY_DIT_KEY, ditKeyCode)
                .putInt(KEY_DAH_KEY, dahKeyCode)
                .putBoolean(KEY_SOUND, soundEnabled)
                .putBoolean(KEY_HAPTICS, hapticsEnabled)
                .putInt(KEY_LETTER_SPACING, letterSpacingDits)
                .putInt(KEY_WORD_SPACING, wordSpacingDits)
                .putInt(KEY_LEVEL, currentUnlockedLevel)
                .putInt(KEY_BACKUP_LEVEL, currentUnlockedLevel)
                .commit();
    }

    public int getWpm() { return wpm; }
    public void setWpm(int wpm) { this.wpm = Math.max(5, Math.min(45, wpm)); save(); }

    public int getLetterSpacingDits() { return letterSpacingDits; }
    public void setLetterSpacingDits(int dits) { this.letterSpacingDits = Math.max(2, Math.min(8, dits)); save(); }

    public int getWordSpacingDits() { return wordSpacingDits; }
    public void setWordSpacingDits(int dits) { this.wordSpacingDits = Math.max(5, Math.min(14, dits)); save(); }

    public float getPitchHz() { return pitchHz; }
    public void setPitchHz(float pitchHz) { this.pitchHz = Math.max(400.0f, Math.min(1000.0f, pitchHz)); save(); }

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

    public int getCurrentUnlockedLevel() {
        if (prefs != null) {
            int saved = prefs.getInt(KEY_LEVEL, 1);
            int backup = prefs.getInt(KEY_BACKUP_LEVEL, 1);
            this.currentUnlockedLevel = Math.max(this.currentUnlockedLevel, Math.max(saved, backup));
        }
        return this.currentUnlockedLevel;
    }

    public synchronized void setCurrentUnlockedLevel(int level) {
        this.currentUnlockedLevel = Math.max(1, level);
        if (prefs != null) {
            prefs.edit()
                    .putInt(KEY_LEVEL, this.currentUnlockedLevel)
                    .putInt(KEY_BACKUP_LEVEL, this.currentUnlockedLevel)
                    .commit();
        }
    }

    public boolean isLevelUnlocked(int level) {
        return level <= getCurrentUnlockedLevel();
    }

    public boolean isReceivePassed(int level) {
        if (level < getCurrentUnlockedLevel()) {
            return true;
        }
        if (inMemoryReceivePassed.contains(level)) {
            return true;
        }
        if (prefs != null) {
            return prefs.getBoolean("level_" + level + "_receive_passed", false);
        }
        return false;
    }

    public synchronized void setReceivePassed(int level, boolean passed) {
        if (passed) {
            inMemoryReceivePassed.add(level);
        } else {
            inMemoryReceivePassed.remove(level);
        }
        if (prefs != null) {
            prefs.edit().putBoolean("level_" + level + "_receive_passed", passed).commit();
        }
    }

    public boolean isSendPassed(int level) {
        if (level < getCurrentUnlockedLevel()) {
            return true;
        }
        if (inMemorySendPassed.contains(level)) {
            return true;
        }
        if (prefs != null) {
            return prefs.getBoolean("level_" + level + "_send_passed", false);
        }
        return false;
    }

    public synchronized void setSendPassed(int level, boolean passed) {
        if (passed) {
            inMemorySendPassed.add(level);
        } else {
            inMemorySendPassed.remove(level);
        }
        if (prefs != null) {
            prefs.edit().putBoolean("level_" + level + "_send_passed", passed).commit();
        }
    }

    public boolean canUnlockNextLevel(int completedLevel) {
        return isReceivePassed(completedLevel) && isSendPassed(completedLevel);
    }

    public synchronized boolean unlockNextLevel(int completedLevel) {
        int current = getCurrentUnlockedLevel();
        if (completedLevel >= current && canUnlockNextLevel(completedLevel)) {
            this.currentUnlockedLevel = completedLevel + 1;
            if (prefs != null) {
                prefs.edit()
                        .putInt(KEY_LEVEL, this.currentUnlockedLevel)
                        .putInt(KEY_BACKUP_LEVEL, this.currentUnlockedLevel)
                        .commit();
            }
            return true;
        }
        return false;
    }

    public synchronized boolean forceUnlockNextLevel(int completedLevel) {
        setReceivePassed(completedLevel, true);
        setSendPassed(completedLevel, true);
        return unlockNextLevel(completedLevel);
    }

    public void recordLetterFailure(String letter) {
        if (letter == null || letter.isEmpty()) return;
        String key = letter.toUpperCase();
        inMemoryFailures.put(key, inMemoryFailures.getOrDefault(key, 0) + 1);
        if (prefs != null) {
            int current = prefs.getInt("fail_cnt_" + key, 0);
            prefs.edit().putInt("fail_cnt_" + key, current + 1).commit();
        }
    }

    public int getLetterFailureCount(String letter) {
        if (letter == null || letter.isEmpty()) return 0;
        String key = letter.toUpperCase();
        if (prefs != null) {
            return prefs.getInt("fail_cnt_" + key, inMemoryFailures.getOrDefault(key, 0));
        }
        return inMemoryFailures.getOrDefault(key, 0);
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
