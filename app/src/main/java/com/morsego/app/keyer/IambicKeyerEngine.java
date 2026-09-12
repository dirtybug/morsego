package com.morsego.app.keyer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class IambicKeyerEngine {

    public interface KeyerListener {
        void onToneStart();
        void onToneStop();
        void onElementEmitted(char element);
    }

    private final KeyerSettings settings;
    private final KeyerListener listener;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private volatile boolean ditPressed = false;
    private volatile boolean dahPressed = false;
    private volatile boolean ditMemory = false;
    private volatile boolean dahMemory = false;
    private volatile boolean ditNeedsRelease = false;
    private volatile boolean dahNeedsRelease = false;

    public IambicKeyerEngine(KeyerSettings settings, KeyerListener listener) {
        this.settings = settings;
        this.listener = listener;
    }

    public void onDitChanged(boolean pressed) {
        ditPressed = pressed;
        if (settings.getMode() == KeyerSettings.Mode.STRAIGHT_KEY) {
            handleStraightKey();
            return;
        }

        if (pressed) {
            ditNeedsRelease = false;
            ditMemory = true;
            startLoopIfNeeded();
        } else {
            ditNeedsRelease = false;
        }
    }

    public void onDahChanged(boolean pressed) {
        dahPressed = pressed;
        if (settings.getMode() == KeyerSettings.Mode.STRAIGHT_KEY) {
            handleStraightKey();
            return;
        }

        if (pressed) {
            dahNeedsRelease = false;
            dahMemory = true;
            startLoopIfNeeded();
        } else {
            dahNeedsRelease = false;
        }
    }

    private void handleStraightKey() {
        if (ditPressed || dahPressed) {
            if (listener != null) listener.onToneStart();
        } else {
            if (listener != null) listener.onToneStop();
        }
    }

    private synchronized void startLoopIfNeeded() {
        if (isRunning.get()) return;

        isRunning.set(true);
        executor.execute(() -> {
            char lastElement = ' ';

            while (isRunning.get()) {
                boolean curDit = (!ditNeedsRelease && ditPressed) || ditMemory;
                boolean curDah = (!dahNeedsRelease && dahPressed) || dahMemory;

                if (!curDit && !curDah) {
                    isRunning.set(false);
                    break;
                }

                int wpm = settings.getWpm();
                long ditDuration = MorseTiming.ditDurationMs(wpm);
                long dahDuration = MorseTiming.dahDurationMs(wpm);
                long elementSpace = MorseTiming.intraCharSpaceMs(wpm);

                boolean sendDit;
                if (curDit && curDah) {
                    sendDit = (lastElement != '.');
                } else if (curDit) {
                    sendDit = true;
                } else {
                    sendDit = false;
                }

                if (sendDit) {
                    ditMemory = false;
                    ditNeedsRelease = true; // Consumed: requires releasing paddle before another dit
                    lastElement = '.';
                    if (listener != null) {
                        listener.onToneStart();
                        listener.onElementEmitted('.');
                    }
                    sleep(ditDuration);
                    if (listener != null) listener.onToneStop();
                } else {
                    dahMemory = false;
                    dahNeedsRelease = true; // Consumed: requires releasing paddle before another dah
                    lastElement = '-';
                    if (listener != null) {
                        listener.onToneStart();
                        listener.onElementEmitted('-');
                    }
                    sleep(dahDuration);
                    if (listener != null) listener.onToneStop();
                }

                // Intra-element spacing (1 dit unit)
                long startPause = System.currentTimeMillis();
                while (System.currentTimeMillis() - startPause < elementSpace) {
                    if (ditPressed && !ditNeedsRelease) ditMemory = true;
                    if (dahPressed && !dahNeedsRelease) dahMemory = true;
                    sleep(5);
                }

                // In Mode A, clear memory if paddle was already released
                if (settings.getMode() == KeyerSettings.Mode.IAMBIC_A) {
                    if (!ditPressed) ditMemory = false;
                    if (!dahPressed) dahMemory = false;
                }
            }
        });
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }

    public void stop() {
        isRunning.set(false);
        ditMemory = false;
        dahMemory = false;
        ditNeedsRelease = false;
        dahNeedsRelease = false;
        if (listener != null) listener.onToneStop();
    }

    public void release() {
        stop();
        executor.shutdownNow();
    }
}
