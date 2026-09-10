package com.morsego.app.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ultra low-latency audio synthesizer for Morse code CW sidetone.
 * Uses real-time AudioTrack PCM streaming with cosine-smoothed envelope to eliminate clicks.
 */
class MorseAudioSynthesizer(
    private var frequencyHz: Float = 700f
) {
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 1024
        private const val RAMP_SAMPLES = 176 // ~4ms at 44.1kHz for anti-click envelope
    }

    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    @Volatile
    private var isPlaying = false

    @Volatile
    private var targetAmplitude = 0f

    private var currentAmplitude = 0f
    private var phase = 0.0

    init {
        initAudioTrack()
    }

    private fun initAudioTrack() {
        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(minBufferSize, BUFFER_SIZE * 4)

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
        startAudioLoop()
    }

    private fun startAudioLoop() {
        synthJob?.cancel()
        synthJob = scope.launch {
            val shortBuffer = ShortArray(BUFFER_SIZE)
            val amplitudeStep = 1.0f / RAMP_SAMPLES

            while (isActive) {
                if (!isPlaying && currentAmplitude <= 0f) {
                    // Output silence when idle with minimal CPU
                    shortBuffer.fill(0)
                    audioTrack?.write(shortBuffer, 0, BUFFER_SIZE)
                    continue
                }

                val twoPiFreq = 2.0 * PI * frequencyHz / SAMPLE_RATE

                for (i in 0 until BUFFER_SIZE) {
                    // Smooth amplitude ramp to prevent pops/clicks
                    if (currentAmplitude < targetAmplitude) {
                        currentAmplitude = minOf(targetAmplitude, currentAmplitude + amplitudeStep)
                    } else if (currentAmplitude > targetAmplitude) {
                        currentAmplitude = maxOf(0f, currentAmplitude - amplitudeStep)
                    }

                    val sampleValue = (sin(phase) * currentAmplitude * 30000).toInt()
                    shortBuffer[i] = sampleValue.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

                    phase += twoPiFreq
                    if (phase > 2.0 * PI) {
                        phase -= 2.0 * PI
                    }
                }

                audioTrack?.write(shortBuffer, 0, BUFFER_SIZE)
            }
        }
    }

    fun startTone() {
        isPlaying = true
        targetAmplitude = 1.0f
    }

    fun stopTone() {
        isPlaying = false
        targetAmplitude = 0.0f
    }

    fun setFrequency(newFreq: Float) {
        frequencyHz = newFreq.coerceIn(300f, 1500f)
    }

    fun getFrequency(): Float = frequencyHz

    suspend fun playToneDuration(durationMs: Long) {
        startTone()
        kotlinx.coroutines.delay(durationMs)
        stopTone()
    }

    fun release() {
        synthJob?.cancel()
        isPlaying = false
        targetAmplitude = 0f
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
