package com.morsego.app.keyer;

import org.junit.Test;
import static org.junit.Assert.*;

public class MorseTimingTest {

    @Test
    public void testDitDurationCalculations() {
        // At 20 WPM: 1200 / 20 = 60ms
        assertEquals(60L, MorseTiming.ditDurationMs(20));
        // At 15 WPM: 1200 / 15 = 80ms
        assertEquals(80L, MorseTiming.ditDurationMs(15));
        // At 10 WPM: 1200 / 10 = 120ms
        assertEquals(120L, MorseTiming.ditDurationMs(10));
    }

    @Test
    public void testDahDurationCalculations() {
        // Dah is 3x Dit
        assertEquals(180L, MorseTiming.dahDurationMs(20));
        assertEquals(240L, MorseTiming.dahDurationMs(15));
    }

    @Test
    public void testSpacesCalculations() {
        int wpm = 20; // dit = 60ms
        assertEquals(60L, MorseTiming.intraCharSpaceMs(wpm));
        assertEquals(180L, MorseTiming.interCharSpaceMs(wpm)); // 3 dits
        assertEquals(420L, MorseTiming.wordSpaceMs(wpm));      // 7 dits
    }

    @Test
    public void testIntraElementPauseEvaluation() {
        int wpm = 20; // dit = 60ms
        // Ideal pause: 60ms -> ratio 1.0x (Good, no failure)
        MorseTiming.PauseEvaluation evalGood = MorseTiming.evaluateIntraElementPause(60, wpm);
        assertTrue(evalGood.isGood);
        assertFalse(evalGood.isTimingFailure);

        // Too fast pause: 20ms (< 0.45x) -> TIMING FAILURE
        MorseTiming.PauseEvaluation evalTooFast = MorseTiming.evaluateIntraElementPause(20, wpm);
        assertFalse(evalTooFast.isGood);
        assertTrue(evalTooFast.isTimingFailure);

        // Too long intra-element pause: 150ms (> 2.0x) -> TIMING FAILURE
        MorseTiming.PauseEvaluation evalTooSlow = MorseTiming.evaluateIntraElementPause(150, wpm);
        assertFalse(evalTooSlow.isGood);
        assertTrue(evalTooSlow.isTimingFailure);
    }

    @Test
    public void testInterLetterPauseEvaluation() {
        int wpm = 20; // inter-letter space = 180ms
        // Ideal pause: 180ms -> ratio 1.0x (Good, no failure)
        MorseTiming.PauseEvaluation evalGood = MorseTiming.evaluateLetterPause(180, wpm);
        assertTrue(evalGood.isGood);
        assertFalse(evalGood.isTimingFailure);

        // Too fast letter pause: 50ms (< 0.40x) -> TIMING FAILURE
        MorseTiming.PauseEvaluation evalTooFast = MorseTiming.evaluateLetterPause(50, wpm);
        assertFalse(evalTooFast.isGood);
        assertTrue(evalTooFast.isTimingFailure);

        // Generous pause of 650ms (3.6x) is accepted with the generous tolerance
        MorseTiming.PauseEvaluation evalTolerated = MorseTiming.evaluateLetterPause(650, wpm);
        assertFalse(evalTolerated.isTimingFailure);

        // Too long letter pause: 1000ms (> 5.0x) -> TIMING FAILURE
        MorseTiming.PauseEvaluation evalTooSlow = MorseTiming.evaluateLetterPause(1000, wpm);
        assertFalse(evalTooSlow.isGood);
        assertTrue(evalTooSlow.isTimingFailure);
    }
}
