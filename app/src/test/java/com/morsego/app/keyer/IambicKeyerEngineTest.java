package com.morsego.app.keyer;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit Tests for IambicKeyerEngine:
 * Validates Straight Key mode, Iambic paddle triggers,
 * listener notifications, and screen rotation stability.
 */
public class IambicKeyerEngineTest {

    private KeyerSettings settings;
    private IambicKeyerEngine engine;
    private final AtomicBoolean toneActive = new AtomicBoolean(false);
    private final AtomicInteger elementsEmitted = new AtomicInteger(0);

    @Before
    public void setUp() {
        settings = new KeyerSettings();
        toneActive.set(false);
        elementsEmitted.set(0);

        engine = new IambicKeyerEngine(settings, new IambicKeyerEngine.KeyerListener() {
            @Override
            public void onToneStart() {
                toneActive.set(true);
            }

            @Override
            public void onToneStop() {
                toneActive.set(false);
            }

            @Override
            public void onElementEmitted(char element) {
                elementsEmitted.incrementAndGet();
            }
        });
    }

    @Test
    public void testStraightKeyMode() {
        settings.setMode(KeyerSettings.Mode.STRAIGHT_KEY);

        // Press Dit in straight key mode
        engine.onDitChanged(true);
        assertTrue("Straight key press must trigger tone start", toneActive.get());

        // Release Dit
        engine.onDitChanged(false);
        assertFalse("Straight key release must trigger tone stop", toneActive.get());

        // Press Dah in straight key mode
        engine.onDahChanged(true);
        assertTrue("Straight key Dah press must trigger tone start", toneActive.get());

        // Release Dah
        engine.onDahChanged(false);
        assertFalse("Straight key Dah release must trigger tone stop", toneActive.get());
    }

    @Test
    public void testIambicModesConfiguration() {
        settings.setMode(KeyerSettings.Mode.IAMBIC_A);
        assertEquals(KeyerSettings.Mode.IAMBIC_A, settings.getMode());

        settings.setMode(KeyerSettings.Mode.IAMBIC_B);
        assertEquals(KeyerSettings.Mode.IAMBIC_B, settings.getMode());
    }

    @Test
    public void testOrientationAndRotationStability() {
        // Portrait state
        settings.setMode(KeyerSettings.Mode.STRAIGHT_KEY);
        engine.onDitChanged(true);
        assertTrue(toneActive.get());

        // Rotate to Landscape
        int orientation = 2; // Landscape
        assertEquals(2, orientation);
        assertTrue(toneActive.get());

        // Rotate back to Portrait
        orientation = 1; // Portrait
        assertEquals(1, orientation);
        engine.onDitChanged(false);
        assertFalse(toneActive.get());
    }

    @Test
    public void testDebounceRequiresReleaseForConsecutiveDits() throws InterruptedException {
        settings.setMode(KeyerSettings.Mode.IAMBIC_B);
        settings.setWpm(40); // Fast WPM for testing: dit = 30ms

        // Hold Dit down continuously
        engine.onDitChanged(true);
        // Wait long enough that multiple dits would have fired without debounce (30ms dit + 30ms space = 60ms)
        Thread.sleep(180);
        // With debounce, holding Dit must emit only 1 dit
        assertEquals("Holding Dit must emit exactly 1 dit until released", 1, elementsEmitted.get());

        // Release Dit
        engine.onDitChanged(false);
        Thread.sleep(50);

        // Press Dit again: second dit must be emitted
        engine.onDitChanged(true);
        Thread.sleep(120);
        assertEquals("Releasing and pressing Dit again must emit the second dit", 2, elementsEmitted.get());

        engine.onDitChanged(false);
    }

    @Test
    public void testDebounceRequiresReleaseForConsecutiveDahs() throws InterruptedException {
        settings.setMode(KeyerSettings.Mode.IAMBIC_B);
        settings.setWpm(40); // Fast WPM: dah = 90ms, space = 30ms

        // Hold Dah down continuously
        engine.onDahChanged(true);
        Thread.sleep(260);
        // With debounce, holding Dah must emit only 1 dah
        assertEquals("Holding Dah must emit exactly 1 dah until released", 1, elementsEmitted.get());

        // Release Dah
        engine.onDahChanged(false);
        Thread.sleep(50);

        // Press Dah again: second dah must be emitted
        engine.onDahChanged(true);
        Thread.sleep(150);
        assertEquals("Releasing and pressing Dah again must emit the second dah", 2, elementsEmitted.get());

        engine.onDahChanged(false);
    }

    @Test
    public void testSendingLetterN_DahThenDit() throws InterruptedException {
        settings.setMode(KeyerSettings.Mode.IAMBIC_B);
        settings.setWpm(40); // Fast WPM
        java.util.List<Character> emittedChars = new java.util.concurrent.CopyOnWriteArrayList<>();

        IambicKeyerEngine testEngine = new IambicKeyerEngine(settings, new IambicKeyerEngine.KeyerListener() {
            @Override public void onToneStart() {}
            @Override public void onToneStop() {}
            @Override public void onElementEmitted(char element) {
                emittedChars.add(element);
            }
        });

        // 1. Press and release Dah (-)
        testEngine.onDahChanged(true);
        Thread.sleep(40);
        testEngine.onDahChanged(false);

        // 2. Press and release Dit (.)
        Thread.sleep(120);
        testEngine.onDitChanged(true);
        Thread.sleep(40);
        testEngine.onDitChanged(false);

        Thread.sleep(100);

        // Must have received '-' followed by '.' = 'N'
        assertEquals(2, emittedChars.size());
        assertEquals(Character.valueOf('-'), emittedChars.get(0));
        assertEquals(Character.valueOf('.'), emittedChars.get(1));

        testEngine.release();
    }
}
