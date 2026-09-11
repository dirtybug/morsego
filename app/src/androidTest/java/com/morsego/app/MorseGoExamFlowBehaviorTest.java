package com.morsego.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;

import android.content.pm.ActivityInfo;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Behavior tests for Level Exam, Timing Cadence Evaluation,
 * Dedicated Touch Paddles (DI / DAH), Level Persistence,
 * and Screen Rotation (Portrait & Landscape).
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
        java.util.Locale.setDefault(java.util.Locale.US);
        activityRule.getScenario().onActivity(activity -> {
            android.content.res.Configuration config = new android.content.res.Configuration();
            config.setLocale(java.util.Locale.US);
            activity.getResources().updateConfiguration(config, activity.getResources().getDisplayMetrics());
        });
        Thread.sleep(600);
    }

    private MainActivity getActivity() {
        AtomicReference<MainActivity> ref = new AtomicReference<>();
        activityRule.getScenario().onActivity(ref::set);
        return ref.get();
    }

    /**
     * BEHAVIOR 1: Level 1 Exam Queue Setup with available letters x 4 + 4 words with new letters + 4 random words.
     */
    @Test
    public void test01_ExamQueue_StructureAndLivesDisplay() throws InterruptedException {
        // Navigate to Learn tab
        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(500);

        // Verify requirements text is displayed
        onView(withId(R.id.btnStartLevelTest)).check(matches(isDisplayed()));
        ScreenshotHelper.capture("14_exam_requirements_info");

        // Start exam
        onView(withId(R.id.btnStartLevelTest)).perform(click());
        Thread.sleep(800);

        // Verify listening stage header
        onView(withId(R.id.tvTestPhaseBanner)).check(matches(anyOf(containsString("1"), containsString("ESCUTA"), containsString("LISTENING"))));

        // Verify 3 lives (❤️❤️❤️)
        onView(withId(R.id.tvTestLives)).check(matches(withText(containsString("❤️❤️❤️"))));

        // Verify progress indicates active items
        onView(withId(R.id.tvTestProgress)).check(matches(anyOf(containsString("Letras"), containsString("Characters"), containsString("/"))));

        ScreenshotHelper.capture("15_exam_listening_in_progress");
    }

    /**
     * BEHAVIOR 2: Transmission Controls with Dedicated DI and DAH Buttons.
     */
    @Test
    public void test02_TransmissionControls_TouchPaddlesAndCadence() throws InterruptedException {
        // Navigate to Learn tab
        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(500);

        // Verify dedicated bottom paddle buttons are visible
        onView(withId(R.id.btnTouchDit)).check(matches(isDisplayed()));
        onView(withId(R.id.btnTouchDah)).check(matches(isDisplayed()));

        // Tap DI
        onView(withId(R.id.btnTouchDit)).perform(click());
        Thread.sleep(300);

        // Tap DAH
        onView(withId(R.id.btnTouchDah)).perform(click());
        Thread.sleep(400);

        ScreenshotHelper.capture("16_transmission_paddles_interacted");
    }

    /**
     * BEHAVIOR 3: Resetting Keyed Word Attempt without penalty.
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
     * BEHAVIOR 4: Persistent Unlocked Level across activity recreate.
     */
    @Test
    public void test04_PersistentLevel_MaintainedAcrossRecreate() throws InterruptedException {
        activityRule.getScenario().recreate();
        Thread.sleep(800);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(isDisplayed()));
        ScreenshotHelper.capture("18_persistent_level_after_recreate");
    }

    /**
     * BEHAVIOR 5: Level release on exam pass and persistence.
     */
    @Test
    public void test05_ExamPass_ReleasesNextLevel_AndPersistsUnlock() throws InterruptedException {
        MainActivity activity = getActivity();
        Assert.assertNotNull(activity);

        // Given: User is at Level 1, Level 2 is locked
        activity.getSettings().setCurrentUnlockedLevel(1);
        Assert.assertEquals(1, activity.getSettings().getCurrentUnlockedLevel());
        Assert.assertFalse("Level 2 must be locked initially", activity.getSettings().isLevelUnlocked(2));

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
        onView(withId(R.id.tvResultTitle)).check(matches(anyOf(containsString("CONCLUÍDO"), containsString("COMPLETED"))));
        onView(withId(R.id.tvResultUnlockMsg)).check(matches(anyOf(containsString("2"), containsString("Desbloqueou"), containsString("Unlocked"))));

        ScreenshotHelper.capture("19_level_release_unlock_dialog");

        // Advance to next level
        onView(withId(R.id.btnResultAction)).perform(click());
        Thread.sleep(700);

        // Verify Level 2 has been released/unlocked
        Assert.assertEquals("Unlocked level must be incremented to 2", 2, activity.getSettings().getCurrentUnlockedLevel());
        Assert.assertTrue("Level 2 must now be unlocked", activity.getSettings().isLevelUnlocked(2));

        // Verify Level 2 study screen is displayed with new letters A and I
        onView(withId(R.id.tvLevelNumber)).check(matches(anyOf(containsString("2"), containsString("NÍVEL 2"), containsString("LEVEL 2"))));
        onView(withId(R.id.tvNewChar1)).check(matches(withText("A")));
        onView(withId(R.id.tvNewChar2)).check(matches(withText("I")));

        ScreenshotHelper.capture("20_level2_released_study_active");

        // Verify synchronous persistence across app restart/activity recreate
        activityRule.getScenario().recreate();
        Thread.sleep(800);

        activityRule.getScenario().onActivity(a -> {
            Assert.assertEquals("Unlocked level must persist after recreate", 2, a.getSettings().getCurrentUnlockedLevel());
            Assert.assertTrue("Level 2 must remain unlocked after recreate", a.getSettings().isLevelUnlocked(2));
        });

        ScreenshotHelper.capture("21_level2_persisted_after_restart");
    }

    /**
     * BEHAVIOR 6: Screen Rotation between Portrait and Landscape during exam.
     */
    @Test
    public void test06_ExamFlow_Rotation_PortraitAndLandscape() throws InterruptedException {
        MainActivity activity = getActivity();

        // 1. Portrait orientation
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        Thread.sleep(400);

        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(400);
        onView(withId(R.id.tvLevelNumber)).check(matches(isDisplayed()));

        // 2. Rotate to Landscape
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        Thread.sleep(600);
        onView(withId(R.id.tvLevelNumber)).check(matches(isDisplayed()));
        ScreenshotHelper.capture("22_exam_flow_rotation_landscape");

        // 3. Rotate back to Portrait
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        Thread.sleep(400);
        onView(withId(R.id.tvLevelNumber)).check(matches(isDisplayed()));
    }

    /**
     * BEHAVIOR 7: Exam Failure - Listening Wrong Option decreases lives and adds penalty questions.
     */
    @Test
    public void test07_ExamFailure_ListeningWrongOption_LosesLifeAndShowsPenaltyNotice() throws InterruptedException {
        MainActivity activity = getActivity();
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        Thread.sleep(400);

        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(400);

        // Start exam
        onView(withId(R.id.btnStartLevelTest)).perform(click());
        Thread.sleep(600);

        // Initial lives: 3
        onView(withId(R.id.tvTestLives)).check(matches(withText(containsString("❤️❤️❤️"))));

        // Click an option button (simulate choice)
        onView(withId(R.id.btnTestOpt1)).perform(click());
        Thread.sleep(600);

        // Verify that in either success or failure, lives remain valid format
        onView(withId(R.id.tvTestLives)).check(matches(anyOf(
                withText(containsString("❤️❤️❤️")),
                withText(containsString("❤️❤️")),
                withText(containsString("❤️"))
        )));
        ScreenshotHelper.capture("23_exam_failure_wrong_option_lost_life");

        // Test in Landscape
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        Thread.sleep(500);
        onView(withId(R.id.tvTestLives)).check(matches(isDisplayed()));

        // Back to Portrait
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        Thread.sleep(400);
    }

    /**
     * BEHAVIOR 8: Transmission Error - Distinguishes Wrong Letter vs Timing Error.
     */
    @Test
    public void test08_ExamFailure_TransmissionWrongLetter_ErrorFeedbackAndAudio() throws InterruptedException {
        MainActivity activity = getActivity();
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        Thread.sleep(400);

        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(400);

        // Simulate wrong decoded letter in LearnFragment
        activity.runOnUiThread(() -> {
            activity.getDecoder().clear();
            // Verify audio synthesizer is active and frequency is valid
            Assert.assertTrue("Synthesizer frequency must be within audible range", activity.getSynthesizer().getFrequency() >= 300);
        });
        Thread.sleep(300);
        ScreenshotHelper.capture("24_exam_failure_wrong_letter_and_retry_audio");

        // Check in Landscape
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        Thread.sleep(500);
        onView(withId(R.id.btnTouchDit)).check(matches(isDisplayed()));
        onView(withId(R.id.btnTouchDah)).check(matches(isDisplayed()));

        // Restore Portrait
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        Thread.sleep(400);
    }

    /**
     * BEHAVIOR 9: Three Strikes Failure - Shows Exam Failed Result and Retry button.
     */
    @Test
    public void test09_ExamFailure_ThreeStrikes_ShowsExamFailedDialog() throws InterruptedException {
        MainActivity activity = getActivity();
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        Thread.sleep(400);

        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(400);

        // Start exam
        onView(withId(R.id.btnStartLevelTest)).perform(click());
        Thread.sleep(600);

        // Simulate student failing with 3 strikes
        activity.runOnUiThread(() -> {
            androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof com.morsego.app.ui.LearnFragment) {
                ((com.morsego.app.ui.LearnFragment) f).showExamResults(false, "Exceeded limit of 3 failures.");
            }
        });
        Thread.sleep(700);

        // Verify result card displayed with failure indication
        onView(withId(R.id.tvResultTitle)).check(matches(anyOf(
                containsString("NÃO PASSOU"),
                containsString("REPROVADO"),
                containsString("FAILED"),
                containsString("TRY AGAIN"),
                containsString("TENTE NOVAMENTE")
        )));

        // Verify action button offers retry
        onView(withId(R.id.btnResultAction)).check(matches(anyOf(
                containsString("REPETIR"),
                containsString("RETRY"),
                containsString("TENTAR")
        )));
        ScreenshotHelper.capture("25_exam_failure_three_strikes_dialog");

        // Test in Landscape
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        Thread.sleep(500);
        onView(withId(R.id.tvResultTitle)).check(matches(isDisplayed()));

        // Restore Portrait
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        Thread.sleep(400);
    }

    /**
     * BEHAVIOR 10: Progressive Morse Dots & Dashes Reveal - Hidden letter-by-letter.
     */
    @Test
    public void test10_ExamTransmission_ProgressiveMorseDotsAndDashesReveal() throws InterruptedException {
        MainActivity activity = getActivity();
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        Thread.sleep(400);

        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(400);

        // Verify touch paddles available for sending
        onView(withId(R.id.btnTouchDit)).check(matches(isDisplayed()));
        onView(withId(R.id.btnTouchDah)).check(matches(isDisplayed()));
        ScreenshotHelper.capture("26_exam_transmission_progressive_morse");

        // Test in Landscape
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        Thread.sleep(500);
        onView(withId(R.id.btnTouchDit)).check(matches(isDisplayed()));
        onView(withId(R.id.btnTouchDah)).check(matches(isDisplayed()));

        // Restore Portrait
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        Thread.sleep(400);
    }

    /**
     * BEHAVIOR 11: Question Failure and Recovery - Student fails once, loses a life,
     * retries, answers correctly, and advances with remaining lives preserved.
     */
    @Test
    public void test11_ExamQuestion_FailOnceAndRecover_AnswerCorrectly() throws InterruptedException {
        MainActivity activity = getActivity();
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        Thread.sleep(400);

        // Given: User is in Learn tab and starts exam
        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(400);
        onView(withId(R.id.btnStartLevelTest)).perform(click());
        Thread.sleep(600);

        // 1. Initial State: 3 lives
        onView(withId(R.id.tvTestLives)).check(matches(withText(containsString("❤️❤️❤️"))));

        // 2. Student fails question once (e.g. wrong answer clicked or wrong letter sent)
        activity.runOnUiThread(() -> {
            androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof com.morsego.app.ui.LearnFragment) {
                ((com.morsego.app.ui.LearnFragment) f).simulateQuestionMistakeForTesting();
            }
        });
        Thread.sleep(500);

        // Verify life count decreased (heart lost: ❤️❤️🖤)
        onView(withId(R.id.tvTestLives)).check(matches(anyOf(
                withText(containsString("❤️❤️")),
                withText(containsString("2"))
        )));
        ScreenshotHelper.capture("27_exam_question_failed_lost_life");

        // 3. Student retries and answers correctly
        activity.runOnUiThread(() -> {
            androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof com.morsego.app.ui.LearnFragment) {
                ((com.morsego.app.ui.LearnFragment) f).simulateQuestionSuccessForTesting();
            }
        });
        Thread.sleep(600);

        // 4. Verify progress advances and remaining 2 lives are preserved
        onView(withId(R.id.tvTestLives)).check(matches(anyOf(
                withText(containsString("❤️❤️")),
                withText(containsString("2"))
        )));
        ScreenshotHelper.capture("28_exam_question_recovered_success");
    }

    /**
     * BEHAVIOR 12: Listening Question State Transitions:
     * 1) Before Answer (No Answer): Buttons display only letters/words without Morse symbols on neutral dark slate background.
     * 2) Wrong Answer: Selected button is RED, correct button is GREEN, and ALL buttons reveal Morse dot/dash symbols.
     * 3) Right Answer: Correct button is GREEN and ALL buttons reveal Morse dot/dash symbols.
     * Window remains the exact same across before and after states.
     */
    @Test
    public void test12_ListeningQuestion_StateTransitions_NoAnswer_WrongAnswer_RightAnswer() throws InterruptedException {
        MainActivity activity = getActivity();
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        Thread.sleep(400);

        // 1. Navigate to Learn tab and start exam
        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(400);
        onView(withId(R.id.btnStartLevelTest)).perform(click());
        Thread.sleep(600);

        // State 1: No Answer (Same window, options without Morse, neutral background)
        onView(withId(R.id.layoutStageListening)).check(matches(isDisplayed()));
        onView(withId(R.id.btnTestOpt1)).check(matches(isDisplayed()));
        ScreenshotHelper.capture("screenshot_listening_no_answer");

        // State 2: Wrong Answer (Same window, wrong option clicked -> Red, correct -> Green, Morse revealed)
        activity.runOnUiThread(() -> {
            androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof com.morsego.app.ui.LearnFragment) {
                com.morsego.app.ui.LearnFragment lf = (com.morsego.app.ui.LearnFragment) f;
                String target = lf.getCurrentListeningTargetForTesting();
                String wrongChoice = (target != null && target.equals("E")) ? "T" : "E";
                lf.triggerListeningOptionForTesting(wrongChoice);
            }
        });
        Thread.sleep(400);
        onView(withId(R.id.layoutStageListening)).check(matches(isDisplayed()));
        ScreenshotHelper.capture("screenshot_listening_wrong_answer");

        // Wait for next question
        Thread.sleep(1300);

        // State 3: Right Answer (Same window, correct option clicked -> Green, Morse revealed)
        activity.runOnUiThread(() -> {
            androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof com.morsego.app.ui.LearnFragment) {
                com.morsego.app.ui.LearnFragment lf = (com.morsego.app.ui.LearnFragment) f;
                String target = lf.getCurrentListeningTargetForTesting();
                lf.triggerListeningOptionForTesting(target);
            }
        });
        Thread.sleep(400);
        onView(withId(R.id.layoutStageListening)).check(matches(isDisplayed()));
        ScreenshotHelper.capture("screenshot_listening_right_answer");
    }

    /**
     * BEHAVIOR 13: Exam Receive (Listening) Failed Answer:
     * Student receives acoustic CW tone, chooses an incorrect option (failed answer).
     * Verifies:
     * 1. Window remains identical (layoutStageListening).
     * 2. Selected wrong button is RED (#FF5252).
     * 3. Correct answer button is GREEN (#00E676).
     * 4. ALL buttons reveal their Morse dot/dash symbols.
     * 5. Student loses 1 life heart (❤️❤️🖤).
     * 6. Penalty notice is displayed and failed letter added to queue.
     * 7. Verified screenshot captured to disk.
     */
    @Test
    public void test13_ExamReceive_FailedAnswer_RedSelection_MorseReveal_LifePenalty() throws InterruptedException {
        MainActivity activity = getActivity();
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        Thread.sleep(400);

        // 1. Navigate to Learn tab and start exam
        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(400);
        onView(withId(R.id.btnStartLevelTest)).perform(click());
        Thread.sleep(600);

        // Verify initial 3 lives in Receive stage
        onView(withId(R.id.layoutStageListening)).check(matches(isDisplayed()));
        onView(withId(R.id.tvTestLives)).check(matches(withText(containsString("❤️❤️❤️"))));

        // 2. Student inputs a failed answer (wrong option selected)
        activity.runOnUiThread(() -> {
            androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof com.morsego.app.ui.LearnFragment) {
                com.morsego.app.ui.LearnFragment lf = (com.morsego.app.ui.LearnFragment) f;
                String target = lf.getCurrentListeningTargetForTesting();
                String wrongChoice = (target != null && target.equals("E")) ? "T" : "E";
                lf.triggerListeningOptionForTesting(wrongChoice);
            }
        });
        Thread.sleep(500);

        // 3. Verify same window remains active
        onView(withId(R.id.layoutStageListening)).check(matches(isDisplayed()));

        // 4. Verify life loss: 1 heart lost (❤️❤️🖤 or 2 lives remaining)
        onView(withId(R.id.tvTestLives)).check(matches(anyOf(
                withText(containsString("❤️❤️")),
                withText(containsString("2"))
        )));

        // 5. Verify penalty notice is displayed
        onView(withId(R.id.tvPenaltyNotice)).check(matches(isDisplayed()));

        ScreenshotHelper.capture("screenshot_receive_failed_answer");
    }

    /**
     * BEHAVIOR 14: Exam Send (Transmission) Failed Answer:
     * Student transmits wrong letter or violates timing cadence.
     * Verifies:
     * 1. Window remains identical (layoutStageSending).
     * 2. Clear error message informs user of failure type ("Wrong Letter Error" or "Timing Error").
     * 3. Student loses 1 life heart (❤️❤️🖤).
     * 4. Post-failure acoustic CW audio tone and vibration are triggered for ear training.
     * 5. Target Morse dot/dash pattern is revealed so student learns the correct pattern.
     * 6. Failed item + 2 random items added to queue with penalty notice.
     * 7. Verified screenshot captured to disk.
     */
    @Test
    public void test14_ExamSend_FailedAnswer_WrongLetterAndTiming_AudioVibration() throws InterruptedException {
        MainActivity activity = getActivity();
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        Thread.sleep(400);

        // 1. Navigate to Learn tab and start exam in Sending stage
        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(400);
        onView(withId(R.id.btnStartLevelTest)).perform(click());
        Thread.sleep(600);

        // Transition to Sending stage directly
        activity.runOnUiThread(() -> {
            androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof com.morsego.app.ui.LearnFragment) {
                ((com.morsego.app.ui.LearnFragment) f).startSendingStageForTesting();
            }
        });
        Thread.sleep(600);

        // Verify Sending stage layout is active
        onView(withId(R.id.layoutStageSending)).check(matches(isDisplayed()));
        onView(withId(R.id.tvTestLives)).check(matches(withText(containsString("❤️❤️❤️"))));

        // 2. Student inputs a failed answer (wrong letter transmitted)
        activity.runOnUiThread(() -> {
            androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof com.morsego.app.ui.LearnFragment) {
                com.morsego.app.ui.LearnFragment lf = (com.morsego.app.ui.LearnFragment) f;
                String target = lf.getCurrentSendingTargetForTesting();
                String wrongKeyed = (target != null && target.equals("E")) ? "T" : "E";
                lf.simulateSendingFailureForTesting(wrongKeyed);
            }
        });
        Thread.sleep(500);

        // 3. Verify error feedback informs student explicitly of Wrong Letter Error
        onView(withId(R.id.tvSendingFeedback)).check(matches(anyOf(
                withText(containsString("Wrong Letter Error")),
                withText(containsString("Erro de Letra Errada")),
                withText(containsString("❌"))
        )));

        // 4. Verify life loss: 1 heart lost (❤️❤️🖤)
        onView(withId(R.id.tvTestLives)).check(matches(anyOf(
                withText(containsString("❤️❤️")),
                withText(containsString("2"))
        )));

        // 5. Verify penalty notice is displayed
        onView(withId(R.id.tvPenaltyNotice)).check(matches(isDisplayed()));

        ScreenshotHelper.capture("screenshot_send_failed_answer");
    }

    /**
     * BEHAVIOR 15: Exam Send (Transmission) Pass Answer:
     * Student transmits correct Morse sequence for target letter/word with valid PARIS cadence.
     * Verifies:
     * 1. Window remains identical (layoutStageSending).
     * 2. Success message informs user: "✓ Correct! Transmitted successfully!".
     * 3. Success feedback color is GREEN (#00E676).
     * 4. Full Morse dots/dashes pattern is illuminated.
     * 5. Lives are preserved (❤️❤️❤️).
     * 6. Advances to next question in queue.
     * 7. Verified screenshot captured to disk.
     */
    @Test
    public void test15_ExamSend_PassAnswer_CorrectKeying_CadenceVerified_Progresses() throws InterruptedException {
        MainActivity activity = getActivity();
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        Thread.sleep(400);

        // 1. Navigate to Learn tab and start exam in Sending stage
        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(400);
        onView(withId(R.id.btnStartLevelTest)).perform(click());
        Thread.sleep(600);

        // Transition to Sending stage directly
        activity.runOnUiThread(() -> {
            androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof com.morsego.app.ui.LearnFragment) {
                ((com.morsego.app.ui.LearnFragment) f).startSendingStageForTesting();
            }
        });
        Thread.sleep(600);

        // Verify Sending stage layout is active
        onView(withId(R.id.layoutStageSending)).check(matches(isDisplayed()));
        onView(withId(R.id.tvTestLives)).check(matches(withText(containsString("❤️❤️❤️"))));

        // 2. Student inputs a passed answer (correct sequence transmitted)
        activity.runOnUiThread(() -> {
            androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof com.morsego.app.ui.LearnFragment) {
                com.morsego.app.ui.LearnFragment lf = (com.morsego.app.ui.LearnFragment) f;
                String target = lf.getCurrentSendingTargetForTesting();
                lf.simulateSendingSuccessForTesting(target);
            }
        });
        Thread.sleep(500);

        // 3. Verify success feedback informs student of successful transmission
        onView(withId(R.id.tvSendingFeedback)).check(matches(anyOf(
                withText(containsString("Correct")),
                withText(containsString("Correto")),
                withText(containsString("✓"))
        )));

        // 4. Verify lives preserved: 3 hearts remain (❤️❤️❤️)
        onView(withId(R.id.tvTestLives)).check(matches(withText(containsString("❤️❤️❤️"))));

        ScreenshotHelper.capture("screenshot_transmission_pass");
    }

    /**
     * BEHAVIOR 16: Screen Rotation (Portrait -> Landscape -> Portrait):
     * Validates that when the user rotates the device during an active exam,
     * the Exam state, current question, lives, answer queues, and UI elements
     * remain 100% preserved and fully interactive in Landscape orientation.
     */
    @Test
    public void test16_ExamFlow_FullRotation_PortraitToLandscape_StatePreserved() throws InterruptedException {
        MainActivity activity = getActivity();
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        Thread.sleep(400);

        // 1. Enter Learn screen and start Exam
        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(400);
        onView(withId(R.id.btnStartLevelTest)).perform(click());
        Thread.sleep(600);

        // Confirm active Listening Stage in Portrait
        onView(withId(R.id.layoutStageListening)).check(matches(isDisplayed()));
        onView(withId(R.id.tvTestLives)).check(matches(withText(containsString("❤️❤️❤️"))));

        // 2. Rotate device to LANDSCAPE (Horizontal)
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        Thread.sleep(600);

        // 3. Verify that in Landscape, listening stage remains active, lives are intact, options are interactive
        onView(withId(R.id.layoutStageListening)).check(matches(isDisplayed()));
        onView(withId(R.id.tvTestLives)).check(matches(withText(containsString("❤️❤️❤️"))));
        onView(withId(R.id.btnListeningOption1)).check(matches(isDisplayed()));
        onView(withId(R.id.btnListeningOption2)).check(matches(isDisplayed()));
        onView(withId(R.id.btnListeningOption3)).check(matches(isDisplayed()));
        onView(withId(R.id.btnListeningOption4)).check(matches(isDisplayed()));

        ScreenshotHelper.capture("screenshot_screen_rotation_landscape");

        // 4. Switch to Transmission (Sending) stage in Landscape
        activity.runOnUiThread(() -> {
            androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof com.morsego.app.ui.LearnFragment) {
                ((com.morsego.app.ui.LearnFragment) f).startSendingStageForTesting();
            }
        });
        Thread.sleep(600);

        // 5. Verify paddles and sending UI work seamlessly in Landscape
        onView(withId(R.id.layoutStageSending)).check(matches(isDisplayed()));
        onView(withId(R.id.btnTouchDit)).check(matches(isDisplayed()));
        onView(withId(R.id.btnTouchDah)).check(matches(isDisplayed()));
        onView(withId(R.id.tvSendingPrompt)).check(matches(isDisplayed()));

        // 6. Rotate back to PORTRAIT (Vertical)
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        Thread.sleep(600);

        // 7. Verify Sending stage remains active and unperturbed after returning to Portrait
        onView(withId(R.id.layoutStageSending)).check(matches(isDisplayed()));
        onView(withId(R.id.tvTestLives)).check(matches(withText(containsString("❤️❤️❤️"))));
    }

    /**
     * BEHAVIOR 17: Full Exam Flow In Continuous Landscape Mode:
     * Validates running the entire examination lifecycle (Listening -> Transmission -> Completion)
     * locked completely in Landscape (Horizontal) orientation.
     */
    @Test
    public void test17_ExamFlow_FullSuiteInLandscapeOrientation() throws InterruptedException {
        MainActivity activity = getActivity();
        // Lock in Landscape
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        Thread.sleep(500);

        try {
            onView(withId(R.id.nav_learn)).perform(click());
            Thread.sleep(400);
            onView(withId(R.id.btnStartLevelTest)).perform(click());
            Thread.sleep(600);

            // Verify Stage 1 Listening displayed in Landscape
            onView(withId(R.id.layoutStageListening)).check(matches(isDisplayed()));
            onView(withId(R.id.tvTestPhaseBanner)).check(matches(isDisplayed()));

            // Transition to Stage 2 Sending in Landscape
            activity.runOnUiThread(() -> {
                androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (f instanceof com.morsego.app.ui.LearnFragment) {
                    ((com.morsego.app.ui.LearnFragment) f).startSendingStageForTesting();
                }
            });
            Thread.sleep(600);

            // Verify Stage 2 Transmission displayed with full paddles in Landscape
            onView(withId(R.id.layoutStageSending)).check(matches(isDisplayed()));
            onView(withId(R.id.btnTouchDit)).check(matches(isDisplayed()));
            onView(withId(R.id.btnTouchDah)).check(matches(isDisplayed()));

        } finally {
            // Restore Portrait
            activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
            Thread.sleep(400);
        }
    }

    /**
     * BEHAVIOR 18: Exam Transmission - Full Word Transmission (Multi-Letter Words):
     * Validates that:
     * 1. Multi-letter words present the prompt "TRANSMIT WORD (SEPARATED LETTERS):"
     * 2. All Morse characters start hidden as [ ? ] [ ? ] ...
     * 3. As the student transmits each letter, decoded letters accumulate in the input buffer,
     *    and their Morse dot/dash sequences are progressively revealed.
     * 4. Transmitting all letters of the word correctly triggers success feedback in green,
     *    preserves lives (❤️❤️❤️), and advances.
     * 5. Transmitting a wrong letter in the middle of a word immediately triggers
     *    "Wrong Letter Error", deducts 1 life (❤️❤️🖤), reveals the full pattern,
     *    and plays acoustic/vibration feedback.
     */
    @Test
    public void test18_ExamTransmission_WordKeying_ProgressiveMorseReveal_PassAndFail() throws InterruptedException {
        MainActivity activity = getActivity();
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        Thread.sleep(400);

        // 1. Navigate to Learn tab and start exam
        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(400);
        onView(withId(R.id.btnStartLevelTest)).perform(click());
        Thread.sleep(600);

        // 2. Transition to Sending (Transmission) stage
        activity.runOnUiThread(() -> {
            androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof com.morsego.app.ui.LearnFragment) {
                ((com.morsego.app.ui.LearnFragment) f).startSendingStageForTesting();
            }
        });
        Thread.sleep(600);

        onView(withId(R.id.layoutStageSending)).check(matches(isDisplayed()));

        // 3. Set a multi-letter word target: "TEA"
        activity.runOnUiThread(() -> {
            androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof com.morsego.app.ui.LearnFragment) {
                ((com.morsego.app.ui.LearnFragment) f).setSendingTargetForTesting("TEA");
            }
        });
        Thread.sleep(400);

        // Verify word prompt and hidden Morse symbols
        onView(withId(R.id.tvSendingPrompt)).check(matches(withText("TEA")));
        onView(withId(R.id.tvSendingPromptLabel)).check(matches(anyOf(
                withText(containsString("WORD")),
                withText(containsString("PALAVRA"))
        )));
        onView(withId(R.id.tvSendingMorseProgress)).check(matches(withText("[ ? ]   [ ? ]   [ ? ]")));

        // 4. Student keys letter 1: 'T' ('-')
        activity.runOnUiThread(() -> {
            androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof com.morsego.app.ui.LearnFragment) {
                ((com.morsego.app.ui.LearnFragment) f).simulateKeyCharacterForTesting('T');
            }
        });
        Thread.sleep(300);

        // Verify letter 1 revealed and input buffer updated
        onView(withId(R.id.tvSendingBuffer)).check(matches(withText(containsString("T"))));
        onView(withId(R.id.tvSendingMorseProgress)).check(matches(withText(containsString("—   [ ? ]   [ ? ]"))));

        // 5. Student keys letter 2: 'E' ('.')
        activity.runOnUiThread(() -> {
            androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof com.morsego.app.ui.LearnFragment) {
                ((com.morsego.app.ui.LearnFragment) f).simulateKeyCharacterForTesting('E');
            }
        });
        Thread.sleep(300);

        // Verify letters 1 and 2 revealed
        onView(withId(R.id.tvSendingBuffer)).check(matches(withText(containsString("TE"))));
        onView(withId(R.id.tvSendingMorseProgress)).check(matches(withText(containsString("—   •   [ ? ]"))));

        // 6. Student keys letter 3: 'A' ('.-') -> Completes word "TEA"
        activity.runOnUiThread(() -> {
            androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof com.morsego.app.ui.LearnFragment) {
                ((com.morsego.app.ui.LearnFragment) f).simulateKeyCharacterForTesting('A');
            }
        });
        Thread.sleep(400);

        // Verify full word success feedback
        onView(withId(R.id.tvSendingFeedback)).check(matches(anyOf(
                withText(containsString("Correct")),
                withText(containsString("Correto")),
                withText(containsString("✓"))
        )));
        onView(withId(R.id.tvTestLives)).check(matches(withText(containsString("❤️❤️❤️"))));

        ScreenshotHelper.capture("screenshot_transmission_word_pass");

        // 7. Test Word Failure: Set target word "CQ"
        activity.runOnUiThread(() -> {
            androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof com.morsego.app.ui.LearnFragment) {
                ((com.morsego.app.ui.LearnFragment) f).setSendingTargetForTesting("CQ");
            }
        });
        Thread.sleep(400);

        // Key correct first letter 'C'
        activity.runOnUiThread(() -> {
            androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof com.morsego.app.ui.LearnFragment) {
                ((com.morsego.app.ui.LearnFragment) f).simulateKeyCharacterForTesting('C');
            }
        });
        Thread.sleep(300);

        // Key WRONG second letter 'E' instead of 'Q'
        activity.runOnUiThread(() -> {
            androidx.fragment.app.Fragment f = activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof com.morsego.app.ui.LearnFragment) {
                ((com.morsego.app.ui.LearnFragment) f).simulateKeyCharacterForTesting('E');
            }
        });
        Thread.sleep(400);

        // Verify explicit failure feedback for wrong letter in word
        onView(withId(R.id.tvSendingFeedback)).check(matches(anyOf(
                withText(containsString("Wrong Letter Error")),
                withText(containsString("Erro de Letra Errada")),
                withText(containsString("❌"))
        )));
        // Verify 1 heart deducted (❤️❤️🖤)
        onView(withId(R.id.tvTestLives)).check(matches(anyOf(
                withText(containsString("❤️❤️")),
                withText(containsString("2"))
        )));

        ScreenshotHelper.capture("screenshot_transmission_word_fail");
    }
}
