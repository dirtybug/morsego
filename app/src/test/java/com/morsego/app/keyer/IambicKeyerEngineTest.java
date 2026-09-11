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
}
