package com.morsego.app;

import android.content.pm.ActivityInfo;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.morsego.app.tree.MorseBinaryTree;
import com.morsego.app.tree.MorseTreeNode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;

/**
 * Instrumented UI Behavior Tests for Step-by-Step Morse Binary Tree Release:
 * Validates node state and visual canvas rendering step by step across all levels,
 * supporting both Vertical (Portrait) and Horizontal (Landscape) device orientations.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MorseGoTreeStepByStepReleaseBehaviorTest {

    private ActivityScenario<MainActivity> scenario;

    @Before
    public void setUp() {
        java.util.Locale.setDefault(java.util.Locale.US);
        scenario = ActivityScenario.launch(MainActivity.class);
        scenario.onActivity(activity -> {
            android.content.res.Configuration config = new android.content.res.Configuration();
            config.setLocale(java.util.Locale.US);
            activity.getResources().updateConfiguration(config, activity.getResources().getDisplayMetrics());
        });
    }

    private void updateLevelOnUI(int level) throws InterruptedException {
        scenario.onActivity(activity -> {
            activity.getSettings().setCurrentUnlockedLevel(level);
            activity.navigateToTab(MainActivity.TAB_TREE);
        });
        Thread.sleep(500);
    }

    /**
     * STEP 1: Level 1 Release - Root nodes T and E unlocked.
     */
    @Test
    public void testStep01_TreeRelease_Level1_Root_T_and_E() throws InterruptedException {
        updateLevelOnUI(1);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(anyOf(containsString("1"), containsString("NÍVEL 1"), containsString("LEVEL 1"))));
        onView(withId(R.id.morseTreeView)).check(matches(isDisplayed()));

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        MorseTreeNode root = tree.getRoot();
        Assert.assertTrue("Node T must be unlocked at step 1", root.getDahChild().isUnlocked());
        Assert.assertTrue("Node E must be unlocked at step 1", root.getDitChild().isUnlocked());
        Assert.assertFalse("Node A must be locked at step 1", root.getDitChild().getDahChild().isUnlocked());
        Assert.assertFalse("Node I must be locked at step 1", root.getDitChild().getDitChild().isUnlocked());

        ScreenshotHelper.capture("step01_tree_release_level1_root");
    }

    /**
     * STEP 2: Level 2 Release - Branches under E (A and I) unlocked.
     */
    @Test
    public void testStep02_TreeRelease_Level2_Branch_E_A_and_I() throws InterruptedException {
        updateLevelOnUI(2);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(anyOf(containsString("2"), containsString("NÍVEL 2"), containsString("LEVEL 2"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("A"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("I"))));

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        MorseTreeNode root = tree.getRoot();
        Assert.assertTrue("Node A must be unlocked", root.getDitChild().getDahChild().isUnlocked());
        Assert.assertTrue("Node I must be unlocked", root.getDitChild().getDitChild().isUnlocked());
        Assert.assertFalse("Node M must remain locked", root.getDahChild().getDahChild().isUnlocked());
        Assert.assertFalse("Node N must remain locked", root.getDahChild().getDitChild().isUnlocked());

        ScreenshotHelper.capture("step02_tree_release_level2_branch_e");
    }

    /**
     * STEP 3: Level 3 Release - Branches under T (M and N) unlocked.
     */
    @Test
    public void testStep03_TreeRelease_Level3_Branch_T_M_and_N() throws InterruptedException {
        updateLevelOnUI(3);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(anyOf(containsString("3"), containsString("NÍVEL 3"), containsString("LEVEL 3"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("M"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("N"))));

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        MorseTreeNode root = tree.getRoot();
        Assert.assertTrue("Node M must be unlocked", root.getDahChild().getDahChild().isUnlocked());
        Assert.assertTrue("Node N must be unlocked", root.getDahChild().getDitChild().isUnlocked());

        ScreenshotHelper.capture("step03_tree_release_level3_branch_t");
    }

    /**
     * STEP 4: Level 4 Release - Branches under I (S and U) unlocked.
     */
    @Test
    public void testStep04_TreeRelease_Level4_Branch_I_S_and_U() throws InterruptedException {
        updateLevelOnUI(4);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(anyOf(containsString("4"), containsString("NÍVEL 4"), containsString("LEVEL 4"))));

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        MorseTreeNode nodeI = tree.getRoot().getDitChild().getDitChild();
        Assert.assertTrue("Node S must be unlocked", nodeI.getDitChild().isUnlocked());
        Assert.assertTrue("Node U must be unlocked", nodeI.getDahChild().isUnlocked());

        ScreenshotHelper.capture("step04_tree_release_level4_branch_i");
    }

    /**
     * STEP 5: Level 13 Release - Complete Alphabet (26 Letters A-Z).
     */
    @Test
    public void testStep05_TreeRelease_Level13_Alphabet_Complete() throws InterruptedException {
        updateLevelOnUI(13);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(anyOf(containsString("13"), containsString("NÍVEL 13"), containsString("LEVEL 13"))));

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        MorseTreeNode nodeG = tree.getRoot().getDahChild().getDahChild().getDitChild();
        Assert.assertTrue("Node Z under G must be unlocked", nodeG.getDitChild().isUnlocked());
        Assert.assertTrue("Node Q under G must be unlocked", nodeG.getDahChild().isUnlocked());

        ScreenshotHelper.capture("step05_tree_level13_alphabet_complete");
    }

    /**
     * STEP 6: Level 21 Release - 100% Binary Tree Unlocked.
     */
    @Test
    public void testStep06_TreeRelease_Level21_Full_Tree() throws InterruptedException {
        updateLevelOnUI(21);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(anyOf(containsString("21"), containsString("NÍVEL 21"), containsString("LEVEL 21"))));

        ScreenshotHelper.capture("step06_tree_level21_full_tree");
    }

    /**
     * STEP 7: Sequential Sweep through all 21 levels.
     */
    @Test
    public void testStep07_All21LevelsSequentialSweep_UI() throws InterruptedException {
        MorseBinaryTree tree = MorseBinaryTree.getInstance();

        for (int lvl = 1; lvl <= 21; lvl++) {
            final int currentLvl = lvl;
            scenario.onActivity(activity -> {
                activity.getSettings().setCurrentUnlockedLevel(currentLvl);
                activity.navigateToTab(MainActivity.TAB_TREE);
            });
            Thread.sleep(150);

            onView(withId(R.id.tvTreeLevelTitle)).check(matches(containsString(String.valueOf(currentLvl))));
        }

        ScreenshotHelper.capture("step07_all_21_levels_completed_tree");
    }

    /**
     * STEP 8: Dedicated release of X and B under D with K illuminated green.
     */
    @Test
    public void testStep08_TreeRelease_Level11_X_and_B_under_D() throws InterruptedException {
        updateLevelOnUI(11);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(anyOf(containsString("11"), containsString("NÍVEL 11"), containsString("LEVEL 11"))));

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        MorseTreeNode root = tree.getRoot();
        MorseTreeNode nodeN = root.getDahChild().getDitChild();
        MorseTreeNode nodeK = nodeN.getDahChild();
        MorseTreeNode nodeD = nodeN.getDitChild();
        MorseTreeNode nodeB = nodeD.getDitChild();
        MorseTreeNode nodeX = nodeD.getDahChild();

        Assert.assertTrue("Node K must be UNLOCKED at level 11", nodeK.isUnlocked());
        Assert.assertTrue("Node D must be UNLOCKED at level 11", nodeD.isUnlocked());
        Assert.assertTrue("Node X (-..-) must be UNLOCKED at level 11", nodeX.isUnlocked());
        Assert.assertTrue("Node B (-...) must be UNLOCKED at level 11", nodeB.isUnlocked());

        ScreenshotHelper.capture("step_level11_X_and_B");
    }

    /**
     * ROTATION TEST: Validates device rotation between Portrait (Vertical)
     * and Landscape (Horizontal) orientations with state persistence.
     */
    @Test
    public void testStep09_ScreenRotation_PortraitAndLandscape_Orientation() throws InterruptedException {
        // 1. Set Level 11 in Portrait
        updateLevelOnUI(11);
        scenario.onActivity(activity -> {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });
        Thread.sleep(400);

        onView(withId(R.id.morseTreeView)).check(matches(isDisplayed()));
        onView(withId(R.id.tvTreeLevelTitle)).check(matches(containsString("11")));

        // 2. Rotate to Landscape (Horizontal)
        scenario.onActivity(activity -> {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        });
        Thread.sleep(600);

        // Verify tree view and state remain active and displayed in landscape
        onView(withId(R.id.morseTreeView)).check(matches(isDisplayed()));
        onView(withId(R.id.tvTreeLevelTitle)).check(matches(containsString("11")));
        ScreenshotHelper.capture("step09_tree_landscape_rotation");

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        MorseTreeNode nodeD = tree.getRoot().getDahChild().getDitChild().getDitChild();
        Assert.assertTrue("Node X must remain unlocked in landscape", nodeD.getDahChild().isUnlocked());
        Assert.assertTrue("Node B must remain unlocked in landscape", nodeD.getDitChild().isUnlocked());

        // 3. Rotate back to Portrait (Vertical)
        scenario.onActivity(activity -> {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        });
        Thread.sleep(600);

        onView(withId(R.id.morseTreeView)).check(matches(isDisplayed()));
        onView(withId(R.id.tvTreeLevelTitle)).check(matches(containsString("11")));
    }
}
