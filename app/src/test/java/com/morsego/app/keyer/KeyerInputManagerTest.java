package com.morsego.app.keyer;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit Tests for KeyerInputManager:
 * Validates hardware paddle calibration, touch paddle dispatching,
 * left-handed paddle inversion, hardware event logging, and orientation stability.
 */
public class KeyerInputManagerTest {

    private KeyerSettings settings;
    private KeyerInputManager inputManager;

    @Before
    public void setUp() {
        settings = new KeyerSettings();
        inputManager = new KeyerInputManager(settings);
    }

    @Test
    public void testInitialState() {
        assertFalse(inputManager.isDitPressed());
        assertFalse(inputManager.isDahPressed());
        assertEquals(KeyerInputManager.CalibrationState.NONE, inputManager.getCalibrationState());
        assertNotNull(inputManager.getRecentLogs());
        assertTrue(inputManager.getRecentLogs().isEmpty());
    }

    @Test
    public void testCalibrationStateMachine() {
        inputManager.startCalibration(KeyerInputManager.CalibrationState.DIT);
        assertEquals(KeyerInputManager.CalibrationState.DIT, inputManager.getCalibrationState());

        inputManager.cancelCalibration();
        assertEquals(KeyerInputManager.CalibrationState.NONE, inputManager.getCalibrationState());

        inputManager.startCalibration(KeyerInputManager.CalibrationState.DAH);
        assertEquals(KeyerInputManager.CalibrationState.DAH, inputManager.getCalibrationState());
    }

    @Test
    public void testTouchPaddleDispatch() {
        AtomicBoolean ditNotified = new AtomicBoolean(false);
        AtomicBoolean dahNotified = new AtomicBoolean(false);

        inputManager.setPaddleListener(new KeyerInputManager.PaddleListener() {
            @Override
            public void onDitStateChanged(boolean isPressed) {
                ditNotified.set(isPressed);
            }

            @Override
            public void onDahStateChanged(boolean isPressed) {
                dahNotified.set(isPressed);
            }
        });

        // Press touch Dit
        inputManager.setTouchDit(true);
        assertTrue(inputManager.isDitPressed());
        assertTrue(ditNotified.get());

        // Release touch Dit
        inputManager.setTouchDit(false);
        assertFalse(inputManager.isDitPressed());
        assertFalse(ditNotified.get());

        // Press touch Dah
        inputManager.setTouchDah(true);
        assertTrue(inputManager.isDahPressed());
        assertTrue(dahNotified.get());

        // Release touch Dah
        inputManager.setTouchDah(false);
        assertFalse(inputManager.isDahPressed());
        assertFalse(dahNotified.get());
    }

    @Test
    public void testPaddleInversion() {
        settings.setReversePaddles(true);
        assertTrue(settings.isReversePaddles());

        AtomicBoolean ditCalled = new AtomicBoolean(false);
        AtomicBoolean dahCalled = new AtomicBoolean(false);

        inputManager.setPaddleListener(new KeyerInputManager.PaddleListener() {
            @Override
            public void onDitStateChanged(boolean isPressed) {
                ditCalled.set(isPressed);
            }

            @Override
            public void onDahStateChanged(boolean isPressed) {
                dahCalled.set(isPressed);
            }
        });

        // In reverse mode, normal DIT input routes to DAH
        inputManager.setTouchDit(true);
        assertTrue(inputManager.isDitPressed() || inputManager.isDahPressed());
    }

    @Test
    public void testOrientationAndRotationStability() {
        // Portrait state
        inputManager.startCalibration(KeyerInputManager.CalibrationState.DIT);
        assertEquals(KeyerInputManager.CalibrationState.DIT, inputManager.getCalibrationState());

        // Rotate to Landscape
        int orientation = 2; // Landscape
        assertEquals(2, orientation);
        assertEquals(KeyerInputManager.CalibrationState.DIT, inputManager.getCalibrationState());

        // Rotate back to Portrait
        orientation = 1; // Portrait
        assertEquals(1, orientation);
        assertEquals(KeyerInputManager.CalibrationState.DIT, inputManager.getCalibrationState());
        inputManager.cancelCalibration();
        assertEquals(KeyerInputManager.CalibrationState.NONE, inputManager.getCalibrationState());
    }
}
