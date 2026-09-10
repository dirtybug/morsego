package com.morsego.app.viewmodel

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morsego.app.audio.MorseAudioSynthesizer
import com.morsego.app.keyer.CalibrationTarget
import com.morsego.app.keyer.IambicKeyerEngine
import com.morsego.app.keyer.KeyerInputManager
import com.morsego.app.keyer.MorseDecoder
import com.morsego.app.keyer.MorseTiming
import com.morsego.app.model.KeyerMode
import com.morsego.app.model.KeyerSettings
import com.morsego.app.model.KochLesson
import com.morsego.app.model.KochMethod
import com.morsego.app.model.MorseDictionary
import com.morsego.app.model.UserProgress
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MorseViewModel : ViewModel() {

    private val synthesizer = MorseAudioSynthesizer()
    val inputManager = KeyerInputManager()

    private val _settings = MutableStateFlow(KeyerSettings())
    val settings: StateFlow<KeyerSettings> = _settings.asStateFlow()

    private val _progress = MutableStateFlow(UserProgress())
    val progress: StateFlow<UserProgress> = _progress.asStateFlow()

    private val _isTonePlaying = MutableStateFlow(false)
    val isTonePlaying: StateFlow<Boolean> = _isTonePlaying.asStateFlow()

    private val _lastSentElement = MutableStateFlow<Char?>(null)
    val lastSentElement: StateFlow<Char?> = _lastSentElement.asStateFlow()

    val decoder = MorseDecoder(
        scope = viewModelScope,
        wpm = _settings.value.wpm,
        onCharDecoded = { char ->
            handleCharDecoded(char)
        }
    )

    private val iambicEngine = IambicKeyerEngine(
        scope = viewModelScope,
        settings = _settings.value,
        onToneStart = {
            if (_settings.value.soundEnabled) {
                synthesizer.startTone()
            }
            _isTonePlaying.value = true
        },
        onToneStop = {
            synthesizer.stopTone()
            _isTonePlaying.value = false
        },
        onElementSent = { elem ->
            _lastSentElement.value = elem
            decoder.onElementReceived(elem)
        }
    )

    private var vibrator: Vibrator? = null
    private var playbackJob: Job? = null

    // Koch Practice State
    val kochLessons: List<KochLesson> = KochMethod.getLessons()
    private val _selectedLessonIndex = MutableStateFlow(0)
    val selectedLessonIndex: StateFlow<Int> = _selectedLessonIndex.asStateFlow()

    private val _currentPromptChar = MutableStateFlow("K")
    val currentPromptChar: StateFlow<String> = _currentPromptChar.asStateFlow()

    private val _practiceFeedback = MutableStateFlow<String?>(null)
    val practiceFeedback: StateFlow<String?> = _practiceFeedback.asStateFlow()

    private val _isPracticeCorrect = MutableStateFlow<Boolean?>(null)
    val isPracticeCorrect: StateFlow<Boolean?> = _isPracticeCorrect.asStateFlow()

    init {
        // Observe physical paddle states from inputManager
        viewModelScope.launch {
            inputManager.isDitPressed.collect { pressed ->
                if (_settings.value.mode == KeyerMode.STRAIGHT_KEY) {
                    decoder.onManualToneState(pressed)
                }
                iambicEngine.onDitChanged(pressed)
                if (pressed) triggerHaptic()
            }
        }

        viewModelScope.launch {
            inputManager.isDahPressed.collect { pressed ->
                if (_settings.value.mode == KeyerMode.STRAIGHT_KEY) {
                    decoder.onManualToneState(pressed)
                }
                iambicEngine.onDahChanged(pressed)
                if (pressed) triggerHaptic()
            }
        }
    }

    fun initSystemServices(context: Context) {
        if (vibrator == null) {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        }
    }

    private fun triggerHaptic() {
        if (!_settings.value.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        } catch (_: Exception) {}
    }

    fun setWpm(newWpm: Int) {
        val updated = _settings.value.copy(wpm = newWpm)
        _settings.value = updated
        inputManager.updateSettings(updated)
        iambicEngine.updateSettings(updated)
        decoder.setWpm(newWpm)
    }

    fun setTonePitch(pitchHz: Float) {
        val updated = _settings.value.copy(pitchHz = pitchHz)
        _settings.value = updated
        synthesizer.setFrequency(pitchHz)
    }

    fun setKeyerMode(mode: KeyerMode) {
        val updated = _settings.value.copy(mode = mode)
        _settings.value = updated
        iambicEngine.updateSettings(updated)
    }

    fun toggleReversePaddles() {
        val updated = _settings.value.copy(reversePaddles = !_settings.value.reversePaddles)
        _settings.value = updated
        inputManager.updateSettings(updated)
    }

    fun toggleSound() {
        val updated = _settings.value.copy(soundEnabled = !_settings.value.soundEnabled)
        _settings.value = updated
    }

    fun toggleHaptics() {
        val updated = _settings.value.copy(hapticsEnabled = !_settings.value.hapticsEnabled)
        _settings.value = updated
    }

    // Forward hardware events
    fun handleKeyEvent(event: KeyEvent): Boolean {
        return inputManager.handleKeyEvent(event)
    }

    fun handleGenericMotionEvent(event: MotionEvent): Boolean {
        return inputManager.handleGenericMotionEvent(event)
    }

    // Touch paddle presses
    fun setTouchDit(pressed: Boolean) {
        inputManager.setTouchDitPressed(pressed)
    }

    fun setTouchDah(pressed: Boolean) {
        inputManager.setTouchDahPressed(pressed)
    }

    fun selectLesson(index: Int) {
        if (index in kochLessons.indices) {
            _selectedLessonIndex.value = index
            val lesson = kochLessons[index]
            _currentPromptChar.value = lesson.allCharacters.random()
            _practiceFeedback.value = null
            _isPracticeCorrect.value = null
        }
    }

    fun nextPracticePrompt() {
        val lesson = kochLessons[_selectedLessonIndex.value]
        _currentPromptChar.value = lesson.allCharacters.random()
        _practiceFeedback.value = null
        _isPracticeCorrect.value = null
    }

    private fun handleCharDecoded(char: Char) {
        val prompt = _currentPromptChar.value
        val isCorrect = char.uppercaseChar() == prompt.uppercase().firstOrNull()

        _progress.value = _progress.value.copy(
            totalCharactersKeyed = _progress.value.totalCharactersKeyed + 1,
            correctKeyedCount = _progress.value.correctKeyedCount + (if (isCorrect) 1 else 0)
        )

        _isPracticeCorrect.value = isCorrect
        _practiceFeedback.value = if (isCorrect) {
            "✓ Correto! Transmitiu '$char' perfeitamente."
        } else {
            "✗ Incorreto: Transmitiu '$char' mas era esperado '$prompt'."
        }
    }

    /**
     * Plays a single character or Morse pattern audio through synthesizer with proper PARIS timing
     */
    fun playMorsePattern(pattern: String) {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val dit = MorseTiming.ditDurationMs(_settings.value.wpm)
            val dah = MorseTiming.dahDurationMs(_settings.value.wpm)
            val intra = MorseTiming.intraCharSpaceMs(_settings.value.wpm)
            val inter = MorseTiming.interCharSpaceMs(_settings.value.wpm)

            for (i in pattern.indices) {
                val c = pattern[i]
                when (c) {
                    '.' -> {
                        _isTonePlaying.value = true
                        synthesizer.startTone()
                        delay(dit)
                        synthesizer.stopTone()
                        _isTonePlaying.value = false
                        delay(intra)
                    }
                    '-' -> {
                        _isTonePlaying.value = true
                        synthesizer.startTone()
                        delay(dah)
                        synthesizer.stopTone()
                        _isTonePlaying.value = false
                        delay(intra)
                    }
                    ' ' -> {
                        delay(inter)
                    }
                    '/' -> {
                        delay(dit * 7)
                    }
                }
            }
        }
    }

    fun playCharacterSound(charStr: String) {
        val morse = MorseDictionary.getMorse(charStr) ?: return
        playMorsePattern(morse)
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
        iambicEngine.stop()
        synthesizer.release()
    }
}
