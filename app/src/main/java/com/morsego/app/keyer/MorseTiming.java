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

    public static long interCharSpaceMs(int wpm) {
        return ditDurationMs(wpm) * 3;
    }

    public static long wordSpaceMs(int wpm) {
        return ditDurationMs(wpm) * 7;
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
        boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");

        if (ratio < 0.45f) {
            String feedback = isPt ?
                    "Falha: Pausa muito curta entre ponto/traço (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x < mín 0.45x)" :
                    "Failure: Pause too short between dit/dah (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x < min 0.45x)";
            return new PauseEvaluation(false, true, feedback, ratio);
        } else if (ratio > 2.0f) {
            String feedback = isPt ?
                    "Falha: Pausa excessiva dentro da mesma letra (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x > máx 2.0x)" :
                    "Failure: Excessive pause inside same character (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x > max 2.0x)";
            return new PauseEvaluation(false, true, feedback, ratio);
        } else if (ratio >= 0.5f && ratio <= 1.8f) {
            String feedback = isPt ?
                    "Cadência correta entre ponto/traço (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x)" :
                    "Correct cadence between dit/dah (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x)";
            return new PauseEvaluation(true, false, feedback, ratio);
        } else {
            String feedback = isPt ?
                    "Cadência aceitável entre elementos (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x)" :
                    "Acceptable cadence between elements (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x)";
            return new PauseEvaluation(true, false, feedback, ratio);
        }
    }

    /**
     * Evaluates pause between letters in a word (Nominal: 3 Dits).
     * Violating minimum (< 0.40x) or maximum (> 3.2x) is considered a TIMING FAILURE.
     */
    public static PauseEvaluation evaluateLetterPause(long pauseMs, int wpm) {
        long ideal = interCharSpaceMs(wpm);
        float ratio = (float) pauseMs / ideal;
        boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");

        if (ratio < 0.40f) {
            String feedback = isPt ?
                    "Falha: Pausa insuficiente entre letras (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x < mín 0.4x)" :
                    "Failure: Insufficient pause between letters (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x < min 0.4x)";
            return new PauseEvaluation(false, true, feedback, ratio);
        } else if (ratio > 3.2f) {
            String feedback = isPt ?
                    "Falha: Pausa excessiva entre letras (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x > máx 3.2x)" :
                    "Failure: Excessive pause between letters (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x > max 3.2x)";
            return new PauseEvaluation(false, true, feedback, ratio);
        } else if (ratio >= 0.7f && ratio <= 1.8f) {
            String feedback = isPt ?
                    "Excelente separação de letras (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x)" :
                    "Excellent letter spacing (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x)";
            return new PauseEvaluation(true, false, feedback, ratio);
        } else {
            String feedback = isPt ?
                    "Separação aceitável entre letras (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x)" :
                    "Acceptable letter spacing (" + String.format(java.util.Locale.US, "%.1f", ratio) + "x)";
            return new PauseEvaluation(true, false, feedback, ratio);
        }
    }
}
