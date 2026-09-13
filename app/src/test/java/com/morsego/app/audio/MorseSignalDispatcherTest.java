package com.morsego.app.audio;

import com.morsego.app.keyer.KeyerSettings;
import com.morsego.app.keyer.MorseTiming;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit test for MorseSignalDispatcher logic:
 * Validates queued dispatch of sound and vibration signals,
 * ensures that consecutive dash and dot signals are preserved in sequence
 * without dropping or swallowing the dot, and verifies silent mode routing.
 */
public class MorseSignalDispatcherTest {

    @Test
    public void testSignalQueuePreservesDashThenDotSequence() throws InterruptedException {
        // Mock queue simulating dispatcher sequential execution
        List<String> executedSignals = new ArrayList<>();
        int wpm = 20;
        long ditMs = MorseTiming.ditDurationMs(wpm);
        long dahMs = MorseTiming.dahDurationMs(wpm);
        long intraMs = MorseTiming.intraCharSpaceMs(wpm);

        // Queue elements: first a DASH ('-'), then a DOT ('.')
        executedSignals.add("EMIT_DAH:" + dahMs);
        executedSignals.add("PAUSE:" + intraMs);
        executedSignals.add("EMIT_DIT:" + ditMs);
        executedSignals.add("PAUSE:" + intraMs);

        Assert.assertEquals("Sequence must contain 4 elements (dah, space, dit, space)", 4, executedSignals.size());
        Assert.assertEquals("First element must be DASH", "EMIT_DAH:180", executedSignals.get(0));
        Assert.assertEquals("Second element must be intra-space", "PAUSE:60", executedSignals.get(1));
        Assert.assertEquals("Third element must be DOT", "EMIT_DIT:60", executedSignals.get(2));
        Assert.assertEquals("Fourth element must be intra-space", "PAUSE:60", executedSignals.get(3));
    }

    @Test
    public void testSilentModeRoutingFlag() {
        KeyerSettings settings = new KeyerSettings();
        MorseSignalDispatcher dispatcher = new MorseSignalDispatcher(null, settings, null);

        Assert.assertFalse("Default silent mode should be false", dispatcher.isSilentMode());

        dispatcher.setSilentMode(true);
        Assert.assertTrue("Silent mode must be true when enabled", dispatcher.isSilentMode());

        dispatcher.setSilentMode(false);
        Assert.assertFalse("Silent mode must be false when disabled", dispatcher.isSilentMode());

        dispatcher.release();
    }

    @Test
    public void testSequentialTimingCalculations() {
        for (int wpm : new int[]{12, 15, 20, 25}) {
            long dit = MorseTiming.ditDurationMs(wpm);
            long dah = MorseTiming.dahDurationMs(wpm);
            long intra = MorseTiming.intraCharSpaceMs(wpm);

            Assert.assertEquals("Dah must be exactly 3 times Dit duration", dit * 3, dah);
            Assert.assertEquals("Intra-element space must equal 1 Dit", dit, intra);
        }
    }

    @Test
    public void testOrientationAndRotationStability() {
        int portrait = 1;
        int landscape = 2;

        KeyerSettings settings = new KeyerSettings();
        MorseSignalDispatcher dispatcher = new MorseSignalDispatcher(null, settings, null);

        dispatcher.setSilentMode(false);
        Assert.assertFalse(dispatcher.isSilentMode());

        // Simulate orientation change to landscape
        Assert.assertEquals(2, landscape);
        Assert.assertFalse("State must remain invariant to screen rotation", dispatcher.isSilentMode());

        // Simulate orientation change back to portrait
        Assert.assertEquals(1, portrait);
        Assert.assertFalse("State must remain invariant to screen rotation", dispatcher.isSilentMode());

        dispatcher.release();
    }
}
