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
        int wpm = 10; // dit = 120ms
        assertEquals(120L, MorseTiming.intraCharSpaceMs(wpm));
        assertEquals(720L, MorseTiming.interCharSpaceMs(wpm)); // default 6 dits = 720ms
        assertEquals(1560L, MorseTiming.wordSpaceMs(wpm));      // default 13 dits = 1560ms (1.560s)
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
        int wpm = 10; // default inter-letter space (6 dits) = 720ms
        // Ideal pause: 720ms -> ratio 1.0x (Good, no failure)
        MorseTiming.PauseEvaluation evalGood = MorseTiming.evaluateLetterPause(720, wpm);
        assertTrue(evalGood.isGood);
        assertFalse(evalGood.isTimingFailure);

        // Too fast letter pause: 200ms (< 0.35x = 252ms) -> TIMING FAILURE
        MorseTiming.PauseEvaluation evalTooFast = MorseTiming.evaluateLetterPause(200, wpm);
        assertFalse(evalTooFast.isGood);
        assertTrue(evalTooFast.isTimingFailure);

        // Generous pause of 1500ms (approx 2.1x) is accepted with generous tolerance
        MorseTiming.PauseEvaluation evalTolerated = MorseTiming.evaluateLetterPause(1500, wpm);
        assertFalse(evalTolerated.isTimingFailure);

        // Too long letter pause: 4000ms (> 5.0x = 3600ms) -> TIMING FAILURE
        MorseTiming.PauseEvaluation evalTooSlow = MorseTiming.evaluateLetterPause(4000, wpm);
        assertTrue(evalTooSlow.isTimingFailure);
    }

    @Test
    public void testConfigurableSpacesCalculations() {
        int wpm = 20; // dit = 60ms
        assertEquals(180L, MorseTiming.interCharSpaceMs(wpm, 3));
        assertEquals(240L, MorseTiming.interCharSpaceMs(wpm, 4));
        assertEquals(300L, MorseTiming.interCharSpaceMs(wpm, 5));
        assertEquals(360L, MorseTiming.interCharSpaceMs(wpm, 6));

        assertEquals(420L, MorseTiming.wordSpaceMs(wpm, 7));
        assertEquals(600L, MorseTiming.wordSpaceMs(wpm, 10));
        assertEquals(780L, MorseTiming.wordSpaceMs(wpm, 13));
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
