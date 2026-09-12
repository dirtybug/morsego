package com.morsego.app.keyer;

import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

public class KeyerInputManager {

    public enum CalibrationState {
        NONE,
        DIT,
        DAH
    }

    public static class HardwareLog {
        public final long timestamp;
        public final String type;
        public final int keyCode;
        public final String name;
        public final boolean isDown;

        public HardwareLog(String type, int keyCode, String name, boolean isDown) {
            this.timestamp = System.currentTimeMillis();
            this.type = type;
            this.keyCode = keyCode;
            this.name = name;
            this.isDown = isDown;
        }
    }

    public interface PaddleListener {
        void onDitStateChanged(boolean isPressed);
        void onDahStateChanged(boolean isPressed);
    }

    private final KeyerSettings settings;
    private PaddleListener listener;
    private CalibrationState calibrationState = CalibrationState.NONE;

    private boolean isDitPressed = false;
    private boolean isDahPressed = false;

    private final List<HardwareLog> recentLogs = new ArrayList<>();
    private Runnable onLogUpdated;

    public KeyerInputManager(KeyerSettings settings) {
        this.settings = settings;
    }

    public void setPaddleListener(PaddleListener listener) {
        this.listener = listener;
    }

    public void setOnLogUpdated(Runnable onLogUpdated) {
        this.onLogUpdated = onLogUpdated;
    }

    public void startCalibration(CalibrationState state) {
        this.calibrationState = state;
    }

    public void cancelCalibration() {
        this.calibrationState = CalibrationState.NONE;
    }

    public CalibrationState getCalibrationState() {
        return calibrationState;
    }

    public List<HardwareLog> getRecentLogs() {
        return new ArrayList<>(recentLogs);
    }

    public boolean isDitPressed() {
        return isDitPressed;
    }

    public boolean isDahPressed() {
        return isDahPressed;
    }

    /**
     * Intercepts hardware KeyEvents from MainActivity
     */
    public boolean handleKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        boolean isDown = (event.getAction() == KeyEvent.ACTION_DOWN);
        boolean isUp = (event.getAction() == KeyEvent.ACTION_UP);

        if (!isDown && !isUp) return false;

        String keyName = KeyEvent.keyCodeToString(keyCode);
        addLog(new HardwareLog(isDown ? "KEY_DOWN" : "KEY_UP", keyCode, keyName, isDown));

        // Calibration handling
        if (calibrationState != CalibrationState.NONE && isDown) {
            if (calibrationState == CalibrationState.DIT) {
                settings.setDitKeyCode(keyCode);
            } else if (calibrationState == CalibrationState.DAH) {
                settings.setDahKeyCode(keyCode);
            }
            calibrationState = CalibrationState.NONE;
            return true;
        }

        int targetDitKey = !settings.isReversePaddles() ? settings.getDitKeyCode() : settings.getDahKeyCode();
        int targetDahKey = !settings.isReversePaddles() ? settings.getDahKeyCode() : settings.getDitKeyCode();

        boolean isDit = (keyCode == targetDitKey) ||
                (keyCode == KeyEvent.KEYCODE_CTRL_LEFT && !settings.isReversePaddles()) ||
                (keyCode == KeyEvent.KEYCODE_CTRL_RIGHT && settings.isReversePaddles());

        boolean isDah = (keyCode == targetDahKey) ||
                (keyCode == KeyEvent.KEYCODE_CTRL_RIGHT && !settings.isReversePaddles()) ||
                (keyCode == KeyEvent.KEYCODE_CTRL_LEFT && settings.isReversePaddles());

        if (isDit) {
            if (isDitPressed != isDown) {
                isDitPressed = isDown;
                if (listener != null) listener.onDitStateChanged(isDown);
            }
            return true;
        } else if (isDah) {
            if (isDahPressed != isDown) {
                isDahPressed = isDown;
                if (listener != null) listener.onDahStateChanged(isDown);
            }
            return true;
        }

        // Spacebar or Enter support for straight-key manual keying
        if (keyCode == KeyEvent.KEYCODE_SPACE || keyCode == KeyEvent.KEYCODE_ENTER) {
            if (isDitPressed != isDown) {
                isDitPressed = isDown;
                if (listener != null) listener.onDitStateChanged(isDown);
            }
            return true;
        }

        return false;
    }

    /**
     * Intercepts Generic Motion Events for devices operating in Mode 1 (Mouse emulation)
     */
    public boolean handleGenericMotionEvent(MotionEvent event) {
        int buttonState = event.getButtonState();
        boolean isPrimary = (buttonState & MotionEvent.BUTTON_PRIMARY) != 0;
        boolean isSecondary = (buttonState & MotionEvent.BUTTON_SECONDARY) != 0;

        if (isPrimary || isSecondary) {
            boolean ditTarget = !settings.isReversePaddles() ? isPrimary : isSecondary;
            boolean dahTarget = !settings.isReversePaddles() ? isSecondary : isPrimary;

            if (isDitPressed != ditTarget) {
                isDitPressed = ditTarget;
                if (listener != null) listener.onDitStateChanged(ditTarget);
            }
            if (isDahPressed != dahTarget) {
                isDahPressed = dahTarget;
                if (listener != null) listener.onDahStateChanged(dahTarget);
            }

            addLog(new HardwareLog("MOUSE", buttonState,
                    isPrimary ? "PRIMARY_BTN (Dit)" : "SECONDARY_BTN (Dah)", true));
            return true;
        } else {
            if (isDitPressed) {
                isDitPressed = false;
                if (listener != null) listener.onDitStateChanged(false);
            }
            if (isDahPressed) {
                isDahPressed = false;
                if (listener != null) listener.onDahStateChanged(false);
            }
        }
        return false;
    }

    /**
     * On-screen touch buttons
     */
    public void setTouchDit(boolean pressed) {
        if (!settings.isReversePaddles()) {
            if (isDitPressed != pressed) {
                isDitPressed = pressed;
                if (listener != null) listener.onDitStateChanged(pressed);
            }
        } else {
            if (isDahPressed != pressed) {
                isDahPressed = pressed;
                if (listener != null) listener.onDahStateChanged(pressed);
            }
        }
    }

    public void setTouchDah(boolean pressed) {
        if (!settings.isReversePaddles()) {
            if (isDahPressed != pressed) {
                isDahPressed = pressed;
                if (listener != null) listener.onDahStateChanged(pressed);
            }
        } else {
            if (isDitPressed != pressed) {
                isDitPressed = pressed;
                if (listener != null) listener.onDitStateChanged(pressed);
            }
        }
    }

    private void addLog(HardwareLog log) {
        if (recentLogs.size() >= 20) {
            recentLogs.remove(0);
        }
        recentLogs.add(log);
        if (onLogUpdated != null) {
            onLogUpdated.run();
        }
    }
}
