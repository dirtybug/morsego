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
        assertEquals(180L, MorseTiming.interCharSpaceMs(wpm)); // 3 dits (Nominal PARIS)
        assertEquals(420L, MorseTiming.wordSpaceMs(wpm));      // 7 dits (Nominal PARIS)

        // MorseGO Default Timings at 10 WPM (6 dits = 720ms, 13 dits = 1560ms)
        assertEquals(720L, MorseTiming.defaultInterCharSpaceMs(10));
        assertEquals(1560L, MorseTiming.defaultWordSpaceMs(10));
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

        // Too fast letter pause: 50ms (< 0.35x) -> TIMING FAILURE
        MorseTiming.PauseEvaluation evalTooFast = MorseTiming.evaluateLetterPause(50, wpm);
        assertFalse(evalTooFast.isGood);
        assertTrue(evalTooFast.isTimingFailure);

        // Generous pause of 650ms (3.6x) is accepted with the generous tolerance
        MorseTiming.PauseEvaluation evalTolerated = MorseTiming.evaluateLetterPause(650, wpm);
        assertFalse(evalTolerated.isTimingFailure);

        // Too long letter pause: 1000ms (> 5.0x) -> TIMING FAILURE
        MorseTiming.PauseEvaluation evalTooSlow = MorseTiming.evaluateLetterPause(1000, wpm);
        assertTrue(evalTooSlow.isTimingFailure);
    }

    @Test
    public void testConfigurableSpacesCalculations() {
        int wpm = 10; // dit = 120ms
        // Default 6 dits = 720ms, 13 dits = 1560ms at 10 WPM
        assertEquals(720L, MorseTiming.interCharSpaceMs(wpm, 6));
        assertEquals(1560L, MorseTiming.wordSpaceMs(wpm, 13));

        // Nominal 3 dits and 7 dits at 20 WPM
        assertEquals(180L, MorseTiming.interCharSpaceMs(20, 3));
        assertEquals(420L, MorseTiming.wordSpaceMs(20, 7));
    }

    @Test
    public void testConfigurableInterLetterPauseEvaluation() {
        int wpm = 20; // dit = 60ms, with 5 dits spacing -> ideal = 300ms
        MorseTiming.PauseEvaluation evalGood = MorseTiming.evaluateLetterPause(300, wpm, 5);
        assertTrue(evalGood.isGood);
        assertFalse(evalGood.isTimingFailure);

        // Pause of 90ms against 300ms ideal is < 0.35x -> TIMING FAILURE
        MorseTiming.PauseEvaluation evalTooFast = MorseTiming.evaluateLetterPause(90, wpm, 5);
        assertTrue(evalTooFast.isTimingFailure);
    }
}
