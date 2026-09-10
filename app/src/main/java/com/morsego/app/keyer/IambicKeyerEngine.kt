package com.morsego.app.keyer

import com.morsego.app.model.KeyerMode
import com.morsego.app.model.KeyerSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Handles Iambic dual-paddle automatic timing (Mode A, Mode B) and Straight Key manual mode.
 */
class IambicKeyerEngine(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private var settings: KeyerSettings = KeyerSettings(),
    private val onToneStart: () -> Unit,
    private val onToneStop: () -> Unit,
    private val onElementSent: (Char) -> Unit
) {
    private var keyerJob: Job? = null

    @Volatile
    private var ditPressed = false

    @Volatile
    private var dahPressed = false

    @Volatile
    private var ditMemory = false

    @Volatile
    private var dahMemory = false

    fun updateSettings(newSettings: KeyerSettings) {
        settings = newSettings
    }

    fun onDitChanged(pressed: Boolean) {
        ditPressed = pressed
        if (pressed && settings.mode != KeyerMode.STRAIGHT_KEY) {
            ditMemory = true
            ensureEngineRunning()
        } else if (settings.mode == KeyerMode.STRAIGHT_KEY) {
            handleStraightKey()
        }
    }

    fun onDahChanged(pressed: Boolean) {
        dahPressed = pressed
        if (pressed && settings.mode != KeyerMode.STRAIGHT_KEY) {
            dahMemory = true
            ensureEngineRunning()
        } else if (settings.mode == KeyerMode.STRAIGHT_KEY) {
            handleStraightKey()
        }
    }

    private fun handleStraightKey() {
        if (ditPressed || dahPressed) {
            onToneStart()
        } else {
            onToneStop()
        }
    }

    private fun ensureEngineRunning() {
        if (keyerJob?.isActive == true) return

        keyerJob = scope.launch {
            var lastElement = ' '

            while (isActive) {
                val currentDit = ditPressed || ditMemory
                val currentDah = dahPressed || dahMemory

                if (!currentDit && !currentDah) {
                    break
                }

                val ditDuration = MorseTiming.ditDurationMs(settings.wpm)
                val dahDuration = MorseTiming.dahDurationMs(settings.wpm)
                val elementSpace = MorseTiming.intraCharSpaceMs(settings.wpm)

                // Decide which element to send next
                val sendDit: Boolean = when {
                    currentDit && currentDah -> {
                        // Squeeze keying alternates
                        lastElement != '.'
                    }
                    currentDit -> true
                    currentDah -> false
                    else -> true
                }

                if (sendDit) {
                    ditMemory = false
                    lastElement = '.'
                    onToneStart()
                    onElementSent('.')
                    delay(ditDuration)
                    onToneStop()
                } else {
                    dahMemory = false
                    lastElement = '-'
                    onToneStart()
                    onElementSent('-')
                    delay(dahDuration)
                    onToneStop()
                }

                // Intra-element spacing (1 Dit)
                // In Iambic B, paddle touches during this pause latch the next element
                val pauseStart = System.currentTimeMillis()
                while (System.currentTimeMillis() - pauseStart < elementSpace) {
                    if (ditPressed) ditMemory = true
                    if (dahPressed) dahMemory = true
                    delay(5)
                }

                // In Iambic A, memory is cleared if paddle was released
                if (settings.mode == KeyerMode.IAMBIC_A) {
                    if (!ditPressed) ditMemory = false
                    if (!dahPressed) dahMemory = false
                }
            }
        }
    }

    fun stop() {
        keyerJob?.cancel()
        ditMemory = false
        dahMemory = false
        onToneStop()
    }
}
