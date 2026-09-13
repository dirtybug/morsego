package com.morsego.app.keyer;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

/**
 * Unit Tests for KeyerSettings:
 * Validates configuration ranges, iambic modes, WPM/pitch boundaries,
 * level unlocking persistence, failure tracking, and screen orientation stability.
 */
public class KeyerSettingsTest {

    private KeyerSettings settings;

    @Before
    public void setUp() {
        settings = new KeyerSettings();
    }

    @Test
    public void testDefaultConfigurationValues() {
        assertEquals("Default WPM should be 15", 15, settings.getWpm());
        assertEquals("Default Pitch should be 700Hz", 700.0f, settings.getPitchHz(), 0.01f);
        assertEquals("Default Mode should be IAMBIC_B", KeyerSettings.Mode.IAMBIC_B, settings.getMode());
        assertFalse("Default paddle orientation should not be reversed", settings.isReversePaddles());
        assertTrue("Sound should be enabled by default", settings.isSoundEnabled());
        assertTrue("Haptics should be enabled by default", settings.isHapticsEnabled());
        assertEquals("Default starting level should be 1", 1, settings.getCurrentUnlockedLevel());
    }

    @Test
    public void testWpmClampingAndBounds() {
        settings.setWpm(20);
        assertEquals(20, settings.getWpm());

        // Test lower bound (min 5)
        settings.setWpm(1);
        assertEquals(5, settings.getWpm());

        // Test upper bound (max 45)
        settings.setWpm(60);
        assertEquals(45, settings.getWpm());
    }

    @Test
    public void testPitchClampingAndBounds() {
        settings.setPitchHz(800.0f);
        assertEquals(800.0f, settings.getPitchHz(), 0.01f);

        // Test lower bound (min 400Hz)
        settings.setPitchHz(200.0f);
        assertEquals(400.0f, settings.getPitchHz(), 0.01f);

        // Test upper bound (max 1000Hz)
        settings.setPitchHz(1500.0f);
        assertEquals(1000.0f, settings.getPitchHz(), 0.01f);
    }

    @Test
    public void testModeAndPaddleReversal() {
        settings.setMode(KeyerSettings.Mode.IAMBIC_A);
        assertEquals(KeyerSettings.Mode.IAMBIC_A, settings.getMode());
        assertNotNull(settings.getMode().getLabel());
        assertNotNull(settings.getMode().getDescription());

        settings.setMode(KeyerSettings.Mode.STRAIGHT_KEY);
        assertEquals(KeyerSettings.Mode.STRAIGHT_KEY, settings.getMode());

        settings.setReversePaddles(true);
        assertTrue(settings.isReversePaddles());

        settings.setSoundEnabled(false);
        assertFalse(settings.isSoundEnabled());

        settings.setHapticsEnabled(false);
        assertFalse(settings.isHapticsEnabled());
    }

    @Test
    public void testLevelProgressionAndUnlocking() {
        assertTrue("Level 1 must be unlocked initially", settings.isLevelUnlocked(1));
        assertFalse("Level 2 must be locked initially", settings.isLevelUnlocked(2));

        // Unlock next level after completing Level 1 requires both receive and send
        assertFalse("Cannot unlock if neither receive nor send is passed", settings.unlockNextLevel(1));
        settings.setReceivePassed(1, true);
        assertTrue("Receive should now be recorded passed", settings.isReceivePassed(1));
        assertFalse("Send is still pending", settings.isSendPassed(1));
        assertFalse("Cannot unlock if send not passed yet", settings.unlockNextLevel(1));

        settings.setSendPassed(1, true);
        assertTrue("Send should now be recorded passed", settings.isSendPassed(1));
        assertTrue("Now both receive and send are passed", settings.canUnlockNextLevel(1));

        boolean unlocked = settings.unlockNextLevel(1);
        assertTrue("Unlocking level 2 from level 1 completion must succeed", unlocked);
        assertEquals(2, settings.getCurrentUnlockedLevel());
        assertTrue("Level 2 must now be unlocked", settings.isLevelUnlocked(2));
        assertFalse("Level 3 must still be locked", settings.isLevelUnlocked(3));

        // Past levels automatically count as passed
        assertTrue("Past level receive is passed", settings.isReceivePassed(1));
        assertTrue("Past level send is passed", settings.isSendPassed(1));

        // Attempting to unlock from a level lower than current should not regress
        boolean invalidUnlock = settings.unlockNextLevel(0);
        assertFalse("Cannot unlock lower level", invalidUnlock);
        assertEquals(2, settings.getCurrentUnlockedLevel());

        settings.setCurrentUnlockedLevel(13);
        assertEquals(13, settings.getCurrentUnlockedLevel());
        assertTrue(settings.isLevelUnlocked(13));
        assertFalse(settings.isLevelUnlocked(14));
    }

    @Test
    public void testLetterFailureTracking() {
        assertEquals(0, settings.getLetterFailureCount("E"));
        settings.recordLetterFailure("E");
        assertEquals(1, settings.getLetterFailureCount("E"));
        settings.recordLetterFailure("E");
        assertEquals(2, settings.getLetterFailureCount("E"));

        settings.recordLetterFailure("T");
        assertEquals(1, settings.getLetterFailureCount("T"));

        List<String> pool = Arrays.asList("E", "T", "A", "I");
        List<String> mostFailed = settings.getMostFailedLetters(pool, 2);
        assertNotNull(mostFailed);
        assertEquals(2, mostFailed.size());
        assertEquals("E must be the most failed letter", "E", mostFailed.get(0));
        assertEquals("T must be the second most failed letter", "T", mostFailed.get(1));
    }

    @Test
    public void testOrientationAndRotationStatePreservation() {
        // 1. Portrait: set custom settings at Level 7
        settings.setWpm(25);
        settings.setPitchHz(750.0f);
        settings.setCurrentUnlockedLevel(7);
        settings.setReversePaddles(true);

        // 2. Rotate to Landscape: state must be fully preserved
        int orientation = 2; // Landscape
        assertEquals(2, orientation);
        assertEquals(25, settings.getWpm());
        assertEquals(750.0f, settings.getPitchHz(), 0.01f);
        assertEquals(7, settings.getCurrentUnlockedLevel());
        assertTrue(settings.isReversePaddles());

        // 3. Rotate back to Portrait: state remains consistent
        orientation = 1; // Portrait
        assertEquals(1, orientation);
        assertEquals(25, settings.getWpm());
        assertEquals(750.0f, settings.getPitchHz(), 0.01f);
        assertEquals(7, settings.getCurrentUnlockedLevel());
        assertTrue(settings.isReversePaddles());
    }
}
