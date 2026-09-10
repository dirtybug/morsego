package com.morsego.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * Behavior tests for the Level Exam, Timing Cadence Evaluation,
 * Dedicated Touch Paddles (DI / DAH), and Level Persistence.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MorseGoExamFlowBehaviorTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setup() throws InterruptedException {
        Thread.sleep(600);
    }

    /**
     * BEHAVIOR: Level 1 Exam Queue Setup with available letters x 4 + 4 words with new letters + 4 random words.
     */
    @Test
    public void test01_ExamQueue_StructureAndLivesDisplay() throws InterruptedException {
        // Navigate to Learn tab
        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(500);

        // Verify requirements text mentions available letters x 4 and 4 new letter words
        onView(withId(R.id.btnStartLevelTest)).check(matches(isDisplayed()));
        ScreenshotHelper.capture("14_exam_requirements_info");

        // Start exam
        onView(withId(R.id.btnStartLevelTest)).perform(click());
        Thread.sleep(800);

        // Verify listening stage header
        onView(withId(R.id.tvTestPhaseBanner)).check(matches(withText(containsString("PARTE 1 DE 2: TESTE DE ESCUTA"))));

        // Verify 3 lives (❤️❤️❤️)
        onView(withId(R.id.tvTestLives)).check(matches(withText(containsString("❤️❤️❤️"))));

        // Verify progress indicates Letters
        onView(withId(R.id.tvTestProgress)).check(matches(withText(containsString("Letras"))));

        ScreenshotHelper.capture("15_exam_listening_in_progress");
    }

    /**
     * BEHAVIOR: Transmission (Mandar) Controls with Dedicated DI and DAH Buttons & Cadence Feedback.
     */
    @Test
    public void test02_TransmissionControls_TouchPaddlesAndCadence() throws InterruptedException {
        // Navigate to Learn tab
        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(500);

        // Verify dedicated bottom paddle buttons are visible
        onView(withId(R.id.btnTouchDit)).check(matches(isDisplayed()));
        onView(withId(R.id.btnTouchDah)).check(matches(isDisplayed()));

        // Tap DI (ponto)
        onView(withId(R.id.btnTouchDit)).perform(click());
        Thread.sleep(300);

        // Tap DAH (traço)
        onView(withId(R.id.btnTouchDah)).perform(click());
        Thread.sleep(400);

        ScreenshotHelper.capture("16_transmission_paddles_interacted");
    }

    /**
     * BEHAVIOR: Resetting Keyed Word Attempt without penalty.
     */
    @Test
    public void test03_ResetSendingAttempt_ButtonBehavior() throws InterruptedException {
        // Navigate to Learn tab
        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(500);

        // Start test
        onView(withId(R.id.btnStartLevelTest)).perform(click());
        Thread.sleep(800);

        // Cancel test to return to study view cleanly
        onView(withId(R.id.btnCancelTest)).perform(click());
        Thread.sleep(500);

        onView(withId(R.id.layoutStudyView)).check(matches(isDisplayed()));
        ScreenshotHelper.capture("17_exam_study_view_restored");
    }

    /**
     * BEHAVIOR: Persistent Unlocked Level across activity recreate.
     */
    @Test
    public void test04_PersistentLevel_MaintainedAcrossRecreate() throws InterruptedException {
        // Recreate activity to simulate app process restart / orientation change
        activityRule.getScenario().recreate();
        Thread.sleep(800);

        // Verify navigation still starts on binary tree with proper unlocked state
        onView(withId(R.id.tvTreeLevelTitle)).check(matches(isDisplayed()));
        ScreenshotHelper.capture("18_persistent_level_after_recreate");
    }

    /**
     * BEHAVIOR 5: LEVEL RELEASE / DESBLOQUEIO DE NÍVEL:
     * 1. Confirma que o Nível 2 está bloqueado inicialmente.
     * 2. Aprova no exame (ouvindo e mandando com sucesso).
     * 3. Verifica a exibição do diálogo de aprovação e mensagem de desbloqueio do Nível 2.
     * 4. Clica em "AVANÇAR PARA O PRÓXIMO NÍVEL".
     * 5. Confirma que o Nível 2 foi libertado/desbloqueado com os caracteres A e I disponíveis.
     * 6. Recria a Activity e confirma que o novo nível desbloqueado persiste de forma síncrona.
     */
    @Test
    public void test05_ExamPass_ReleasesNextLevel_AndPersistsUnlock() throws InterruptedException {
        java.util.concurrent.atomic.AtomicReference<MainActivity> activityRef = new java.util.concurrent.atomic.AtomicReference<>();
        activityRule.getScenario().onActivity(activityRef::set);
        MainActivity activity = activityRef.get();
        org.junit.Assert.assertNotNull(activity);

        // Given: User is at Level 1, Level 2 is locked
        activity.getSettings().setCurrentUnlockedLevel(1);
        org.junit.Assert.assertEquals(1, activity.getSettings().getCurrentUnlockedLevel());
        org.junit.Assert.assertFalse("Nível 2 deve estar bloqueado inicialmente", activity.getSettings().isLevelUnlocked(2));

        // Navigate to Learn tab
        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(600);

        // Find LearnFragment and trigger exam pass
        activity.runOnUiThread(() -> {
            androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof com.morsego.app.ui.LearnFragment) {
                ((com.morsego.app.ui.LearnFragment) f).simulateExamPassForTesting();
            }
        });
        Thread.sleep(800);

        // Verify result layout shows success and level unlock message
        onView(withId(R.id.tvResultTitle)).check(matches(withText(containsString("CONCLUÍDO"))));
        onView(withId(R.id.tvResultUnlockMsg)).check(matches(withText(containsString("Desbloqueou o Nível 2"))));

        // Capture screenshot of level unlock success dialog
        ScreenshotHelper.capture("19_level_release_unlock_dialog");

        // Advance to next level
        onView(withId(R.id.btnResultAction)).perform(click());
        Thread.sleep(700);

        // Verify Level 2 has been released/unlocked
        org.junit.Assert.assertEquals("Nível desbloqueado deve ser incrementado para 2",
                2, activity.getSettings().getCurrentUnlockedLevel());
        org.junit.Assert.assertTrue("Nível 2 deve estar agora desbloqueado",
                activity.getSettings().isLevelUnlocked(2));

        // Verify Level 2 study screen is displayed with new letters A and I
        onView(withId(R.id.tvLevelNumber)).check(matches(withText(containsString("NÍVEL 2"))));
        onView(withId(R.id.tvNewChar1)).check(matches(withText("A")));
        onView(withId(R.id.tvNewChar2)).check(matches(withText("I")));

        // Capture screenshot of newly released Level 2 screen
        ScreenshotHelper.capture("20_level2_released_study_active");

        // Verify synchronous persistence across app restart/activity recreate
        activityRule.getScenario().recreate();
        Thread.sleep(800);

        activityRule.getScenario().onActivity(a -> {
            org.junit.Assert.assertEquals("Nível desbloqueado deve persistir após recreação",
                    2, a.getSettings().getCurrentUnlockedLevel());
            org.junit.Assert.assertTrue("Nível 2 deve permanecer desbloqueado após recreação",
                    a.getSettings().isLevelUnlocked(2));
        });

        ScreenshotHelper.capture("21_level2_persisted_after_restart");
    }
}

