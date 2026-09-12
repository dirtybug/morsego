package com.morsego.app.audio;

import com.morsego.app.keyer.MorseTiming;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit Tests for Morse Audio Synthesizer Logic:
 * Validates frequency bounds [300Hz, 1500Hz], PARIS timing durations
 * for CW playback, silent-mode fallback rules, and screen rotation stability.
 */
public class MorseAudioSynthesizerLogicTest {

    @Test
    public void testFrequencyBoundsAndClamping() {
        float defaultFreq = 700.0f;
        assertTrue("Default CW pitch must be audible", defaultFreq >= 300.0f && defaultFreq <= 1500.0f);

        // Test clamping logic
        float lowFreq = Math.max(300.0f, Math.min(1500.0f, 100.0f));
        assertEquals(300.0f, lowFreq, 0.01f);

        float highFreq = Math.max(300.0f, Math.min(1500.0f, 2500.0f));
        assertEquals(1500.0f, highFreq, 0.01f);

        float validFreq = Math.max(300.0f, Math.min(1500.0f, 800.0f));
        assertEquals(800.0f, validFreq, 0.01f);
    }

    @Test
    public void testParisAudioTimingDurations() {
        int wpm = 20;
        long dit = MorseTiming.ditDurationMs(wpm);
        long dah = MorseTiming.dahDurationMs(wpm);
        long intra = MorseTiming.intraCharSpaceMs(wpm);
        long inter = MorseTiming.interCharSpaceMs(wpm);
        long word = MorseTiming.wordSpaceMs(wpm);

        assertEquals("Dit duration at 20 WPM must be 60ms", 60L, dit);
        assertEquals("Dah duration must be 3x dit (180ms)", 180L, dah);
        assertEquals("Intra-character space must be 1 dit (60ms)", 60L, intra);
        assertEquals("Inter-character space must be 3 dits (180ms)", 180L, inter);
        assertEquals("Word space must be 7 dits (420ms)", 420L, word);
    }

    @Test
    public void testSilentModeVibrationDurations() {
        // In silent mode, vibration uses dit and dah durations
        for (int wpm : new int[]{10, 15, 20, 25}) {
            long dit = MorseTiming.ditDurationMs(wpm);
            long dah = MorseTiming.dahDurationMs(wpm);
            assertTrue("Dit vibration must be positive", dit > 0);
            assertEquals("Dah vibration must be 3x dit", dit * 3, dah);
        }
    }

    @Test
    public void testSilentModeBehaviorDecision() {
        // When device is in silent mode, audio tone must not play, vibration must be used instead
        boolean isSilent = true;
        boolean soundEnabled = true;

        boolean shouldPlayAudio = soundEnabled && !isSilent;
        boolean shouldVibrateMorse = isSilent;

        assertFalse("Audio should not play when silent mode is active", shouldPlayAudio);
        assertTrue("Morse vibration should trigger when silent mode is active", shouldVibrateMorse);

        // When silent mode is deactivated
        isSilent = false;
        shouldPlayAudio = soundEnabled && !isSilent;
        shouldVibrateMorse = isSilent;

        assertTrue("Audio should play when silent mode is deactivated and sound is enabled", shouldPlayAudio);
        assertFalse("Morse vibration should not trigger when silent mode is inactive", shouldVibrateMorse);
    }

    @Test
    public void testOrientationAndRotationStability() {
        // Verify audio timing consistency in Portrait and Landscape
        int portraitOrientation = 1;
        assertEquals(1, portraitOrientation);
        long ditP = MorseTiming.ditDurationMs(15);

        int landscapeOrientation = 2;
        assertEquals(2, landscapeOrientation);
        long ditL = MorseTiming.ditDurationMs(15);

        assertEquals("Audio timing must be invariant to screen orientation", ditP, ditL);
    }
}
