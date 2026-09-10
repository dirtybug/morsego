package com.morsego.app.keyer

/**
 * Standard PARIS Morse code timing calculations.
 * Standard word "PARIS " is 50 dit units in total length.
 */
object MorseTiming {

    /**
     * Calculates the duration of 1 Dit unit in milliseconds based on standard WPM.
     */
    fun ditDurationMs(wpm: Int): Long {
        val safeWpm = wpm.coerceIn(5, 50)
        return (1200L / safeWpm)
    }

    /**
     * 1 Dah = 3 Dit units
     */
    fun dahDurationMs(wpm: Int): Long {
        return ditDurationMs(wpm) * 3
    }

    /**
     * Space between dits/dahs within the same character = 1 Dit
     */
    fun intraCharSpaceMs(wpm: Int): Long {
        return ditDurationMs(wpm)
    }

    /**
     * Space between letters = 3 Dit units (standard) or adjusted for Farnsworth speed
     */
    fun interCharSpaceMs(characterWpm: Int, farnsworthWpm: Int = characterWpm): Long {
        if (farnsworthWpm >= characterWpm) {
            return ditDurationMs(characterWpm) * 3
        }
        // Farnsworth timing: letters played fast, but pause between letters lengthened
        val charDit = ditDurationMs(characterWpm)
        val farnsDit = ditDurationMs(farnsworthWpm)
        return maxOf(charDit * 3, farnsDit * 3)
    }

    /**
     * Space between words = 7 Dit units (standard) or adjusted for Farnsworth speed
     */
    fun wordSpaceMs(characterWpm: Int, farnsworthWpm: Int = characterWpm): Long {
        if (farnsworthWpm >= characterWpm) {
            return ditDurationMs(characterWpm) * 7
        }
        val charDit = ditDurationMs(characterWpm)
        val farnsDit = ditDurationMs(farnsworthWpm)
        return maxOf(charDit * 7, farnsDit * 7)
    }
}
