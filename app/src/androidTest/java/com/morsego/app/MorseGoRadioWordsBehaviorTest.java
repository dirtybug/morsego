package com.morsego.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.morsego.app.keyer.MorseTiming;
import com.morsego.app.tree.MorseBinaryTree;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Behavior tests for transmitting Common Ham Radio Words (CQ, 73, DX, QSL, QTH, RST, SOS, TU)
 * and verifying that timing cadence bounds (minimum and maximum pauses within letters
 * and between letters) are strictly evaluated and enforced as failures when violated.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MorseGoRadioWordsBehaviorTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setup() throws InterruptedException {
        Thread.sleep(600);
        // Navigate to Free Keyer sandbox for radio transmission testing
        onView(withId(R.id.nav_keyer)).perform(click());
        Thread.sleep(400);
    }

    private void transmitRadioPhrase(String text) throws InterruptedException {
        AtomicReference<MainActivity> activityRef = new AtomicReference<>();
        activityRule.getScenario().onActivity(activityRef::set);
        MainActivity activity = activityRef.get();
        if (activity == null) return;

        int wpm = activity.getSettings().getWpm();
        long intraElementPause = MorseTiming.intraCharSpaceMs(wpm);
        long interCharPause = MorseTiming.interCharSpaceMs(wpm) + 50;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == ' ') {
                Thread.sleep(MorseTiming.wordSpaceMs(wpm) + 60);
                continue;
            }

            String morse = MorseBinaryTree.getInstance().getMorse(String.valueOf(c));
            if (morse != null) {
                for (int j = 0; j < morse.length(); j++) {
                    final char elem = morse.charAt(j);
                    activity.runOnUiThread(() -> activity.getDecoder().onElementReceived(elem));
                    Thread.sleep(intraElementPause);
                }
                Thread.sleep(interCharPause);
            }
        }
        Thread.sleep(300);
    }

    private void clearKeyerDisplay() throws InterruptedException {
        onView(withId(R.id.btnClearText)).perform(click());
        Thread.sleep(300);
    }

    /**
     * RADIO WORD 1: Transmitting "CQ" (General Call / Chamada Geral)
     */
    @Test
    public void test01_TransmitCQ_GeneralCall() throws InterruptedException {
        clearKeyerDisplay();
        transmitRadioPhrase("CQ");

        onView(withId(R.id.tvDecodedOutput)).check(matches(withText(containsString("CQ"))));
        ScreenshotHelper.capture("radio_tx_01_CQ");
    }

    /**
     * RADIO WORD 2: Transmitting "73" (Best Regards / Cumprimentos em CW)
     */
    @Test
    public void test02_Transmit73_BestRegards() throws InterruptedException {
        clearKeyerDisplay();
        transmitRadioPhrase("73");

        onView(withId(R.id.tvDecodedOutput)).check(matches(withText(containsString("73"))));
        ScreenshotHelper.capture("radio_tx_02_73");
    }

    /**
     * RADIO WORD 3: Transmitting "DX" (Long Distance Contact)
     */
    @Test
    public void test03_TransmitDX_LongDistance() throws InterruptedException {
        clearKeyerDisplay();
        transmitRadioPhrase("DX");

        onView(withId(R.id.tvDecodedOutput)).check(matches(withText(containsString("DX"))));
        ScreenshotHelper.capture("radio_tx_03_DX");
    }

    /**
     * RADIO WORD 4: Transmitting "QSL" (Confirmation / Confirmado)
     */
    @Test
    public void test04_TransmitQSL_Confirmation() throws InterruptedException {
        clearKeyerDisplay();
        transmitRadioPhrase("QSL");

        onView(withId(R.id.tvDecodedOutput)).check(matches(withText(containsString("QSL"))));
        ScreenshotHelper.capture("radio_tx_04_QSL");
    }

    /**
     * RADIO WORD 5: Transmitting "QTH" (Location / Minha Estação)
     */
    @Test
    public void test05_TransmitQTH_Location() throws InterruptedException {
        clearKeyerDisplay();
        transmitRadioPhrase("QTH");

        onView(withId(R.id.tvDecodedOutput)).check(matches(withText(containsString("QTH"))));
        ScreenshotHelper.capture("radio_tx_05_QTH");
    }

    /**
     * RADIO WORD 6: Transmitting "RST" (Signal Report / Relatório 599)
     */
    @Test
    public void test06_TransmitRST_SignalReport() throws InterruptedException {
        clearKeyerDisplay();
        transmitRadioPhrase("RST");

        onView(withId(R.id.tvDecodedOutput)).check(matches(withText(containsString("RST"))));
        ScreenshotHelper.capture("radio_tx_06_RST");
    }

    /**
     * RADIO WORD 7: Transmitting "SOS" (Emergency Distress Signal)
     */
    @Test
    public void test07_TransmitSOS_EmergencyDistress() throws InterruptedException {
        clearKeyerDisplay();
        transmitRadioPhrase("SOS");

        onView(withId(R.id.tvDecodedOutput)).check(matches(withText(containsString("SOS"))));
        ScreenshotHelper.capture("radio_tx_07_SOS");
    }

    /**
     * RADIO WORD 8: Transmitting "TU" (Thank You / Obrigado)
     */
    @Test
    public void test08_TransmitTU_ThankYou() throws InterruptedException {
        clearKeyerDisplay();
        transmitRadioPhrase("TU");

        onView(withId(R.id.tvDecodedOutput)).check(matches(withText(containsString("TU"))));
        ScreenshotHelper.capture("radio_tx_08_TU");
    }

    /**
     * RADIO WORD 9: Full Ham Radio QSO ("CQ CQ DX DE CT1 73")
     */
    @Test
    public void test09_TransmitFullRadioQSO() throws InterruptedException {
        clearKeyerDisplay();
        transmitRadioPhrase("CQ CQ DX DE CT1 73");

        onView(withId(R.id.tvDecodedOutput)).check(matches(withText(containsString("CQ CQ DX DE CT1 73"))));
        ScreenshotHelper.capture("radio_tx_09_full_qso");
    }

    /**
     * BEHAVIOR 10: Strict Timing Enforcement:
     * Validates that non-respect of min or max times within letters (< 0.45x, > 2.0x)
     * and between letters (< 0.60x, > 2.2x) is strictly flagged as a FAILURE.
     */
    @Test
    public void test10_TimingCadence_MinMaxFailureEnforcement() throws InterruptedException {
        int wpm = 15;
        long nominalDit = MorseTiming.ditDurationMs(wpm); // 80ms
        long nominalLetter = MorseTiming.interCharSpaceMs(wpm); // 240ms

        // 1. Intra-element pause test (ponto/traço dentro da letra):
        // 1a. Compliant pause (1.0x) -> Good, NOT a failure
        MorseTiming.PauseEvaluation evalGoodIntra = MorseTiming.evaluateIntraElementPause(nominalDit, wpm);
        assertTrue("Compliant intra-element pause should be good", evalGoodIntra.isGood);
        assertFalse("Compliant intra-element pause must not fail", evalGoodIntra.isTimingFailure);

        // 1b. Pause too fast (< 0.45x, e.g. 25ms) -> MUST BE TIMING FAILURE!
        MorseTiming.PauseEvaluation evalTooFastIntra = MorseTiming.evaluateIntraElementPause(25, wpm);
        assertTrue("Intra-element pause below minimum must be a timing failure", evalTooFastIntra.isTimingFailure);

        // 1c. Pause too long (> 2.0x, e.g. 200ms) -> MUST BE TIMING FAILURE!
        MorseTiming.PauseEvaluation evalTooSlowIntra = MorseTiming.evaluateIntraElementPause(200, wpm);
        assertTrue("Intra-element pause above maximum must be a timing failure", evalTooSlowIntra.isTimingFailure);

        // 2. Inter-letter pause test (entre letras da palavra):
        // 2a. Compliant pause (1.0x letter space, 240ms) -> Good, NOT a failure
        MorseTiming.PauseEvaluation evalGoodLetter = MorseTiming.evaluateLetterPause(nominalLetter, wpm);
        assertTrue("Compliant letter separation should be good", evalGoodLetter.isGood);
        assertFalse("Compliant letter separation must not fail", evalGoodLetter.isTimingFailure);

        // 2b. Letter pause too short (< 0.60x, e.g. 100ms) -> MUST BE TIMING FAILURE!
        MorseTiming.PauseEvaluation evalTooFastLetter = MorseTiming.evaluateLetterPause(100, wpm);
        assertTrue("Inter-letter pause below minimum must be a timing failure", evalTooFastLetter.isTimingFailure);

        // 2c. Letter pause too long (> 2.2x, e.g. 600ms) -> MUST BE TIMING FAILURE!
        MorseTiming.PauseEvaluation evalTooSlowLetter = MorseTiming.evaluateLetterPause(600, wpm);
        assertTrue("Inter-letter pause above maximum must be a timing failure", evalTooSlowLetter.isTimingFailure);

        // Navigate to Learn tab and capture transmission UI
        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(400);

        ScreenshotHelper.capture("radio_tx_10_timing_cadence_evaluated");
    }
}
