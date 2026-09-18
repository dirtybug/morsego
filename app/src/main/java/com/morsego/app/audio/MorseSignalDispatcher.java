package com.morsego.app.audio;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

import com.morsego.app.keyer.KeyerSettings;
import com.morsego.app.keyer.MorseTiming;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Dedicated sound and vibration signal dispatcher running on a dedicated worker thread.
 * Processes audio and haptic vibration signals sequentially from a thread-safe queue
 * to ensure timing precision and eliminate skipped dots or truncated tones after dashes.
 */
public class MorseSignalDispatcher {

    private enum CommandType {
        START_TONE,
        STOP_TONE,
        EMIT_ELEMENT,
        PLAY_PATTERN,
        STOP_ALL
    }

    private static class SignalCommand {
        final CommandType type;
        final char element;
        final int wpm;
        final String pattern;
        final Runnable onFinished;

        SignalCommand(CommandType type) {
            this(type, ' ', 0, null, null);
        }

        SignalCommand(CommandType type, char element, int wpm) {
            this(type, element, wpm, null, null);
        }

        SignalCommand(CommandType type, String pattern, int wpm, Runnable onFinished) {
            this(type, ' ', wpm, pattern, onFinished);
        }

        SignalCommand(CommandType type, char element, int wpm, String pattern, Runnable onFinished) {
            this.type = type;
            this.element = element;
            this.wpm = wpm;
            this.pattern = pattern;
            this.onFinished = onFinished;
        }
    }

    private final KeyerSettings settings;
    private final MorseAudioSynthesizer synthesizer;
    private Vibrator vibrator;
    private final BlockingQueue<SignalCommand> commandQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isToneActive = new AtomicBoolean(false);
    private Thread workerThread;
    private volatile boolean silentMode = false;

    public MorseSignalDispatcher(Context context, KeyerSettings settings, MorseAudioSynthesizer synthesizer) {
        this.settings = settings;
        this.synthesizer = synthesizer;
        initVibrator(context);
        startWorkerThread();
    }

