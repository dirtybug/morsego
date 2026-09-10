package com.morsego.app.keyer

import android.view.KeyEvent
import android.view.MotionEvent
import com.morsego.app.model.KeyerSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CalibrationTarget {
    NONE,
    DIT_PADDLE,
    DAH_PADDLE
}

data class HardwareLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String,
    val keyCode: Int,
    val keyName: String,
    val isDown: Boolean
)

/**
 * Manages physical input events from USB Type-C CW Keyer hardware (Amazon CW Trainer PCB,
 * Vband adapters, USB keyboards, and OTG mouse emulators) as well as on-screen touch paddles.
 */
class KeyerInputManager(
    private var settings: KeyerSettings = KeyerSettings()
) {
    private val _isDitPressed = MutableStateFlow(false)
    val isDitPressed: StateFlow<Boolean> = _isDitPressed.asStateFlow()

    private val _isDahPressed = MutableStateFlow(false)
    val isDahPressed: StateFlow<Boolean> = _isDahPressed.asStateFlow()

    private val _calibrationState = MutableStateFlow(CalibrationTarget.NONE)
    val calibrationState: StateFlow<CalibrationTarget> = _calibrationState.asStateFlow()

    private val _lastCapturedKey = MutableStateFlow<HardwareLogEntry?>(null)
    val lastCapturedKey: StateFlow<HardwareLogEntry?> = _lastCapturedKey.asStateFlow()

    private val _recentLogs = MutableStateFlow<List<HardwareLogEntry>>(emptyList())
    val recentLogs: StateFlow<List<HardwareLogEntry>> = _recentLogs.asStateFlow()

    fun updateSettings(newSettings: KeyerSettings) {
        settings = newSettings
    }

    fun startCalibration(target: CalibrationTarget) {
        _calibrationState.value = target
    }

    fun cancelCalibration() {
        _calibrationState.value = CalibrationTarget.NONE
    }

    /**
     * Intercepts hardware KeyEvents from MainActivity
     * Returns true if the key was consumed by MorseGo
     */
    fun handleKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val isDown = (event.action == KeyEvent.ACTION_DOWN)
        val isUp = (event.action == KeyEvent.ACTION_UP)

        if (!isDown && !isUp) return false

        val keyName = KeyEvent.keyCodeToString(keyCode)
        val log = HardwareLogEntry(
            eventType = if (isDown) "KEY_DOWN" else "KEY_UP",
            keyCode = keyCode,
            keyName = keyName,
            isDown = isDown
        )
        _lastCapturedKey.value = log
        addLog(log)

        // If calibrating, capture the keycode for the selected paddle
        if (_calibrationState.value != CalibrationTarget.NONE && isDown) {
            when (_calibrationState.value) {
                CalibrationTarget.DIT_PADDLE -> {
                    settings = settings.copy(ditKeyCode = keyCode)
                    _calibrationState.value = CalibrationTarget.NONE
                    return true
                }
                CalibrationTarget.DAH_PADDLE -> {
                    settings = settings.copy(dahKeyCode = keyCode)
                    _calibrationState.value = CalibrationTarget.NONE
                    return true
                }
                CalibrationTarget.NONE -> {}
            }
        }

        val ditKey = if (!settings.reversePaddles) settings.ditKeyCode else settings.dahKeyCode
        val dahKey = if (!settings.reversePaddles) settings.dahKeyCode else settings.ditKeyCode

        val isDit = keyCode == ditKey ||
                keyCode == settings.alternativeDitKeyCode ||
                (keyCode == KeyEvent.KEYCODE_CTRL_LEFT && !settings.reversePaddles) ||
                (keyCode == KeyEvent.KEYCODE_CTRL_RIGHT && settings.reversePaddles)

        val isDah = keyCode == dahKey ||
                keyCode == settings.alternativeDahKeyCode ||
                (keyCode == KeyEvent.KEYCODE_CTRL_RIGHT && !settings.reversePaddles) ||
                (keyCode == KeyEvent.KEYCODE_CTRL_LEFT && settings.reversePaddles)

        if (isDit) {
            _isDitPressed.value = isDown
            return true
        } else if (isDah) {
            _isDahPressed.value = isDown
            return true
        }

        // Support single straight key on Spacebar or Enter
        if (keyCode == KeyEvent.KEYCODE_SPACE || keyCode == KeyEvent.KEYCODE_ENTER) {
            _isDitPressed.value = isDown
            return true
        }

        return false
    }

    /**
     * Intercepts Generic Motion Events for devices operating in Mode 1 (Mouse emulation)
     */
    fun handleGenericMotionEvent(event: MotionEvent): Boolean {
        val buttonState = event.buttonState
        val isPrimary = (buttonState and MotionEvent.BUTTON_PRIMARY) != 0
        val isSecondary = (buttonState and MotionEvent.BUTTON_SECONDARY) != 0

        if (isPrimary || isSecondary) {
            val ditTarget = if (!settings.reversePaddles) isPrimary else isSecondary
            val dahTarget = if (!settings.reversePaddles) isSecondary else isPrimary

            _isDitPressed.value = ditTarget
            _isDahPressed.value = dahTarget

            val log = HardwareLogEntry(
                eventType = "MOUSE_BUTTON",
                keyCode = buttonState,
                keyName = if (isPrimary) "MOUSE_PRIMARY (Dit)" else "MOUSE_SECONDARY (Dah)",
                isDown = true
            )
            _lastCapturedKey.value = log
            addLog(log)
            return true
        } else {
            // Released
            _isDitPressed.value = false
            _isDahPressed.value = false
        }
        return false
    }

    /**
     * Direct touch input from on-screen buttons
     */
    fun setTouchDitPressed(pressed: Boolean) {
        val target = if (!settings.reversePaddles) pressed else _isDahPressed.value
        _isDitPressed.value = target
    }

    fun setTouchDahPressed(pressed: Boolean) {
        val target = if (!settings.reversePaddles) pressed else _isDitPressed.value
        _isDahPressed.value = target
    }

    private fun addLog(entry: HardwareLogEntry) {
        val current = _recentLogs.value.toMutableList()
        if (current.size >= 20) {
            current.removeAt(0)
        }
        current.add(entry)
        _recentLogs.value = current
    }
}
