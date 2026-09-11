package com.morsego.app.tree;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Dedicated Unit Tests for nodes under M and N in the Morse Binary Tree:
 *
 * T-branch subtree structure:
 *           T (-)
 *          /     \
 *       M (--)   N (-.)
 *       /    \   /    \
 *     O(---) G(--.) K(-.-) D(-..)
 *    /  \    /   \   /   \   /   \
 *   8    9  Q    Z   Y     C X     B
 *        |
 *        0
 *
 * Validates:
 * 1. Direct branches under N: D (-..) and K (-.-) at Level 6.
 * 2. Direct branches under M: G (--.) and O (---) at Level 7.
 * 3. Descendants of D (under N): B (-...) and X (-..-) at Level 11.
 * 4. Descendants of K (under N): C (-.-.) and Y (-.--) at Level 12.
 * 5. Descendants of G (under M): Z (--..) and Q (--.-) at Level 13.
 * 6. Descendants of O (under M): Numbers 8, 9, and 0 in depth 4 and 5.
 * 7. Isolation and integrity between M and N branches.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MorseUnderMAndNTreeReleaseTest {

    private MorseBinaryTree tree;
    private MorseTreeNode nodeT;
    private MorseTreeNode nodeM;
    private MorseTreeNode nodeN;

    @Before
    public void setUp() {
        tree = MorseBinaryTree.getInstance();
        MorseTreeNode root = tree.getRoot();
        assertNotNull("Root must not be null", root);

        nodeT = root.getDahChild();
        assertNotNull("Node T (-) must exist under root", nodeT);

        nodeM = nodeT.getDahChild();
        nodeN = nodeT.getDitChild();
        assertNotNull("Node M (--) must exist under T", nodeM);
        assertNotNull("Node N (-.) must exist under T", nodeN);
    }

    /**
     * TEST 1: Direct children under N (-.): D (-..) and K (-.-) [LEVEL 6]
     * Validates that D is Dit (.) child of N and K is Dah (-) child of N.
     * Validates that at Level 5 they are locked and at Level 6 they become unlocked.
     */
    @Test
    public void test01_UnderN_DirectChildren_D_and_K_ReleaseLevel6() {
        MorseTreeNode nodeD = nodeN.getDitChild();
        MorseTreeNode nodeK = nodeN.getDahChild();

        assertNotNull("Node D must exist under N as DIT child", nodeD);
        assertNotNull("Node K must exist under N as DAH child", nodeK);
        assertEquals("D", nodeD.getCharacter());
        assertEquals("-..", nodeD.getMorseCode());
        assertEquals("K", nodeK.getCharacter());
        assertEquals("-.-", nodeK.getMorseCode());

        // At Level 5, D and K must be LOCKED
        tree.unlockNodesUpToLevel(5);
        assertFalse("D under N must be locked at level 5", nodeD.isUnlocked());
        assertFalse("K under N must be locked at level 5", nodeK.isUnlocked());

        // At Level 6, D and K are UNLOCKED
        tree.unlockNodesUpToLevel(6);
        assertTrue("D under N must be UNLOCKED at level 6", nodeD.isUnlocked());
        assertTrue("K under N must be UNLOCKED at level 6", nodeK.isUnlocked());

        TreeLevel l6 = tree.getLevel(6);
        assertEquals("D", l6.getChar1());
        assertEquals("K", l6.getChar2());
        assertTrue(l6.getAllCharacters().contains("D"));
        assertTrue(l6.getAllCharacters().contains("K"));
    }

    /**
     * TEST 2: Direct children under M (--): G (--.) and O (---) [LEVEL 7]
     * Validates that G is Dit (.) child of M and O is Dah (-) child of M.
     * Validates that at Level 6 they are locked and at Level 7 they become unlocked.
     */
    @Test
    public void test02_UnderM_DirectChildren_G_and_O_ReleaseLevel7() {
        MorseTreeNode nodeG = nodeM.getDitChild();
        MorseTreeNode nodeO = nodeM.getDahChild();

        assertNotNull("Node G must exist under M as DIT child", nodeG);
        assertNotNull("Node O must exist under M as DAH child", nodeO);
        assertEquals("G", nodeG.getCharacter());
        assertEquals("--.", nodeG.getMorseCode());
        assertEquals("O", nodeO.getCharacter());
        assertEquals("---", nodeO.getMorseCode());

        // At Level 6, G and O under M must be LOCKED
        tree.unlockNodesUpToLevel(6);
        assertFalse("G under M must be locked at level 6", nodeG.isUnlocked());
        assertFalse("O under M must be locked at level 6", nodeO.isUnlocked());

        // At Level 7, G and O are UNLOCKED
        tree.unlockNodesUpToLevel(7);
        assertTrue("G under M must be UNLOCKED at level 7", nodeG.isUnlocked());
        assertTrue("O under M must be UNLOCKED at level 7", nodeO.isUnlocked());

        TreeLevel l7 = tree.getLevel(7);
        assertEquals("G", l7.getChar1());
        assertEquals("O", l7.getChar2());
        assertTrue(l7.getAllCharacters().contains("G"));
        assertTrue(l7.getAllCharacters().contains("O"));
    }

    /**
     * TEST 3: Sub-branch under D (under N): B (-...) and X (-..-) [LEVEL 11]
     * Validates descent: T -> N -> D -> B (dit) and X (dah).
     * Validates locked at Level 10 and unlocked at Level 11.
     */
    @Test
    public void test03_UnderN_SubBranchD_Children_B_and_X_ReleaseLevel11() {
        MorseTreeNode nodeD = nodeN.getDitChild();
        assertNotNull(nodeD);

        MorseTreeNode nodeB = nodeD.getDitChild();
        MorseTreeNode nodeX = nodeD.getDahChild();

        assertNotNull("B must exist under D (sub-branch of N)", nodeB);
        assertNotNull("X must exist under D (sub-branch of N)", nodeX);
        assertEquals("B", nodeB.getCharacter());
        assertEquals("-...", nodeB.getMorseCode());
        assertEquals("X", nodeX.getCharacter());
        assertEquals("-..-", nodeX.getMorseCode());

        // At Level 10, B and X must be locked
        tree.unlockNodesUpToLevel(10);
        assertFalse("B under D (N) must be locked at level 10", nodeB.isUnlocked());
        assertFalse("X under D (N) must be locked at level 10", nodeX.isUnlocked());

        // At Level 11, B and X are unlocked
        tree.unlockNodesUpToLevel(11);
        assertTrue("B under D (N) must be UNLOCKED at level 11", nodeB.isUnlocked());
        assertTrue("X under D (N) must be UNLOCKED at level 11", nodeX.isUnlocked());

        TreeLevel l11 = tree.getLevel(11);
        assertEquals("B", l11.getChar1());
        assertEquals("X", l11.getChar2());
    }

    /**
     * TEST 4: Sub-branch under K (under N): C (-.-.) and Y (-.--) [LEVEL 12]
     * Validates descent: T -> N -> K -> C (dit) and Y (dah).
     * Validates locked at Level 11 and unlocked at Level 12.
     */
    @Test
    public void test04_UnderN_SubBranchK_Children_C_and_Y_ReleaseLevel12() {
        MorseTreeNode nodeK = nodeN.getDahChild();
        assertNotNull(nodeK);

        MorseTreeNode nodeC = nodeK.getDitChild();
        MorseTreeNode nodeY = nodeK.getDahChild();

        assertNotNull("C must exist under K (sub-branch of N)", nodeC);
        assertNotNull("Y must exist under K (sub-branch of N)", nodeY);
        assertEquals("C", nodeC.getCharacter());
        assertEquals("-.-.", nodeC.getMorseCode());
        assertEquals("Y", nodeY.getCharacter());
        assertEquals("-.--", nodeY.getMorseCode());

        // At Level 11, C and Y must be locked
        tree.unlockNodesUpToLevel(11);
        assertFalse("C under K (N) must be locked at level 11", nodeC.isUnlocked());
        assertFalse("Y under K (N) must be locked at level 11", nodeY.isUnlocked());

        // At Level 12, C and Y are unlocked
        tree.unlockNodesUpToLevel(12);
        assertTrue("C under K (N) must be UNLOCKED at level 12", nodeC.isUnlocked());
        assertTrue("Y under K (N) must be UNLOCKED at level 12", nodeY.isUnlocked());

        TreeLevel l12 = tree.getLevel(12);
        assertEquals("C", l12.getChar1());
        assertEquals("Y", l12.getChar2());
    }

    /**
     * TEST 5: Sub-branch under G (under M): Z (--..) and Q (--.-) [LEVEL 13]
     * Validates descent: T -> M -> G -> Z (dit) and Q (dah).
     * Validates locked at Level 12 and unlocked at Level 13.
     */
    @Test
    public void test05_UnderM_SubBranchG_Children_Z_and_Q_ReleaseLevel13() {
        MorseTreeNode nodeG = nodeM.getDitChild();
        assertNotNull(nodeG);

        MorseTreeNode nodeZ = nodeG.getDitChild();
        MorseTreeNode nodeQ = nodeG.getDahChild();

        assertNotNull("Z must exist under G (sub-branch of M)", nodeZ);
        assertNotNull("Q must exist under G (sub-branch of M)", nodeQ);
        assertEquals("Z", nodeZ.getCharacter());
        assertEquals("--..", nodeZ.getMorseCode());
        assertEquals("Q", nodeQ.getCharacter());
        assertEquals("--.-", nodeQ.getMorseCode());

        // At Level 12, Z and Q must be locked
        tree.unlockNodesUpToLevel(12);
        assertFalse("Z under G (M) must be locked at level 12", nodeZ.isUnlocked());
        assertFalse("Q under G (M) must be locked at level 12", nodeQ.isUnlocked());

        // At Level 13, Z and Q are unlocked
        tree.unlockNodesUpToLevel(13);
        assertTrue("Z under G (M) must be UNLOCKED at level 13", nodeZ.isUnlocked());
        assertTrue("Q under G (M) must be UNLOCKED at level 13", nodeQ.isUnlocked());

        TreeLevel l13 = tree.getLevel(13);
        assertEquals("Z", l13.getChar1());
        assertEquals("Q", l13.getChar2());
    }

    /**
     * TEST 6: Sub-branch under O (under M): Numbers 8 (---..), 9 (----.) and 0 (-----)
     * Validates telegraphic numbering descent:
     * START -> T (-) -> M (--) -> O (---):
     * - Dit child: 8 (---..)
     * - Dah child: 9 (----.) -> Dah child: 0 (-----)
     */
    @Test
    public void test06_UnderM_SubBranchO_Numbers_8_9_and_0() {
        MorseTreeNode nodeO = nodeM.getDahChild();
        assertNotNull(nodeO);

        MorseTreeNode node8 = nodeO.getDitChild();
        MorseTreeNode node9 = nodeO.getDahChild();

        assertNotNull("8 must exist under O as Dit child", node8);
        assertNotNull("9 must exist under O as Dah child", node9);
        assertEquals("8", node8.getCharacter());
        assertEquals("---..", node8.getMorseCode());
        assertEquals("9", node9.getCharacter());
        assertEquals("----.", node9.getMorseCode());

        MorseTreeNode node0 = node9.getDahChild();
        assertNotNull("0 must exist under 9 as Dah child", node0);
        assertEquals("0", node0.getCharacter());
        assertEquals("-----", node0.getMorseCode());

        // Verify independent level release: Level 17 unlocks 8 while 9 and 0 remain locked
        tree.unlockNodesUpToLevel(17);
        assertTrue("8 must be unlocked at level 17", node8.isUnlocked());
        assertFalse("9 must remain locked at level 17", node9.isUnlocked());
        assertFalse("0 must remain locked at level 17", node0.isUnlocked());

        // Level 18 unlocks 9 and 0
        tree.unlockNodesUpToLevel(18);
        assertTrue("9 must be unlocked at level 18", node9.isUnlocked());
        assertTrue("0 must be unlocked at level 18", node0.isUnlocked());
    }

    /**
     * TEST 7: Isolation and Integrity between Branch M and Branch N:
     * Ensures unlocking N branches (D, K) does NOT inadvertently unlock M branches (G, O).
     */
    @Test
    public void test07_IsolationBetweenBranchM_and_BranchN() {
        // At Level 6: Unlocks N branches (D and K), but M branches (G and O) MUST remain locked
        tree.unlockNodesUpToLevel(6);

        MorseTreeNode nodeD = nodeN.getDitChild();
        MorseTreeNode nodeK = nodeN.getDahChild();
        MorseTreeNode nodeG = nodeM.getDitChild();
        MorseTreeNode nodeO = nodeM.getDahChild();

        assertTrue("N branch (D) unlocked at level 6", nodeD.isUnlocked());
        assertTrue("N branch (K) unlocked at level 6", nodeK.isUnlocked());

        assertFalse("M branch (G) MUST remain LOCKED at level 6", nodeG.isUnlocked());
        assertFalse("M branch (O) MUST remain LOCKED at level 6", nodeO.isUnlocked());
    }
}
