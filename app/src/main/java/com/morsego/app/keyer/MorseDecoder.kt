package com.morsego.app.keyer

import com.morsego.app.model.MorseDictionary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Decodes Morse dit/dah elements and timing into readable text in real-time.
 */
class MorseDecoder(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private var wpm: Int = 15,
    private val onCharDecoded: (Char) -> Unit = {}
) {
    private val _currentPattern = MutableStateFlow("")
    val currentPattern: StateFlow<String> = _currentPattern.asStateFlow()

    private val _decodedText = MutableStateFlow("")
    val decodedText: StateFlow<String> = _decodedText.asStateFlow()

    private var pauseWatcherJob: Job? = null
    private var lastElementTime = 0L

    // For manual / straight-key timing
    private var toneStartTime = 0L
    private var isToneActive = false

    fun setWpm(newWpm: Int) {
        wpm = newWpm.coerceIn(5, 50)
    }

    /**
     * Called when an element ('.' or '-') is emitted by the Iambic Engine or Keyer
     */
    fun onElementReceived(element: Char) {
        _currentPattern.value += element
        lastElementTime = System.currentTimeMillis()
        restartPauseWatcher()
    }

    /**
     * Called when tone turns on/off in straight key mode
     */
    fun onManualToneState(toneOn: Boolean) {
        val now = System.currentTimeMillis()
        if (toneOn && !isToneActive) {
            toneStartTime = now
            isToneActive = true
            pauseWatcherJob?.cancel()
        } else if (!toneOn && isToneActive) {
            isToneActive = false
            val duration = now - toneStartTime
            val ditDuration = MorseTiming.ditDurationMs(wpm)

            // If duration is longer than 2 dits, classify as dash, else dot
            val element = if (duration >= (ditDuration * 2.0)) '-' else '.'
            onElementReceived(element)
        }
    }

    private fun restartPauseWatcher() {
        pauseWatcherJob?.cancel()
        pauseWatcherJob = scope.launch {
            val charPause = MorseTiming.interCharSpaceMs(wpm)
            val wordPause = MorseTiming.wordSpaceMs(wpm)

            delay(charPause)
            if (isActive && _currentPattern.value.isNotEmpty()) {
                commitCharacter()
            }

            delay(wordPause - charPause)
            if (isActive && _decodedText.value.isNotEmpty() && !_decodedText.value.endsWith(" ")) {
                _decodedText.value += " "
            }
        }
    }

    private fun commitCharacter() {
        val pattern = _currentPattern.value
        _currentPattern.value = ""
        if (pattern.isEmpty()) return

        val char = MorseDictionary.getChar(pattern) ?: '?'
        _decodedText.value += char
        onCharDecoded(char[0])
    }

    fun clear() {
        pauseWatcherJob?.cancel()
        _currentPattern.value = ""
        _decodedText.value = ""
    }

    fun backspace() {
        if (_decodedText.value.isNotEmpty()) {
            _decodedText.value = _decodedText.value.dropLast(1)
        }
    }
}
