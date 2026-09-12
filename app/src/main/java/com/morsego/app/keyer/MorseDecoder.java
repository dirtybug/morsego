package com.morsego.app.keyer;

import android.os.Handler;
import android.os.Looper;

import com.morsego.app.tree.MorseBinaryTree;

public class MorseDecoder {

    public interface DecoderListener {
        void onPatternChanged(String currentPattern);
        void onTextUpdated(String fullText);
        void onCharacterDecoded(char character);
        default void onTimingFeedback(MorseTiming.PauseEvaluation evaluation) {}
        default void onTimingFailure(MorseTiming.PauseEvaluation evaluation) {}
    }

    private final KeyerSettings settings;
    private DecoderListener listener;
    private final Handler handler;

    private final StringBuilder currentPattern = new StringBuilder();
    private final StringBuilder decodedText = new StringBuilder();

    private Runnable charPauseRunnable;
    private Runnable wordPauseRunnable;
    private Runnable maxLetterPauseRunnable;

    // Timing tracking
    private long lastToneStopTime = 0;
    private long lastCharToneStopTime = 0;

    // Manual / Straight-key timing
    private long toneStartTime = 0;
    private boolean isToneActive = false;

    public MorseDecoder(KeyerSettings settings) {
        this.settings = settings;
        Handler h = null;
        try {
            Looper looper = Looper.getMainLooper();
            if (looper != null) {
                h = new Handler(looper);
            }
        } catch (Exception ignored) {}
        this.handler = h;
    }

    private void postToHandler(Runnable r) {
        if (handler != null) {
            handler.post(r);
        } else {
            r.run();
        }
    }

    private void postDelayedToHandler(Runnable r, long delay) {
        if (handler != null) {
            handler.postDelayed(r, delay);
        }
    }

    private void removeCallbacksFromHandler(Runnable r) {
        if (handler != null && r != null) {
            handler.removeCallbacks(r);
        }
    }

    public void setListener(DecoderListener listener) {
        this.listener = listener;
    }

    public synchronized void onToneStarted() {
        cancelMaxLetterPauseWatcher();
        long now = System.currentTimeMillis();

        if (lastToneStopTime > 0) {
            long pause = now - lastToneStopTime;

            if (currentPattern.length() > 0) {
                // Intra-element pause between dits/dahs of the same character
                MorseTiming.PauseEvaluation eval = MorseTiming.evaluateIntraElementPause(pause, settings.getWpm());
                notifyTimingFeedback(eval);
                if (eval.isTimingFailure) {
                    notifyTimingFailure(eval);
                }
            } else if (lastCharToneStopTime > 0) {
                // Inter-letter pause between letters of a word
                MorseTiming.PauseEvaluation eval = MorseTiming.evaluateLetterPause(pause, settings.getWpm());
                notifyTimingFeedback(eval);
                if (eval.isTimingFailure) {
                    notifyTimingFailure(eval);
                }
            }
        }
    }

    public synchronized void onToneStopped() {
        lastToneStopTime = System.currentTimeMillis();
        restartPauseWatchers();
    }

    public synchronized void onElementReceived(char element) {
        currentPattern.append(element);
        notifyPatternChanged();
        restartPauseWatchers();
    }

    public synchronized void onManualToneState(boolean toneOn) {
        long now = System.currentTimeMillis();
        if (toneOn && !isToneActive) {
            toneStartTime = now;
            isToneActive = true;
            onToneStarted();
            cancelPauseWatchers();
        } else if (!toneOn && isToneActive) {
            isToneActive = false;
            onToneStopped();
            long duration = now - toneStartTime;
            long ditDuration = MorseTiming.ditDurationMs(settings.getWpm());

            char element = (duration >= ditDuration * 2) ? '-' : '.';
            currentPattern.append(element);
            notifyPatternChanged();
            restartPauseWatchers();
        }
    }

