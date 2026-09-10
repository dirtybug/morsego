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
 * Testes de Comportamento Instrumentalizados da Árvore Binária Passo a Passo:
 * Demonstra a libertação e iluminação visual da árvore ao longo dos níveis,
 * validando o estado dos nós e capturando screenshots a cada passo.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MorseGoTreeStepByStepReleaseBehaviorTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setup() throws InterruptedException {
        Thread.sleep(500);
        // Garantir que estamos na aba da Árvore Binária
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
     * PASSO 1: Release do Nível 1 - Apenas a Raiz (T e E) desbloqueada.
     * Nós filhos de E (A, I) e T (M, N) com cadeado fechado / escurecidos.
     */
    @Test
    public void testPasso01_TreeRelease_Level1_Root_T_and_E() throws InterruptedException {
        updateLevelOnUI(1);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(withText(containsString("NÍVEL 1"))));
        onView(withId(R.id.morseTreeView)).check(matches(isDisplayed()));

        // Validação dos nós na árvore
        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        MorseTreeNode root = tree.getRoot();
        Assert.assertTrue("T deve estar desbloqueado no passo 1", root.getDahChild().isUnlocked());
        Assert.assertTrue("E deve estar desbloqueado no passo 1", root.getDitChild().isUnlocked());
        Assert.assertFalse("A deve estar bloqueado no passo 1", root.getDitChild().getDahChild().isUnlocked());
        Assert.assertFalse("I deve estar bloqueado no passo 1", root.getDitChild().getDitChild().isUnlocked());

        ScreenshotHelper.capture("step01_tree_release_level1_root");
    }

    /**
     * PASSO 2: Release do Nível 2 - Ramos do E (A e I) libertados.
     * Os nós A e I passam a iluminados. Ramos do T (M e N) permanecem bloqueados.
     */
    @Test
    public void testPasso02_TreeRelease_Level2_Branch_E_A_and_I() throws InterruptedException {
        updateLevelOnUI(2);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(withText(containsString("NÍVEL 2"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("A"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("I"))));

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        MorseTreeNode root = tree.getRoot();
        Assert.assertTrue("A deve estar agora desbloqueado", root.getDitChild().getDahChild().isUnlocked());
        Assert.assertTrue("I deve estar agora desbloqueado", root.getDitChild().getDitChild().isUnlocked());
        Assert.assertFalse("M ainda deve estar bloqueado", root.getDahChild().getDahChild().isUnlocked());
        Assert.assertFalse("N ainda deve estar bloqueado", root.getDahChild().getDitChild().isUnlocked());

        ScreenshotHelper.capture("step02_tree_release_level2_branch_e");
    }

    /**
     * PASSO 3: Release do Nível 3 - Ramos do T (M e N) libertados.
     * Agora toda a profundidade 2 está completa e iluminada [T, E, A, I, M, N].
     */
    @Test
    public void testPasso03_TreeRelease_Level3_Branch_T_M_and_N() throws InterruptedException {
        updateLevelOnUI(3);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(withText(containsString("NÍVEL 3"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("M"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("N"))));

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        MorseTreeNode root = tree.getRoot();
        Assert.assertTrue("M deve estar agora desbloqueado", root.getDahChild().getDahChild().isUnlocked());
        Assert.assertTrue("N deve estar agora desbloqueado", root.getDahChild().getDitChild().isUnlocked());
        Assert.assertEquals("6 caracteres desbloqueados na profundidade 2", 6,
                tree.getLevel(3).getAllCharacters().size());

        ScreenshotHelper.capture("step03_tree_release_level3_branch_t");
    }

    /**
     * PASSO 4: Release do Nível 4 - Ramos do I (S e U) libertados.
     * Profundidade 3 começa a abrir.
     */
    @Test
    public void testPasso04_TreeRelease_Level4_Branch_I_S_and_U() throws InterruptedException {
        updateLevelOnUI(4);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(withText(containsString("NÍVEL 4"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("S"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("U"))));

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        Assert.assertTrue(tree.findNodeByCharacter("S").isUnlocked());
        Assert.assertTrue(tree.findNodeByCharacter("U").isUnlocked());

        ScreenshotHelper.capture("step04_tree_release_level4_branch_i");
    }

    /**
     * PASSO 5: Release do Nível 13 - Conclusão de Todas as 26 Letras (Z e Q).
     * O alfabeto inteiro de A a Z está verde na árvore e o modo Rádio CW abre.
     */
    @Test
    public void testPasso05_TreeRelease_Level13_Alphabet_Complete() throws InterruptedException {
        updateLevelOnUI(13);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(withText(containsString("NÍVEL 13"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("Z"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("Q"))));

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        Assert.assertEquals(26, tree.getLevel(13).getAllCharacters().size());

        ScreenshotHelper.capture("step05_tree_release_level13_alphabet_complete");
    }

    /**
     * PASSO 6: Release do Nível 21 - Árvore Binária 100% Desbloqueada.
     * Todos os 21 níveis, números 0-9 e caracteres de telegrafia totalmente abertos.
     */
    @Test
    public void testPasso06_TreeRelease_Level21_Full_Tree() throws InterruptedException {
        updateLevelOnUI(21);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(withText(containsString("NÍVEL 21"))));
        ScreenshotHelper.capture("step06_tree_release_level21_full");
    }

    /**
     * PASSO 7: Varredura Sequencial Completa de Todos os 21 Níveis na UI:
     * Percorre do Nível 1 ao Nível 21 na Activity em tempo real,
     * validando que a cada passo o Canvas e os nós da árvore são atualizados
     * e o conjunto de caracteres acumulados cresce rigorosamente até 42 itens.
     */
    @Test
    public void testPasso07_All21LevelsSequentialSweep_UI() throws InterruptedException {
        MorseBinaryTree tree = MorseBinaryTree.getInstance();

        for (int lvl = 1; lvl <= 21; lvl++) {
            updateLevelOnUI(lvl);

            // Valida título do nível no ecrã
            onView(withId(R.id.tvTreeLevelTitle)).check(matches(withText(containsString("NÍVEL " + lvl))));

            // Valida que o tamanho do pool acumulado é exatamente lvl * 2
            Assert.assertEquals(lvl * 2, tree.getLevel(lvl).getAllCharacters().size());

            // Valida nós desbloqueados na árvore
            Assert.assertTrue(tree.findNodeByCharacter(tree.getLevel(lvl).getChar1()).isUnlocked());
            Assert.assertTrue(tree.findNodeByCharacter(tree.getLevel(lvl).getChar2()).isUnlocked());
        }

        // Ao final do 21º passo, captura ecrã de celebração final
        ScreenshotHelper.capture("step07_all_21_levels_sweep_complete");
    }

    /**
     * PASSO 8: Release dedicado de X (-..-) e B (-...) sob o nó D (Ramo do N) na UI:
     * Valida que no Nível 11 os nós X e B são iluminados a verde no Canvas,
     * e o cabeçalho exibe as novas letras B e X.
     */
    @Test
    public void testPasso08_TreeRelease_Level11_X_and_B_under_D() throws InterruptedException {
        updateLevelOnUI(11);

        onView(withId(R.id.tvTreeLevelTitle)).check(matches(withText(containsString("NÍVEL 11"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("B"))));
        onView(withId(R.id.tvTreeLevelSub)).check(matches(withText(containsString("X"))));

        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        MorseTreeNode nodeD = tree.findNodeByCharacter("D");
        Assert.assertNotNull(nodeD);

        // Valida que X e B sob D estão desbloqueados
        Assert.assertTrue("X (-..-) sob D deve estar desbloqueado na UI", nodeD.getDahChild().isUnlocked());
        Assert.assertTrue("B (-...) sob D deve estar desbloqueado na UI", nodeD.getDitChild().isUnlocked());

        ScreenshotHelper.capture("step08_tree_release_level11_X_and_B");
    }
}

