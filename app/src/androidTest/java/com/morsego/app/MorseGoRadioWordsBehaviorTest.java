package com.morsego.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.morsego.app.keyer.MorseTiming;
import com.morsego.app.tree.MorseBinaryTree;
import com.morsego.app.tree.MorseRadioWords;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Behavior Tests para Palavras Comuns de Rádio CW (CQ, 73, DX, QSL, QTH, RST, SOS, TU).
 * Regras estritas:
 * 1. O teste de palavras de rádio só está disponível APÓS desbloquear todas as letras do alfabeto (Nível >= 13).
 * 2. Dividido estritamente em duas etapas separadas:
 *    - ETAPA 1: OUVIR (Listening) - Áudio dos termos de rádio e descodificação/escolha.
 *    - ETAPA 2: MANDAR (Transmission) - Envio através das pás táteis/físicas com validação rigorosa de tempos mín/máx.
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
    }

    private void transmitRadioPhrase(String text) throws InterruptedException {
        AtomicReference<MainActivity> activityRef = new AtomicReference<>();
        activityRule.getScenario().onActivity(activityRef::set);
        MainActivity activity = activityRef.get();
        if (activity == null) return;

        int wpm = activity.getSettings().getWpm();
        long intraElementPause = MorseTiming.intraCharSpaceMs(wpm);
        long interCharPause = MorseTiming.interCharSpaceMs(wpm) + 40;

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

    // =========================================================================
    // 1. VERIFICAÇÃO DE BLOQUEIO: Só disponível após desbloquear todas as 26 letras
    // =========================================================================

    @Test
    public void test01_RadioWords_LockedWhenLettersNotAllUnlocked() throws InterruptedException {
        AtomicReference<MainActivity> activityRef = new AtomicReference<>();
        activityRule.getScenario().onActivity(activityRef::set);
        MainActivity activity = activityRef.get();
        assertNotNull(activity);

        // Given user is at Level 1 (only E and T unlocked)
        activity.getSettings().setCurrentUnlockedLevel(1);
        int currentLevel = activity.getSettings().getCurrentUnlockedLevel();

        // Then verify radio words are strictly locked
        assertFalse("Palavras de rádio devem estar bloqueadas quando faltam letras (nível < 13)",
                MorseRadioWords.isUnlocked(currentLevel));

        List<String> poolLevel1 = MorseBinaryTree.getInstance().getLevel(1).getAllCharacters();
        assertFalse("Nível 1 não contém todas as 26 letras",
                MorseRadioWords.hasAllLettersUnlocked(poolLevel1));

        // Screenshot da tela com estado inicial
        onView(withId(R.id.nav_tree)).perform(click());
        Thread.sleep(400);
        ScreenshotHelper.capture("01_radio_words_locked_state");
    }

    @Test
    public void test02_RadioWords_UnlockedWhenAllLettersUnlockedAtLevel13() throws InterruptedException {
        AtomicReference<MainActivity> activityRef = new AtomicReference<>();
        activityRule.getScenario().onActivity(activityRef::set);
        MainActivity activity = activityRef.get();
        assertNotNull(activity);

        // When user unlocks Level 13 (where Z and Q complete the full A-Z alphabet)
        activity.getSettings().setCurrentUnlockedLevel(13);
        int currentLevel = activity.getSettings().getCurrentUnlockedLevel();

        // Then verify radio words are unlocked
        assertTrue("Palavras de rádio devem estar desbloqueadas quando todas as 26 letras forem alcançadas",
                MorseRadioWords.isUnlocked(currentLevel));

        List<String> poolLevel13 = MorseBinaryTree.getInstance().getLevel(13).getAllCharacters();
        assertTrue("Nível 13 deve ter todas as 26 letras completas na árvore Morse",
                MorseRadioWords.hasAllLettersUnlocked(poolLevel13));

        // Screenshot do desbloqueio
        ScreenshotHelper.capture("02_radio_words_unlocked_level13");
    }

    // =========================================================================
    // 2. ETAPA SEPARADA 1: OUVIR (Listening / Escuta de Termos de Rádio)
    // =========================================================================

    @Test
    public void test03_Stage1_Ouvir_RadioWord_CQ() throws InterruptedException {
        AtomicReference<MainActivity> activityRef = new AtomicReference<>();
        activityRule.getScenario().onActivity(activityRef::set);
        MainActivity activity = activityRef.get();
        assertNotNull(activity);

        // Ensure user is qualified with all letters unlocked
        activity.getSettings().setCurrentUnlockedLevel(13);

        // Generate listening choices for CQ
        List<String> choices = MorseRadioWords.generateListeningChoices("CQ", 4);
        assertEquals(4, choices.size());
        assertTrue(choices.contains("CQ"));

        // Simulate audio playback for "CQ" (-.-. --.-)
        String morseCQ = MorseBinaryTree.getInstance().getMorse("C") + " " + MorseBinaryTree.getInstance().getMorse("Q");
        assertEquals("-.-. --.-", morseCQ);

        activity.runOnUiThread(() ->
                activity.getSynthesizer().playMorsePattern(morseCQ, activity.getSettings().getWpm(), null));
        Thread.sleep(800);

        ScreenshotHelper.capture("03_radio_words_stage1_ouvir_CQ");
    }

    @Test
    public void test04_Stage1_Ouvir_RadioWord_73_and_SOS() throws InterruptedException {
        AtomicReference<MainActivity> activityRef = new AtomicReference<>();
        activityRule.getScenario().onActivity(activityRef::set);
        MainActivity activity = activityRef.get();
        assertNotNull(activity);

        activity.getSettings().setCurrentUnlockedLevel(13);

        // Test listening choices for "73"
        List<String> choices73 = MorseRadioWords.generateListeningChoices("73", 4);
        assertTrue(choices73.contains("73"));

        // Play 73 audio
        activity.runOnUiThread(() ->
                activity.getSynthesizer().playMorsePattern("--... ...--", activity.getSettings().getWpm(), null));
        Thread.sleep(700);

        // Test listening choices for "SOS"
        List<String> choicesSOS = MorseRadioWords.generateListeningChoices("SOS", 4);
        assertTrue(choicesSOS.contains("SOS"));

        ScreenshotHelper.capture("04_radio_words_stage1_ouvir_73_SOS");
    }

    // =========================================================================
    // 3. ETAPA SEPARADA 2: MANDAR (Transmission / Envio com Pás & Cadência)
    // =========================================================================

    @Test
    public void test05_Stage2_Mandar_RadioWord_CQ() throws InterruptedException {
        // Navigate to Keyer transmission view
        onView(withId(R.id.nav_keyer)).perform(click());
        Thread.sleep(400);

        onView(withId(R.id.btnClearText)).perform(click());
        Thread.sleep(200);

        // Transmit "CQ" with proper inter-element and inter-letter timing
        transmitRadioPhrase("CQ");

        onView(withId(R.id.tvDecodedOutput)).check(matches(withText(containsString("CQ"))));
        ScreenshotHelper.capture("05_radio_words_stage2_mandar_CQ");
    }

    @Test
    public void test06_Stage2_Mandar_RadioWords_DX_QSL_QTH() throws InterruptedException {
        onView(withId(R.id.nav_keyer)).perform(click());
        Thread.sleep(400);

        onView(withId(R.id.btnClearText)).perform(click());
        transmitRadioPhrase("DX");
        onView(withId(R.id.tvDecodedOutput)).check(matches(withText(containsString("DX"))));

        onView(withId(R.id.btnClearText)).perform(click());
        transmitRadioPhrase("QSL");
        onView(withId(R.id.tvDecodedOutput)).check(matches(withText(containsString("QSL"))));

        onView(withId(R.id.btnClearText)).perform(click());
        transmitRadioPhrase("QTH");
        onView(withId(R.id.tvDecodedOutput)).check(matches(withText(containsString("QTH"))));

        ScreenshotHelper.capture("06_radio_words_stage2_mandar_QTH");
    }

    @Test
    public void test07_Stage2_Mandar_FullQSO_CQ_CQ_DX_DE_CT1_73() throws InterruptedException {
        onView(withId(R.id.nav_keyer)).perform(click());
        Thread.sleep(400);

        onView(withId(R.id.btnClearText)).perform(click());
        transmitRadioPhrase("CQ CQ DX DE CT1 73");

        onView(withId(R.id.tvDecodedOutput)).check(matches(withText(containsString("CQ CQ DX DE CT1 73"))));
        ScreenshotHelper.capture("07_radio_words_stage2_mandar_full_qso");
    }

    // =========================================================================
    // 4. VERIFICAÇÃO RIGOROSA DE TEMPOS MÍNIMOS E MÁXIMOS NA TRANSMISSÃO
    // =========================================================================

    @Test
    public void test08_Stage2_Mandar_TimingCadence_FailureEnforcement() throws InterruptedException {
        int wpm = 18;
        long nominalDit = MorseTiming.ditDurationMs(wpm);
        long nominalLetter = MorseTiming.interCharSpaceMs(wpm);

        // 1. Intra-element pause test (< 0.45x or > 2.0x is failure)
        MorseTiming.PauseEvaluation evalGood = MorseTiming.evaluateIntraElementPause(nominalDit, wpm);
        assertTrue(evalGood.isGood);
        assertFalse(evalGood.isTimingFailure);

        MorseTiming.PauseEvaluation evalFastFail = MorseTiming.evaluateIntraElementPause((long)(nominalDit * 0.35f), wpm);
        assertTrue("Pausa intra-elemento abaixo de 0.45x deve falhar", evalFastFail.isTimingFailure);

        MorseTiming.PauseEvaluation evalSlowFail = MorseTiming.evaluateIntraElementPause((long)(nominalDit * 2.5f), wpm);
        assertTrue("Pausa intra-elemento acima de 2.0x deve falhar", evalSlowFail.isTimingFailure);

        // 2. Inter-letter pause test (< 0.60x or > 2.2x is failure)
        MorseTiming.PauseEvaluation evalGoodLetter = MorseTiming.evaluateLetterPause(nominalLetter, wpm);
        assertTrue(evalGoodLetter.isGood);
        assertFalse(evalGoodLetter.isTimingFailure);

        MorseTiming.PauseEvaluation evalFastLetterFail = MorseTiming.evaluateLetterPause((long)(nominalLetter * 0.45f), wpm);
        assertTrue("Pausa entre letras abaixo de 0.60x deve falhar", evalFastLetterFail.isTimingFailure);

        MorseTiming.PauseEvaluation evalSlowLetterFail = MorseTiming.evaluateLetterPause((long)(nominalLetter * 2.6f), wpm);
        assertTrue("Pausa entre letras acima de 2.2x deve falhar", evalSlowLetterFail.isTimingFailure);

        ScreenshotHelper.capture("08_radio_words_stage2_timing_failure_rules");
    }

    // =========================================================================
    // 5. VERIFICAÇÃO DAS PILLS SEPARADAS DE OUVIR E ENVIAR
    // =========================================================================

    @Test
    public void test09_SeparatedPills_ListeningVsTransmission_Verification() throws InterruptedException {
        // Given user is in radio words mode
        List<MorseRadioWords.RadioPill> listenPills = MorseRadioWords.getListeningPills();
        List<MorseRadioWords.RadioPill> sendPills = MorseRadioWords.getTransmissionPills();

        // Verify Listening pills section is strictly separated
        assertNotNull(listenPills);
        assertFalse(listenPills.isEmpty());
        for (MorseRadioWords.RadioPill pill : listenPills) {
            assertEquals("Pill deve pertencer ao modo OUVIR", MorseRadioWords.RadioPill.Mode.OUVIR, pill.mode);
            assertTrue("Rótulo da pill deve indicar ação de ouvir: " + pill.label, pill.label.startsWith("Ouvir "));
        }

        // Verify Transmission pills section is strictly separated
        assertNotNull(sendPills);
        assertFalse(sendPills.isEmpty());
        for (MorseRadioWords.RadioPill pill : sendPills) {
            assertEquals("Pill deve pertencer ao modo ENVIAR", MorseRadioWords.RadioPill.Mode.ENVIAR, pill.mode);
            assertTrue("Rótulo da pill deve indicar ação de envio: " + pill.label, pill.label.startsWith("Enviar "));
        }

        // Navigate to Keyer tab to capture the transmission layout
        onView(withId(R.id.nav_keyer)).perform(click());
        Thread.sleep(400);

        ScreenshotHelper.capture("09_radio_words_separated_pills_verified");
    }
}
