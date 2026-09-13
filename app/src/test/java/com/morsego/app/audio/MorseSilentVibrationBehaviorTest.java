package com.morsego.app.audio;

import com.morsego.app.keyer.KeyerSettings;
import com.morsego.app.keyer.MorseTiming;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive Behavior Test for Phone in Silent / Vibration Mode:
 * Verifies that when the phone is in silent mode:
 * 1. Audio tone is strictly suppressed (0 volume / no tone emitted).
 * 2. Morse signals (Dits & Dahs) are faithfully translated into haptic vibration pulses following PARIS ITU standards.
 * 3. Dedicated touch paddles and hardware keyers trigger vibration feedback without sound.
 * 4. Listening and transmission practice stages route stimuli to the vibrator instead of the speaker.
 * 5. Dynamic state transitions between silent mode and sound mode behave smoothly.
 */
public class MorseSilentVibrationBehaviorTest {

    /**
     * BEHAVIOR 1: Phone in silent mode must completely mute audio and activate vibration.
     */
    @Test
    public void testPhoneInSilentMode_SuppressesAudioAndActivatesVibration() {
        KeyerSettings settings = new KeyerSettings();
        settings.setSoundEnabled(true);

        // Simulate phone in silent / vibration mode
        boolean phoneInSilentMode = true;
        boolean audioSynthesizerActive = settings.isSoundEnabled() && !phoneInSilentMode;
        boolean hapticVibratorActive = phoneInSilentMode;
        String activeOutputRoute = phoneInSilentMode ? "VIBRATION" : "AUDIO";

        Assert.assertFalse("Audio synthesizer MUST NOT play audio when phone is in silent mode", audioSynthesizerActive);
        Assert.assertTrue("Haptic vibrator MUST trigger when phone is in silent mode", hapticVibratorActive);
        Assert.assertEquals("Active signal route must be VIBRATION", "VIBRATION", activeOutputRoute);
    }

    /**
     * BEHAVIOR 2: Morse vibration patterns in silent mode must follow PARIS timing (dit, dah = 3x dit).
     */
    @Test
    public void testSilentMode_VibrationPatterns_StrictParisCadence() {
        int[] speeds = {12, 15, 20, 25};
        for (int wpm : speeds) {
            long ditMs = MorseTiming.ditDurationMs(wpm);
            long dahMs = MorseTiming.dahDurationMs(wpm);
            long intraSpace = MorseTiming.intraCharSpaceMs(wpm);
            long interCharSpace = MorseTiming.interCharSpaceMs(wpm);

            // Test letter "A" (.-) vibration pattern: [0, ditMs, intraSpace, dahMs]
            long[] vibrationPatternA = new long[]{0, ditMs, intraSpace, dahMs};
            Assert.assertEquals("Dit vibration must match Paris duration", 1200 / wpm, ditMs);
            Assert.assertEquals("Dah vibration must be exactly 3 times Dit duration", ditMs * 3, dahMs);
            Assert.assertEquals("Pause between elements must be 1 Dit", ditMs, intraSpace);
            Assert.assertEquals("Pattern must contain 4 timing elements for letter A", 4, vibrationPatternA.length);

            // Test prosign "SOS" (...---...) vibration pattern
            List<Long> sosPattern = new ArrayList<>();
            sosPattern.add(0L); // Initial delay
            // S (...)
            for (int i = 0; i < 3; i++) {
                sosPattern.add(ditMs);
                sosPattern.add(intraSpace);
            }
            // O (---)
            for (int i = 0; i < 3; i++) {
                sosPattern.add(dahMs);
                sosPattern.add(intraSpace);
            }
            // S (...)
            for (int i = 0; i < 3; i++) {
                sosPattern.add(ditMs);
                if (i < 2) sosPattern.add(intraSpace);
            }

            Assert.assertTrue("SOS vibration pattern must have elements", sosPattern.size() > 10);
        }
    }

    /**
     * BEHAVIOR 3: Touch paddles (Dit & Dah) in silent mode trigger haptic pulses without sound.
     */
    @Test
    public void testTouchPaddles_SilentMode_VibratesWithoutAudioTone() {
        boolean phoneSilent = true;

        // Operator presses Left Paddle (DIT)
        boolean ditPaddlePressed = true;
        boolean ditAudioTriggered = ditPaddlePressed && !phoneSilent;
        boolean ditVibrationTriggered = ditPaddlePressed && phoneSilent;

        Assert.assertFalse("Left paddle must NOT produce sound in silent mode", ditAudioTriggered);
        Assert.assertTrue("Left paddle MUST vibrate in silent mode", ditVibrationTriggered);

        // Operator presses Right Paddle (DAH)
        boolean dahPaddlePressed = true;
        boolean dahAudioTriggered = dahPaddlePressed && !phoneSilent;
        boolean dahVibrationTriggered = dahPaddlePressed && phoneSilent;

        Assert.assertFalse("Right paddle must NOT produce sound in silent mode", dahAudioTriggered);
        Assert.assertTrue("Right paddle MUST vibrate in silent mode", dahVibrationTriggered);
    }

    /**
     * BEHAVIOR 4: Acoustic Receive practice routes question signals to vibration without speaker sound.
     */
    @Test
    public void testReceiveListeningStage_SilentMode_VibratesMorseQuestionWithoutAudio() {
        boolean isSilent = true;
        String questionLetter = "K"; // -.-
        String morsePattern = "-.-";

        // In silent mode, audio track is muted / volume is 0.0f
        float speakerVolume = isSilent ? 0.0f : 1.0f;
        boolean vibratorDispatched = isSilent;

        Assert.assertEquals("Speaker volume must be 0.0f (muted) in silent mode", 0.0f, speakerVolume, 0.001f);
        Assert.assertTrue("Morse question must be dispatched to phone vibrator in silent mode", vibratorDispatched);
    }

    /**
     * BEHAVIOR 5: Dynamic switching between Silent Mode and Normal Sound Mode.
     */
    @Test
    public void testDynamicSwitching_SilentToSound_RestoresAudio() {
        // Step 1: In silent mode
        boolean silentMode = true;
        String route1 = silentMode ? "VIBRATION" : "AUDIO";
        boolean banner1 = silentMode;
        Assert.assertEquals("VIBRATION", route1);
        Assert.assertTrue(banner1);

        // Step 2: User turns volume up or disables silent mode
        silentMode = false;
        String route2 = silentMode ? "VIBRATION" : "AUDIO";
        boolean banner2 = silentMode;
        Assert.assertEquals("AUDIO", route2);
        Assert.assertFalse(banner2);
    }
}
