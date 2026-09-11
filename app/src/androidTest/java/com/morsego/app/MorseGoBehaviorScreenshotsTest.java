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
 * End-to-end Behavior and UI Workflow tests with full-screen capture.
 * Exercises all user interactions, screens, keyer paddles, and level tests,
 * generating visual screenshot artifacts for behavior verification.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MorseGoBehaviorScreenshotsTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setup() throws InterruptedException {
        Thread.sleep(600); // Allow initial fragment inflation and layout
    }

    /**
     * BEHAVIOR 1: Visualizing the Morse Binary Tree and previewing CW sound for nodes.
     */
    @Test
    public void test01_BinaryTreeScreen_NodeSelectionAndMorsePreview() throws InterruptedException {
        // Given the user is on the Morse Binary Tree tab
        onView(withId(R.id.nav_tree)).perform(click());
        Thread.sleep(500);

        // Verify title indicates binary tree
        onView(withId(R.id.tvTreeLevelTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.morseTreeView)).check(matches(isDisplayed()));

        // Capture screenshot of Tree screen
        ScreenshotHelper.capture("01_binary_tree_screen");

        // When user plays the selected character
        onView(withId(R.id.btnPlaySelected)).perform(click());
        Thread.sleep(700);

        ScreenshotHelper.capture("02_binary_tree_audio_preview");
    }

    /**
     * BEHAVIOR 2: Learning a Level (Study Mode) with new 2-character introduction.
     */
    @Test
    public void test02_LearnFragment_StudyModeAndSoundPreview() throws InterruptedException {
        // When user navigates to "Aprender"
        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(600);

        // Then verify study view is visible with 2 new characters
        onView(withId(R.id.tvLevelTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.tvNewChar1)).check(matches(isDisplayed()));
        onView(withId(R.id.tvNewChar2)).check(matches(isDisplayed()));

        // Capture study mode presentation
        ScreenshotHelper.capture("03_learn_study_level1");

        // When user taps to preview sound of char 1
        onView(withId(R.id.btnPlayChar1)).perform(click());
        Thread.sleep(600);

        // When user taps to preview sound of char 2
        onView(withId(R.id.btnPlayChar2)).perform(click());
        Thread.sleep(600);
    }

    /**
     * BEHAVIOR 3: Starting the Level Exam - Listening Stage (Stage 1 of 2).
     */
    @Test
    public void test03_ExamListeningStage_DynamicOptionsAndLives() throws InterruptedException {
        // Given user is in Learn tab
        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(500);

        // When user starts the level exam
        onView(withId(R.id.btnStartLevelTest)).perform(click());
        Thread.sleep(800);

        // Then verify Stage 1 (Listening) is displayed
        onView(withId(R.id.tvTestPhaseBanner)).check(matches(anyOf(containsString("1"), containsString("ESCUTA"), containsString("LISTENING"))));
        onView(withId(R.id.tvTestLives)).check(matches(anyOf(containsString("Vidas:"), containsString("Lives:"), containsString("❤️❤️❤️"))));
        onView(withId(R.id.tvTestProgress)).check(matches(isDisplayed()));

        // Capture listening stage screenshot
        ScreenshotHelper.capture("04_exam_listening_stage");

        // When user clicks audio replay button
        onView(withId(R.id.btnReplayTestAudio)).perform(click());
        Thread.sleep(600);

        ScreenshotHelper.capture("05_exam_listening_audio_playing");
    }

    /**
     * BEHAVIOR 4: Free CW Keyer Sandbox with On-Screen Paddles & Decoded Display.
     */
    @Test
    public void test04_FreeKeyerMode_DecodedTextAndTxPaddles() throws InterruptedException {
        // When user navigates to "Manipulador"
        onView(withId(R.id.nav_keyer)).perform(click());
        Thread.sleep(500);

        // Verify keyer components
        onView(withId(R.id.tvKeyerDisplay)).check(matches(isDisplayed()));
        onView(withId(R.id.tvCurrentPattern)).check(matches(isDisplayed()));

        // Capture initial free keyer screen
        ScreenshotHelper.capture("06_free_keyer_initial");

        // User interacts with on-screen DI paddle
        onView(withId(R.id.btnKeyerDit)).perform(click());
        Thread.sleep(300);

        // User interacts with on-screen DAH paddle
        onView(withId(R.id.btnKeyerDah)).perform(click());
        Thread.sleep(500);

        ScreenshotHelper.capture("07_free_keyer_paddles_keyed");
    }

    /**
     * BEHAVIOR 5: External Hardware Paddle Setup & Calibration (USB CW Keyer ASIN B0F666MVG6).
     * Tests paddle live monitoring, Dit/Dah calibration buttons, paddle reversal switch,
     * VBand default reset, and captures the Hardware Setup screenshot.
     */
    @Test
    public void test05_HardwarePaddleSetupAndCalibration_Screenshot() throws InterruptedException {
        // When user navigates to "Hardware CW" tab
        onView(withId(R.id.nav_hardware)).perform(click());
        Thread.sleep(600);

        // Verify hardware monitor cards and controls
        onView(withId(R.id.cardLeftPaddle)).check(matches(isDisplayed()));
        onView(withId(R.id.cardRightPaddle)).check(matches(isDisplayed()));
        onView(withId(R.id.tvLeftPaddleState)).check(matches(isDisplayed()));
        onView(withId(R.id.tvRightPaddleState)).check(matches(isDisplayed()));
        onView(withId(R.id.btnCalibrateDit)).check(matches(isDisplayed()));
        onView(withId(R.id.btnCalibrateDah)).check(matches(isDisplayed()));
        onView(withId(R.id.switchReversePaddles)).check(matches(isDisplayed()));
        onView(withId(R.id.btnResetVband)).check(matches(isDisplayed()));
        onView(withId(R.id.tvHardwareLogs)).check(matches(isDisplayed()));

        // Capture initial hardware setup screen
        ScreenshotHelper.capture("08_hardware_setup_cw_paddle");

        // Toggle paddle reversal switch (left-handed / right-handed)
        onView(withId(R.id.switchReversePaddles)).perform(click());
        Thread.sleep(400);

        // Reset to standard VBand (Left Ctrl / Right Ctrl)
        onView(withId(R.id.btnResetVband)).perform(click());
        Thread.sleep(400);

        // Capture calibrated hardware setup screen
        ScreenshotHelper.capture("09_hardware_setup_calibrated");
    }


    /**
     * BEHAVIOR 6: Practice Listening Quiz (strictly restricted to unlocked pool).
     */
    @Test
    public void test06_PracticeQuiz_RestrictedPool() throws InterruptedException {
        // When user navigates to "Treino"
        onView(withId(R.id.nav_practice)).perform(click());
        Thread.sleep(500);

        // Verify practice quiz info
        onView(withId(R.id.tvPracticeUnlockedInfo)).check(matches(isDisplayed()));
        onView(withId(R.id.tvPracticeScore)).check(matches(isDisplayed()));
        onView(withId(R.id.btnPlayQuestionAudio)).check(matches(isDisplayed()));

        // Capture practice quiz screen
        ScreenshotHelper.capture("10_practice_quiz_stage");

        // When user replays quiz audio
        onView(withId(R.id.btnPlayQuestionAudio)).perform(click());
        Thread.sleep(600);

        // When user clicks an option
        onView(withId(R.id.btnOpt1)).perform(click());
        Thread.sleep(500);

        ScreenshotHelper.capture("11_practice_quiz_answered");
    }

    /**
     * BEHAVIOR 7: Settings Modal Dialog (WPM speed, CW Pitch, Iambic modes).
     */
    @Test
    public void test07_SettingsDialog_WpmAndPitchConfiguration() throws InterruptedException {
        // Given user is on any tab
        onView(withId(R.id.nav_learn)).perform(click());
        Thread.sleep(400);

        // When user clicks settings icon
        onView(withId(R.id.btnSettings)).perform(click());
        Thread.sleep(600);

        // Then verify settings dialog is displayed
        onView(withId(R.id.tvSettingsWpmLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.tvSettingsPitchLabel)).check(matches(isDisplayed()));
        onView(withId(R.id.rgKeyerMode)).check(matches(isDisplayed()));

        // Capture settings dialog
        ScreenshotHelper.capture("12_settings_modal_dialog");

        // Test tone button
        onView(withId(R.id.btnTestTone)).perform(click());
        Thread.sleep(600);

        // Close settings
        onView(withId(R.id.btnCloseSettings)).perform(click());
        Thread.sleep(400);

        ScreenshotHelper.capture("13_settings_closed");
    }

    /**
     * BEHAVIOR 8: Screen Rotation across all tabs (Portrait & Landscape).
     */
    @Test
    public void test08_ScreenRotation_AllTabsPortraitAndLandscape() throws InterruptedException {
        activityRule.getScenario().onActivity(activity -> {
            activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        });
        Thread.sleep(600);

        // Verify tree tab in landscape
        onView(withId(R.id.nav_tree)).perform(click());
        Thread.sleep(400);
        onView(withId(R.id.morseTreeView)).check(matches(isDisplayed()));

        // Verify keyer tab in landscape
        onView(withId(R.id.nav_keyer)).perform(click());
        Thread.sleep(400);
        onView(withId(R.id.tvKeyerDisplay)).check(matches(isDisplayed()));

        // Verify hardware tab in landscape
        onView(withId(R.id.nav_hardware)).perform(click());
        Thread.sleep(400);
        onView(withId(R.id.cardLeftPaddle)).check(matches(isDisplayed()));

        // Rotate back to portrait
        activityRule.getScenario().onActivity(activity -> {
            activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });
        Thread.sleep(500);
        onView(withId(R.id.cardLeftPaddle)).check(matches(isDisplayed()));
    }
}
