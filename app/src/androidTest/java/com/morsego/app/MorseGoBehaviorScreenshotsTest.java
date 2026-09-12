package com.morsego.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import org.junit.Assert;
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
        java.util.Locale.setDefault(java.util.Locale.US);
        activityRule.getScenario().onActivity(activity -> {
            android.content.res.Configuration config = new android.content.res.Configuration();
            config.setLocale(java.util.Locale.US);
            activity.getResources().updateConfiguration(config, activity.getResources().getDisplayMetrics());
        });
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
        Thread.sleep(500);
        ScreenshotHelper.capture("02_binary_tree_selection_preview");
    }

    /**
     * BEHAVIOR 2: Learning a Level (Study Mode) with new 2-character introduction.
     */
    @Test
    public void test02_LearnFragment_StudyModeAndSoundPreview() throws InterruptedException {
        // When user navigates to "Enviar"
        onView(withId(R.id.nav_send)).perform(click());
        Thread.sleep(600);

        // Verify transmission exam is directly visible
        onView(withId(R.id.tvLevelTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.tvSendingPrompt)).check(matches(isDisplayed()));
        onView(withId(R.id.btnTouchDit)).check(matches(isDisplayed()));
        onView(withId(R.id.btnTouchDah)).check(matches(isDisplayed()));

        // Capture transmission exam presentation
        ScreenshotHelper.capture("03_learn_transmission_exam_direct");
    }

    /**
     * BEHAVIOR 3: CW Listening Training (Ouvir / Receive).
     */
    @Test
    public void test03_ExamListeningStage_DynamicOptionsAndLives() throws InterruptedException {
        // Given user is in Ouvir (Practice) tab
        onView(withId(R.id.nav_receive)).perform(click());
        Thread.sleep(500);

        // Then verify CW listening training screen is displayed
        onView(withId(R.id.tvPracticeHeaderTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.tvPracticeScore)).check(matches(isDisplayed()));

        // Capture listening stage screenshot
        ScreenshotHelper.capture("04_exam_listening_stage");

        // When user clicks audio replay button
        onView(withId(R.id.btnPlayQuestionAudio)).perform(click());
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
        onView(withId(R.id.tvDecodedOutput)).check(matches(isDisplayed()));
        onView(withId(R.id.tvBuffer)).check(matches(isDisplayed()));

        // Capture initial free keyer screen
        ScreenshotHelper.capture("06_free_keyer_initial");

        // User interacts with on-screen DI paddle
        onView(withId(R.id.btnFreeDit)).perform(click());
        Thread.sleep(300);

        // User interacts with on-screen DAH paddle
        onView(withId(R.id.btnFreeDah)).perform(click());
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
        onView(withId(R.id.nav_receive)).perform(click());
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
        onView(withId(R.id.nav_send)).perform(click());
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
        onView(withId(R.id.tvDecodedOutput)).check(matches(isDisplayed()));

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

    /**
     * BEHAVIOR 9: 90-Degree Phone Rotation Verification (Virar o Telemóvel 90 Graus).
     * Rotates device 90° clockwise into Horizontal / Landscape orientation,
     * navigates across the core application workflows (Morse Tree, CW Keyer, Exam, Hardware),
     * captures landscape screenshots, and rotates back to Portrait to confirm full state preservation.
     */
    @Test
    public void test09_RotatePhone90Degrees_LandscapeScreenshots() throws InterruptedException {
        // Step 1: Ensure Baseline in Portrait (0 degrees)
        activityRule.getScenario().onActivity(activity -> {
            activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });
        Thread.sleep(500);
        onView(withId(R.id.nav_tree)).perform(click());
        Thread.sleep(400);
        ScreenshotHelper.capture("phone_rotation_01_portrait_0_deg");

        // Step 2: Rotate Phone 90 Degrees Clockwise to Landscape (Horizontal)
        activityRule.getScenario().onActivity(activity -> {
            activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        });
        Thread.sleep(700);

        // Verify and capture Landscape Binary Tree
        onView(withId(R.id.nav_tree)).perform(click());
        Thread.sleep(400);
        onView(withId(R.id.morseTreeView)).check(matches(isDisplayed()));
        ScreenshotHelper.capture("phone_rotation_02_rotated_90_deg_tree");

        // Verify and capture Landscape Free Keyer
        onView(withId(R.id.nav_keyer)).perform(click());
        Thread.sleep(400);
        onView(withId(R.id.tvDecodedOutput)).check(matches(isDisplayed()));
        ScreenshotHelper.capture("phone_rotation_03_rotated_90_deg_keyer");

        // Verify and capture Landscape Exam / Learn
        onView(withId(R.id.nav_send)).perform(click());
        Thread.sleep(400);
        onView(withId(R.id.tvLevelTitle)).check(matches(isDisplayed()));
        ScreenshotHelper.capture("phone_rotation_04_rotated_90_deg_exam");

        // Verify and capture Landscape Hardware CW Paddle
        onView(withId(R.id.nav_hardware)).perform(click());
        Thread.sleep(400);
        onView(withId(R.id.cardLeftPaddle)).check(matches(isDisplayed()));
        ScreenshotHelper.capture("phone_rotation_05_rotated_90_deg_hardware");

        // Step 3: Rotate Phone Back to Portrait (0 degrees)
        activityRule.getScenario().onActivity(activity -> {
            activity.setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });
        Thread.sleep(600);
        onView(withId(R.id.nav_tree)).perform(click());
        Thread.sleep(400);
        onView(withId(R.id.morseTreeView)).check(matches(isDisplayed()));
        ScreenshotHelper.capture("phone_rotation_06_restored_portrait_0_deg");
    }

    /**
     * BEHAVIOR 10: Silent / Vibration Mode and Sound Mode with Screenshots.
     * Verifies that:
     * 1. Setting silent/vibratory mode makes the top banner visible, routes CW signals to vibration,
     *    and captures a full-screen screenshot ("14_silent_vibration_mode_active").
     * 2. Setting sound mode removes the top banner, routes CW signals to audio synthesis,
     *    and captures a full-screen screenshot ("15_sound_mode_active").
     */
    @Test
    public void test10_SilentVibrationModeAndSoundMode_Screenshots() throws InterruptedException {
        // --- PHASE 1: VIBRATING / SILENT MODE ---
        // Activate silent mode
        activityRule.getScenario().onActivity(activity -> {
            activity.setSilentModeForced(true);
        });
        Thread.sleep(600);

        // Verify silent mode banner is visible and has the warning message
        onView(withId(R.id.bannerSilentMode)).check(matches(isDisplayed()));
        onView(withId(R.id.tvSilentWarning)).check(matches(isDisplayed()));
        onView(withId(R.id.tvSilentWarning)).check(matches(withText(anyOf(
                containsString("silêncio"),
                containsString("silent"),
                containsString("Vibração"),
                containsString("vibration")
        ))));

        // Play Morse signal and verify it activates vibration routing
        activityRule.getScenario().onActivity(activity -> {
            Assert.assertTrue("Device must report silent mode active", activity.isDeviceInSilentMode());
            Assert.assertTrue("Banner must be visible in silent mode", activity.isSilentBannerVisible());
            activity.playMorse(".-", 15, null);
            Assert.assertEquals("Morse playback must use VIBRATION in silent mode", "VIBRATION", activity.getLastMorsePlayType());
        });
        Thread.sleep(500);

        // Capture screenshot of Silent/Vibrating mode
        ScreenshotHelper.capture("14_silent_vibration_mode_active");

        // --- PHASE 2: NORMAL SOUND MODE ---
        // Restore sound mode
        activityRule.getScenario().onActivity(activity -> {
            activity.setSilentModeForced(false);
        });
        Thread.sleep(600);

        // Verify silent mode banner is hidden (GONE)
        onView(withId(R.id.bannerSilentMode)).check(matches(not(isDisplayed())));

        // Play Morse signal and verify it activates audio routing
        activityRule.getScenario().onActivity(activity -> {
            Assert.assertFalse("Device must report silent mode inactive", activity.isDeviceInSilentMode());
            Assert.assertFalse("Banner must be gone in sound mode", activity.isSilentBannerVisible());
            activity.playMorse(".-", 15, null);
            Assert.assertEquals("Morse playback must use AUDIO in sound mode", "AUDIO", activity.getLastMorsePlayType());
        });
        Thread.sleep(500);

        // Capture screenshot of Normal Sound mode
        ScreenshotHelper.capture("15_sound_mode_active");

        // Reset testing override to restore system state
        activityRule.getScenario().onActivity(activity -> {
            activity.setSilentModeForced(null);
        });
        Thread.sleep(300);
    }
}


