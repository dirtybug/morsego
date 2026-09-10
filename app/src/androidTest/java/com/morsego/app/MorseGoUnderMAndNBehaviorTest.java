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
 * Testes de Comportamento Instrumentalizados na UI para os nós POR BAIXO DO M E DO N:
 * Valida a libertação no ecrã da Árvore dos nós sob N (D, K e sub-ramos B, X, C, Y)
 * e sob M (G, O e sub-ramos Z, Q), capturando screenshots a cada etapa.
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
     * BEHAVIOR 1: Release dos nós sob N (-.) no Nível 6: D (-..) e K (-.-).
     * Confirma visualmente a libertação de D e K mantendo G e O (sob M) com cadeados.
     */
    @Test
    public void test01_UnderN_BranchRelease_Level6_D_and_K() throws InterruptedException {
        updateLevelOnUI(6);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(withText(containsString("NÍVEL 6"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("D"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("K"))));
        onView(withId(R.id.morseTreeView)).check(matches(isDisplayed()));

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        MorseTreeNode nodeN = tree.findNodeByCharacter("N");
        MorseTreeNode nodeM = tree.findNodeByCharacter("M");

        // Sob N: D e K desbloqueados
        Assert.assertTrue("D sob N deve estar desbloqueado na UI", nodeN.getDitChild().isUnlocked());
        Assert.assertTrue("K sob N deve estar desbloqueado na UI", nodeN.getDahChild().isUnlocked());

        // Sob M: G e O continuam bloqueados
        Assert.assertFalse("G sob M deve continuar bloqueado no nível 6", nodeM.getDitChild().isUnlocked());
        Assert.assertFalse("O sob M deve continuar bloqueado no nível 6", nodeM.getDahChild().isUnlocked());

        ScreenshotHelper.capture("step_under_n_level6_D_and_K");
    }

    /**
     * BEHAVIOR 2: Release dos nós sob M (--) no Nível 7: G (--.) e O (---).
     * Confirma visualmente a libertação de G e O, completando toda a profundidade 3 sob o ramo T.
     */
    @Test
    public void test02_UnderM_BranchRelease_Level7_G_and_O() throws InterruptedException {
        updateLevelOnUI(7);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(withText(containsString("NÍVEL 7"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("G"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("O"))));

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        MorseTreeNode nodeM = tree.findNodeByCharacter("M");

        // Sob M: G e O agora desbloqueados
        Assert.assertTrue("G sob M deve estar desbloqueado na UI", nodeM.getDitChild().isUnlocked());
        Assert.assertTrue("O sob M deve estar desbloqueado na UI", nodeM.getDahChild().isUnlocked());

        ScreenshotHelper.capture("step_under_m_level7_G_and_O");
    }

    /**
     * BEHAVIOR 3: Release dos sub-ramos sob N: B, X (sob D, Nível 11) e C, Y (sob K, Nível 12).
     */
    @Test
    public void test03_UnderN_SubBranches_Level11_and_12() throws InterruptedException {
        // Nível 11: B e X sob D
        updateLevelOnUI(11);
        onView(withId(R.id.tvTreeLevelTitle)).check(matches(withText(containsString("NÍVEL 11"))));

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        Assert.assertTrue("B sob D (N) desbloqueado", tree.findNodeByCharacter("B").isUnlocked());
        Assert.assertTrue("X sob D (N) desbloqueado", tree.findNodeByCharacter("X").isUnlocked());

        // Nível 12: C e Y sob K
        updateLevelOnUI(12);
        onView(withId(R.id.tvTreeLevelTitle)).check(matches(withText(containsString("NÍVEL 12"))));

        Assert.assertTrue("C sob K (N) desbloqueado", tree.findNodeByCharacter("C").isUnlocked());
        Assert.assertTrue("Y sob K (N) desbloqueado", tree.findNodeByCharacter("Y").isUnlocked());

        ScreenshotHelper.capture("step_under_n_level12_descendants_complete");
    }

    /**
     * BEHAVIOR 4: Release dos sub-ramos sob M: Z e Q (sob G, Nível 13).
     */
    @Test
    public void test04_UnderM_SubBranch_Level13_Z_and_Q() throws InterruptedException {
        updateLevelOnUI(13);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(withText(containsString("NÍVEL 13"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("Z"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("Q"))));

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        MorseTreeNode nodeG = tree.findNodeByCharacter("G");
        Assert.assertTrue("Z sob G (M) desbloqueado", nodeG.getDitChild().isUnlocked());
        Assert.assertTrue("Q sob G (M) desbloqueado", nodeG.getDahChild().isUnlocked());

        ScreenshotHelper.capture("step_under_m_level13_descendants_complete");
    }
}
