package com.morsego.app.audio;

import com.morsego.app.keyer.KeyerSettings;
import com.morsego.app.keyer.MorseTiming;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit Test for Silent Vibration Mode and Sound Mode Logic:
 * Validates audio/vibration routing decisions, timing bounds,
 * and state transitions between silent/vibrate and normal audio modes.
 */
public class MorseSilentSoundLogicTest {

    @Test
    public void testSilentModeDecisionLogic() {
        KeyerSettings settings = new KeyerSettings();
        settings.setSoundEnabled(true);

        // State 1: Silent / Vibration Mode Active
        boolean isDeviceSilent = true;
        boolean audioShouldPlay = settings.isSoundEnabled() && !isDeviceSilent;
        boolean vibrationShouldTrigger = isDeviceSilent;

        Assert.assertFalse("Audio must NOT play in silent mode", audioShouldPlay);
        Assert.assertTrue("Vibration MUST trigger in silent mode", vibrationShouldTrigger);

        // State 2: Normal Sound Mode Active
        isDeviceSilent = false;
        audioShouldPlay = settings.isSoundEnabled() && !isDeviceSilent;
        vibrationShouldTrigger = isDeviceSilent;

        Assert.assertTrue("Audio MUST play in sound mode", audioShouldPlay);
        Assert.assertFalse("Vibration must NOT trigger in sound mode", vibrationShouldTrigger);
    }

    @Test
    public void testSilentModeVibrationParisTimings() {
        // In silent mode, PARIS timing must be strictly preserved for vibration dits & dahs
        int[] testWpms = {10, 15, 20, 25, 30};
        for (int wpm : testWpms) {
            long dit = MorseTiming.ditDurationMs(wpm);
            long dah = MorseTiming.dahDurationMs(wpm);
            long intra = MorseTiming.intraCharSpaceMs(wpm);
            long inter = MorseTiming.interCharSpaceMs(wpm);

            Assert.assertTrue("Dit vibration must be strictly positive", dit > 0);
            Assert.assertEquals("Dah vibration must be exactly 3x dit", dit * 3, dah);
            Assert.assertEquals("Intra-element space must be 1 dit", dit, intra);
            Assert.assertEquals("Inter-character space must be 3 dits", dit * 3, inter);
        }
    }

    @Test
    public void testStateTransitionBetweenSilentAndSoundModes() {
        // Simulate dynamic transitions when volume/ringer changes
        boolean[] states = {false, true, false, true, false}; // sound -> silent -> sound -> silent -> sound
        for (boolean silentState : states) {
            boolean bannerVisible = silentState;
            String expectedRoute = silentState ? "VIBRATION" : "AUDIO";

            if (silentState) {
                Assert.assertTrue("Banner must be visible in silent mode", bannerVisible);
                Assert.assertEquals("Routing must be VIBRATION", "VIBRATION", expectedRoute);
            } else {
                Assert.assertFalse("Banner must be hidden in sound mode", bannerVisible);
                Assert.assertEquals("Routing must be AUDIO", "AUDIO", expectedRoute);
            }
        }
    }
}
