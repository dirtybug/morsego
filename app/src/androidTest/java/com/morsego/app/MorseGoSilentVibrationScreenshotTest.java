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

/**
 * Dedicated E2E Behavior & UI Screenshot Test for Silent Vibration Mode and Sound Mode.
 * Verifies banner visibility, Morse signal routing (vibration vs tone synthesis),
 * on-screen paddle keyer interaction, and captures visual screenshot artifacts for each state.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MorseGoSilentVibrationScreenshotTest {

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

    /**
     * TEST 1: Activate Silent / Vibration Mode.
     * Banner must be displayed, CW signals route to vibration, and a screenshot is captured.
     */
    @Test
    public void test01_SilentVibrationMode_BannerDisplayed_Screenshot() throws InterruptedException {
        // Force silent / vibration mode
        activityRule.getScenario().onActivity(activity -> {
            activity.setSilentModeForced(true);
        });
        Thread.sleep(500);

        // Verify top banner is visible
        onView(withId(R.id.bannerSilentMode)).check(matches(isDisplayed()));
        onView(withId(R.id.tvSilentWarning)).check(matches(isDisplayed()));
        onView(withId(R.id.tvSilentWarning)).check(matches(withText(anyOf(
                containsString("silêncio"),
                containsString("silent"),
                containsString("Vibração"),
                containsString("vibration")
        ))));

        // Verify Morse playback triggers vibration
        activityRule.getScenario().onActivity(activity -> {
            Assert.assertTrue("Device must be reported in silent mode", activity.isDeviceInSilentMode());
            Assert.assertTrue("Banner must be visible", activity.isSilentBannerVisible());
            activity.playMorse(".-", 15, null);
            Assert.assertEquals("Playback must use VIBRATION", "VIBRATION", activity.getLastMorsePlayType());
        });
        Thread.sleep(400);

        // Capture screenshot of Silent Vibration mode
        ScreenshotHelper.capture("screenshot_silent_vibration_mode_active");

        // Clean up override
        activityRule.getScenario().onActivity(activity -> activity.setSilentModeForced(null));
    }

    /**
     * TEST 2: Activate Normal Sound Mode.
     * Banner must be hidden (GONE), CW signals route to synthesizer audio, and a screenshot is captured.
     */
    @Test
    public void test02_SoundMode_BannerHidden_Screenshot() throws InterruptedException {
        // Force sound mode (silent mode inactive)
        activityRule.getScenario().onActivity(activity -> {
            activity.setSilentModeForced(false);
        });
        Thread.sleep(500);

        // Verify top banner is GONE
        onView(withId(R.id.bannerSilentMode)).check(matches(not(isDisplayed())));

        // Verify Morse playback triggers audio tone
        activityRule.getScenario().onActivity(activity -> {
            Assert.assertFalse("Device must not be in silent mode", activity.isDeviceInSilentMode());
            Assert.assertFalse("Banner must not be visible", activity.isSilentBannerVisible());
            activity.playMorse(".-", 15, null);
            Assert.assertEquals("Playback must use AUDIO", "AUDIO", activity.getLastMorsePlayType());
        });
        Thread.sleep(400);

        // Capture screenshot of Sound mode
        ScreenshotHelper.capture("screenshot_sound_mode_active");

        // Clean up override
        activityRule.getScenario().onActivity(activity -> activity.setSilentModeForced(null));
    }

    /**
    /**
     * TEST 3: Send Tab Touch Paddle Keying in Silent Vibration Mode and Sound Mode with Screenshots.
     * Dedicated paddles must vibrate when in silent mode and play sound when in sound mode.
     */
    @Test
    public void test03_SendPaddles_SilentAndSound_Screenshots() throws InterruptedException {
        // Navigate to Send tab (transmission exam)
        onView(withId(R.id.nav_send)).perform(click());
        Thread.sleep(500);

        // 1. Silent mode paddle keying (Vibration without sound)
        activityRule.getScenario().onActivity(activity -> activity.setSilentModeForced(true));
        Thread.sleep(400);
        onView(withId(R.id.bannerSilentMode)).check(matches(isDisplayed()));

        // Tap Dit paddle in silent mode
        onView(withId(R.id.btnTouchDit)).perform(click());
        Thread.sleep(300);
        ScreenshotHelper.capture("screenshot_keyer_silent_vibration_mode");

        // 2. Sound mode paddle keying
        activityRule.getScenario().onActivity(activity -> activity.setSilentModeForced(false));
        Thread.sleep(400);
        onView(withId(R.id.bannerSilentMode)).check(matches(not(isDisplayed())));

        // Tap Dah paddle in sound mode
        onView(withId(R.id.btnTouchDah)).perform(click());
        Thread.sleep(300);
        ScreenshotHelper.capture("screenshot_keyer_sound_mode");

        // Clean up override
        activityRule.getScenario().onActivity(activity -> activity.setSilentModeForced(null));
    }

    /**
     * TEST 4: Receive Tab Acoustic Practice in Silent Mode: Vibration without Sound.
     * Verifies that in silent mode, acoustic reception question is delivered via haptic vibration.
     */
    @Test
    public void test04_ReceiveExam_SilentMode_VibrationWithoutSound_Screenshot() throws InterruptedException {
        // Force silent / vibration mode
        activityRule.getScenario().onActivity(activity -> activity.setSilentModeForced(true));
        Thread.sleep(400);

        // Navigate directly to Receive tab
        onView(withId(R.id.nav_receive)).perform(click());
        Thread.sleep(500);

        // Verify banner is visible
        onView(withId(R.id.bannerSilentMode)).check(matches(isDisplayed()));

        // Tap play question audio in silent mode - must trigger vibration without sound
        onView(withId(R.id.btnPlayQuestionAudio)).perform(click());
        Thread.sleep(300);

        activityRule.getScenario().onActivity(activity -> {
            Assert.assertTrue("Phone must be in silent mode", activity.isDeviceInSilentMode());
            Assert.assertEquals("Question audio in silent mode must be routed to VIBRATION", "VIBRATION", activity.getLastMorsePlayType());
        });

        // Capture screenshot of Receive stage in silent vibration mode
        ScreenshotHelper.capture("screenshot_receive_silent_vibration_mode");

        // Clean up override
        activityRule.getScenario().onActivity(activity -> activity.setSilentModeForced(null));
    }
}
