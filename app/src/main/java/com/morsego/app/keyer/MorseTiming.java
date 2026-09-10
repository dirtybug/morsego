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
        public final String feedback;
        public final float ratio;

        public PauseEvaluation(boolean isGood, String feedback, float ratio) {
            this.isGood = isGood;
            this.feedback = feedback;
            this.ratio = ratio;
        }
    }

    /**
     * Evaluates pause between dits and dahs inside the same letter (Nominal: 1 Dit).
     */
    public static PauseEvaluation evaluateIntraElementPause(long pauseMs, int wpm) {
        long ideal = ditDurationMs(wpm);
        float ratio = (float) pauseMs / ideal;

        if (ratio >= 0.5f && ratio <= 1.8f) {
            return new PauseEvaluation(true, "Cadência perfeita entre ponto/traço (" + String.format("%.1f", ratio) + "x)", ratio);
        } else if (ratio < 0.5f) {
            return new PauseEvaluation(false, "Pausa muito rápida entre elementos (" + String.format("%.1f", ratio) + "x)", ratio);
        } else {
            return new PauseEvaluation(false, "Pausa longa entre elementos (" + String.format("%.1f", ratio) + "x)", ratio);
        }
    }

    /**
     * Evaluates pause between letters in a word (Nominal: 3 Dits).
     */
    public static PauseEvaluation evaluateLetterPause(long pauseMs, int wpm) {
        long ideal = interCharSpaceMs(wpm);
        float ratio = (float) pauseMs / ideal;

        if (ratio >= 0.65f && ratio <= 1.9f) {
            return new PauseEvaluation(true, "Excelente separação de letras (" + String.format("%.1f", ratio) + "x)", ratio);
        } else if (ratio < 0.65f) {
            return new PauseEvaluation(false, "Pausa muito curta entre letras (" + String.format("%.1f", ratio) + "x - faça uma pausa maior)", ratio);
        } else {
            return new PauseEvaluation(false, "Pausa excessiva entre letras (" + String.format("%.1f", ratio) + "x)", ratio);
        }
    }
}
