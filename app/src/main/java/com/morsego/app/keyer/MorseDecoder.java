package com.morsego.app.keyer;

import android.os.Handler;
import android.os.Looper;

import com.morsego.app.tree.MorseBinaryTree;

public class MorseDecoder {

    public interface DecoderListener {
        void onPatternChanged(String currentPattern);
        void onTextUpdated(String fullText);
        void onCharacterDecoded(char character);
    }

    private final KeyerSettings settings;
    private DecoderListener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final StringBuilder currentPattern = new StringBuilder();
    private final StringBuilder decodedText = new StringBuilder();

    private Runnable charPauseRunnable;
    private Runnable wordPauseRunnable;

    // Manual / Straight-key timing
    private long toneStartTime = 0;
    private boolean isToneActive = false;

    public MorseDecoder(KeyerSettings settings) {
        this.settings = settings;
    }

    public void setListener(DecoderListener listener) {
        this.listener = listener;
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
            cancelPauseWatchers();
        } else if (!toneOn && isToneActive) {
            isToneActive = false;
            long duration = now - toneStartTime;
            long ditDuration = MorseTiming.ditDurationMs(settings.getWpm());

            char element = (duration >= ditDuration * 2) ? '-' : '.';
            onElementReceived(element);
        }
    }

    private void restartPauseWatchers() {
        cancelPauseWatchers();

        int wpm = settings.getWpm();
        long charPause = MorseTiming.interCharSpaceMs(wpm);
        long wordPause = MorseTiming.wordSpaceMs(wpm);

        charPauseRunnable = () -> {
            commitCharacter();
        };
        handler.postDelayed(charPauseRunnable, charPause);

        wordPauseRunnable = () -> {
            if (decodedText.length() > 0 && decodedText.charAt(decodedText.length() - 1) != ' ') {
                decodedText.append(' ');
                notifyTextUpdated();
            }
        };
        handler.postDelayed(wordPauseRunnable, wordPause);
    }

    private void cancelPauseWatchers() {
        if (charPauseRunnable != null) {
            handler.removeCallbacks(charPauseRunnable);
            charPauseRunnable = null;
        }
        if (wordPauseRunnable != null) {
            handler.removeCallbacks(wordPauseRunnable);
            wordPauseRunnable = null;
        }
    }

    private synchronized void commitCharacter() {
        if (currentPattern.length() == 0) return;

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
    }

    public synchronized void clear() {
        cancelPauseWatchers();
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
            handler.post(() -> listener.onPatternChanged(p));
        }
    }

    private void notifyTextUpdated() {
        if (listener != null) {
            final String t = decodedText.toString();
            handler.post(() -> listener.onTextUpdated(t));
        }
    }
}
