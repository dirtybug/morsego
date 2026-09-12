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

import com.morsego.app.tree.MorseBinaryTree;
import com.morsego.app.tree.MorseTreeNode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Instrumented UI Behavior Tests for nodes under M and N:
 * Validates visual release on canvas of nodes under N (D, K and descendants B, X, C, Y)
 * and under M (G, O and descendants Z, Q), across Portrait and Landscape orientations.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MorseGoUnderMAndNBehaviorTest {

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
        Thread.sleep(500);
        onView(withId(R.id.nav_tree)).perform(click());
        Thread.sleep(400);
    }

    private MainActivity getActivity() {
        AtomicReference<MainActivity> ref = new AtomicReference<>();
        activityRule.getScenario().onActivity(ref::set);
        return ref.get();
    }

    private void updateLevelOnUI(int level) throws InterruptedException {
        MainActivity activity = getActivity();
        activity.runOnUiThread(() -> {
            activity.getSettings().setCurrentUnlockedLevel(level);
            activity.navigateToTab(MainActivity.TAB_TREE);
        });
        Thread.sleep(500);
    }

    /**
     * BEHAVIOR 1: Release of nodes under N (-.) at Level 6: D (-..) and K (-.-).
     * Confirms visual release of D and K while G and O (under M) remain locked.
     */
    @Test
    public void test01_UnderN_BranchRelease_Level6_D_and_K() throws InterruptedException {
        updateLevelOnUI(6);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(withText(anyOf(containsString("6"), containsString("NÍVEL 6"), containsString("LEVEL 6")))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("D"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("K"))));
        onView(withId(R.id.morseTreeView)).check(matches(isDisplayed()));

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        MorseTreeNode nodeN = tree.findNodeByCharacter("N");
        MorseTreeNode nodeM = tree.findNodeByCharacter("M");

        // Under N: D and K unlocked
        Assert.assertTrue("Node D under N must be unlocked on UI", nodeN.getDitChild().isUnlocked());
        Assert.assertTrue("Node K under N must be unlocked on UI", nodeN.getDahChild().isUnlocked());

        // Under M: G and O remain locked
        Assert.assertFalse("Node G under M must remain locked at level 6", nodeM.getDitChild().isUnlocked());
        Assert.assertFalse("Node O under M must remain locked at level 6", nodeM.getDahChild().isUnlocked());

        ScreenshotHelper.capture("step_under_n_level6_D_and_K");
    }

    /**
     * BEHAVIOR 2: Release of nodes under M (--) at Level 7: G (--.) and O (---).
     * Confirms visual release of G and O, completing depth 3 under T branch.
     */
    @Test
    public void test02_UnderM_BranchRelease_Level7_G_and_O() throws InterruptedException {
        updateLevelOnUI(7);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(withText(anyOf(containsString("7"), containsString("NÍVEL 7"), containsString("LEVEL 7")))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("G"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("O"))));

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        MorseTreeNode nodeM = tree.findNodeByCharacter("M");

        // Under M: G and O now unlocked
        Assert.assertTrue("Node G under M must be unlocked on UI", nodeM.getDitChild().isUnlocked());
        Assert.assertTrue("Node O under M must be unlocked on UI", nodeM.getDahChild().isUnlocked());

        ScreenshotHelper.capture("step_under_m_level7_G_and_O");
    }

    /**
     * BEHAVIOR 3: Release of sub-branches under N: B, X (under D, Level 11) and C, Y (under K, Level 12).
     */
    @Test
    public void test03_UnderN_SubBranches_Level11_and_12() throws InterruptedException {
        // Level 11: B and X under D
        updateLevelOnUI(11);
        onView(withId(R.id.tvTreeLevelTitle)).check(matches(withText(anyOf(containsString("11"), containsString("NÍVEL 11"), containsString("LEVEL 11")))));

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        Assert.assertTrue("Node B under D (N) must be unlocked", tree.findNodeByCharacter("B").isUnlocked());
        Assert.assertTrue("Node X under D (N) must be unlocked", tree.findNodeByCharacter("X").isUnlocked());

        // Level 12: C and Y under K
        updateLevelOnUI(12);
        onView(withId(R.id.tvTreeLevelTitle)).check(matches(withText(anyOf(containsString("12"), containsString("NÍVEL 12"), containsString("LEVEL 12")))));

        Assert.assertTrue("Node C under K (N) must be unlocked", tree.findNodeByCharacter("C").isUnlocked());
        Assert.assertTrue("Node Y under K (N) must be unlocked", tree.findNodeByCharacter("Y").isUnlocked());

        ScreenshotHelper.capture("step_under_n_level12_descendants_complete");
    }

    /**
     * BEHAVIOR 4: Release of sub-branches under M: Z and Q (under G, Level 13).
     */
    @Test
    public void test04_UnderM_SubBranch_Level13_Z_and_Q() throws InterruptedException {
        updateLevelOnUI(13);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(withText(anyOf(containsString("13"), containsString("NÍVEL 13"), containsString("LEVEL 13")))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("Z"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("Q"))));

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        MorseTreeNode nodeG = tree.findNodeByCharacter("G");
        Assert.assertTrue("Node Z under G (M) must be unlocked", nodeG.getDitChild().isUnlocked());
        Assert.assertTrue("Node Q under G (M) must be unlocked", nodeG.getDahChild().isUnlocked());

        ScreenshotHelper.capture("step_under_m_level13_descendants_complete");
    }

    /**
     * ROTATION TEST: Validates orientation change between Portrait and Landscape
     * while examining M and N sub-branches.
     */
    @Test
    public void test05_UnderMAndN_Rotation_PortraitAndLandscape() throws InterruptedException {
        updateLevelOnUI(7);

        MainActivity activity = getActivity();
        // 1. Portrait orientation
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        Thread.sleep(400);
        onView(withId(R.id.morseTreeView)).check(matches(isDisplayed()));

        // 2. Landscape orientation
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        Thread.sleep(600);
        onView(withId(R.id.morseTreeView)).check(matches(isDisplayed()));
        ScreenshotHelper.capture("step_under_m_and_n_landscape_rotation");

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        MorseTreeNode nodeM = tree.findNodeByCharacter("M");
        MorseTreeNode nodeN = tree.findNodeByCharacter("N");
        Assert.assertTrue("G under M must remain unlocked in landscape", nodeM.getDitChild().isUnlocked());
        Assert.assertTrue("O under M must remain unlocked in landscape", nodeM.getDahChild().isUnlocked());
        Assert.assertTrue("D under N must remain unlocked in landscape", nodeN.getDitChild().isUnlocked());
        Assert.assertTrue("K under N must remain unlocked in landscape", nodeN.getDahChild().isUnlocked());

        // 3. Restore Portrait
        activity.runOnUiThread(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        Thread.sleep(400);
    }
}
