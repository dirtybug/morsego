package com.morsego.app.audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.morsego.app.keyer.MorseTiming;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Ultra low-latency audio synthesizer in pure Java for CW sidetone.
 * Streams 16-bit PCM mono audio with smooth amplitude envelope to prevent clicks.
 */
public class MorseAudioSynthesizer {
    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = 1024;
    private static final int RAMP_SAMPLES = 176; // ~4ms cosine ramp at 44.1kHz

    private AudioTrack audioTrack;
    private final ExecutorService executorService;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isToneActive = new AtomicBoolean(false);

    private volatile float frequencyHz = 700.0f;
    private volatile float targetAmplitude = 0.0f;
    private float currentAmplitude = 0.0f;
    private double phase = 0.0;

    public MorseAudioSynthesizer() {
        this.executorService = Executors.newSingleThreadExecutor();
        initAudioTrack();
    }

    private void initAudioTrack() {
        int minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );
        int bufferSize = Math.max(minBufferSize, BUFFER_SIZE * 4);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        AudioFormat audioFormat = new AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build();

        audioTrack = new AudioTrack(
                audioAttributes,
                audioFormat,
                bufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
        );

        audioTrack.play();
        startAudioLoop();
    }

    private void startAudioLoop() {
        isRunning.set(true);
        executorService.execute(() -> {
            short[] buffer = new short[BUFFER_SIZE];
            float amplitudeStep = 1.0f / RAMP_SAMPLES;

            while (isRunning.get()) {
                if (!isToneActive.get() && currentAmplitude <= 0.0f) {
                    // Output silence when idle
                    for (int i = 0; i < BUFFER_SIZE; i++) {
                        buffer[i] = 0;
                    }
                    if (audioTrack != null && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                        audioTrack.write(buffer, 0, BUFFER_SIZE);
                    }
                    continue;
                }

                double twoPiFreq = 2.0 * Math.PI * frequencyHz / SAMPLE_RATE;

                for (int i = 0; i < BUFFER_SIZE; i++) {
                    if (currentAmplitude < targetAmplitude) {
                        currentAmplitude = Math.min(targetAmplitude, currentAmplitude + amplitudeStep);
                    } else if (currentAmplitude > targetAmplitude) {
                        currentAmplitude = Math.max(0.0f, currentAmplitude - amplitudeStep);
                    }

                    int sampleVal = (int) (Math.sin(phase) * currentAmplitude * 30000);
                    buffer[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, sampleVal));

                    phase += twoPiFreq;
                    if (phase > 2.0 * Math.PI) {
                        phase -= 2.0 * Math.PI;
                    }
                }

                if (audioTrack != null && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                    audioTrack.write(buffer, 0, BUFFER_SIZE);
                }
            }
        });
    }

    public void startTone() {
        isToneActive.set(true);
        targetAmplitude = 1.0f;
    }

    public void stopTone() {
        isToneActive.set(false);
        targetAmplitude = 0.0f;
    }

    public boolean isToneActive() {
        return isToneActive.get();
    }

    public void setFrequency(float freqHz) {
        this.frequencyHz = Math.max(300.0f, Math.min(1500.0f, freqHz));
    }

    public float getFrequency() {
        return frequencyHz;
    }

    /**
     * Plays a complete Morse sequence with PARIS timing asynchronously
     */
    public void playMorsePattern(String pattern, int wpm, Runnable onFinished) {
        playMorsePattern(pattern, wpm, 6, 13, onFinished);
    }

    public void playMorsePattern(String pattern, int wpm, int letterSpacingDits, int wordSpacingDits, Runnable onFinished) {
        Executors.newSingleThreadExecutor().execute(() -> {
            long dit = MorseTiming.ditDurationMs(wpm);
            long dah = MorseTiming.dahDurationMs(wpm);
            long intra = MorseTiming.intraCharSpaceMs(wpm);
            long inter = MorseTiming.interCharSpaceMs(wpm, letterSpacingDits);
            long word = MorseTiming.wordSpaceMs(wpm, wordSpacingDits);

            try {
                for (int i = 0; i < pattern.length(); i++) {
                    char c = pattern.charAt(i);
                    if (c == '.') {
                        startTone();
                        Thread.sleep(dit);
                        stopTone();
                        Thread.sleep(intra);
                    } else if (c == '-') {
                        startTone();
                        Thread.sleep(dah);
                        stopTone();
                        Thread.sleep(intra);
                    } else if (c == ' ') {
                        long extraPause = Math.max(0, inter - intra);
                        if (extraPause > 0) Thread.sleep(extraPause);
                    } else if (c == '/') {
                        long extraPause = Math.max(0, word - intra);
                        if (extraPause > 0) Thread.sleep(extraPause);
                    }
                }
            } catch (InterruptedException ignored) {
                stopTone();
            }

            if (onFinished != null) {
                onFinished.run();
            }
        });
    }

    public void release() {
        isRunning.set(false);
        stopTone();
        try {
            if (audioTrack != null) {
                audioTrack.stop();
                audioTrack.release();
            }
        } catch (Exception ignored) {}
        executorService.shutdownNow();
    }
}
