package com.morsego.app.keyer;

/**
 * Standard PARIS Morse code timing calculations in milliseconds.
 * 1 standard word "PARIS " = 50 dit time units.
 */
public class MorseTiming {

    public static long ditDurationMs(int wpm) {
        int safeWpm = Math.max(5, Math.min(50, wpm));
        return 1200L / safeWpm;
    }

    public static long dahDurationMs(int wpm) {
        return ditDurationMs(wpm) * 3;
    }

    public static long intraCharSpaceMs(int wpm) {
        return ditDurationMs(wpm);
    }

    public static final int DEFAULT_LETTER_SPACING_DITS = 6;
    public static final int DEFAULT_WORD_SPACING_DITS = 13;

    public static long interCharSpaceMs(int wpm) {
        return interCharSpaceMs(wpm, 3);
    }

    public static long interCharSpaceMs(int wpm, int letterSpacingDits) {
        return ditDurationMs(wpm) * Math.max(1, letterSpacingDits);
    }

    public static long wordSpaceMs(int wpm) {
        return wordSpaceMs(wpm, 7);
    }

    public static long wordSpaceMs(int wpm, int wordSpacingDits) {
        return ditDurationMs(wpm) * Math.max(1, wordSpacingDits);
    }

    public static long defaultInterCharSpaceMs(int wpm) {
        return interCharSpaceMs(wpm, DEFAULT_LETTER_SPACING_DITS);
    }

    public static long defaultWordSpaceMs(int wpm) {
        return wordSpaceMs(wpm, DEFAULT_WORD_SPACING_DITS);
    }

    public static class PauseEvaluation {
        public final boolean isGood;
        public final boolean isTimingFailure;
        public final String feedback;
        public final float ratio;

        public PauseEvaluation(boolean isGood, boolean isTimingFailure, String feedback, float ratio) {
            this.isGood = isGood;
            this.isTimingFailure = isTimingFailure;
            this.feedback = feedback;
            this.ratio = ratio;
        }
    }

    /**
     * Evaluates pause between dits and dahs inside the same letter (Nominal: 1 Dit).
     * Violating minimum (< 0.45x) or maximum (> 2.0x) is considered a TIMING FAILURE.
     */
    public static PauseEvaluation evaluateIntraElementPause(long pauseMs, int wpm) {
        long ideal = ditDurationMs(wpm);
        float ratio = (float) pauseMs / ideal;

        if (ratio < 0.45f) {
            String feedback = "Failure: Pause too short between dit/dah (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x < min 0.45x)";
            return new PauseEvaluation(false, true, feedback, ratio);
        } else if (ratio > 2.0f) {
            String feedback = "Failure: Excessive pause inside same character (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x > max 2.0x)";
            return new PauseEvaluation(false, true, feedback, ratio);
        } else if (ratio >= 0.5f && ratio <= 1.8f) {
            String feedback = "Correct cadence between dit/dah (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x)";
            return new PauseEvaluation(true, false, feedback, ratio);
        } else {
            String feedback = "Acceptable cadence between elements (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x)";
            return new PauseEvaluation(true, false, feedback, ratio);
        }
    }

    /**
     * Evaluates pause between letters in a word (Nominal: 3 Dits default).
     * Violating minimum (< 0.35x) or maximum (> 5.0x) is considered a TIMING FAILURE.
     */
    public static PauseEvaluation evaluateLetterPause(long pauseMs, int wpm) {
        return evaluateLetterPause(pauseMs, wpm, 3);
    }

    public static PauseEvaluation evaluateLetterPause(long pauseMs, int wpm, int letterSpacingDits) {
        long ideal = interCharSpaceMs(wpm, letterSpacingDits);
        float ratio = (float) pauseMs / ideal;

        if (ratio < 0.35f) {
            String feedback = "Failure: Insufficient pause between letters (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x < min 0.35x)";
            return new PauseEvaluation(false, true, feedback, ratio);
        } else if (ratio > 5.0f) {
            String feedback = "Failure: Excessive pause between letters (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x > max 5.0x)";
            return new PauseEvaluation(false, true, feedback, ratio);
        } else if (ratio >= 0.7f && ratio <= 1.8f) {
            String feedback = "Excellent letter spacing (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x)";
            return new PauseEvaluation(true, false, feedback, ratio);
        } else {
            String feedback = "Acceptable letter spacing (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x)";
            return new PauseEvaluation(true, false, feedback, ratio);
        }
    }
}
