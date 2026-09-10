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
}