    private void initVibrator(Context context) {
        if (context == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vm = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vm != null) vibrator = vm.getDefaultVibrator();
            } else {
                vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            }
        } catch (Exception ignored) {}
    }

    private void startWorkerThread() {
        isRunning.set(true);
        workerThread = new Thread(this::runDispatcherLoop, "MorseSignalDispatcherThread");
        workerThread.setPriority(Thread.MAX_PRIORITY);
        workerThread.setDaemon(true);
        workerThread.start();
    }

    private void runDispatcherLoop() {
        while (isRunning.get()) {
            try {
                SignalCommand cmd = commandQueue.take();
                if (!isRunning.get()) break;

                switch (cmd.type) {
                    case START_TONE:
                        handleStartTone();
                        break;
                    case STOP_TONE:
                        handleStopTone();
                        break;
                    case EMIT_ELEMENT:
                        handleEmitElement(cmd.element, cmd.wpm);
                        break;
                    case PLAY_PATTERN:
                        handlePlayPattern(cmd.pattern, cmd.wpm, cmd.onFinished);
                        break;
                    case STOP_ALL:
                        handleStopAll();
                        break;
                }
            } catch (InterruptedException e) {
                if (!isRunning.get()) break;
            } catch (Exception ignored) {}
        }
    }

    private void handleEmitElement(char element, int wpm) {
        long duration = (element == '-') ? MorseTiming.dahDurationMs(wpm) : MorseTiming.ditDurationMs(wpm);
        long intraSpace = MorseTiming.intraCharSpaceMs(wpm);

        if (silentMode) {
            vibrateExact(duration);
            sleepMs(duration);
            sleepMs(intraSpace);
        } else if (settings != null && settings.isSoundEnabled()) {
            if (synthesizer != null) synthesizer.startTone();
            sleepMs(duration);
            if (synthesizer != null) synthesizer.stopTone();
            sleepMs(intraSpace);
        } else {
            sleepMs(duration + intraSpace);
        }
    }

    private void handlePlayPattern(String pattern, int wpm, Runnable onFinished) {
        if (pattern == null || pattern.isEmpty()) {
            if (onFinished != null) onFinished.run();
            return;
        }

        int letterDits = settings != null ? settings.getLetterSpacingDits() : 6;
        int wordDits = settings != null ? settings.getWordSpacingDits() : 13;
        long dit = MorseTiming.ditDurationMs(wpm);
        long dah = MorseTiming.dahDurationMs(wpm);
        long intra = MorseTiming.intraCharSpaceMs(wpm);
        long inter = MorseTiming.interCharSpaceMs(wpm, letterDits);
        long word = MorseTiming.wordSpaceMs(wpm, wordDits);

        for (int i = 0; i < pattern.length() && isRunning.get(); i++) {
            char c = pattern.charAt(i);
            if (c == '.') {
                if (silentMode) {
                    vibrateExact(dit);
                    sleepMs(dit + intra);
                } else {
                    if (synthesizer != null) synthesizer.startTone();
                    sleepMs(dit);
                    if (synthesizer != null) synthesizer.stopTone();
                    sleepMs(intra);
                }
            } else if (c == '-') {
                if (silentMode) {
                    vibrateExact(dah);
                    sleepMs(dah + intra);
                } else {
                    if (synthesizer != null) synthesizer.startTone();
                    sleepMs(dah);
                    if (synthesizer != null) synthesizer.stopTone();
                    sleepMs(intra);
                }
            } else if (c == ' ') {
                long extraPause = Math.max(0, inter - intra);
                if (extraPause > 0) sleepMs(extraPause);
            } else if (c == '/') {
                long extraPause = Math.max(0, word - intra);
                if (extraPause > 0) sleepMs(extraPause);
            }
        }

        if (onFinished != null) {
            onFinished.run();
        }
    }

    private void handleStartTone() {
        isToneActive.set(true);
        if (silentMode) {
            vibrateExact(5000);
        } else if (settings != null && settings.isSoundEnabled()) {
            if (synthesizer != null) synthesizer.startTone();
        }
    }

    private void handleStopTone() {
        isToneActive.set(false);
        if (silentMode) {
            cancelVibration();
        }
        if (synthesizer != null) synthesizer.stopTone();
    }

    private void handleStopAll() {
        commandQueue.clear();
        if (synthesizer != null) synthesizer.stopTone();
        cancelVibration();
        isToneActive.set(false);
    }

    public void emitElement(char element, int wpm) {
        commandQueue.offer(new SignalCommand(CommandType.EMIT_ELEMENT, element, wpm));
    }

    public void startTone() {
        commandQueue.offer(new SignalCommand(CommandType.START_TONE));
    }

    public void stopTone() {
        commandQueue.offer(new SignalCommand(CommandType.STOP_TONE));
    }

    public void playPattern(String pattern, int wpm, Runnable onFinished) {
        commandQueue.offer(new SignalCommand(CommandType.PLAY_PATTERN, pattern, wpm, onFinished));
    }

    public void stopAll() {
        commandQueue.offer(new SignalCommand(CommandType.STOP_ALL));
    }

    public void setSilentMode(boolean isSilent) {
        this.silentMode = isSilent;
    }

    public boolean isSilentMode() {
        return silentMode;
    }

    public void triggerHaptic(long ms) {
        if (silentMode) return;
        if (!settings.isHapticsEnabled() || vibrator == null) return;
        if (isToneActive.get()) return; // Don't interrupt tone
        vibrateExact(ms);
    }

    public void vibrateExact(long ms) {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(ms);
            }
        } catch (Exception ignored) {}
    }

    public void cancelVibration() {
        if (vibrator != null) {
            try {
                vibrator.cancel();
            } catch (Exception ignored) {}
        }
    }

    private void sleepMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }

    public void release() {
        isRunning.set(false);
        commandQueue.clear();
        handleStopAll();
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }
}