    private void restartPauseWatchers() {
        cancelPauseWatchers();

        int wpm = settings.getWpm();
        long charPause = Math.max((long) (MorseTiming.interCharSpaceMs(wpm) * 1.6f), 450L);
        long wordPause = Math.max(MorseTiming.wordSpaceMs(wpm), charPause + 300L);

        charPauseRunnable = () -> {
            commitCharacter();
        };
        postDelayedToHandler(charPauseRunnable, charPause);

        wordPauseRunnable = () -> {
            if (decodedText.length() > 0 && decodedText.charAt(decodedText.length() - 1) != ' ') {
                decodedText.append(' ');
                notifyTextUpdated();
            }
        };
        postDelayedToHandler(wordPauseRunnable, wordPause);
    }

    private void cancelPauseWatchers() {
        if (charPauseRunnable != null) {
            removeCallbacksFromHandler(charPauseRunnable);
            charPauseRunnable = null;
        }
        if (wordPauseRunnable != null) {
            removeCallbacksFromHandler(wordPauseRunnable);
            wordPauseRunnable = null;
        }
    }

    private void scheduleMaxLetterPauseWatcher() {
        cancelMaxLetterPauseWatcher();
        int wpm = settings.getWpm();
        // High generous tolerance: 5.0x inter-character pause with a minimum floor of 2200ms
        long maxWait = Math.max((long) (MorseTiming.interCharSpaceMs(wpm) * 5.0f) + 200L, 2200L);
        maxLetterPauseRunnable = () -> {
            if (listener != null && decodedText.length() > 0) {
                boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
                String feedback = isPt ?
                        "Falha: Pausa excessiva entre letras (> máx 5.0x)" :
                        "Failure: Excessive pause between letters (> max 5.0x)";
                MorseTiming.PauseEvaluation eval = new MorseTiming.PauseEvaluation(
                        false, true, feedback, 5.1f);
                notifyTimingFeedback(eval);
                notifyTimingFailure(eval);
            }
        };
        postDelayedToHandler(maxLetterPauseRunnable, maxWait);
    }

    private void cancelMaxLetterPauseWatcher() {
        if (maxLetterPauseRunnable != null) {
            removeCallbacksFromHandler(maxLetterPauseRunnable);
            maxLetterPauseRunnable = null;
        }
    }

    public synchronized void commitCharacter() {
        if (currentPattern.length() == 0) return;

        lastCharToneStopTime = lastToneStopTime > 0 ? lastToneStopTime : System.currentTimeMillis();

        String pattern = currentPattern.toString();
        currentPattern.setLength(0);
        notifyPatternChanged();

        String found = MorseBinaryTree.getInstance().getChar(pattern);
        char c = (found != null && !found.isEmpty()) ? found.charAt(0) : '?';

        decodedText.append(c);
        notifyTextUpdated();

        if (listener != null) {
            listener.onCharacterDecoded(c);
        }

        scheduleMaxLetterPauseWatcher();
    }

    private void notifyTimingFeedback(MorseTiming.PauseEvaluation eval) {
        if (listener != null) {
            postToHandler(() -> listener.onTimingFeedback(eval));
        }
    }

    private void notifyTimingFailure(MorseTiming.PauseEvaluation eval) {
        if (listener != null) {
            postToHandler(() -> listener.onTimingFailure(eval));
        }
    }

    public synchronized void clear() {
        cancelPauseWatchers();
        cancelMaxLetterPauseWatcher();
        lastToneStopTime = 0;
        lastCharToneStopTime = 0;
        currentPattern.setLength(0);
        decodedText.setLength(0);
        notifyPatternChanged();
        notifyTextUpdated();
    }

    public synchronized void backspace() {
        if (decodedText.length() > 0) {
            decodedText.deleteCharAt(decodedText.length() - 1);
            notifyTextUpdated();
        }
    }

    public synchronized String getCurrentPattern() {
        return currentPattern.toString();
    }

    public synchronized String getDecodedText() {
        return decodedText.toString();
    }

    private void notifyPatternChanged() {
        if (listener != null) {
            final String p = currentPattern.toString();
            postToHandler(() -> listener.onPatternChanged(p));
        }
    }

    private void notifyTextUpdated() {
        if (listener != null) {
            final String t = decodedText.toString();
            postToHandler(() -> listener.onTextUpdated(t));
        }
    }
}
