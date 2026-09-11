package com.morsego.app.tree;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit Tests for Step-by-Step Morse Binary Tree Release:
 * Validates progressive node unlocking and branch illumination level by level (Level 1 through 21).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MorseTreeStepByStepReleaseTest {

    private MorseBinaryTree tree;

    @Before
    public void setUp() {
        tree = MorseBinaryTree.getInstance();
    }

    /**
     * STEP 1: Only the root nodes T (-) and E (.) are unlocked.
     * Children of E (A, I) and children of T (M, N) must be strictly locked.
     */
    @Test
    public void testStep01_ReleaseLevel1_OnlyRootUnlocked() {
        tree.unlockNodesUpToLevel(1);

        MorseTreeNode root = tree.getRoot();
        assertNotNull("Root must not be null", root);

        MorseTreeNode nodeT = root.getDahChild(); // Left branch (-)
        MorseTreeNode nodeE = root.getDitChild(); // Right branch (.)
        assertNotNull("Node T must exist", nodeT);
        assertNotNull("Node E must exist", nodeE);

        // STEP 1: T and E are unlocked
        assertTrue("Step 1: Node T (-) at root must be unlocked", nodeT.isUnlocked());
        assertTrue("Step 1: Node E (.) at root must be unlocked", nodeE.isUnlocked());

        // STEP 1: Descendants must be locked
        MorseTreeNode nodeA = nodeE.getDahChild();
        MorseTreeNode nodeI = nodeE.getDitChild();
        MorseTreeNode nodeM = nodeT.getDahChild();
        MorseTreeNode nodeN = nodeT.getDitChild();

        assertFalse("Step 1: Node A (.-) under E must be locked", nodeA.isUnlocked());
        assertFalse("Step 1: Node I (..) under E must be locked", nodeI.isUnlocked());
        assertFalse("Step 1: Node M (--) under T must be locked", nodeM.isUnlocked());
        assertFalse("Step 1: Node N (-.) under T must be locked", nodeN.isUnlocked());

        TreeLevel l1 = tree.getLevel(1);
        assertEquals(2, l1.getAllCharacters().size());
        assertTrue(l1.getAllCharacters().contains("T"));
        assertTrue(l1.getAllCharacters().contains("E"));
    }

    /**
     * STEP 2: Release of Level 2 unlocks branches under E: A (.-) and I (..).
     * The T branch (M and N) remains locked at this step.
     */
    @Test
    public void testStep02_ReleaseLevel2_UnlocksBranchE_KeepsBranchTLocked() {
        tree.unlockNodesUpToLevel(2);

        MorseTreeNode root = tree.getRoot();
        MorseTreeNode nodeT = root.getDahChild();
        MorseTreeNode nodeE = root.getDitChild();

        MorseTreeNode nodeA = nodeE.getDahChild();
        MorseTreeNode nodeI = nodeE.getDitChild();
        MorseTreeNode nodeM = nodeT.getDahChild();
        MorseTreeNode nodeN = nodeT.getDitChild();

        // Level 1 remains unlocked
        assertTrue("Node T remains unlocked", nodeT.isUnlocked());
        assertTrue("Node E remains unlocked", nodeE.isUnlocked());

        // STEP 2: E branches now unlocked
        assertTrue("Step 2: Node A (.-) under E must be unlocked", nodeA.isUnlocked());
        assertTrue("Step 2: Node I (..) under E must be unlocked", nodeI.isUnlocked());

        // STEP 2: T branches remain locked
        assertFalse("Step 2: Node M (--) under T must remain locked at level 2", nodeM.isUnlocked());
        assertFalse("Step 2: Node N (-.) under T must remain locked at level 2", nodeN.isUnlocked());

        TreeLevel l2 = tree.getLevel(2);
        assertEquals(4, l2.getAllCharacters().size());
        assertTrue(l2.getAllCharacters().containsAll(List.of("T", "E", "A", "I")));
    }

    /**
     * STEP 3: Release of Level 3 unlocks branches under T: M (--) and N (-.).
     * Now entire depth 2 is 100% unlocked [T, E, A, I, M, N].
     */
    @Test
    public void testStep03_ReleaseLevel3_UnlocksBranchT_CompletesDepth2() {
        tree.unlockNodesUpToLevel(3);

        MorseTreeNode root = tree.getRoot();
        MorseTreeNode nodeT = root.getDahChild();
        MorseTreeNode nodeE = root.getDitChild();
        MorseTreeNode nodeA = nodeE.getDahChild();
        MorseTreeNode nodeI = nodeE.getDitChild();
        MorseTreeNode nodeM = nodeT.getDahChild();
        MorseTreeNode nodeN = nodeT.getDitChild();

        assertTrue("Step 3: Node T unlocked", nodeT.isUnlocked());
        assertTrue("Step 3: Node E unlocked", nodeE.isUnlocked());
        assertTrue("Step 3: Node A unlocked", nodeA.isUnlocked());
        assertTrue("Step 3: Node I unlocked", nodeI.isUnlocked());
        assertTrue("Step 3: Node M (--) under T must now be unlocked", nodeM.isUnlocked());
        assertTrue("Step 3: Node N (-.) under T must now be unlocked", nodeN.isUnlocked());

        // Depth 3 remains locked
        assertFalse("Node O (---) under M must remain locked", nodeM.getDahChild().isUnlocked());
        assertFalse("Node G (--.) under M must remain locked", nodeM.getDitChild().isUnlocked());
        assertFalse("Node K (-.-) under N must remain locked", nodeN.getDahChild().isUnlocked());
        assertFalse("Node D (-..) under N must remain locked", nodeN.getDitChild().isUnlocked());

        TreeLevel l3 = tree.getLevel(3);
        assertEquals(6, l3.getAllCharacters().size());
        assertTrue(l3.getAllCharacters().containsAll(List.of("T", "E", "A", "I", "M", "N")));
    }

    /**
     * STEP 4: Release of Level 4 unlocks S (...) and U (..-) under I.
     */
    @Test
    public void testStep04_ReleaseLevel4_UnlocksBranchI_S_and_U() {
        tree.unlockNodesUpToLevel(4);

        MorseTreeNode nodeI = tree.getRoot().getDitChild().getDitChild();
        MorseTreeNode nodeS = nodeI.getDitChild();
        MorseTreeNode nodeU = nodeI.getDahChild();

        assertNotNull("Node S must exist", nodeS);
        assertNotNull("Node U must exist", nodeU);
        assertTrue("Step 4: Node S (...) must be unlocked", nodeS.isUnlocked());
        assertTrue("Step 4: Node U (..-) must be unlocked", nodeU.isUnlocked());

        TreeLevel l4 = tree.getLevel(4);
        assertEquals(8, l4.getAllCharacters().size());
        assertTrue(l4.getAllCharacters().contains("S"));
        assertTrue(l4.getAllCharacters().contains("U"));
    }

    /**
     * STEP 5: Release of Level 6 unlocks D (-..) and K (-.-) under N.
     */
    @Test
    public void testStep05_ReleaseLevel6_UnlocksBranchN_D_and_K() {
        tree.unlockNodesUpToLevel(6);

        MorseTreeNode nodeN = tree.getRoot().getDahChild().getDitChild();
        MorseTreeNode nodeD = nodeN.getDitChild();
        MorseTreeNode nodeK = nodeN.getDahChild();

        assertTrue("Step 5: Node D (-..) under N must be unlocked at level 6", nodeD.isUnlocked());
        assertTrue("Step 5: Node K (-.-) under N must be unlocked at level 6", nodeK.isUnlocked());

        // Nodes under M (G and O) remain locked
        MorseTreeNode nodeM = tree.getRoot().getDahChild().getDahChild();
        assertFalse("Node G must remain locked at level 6", nodeM.getDitChild().isUnlocked());
        assertFalse("Node O must remain locked at level 6", nodeM.getDahChild().isUnlocked());
    }

    /**
     * STEP 6: Release of Level 7 unlocks G (--.) and O (---) under M.
     */
    @Test
    public void testStep06_ReleaseLevel7_UnlocksBranchM_G_and_O() {
        tree.unlockNodesUpToLevel(7);

        MorseTreeNode nodeM = tree.getRoot().getDahChild().getDahChild();
        MorseTreeNode nodeG = nodeM.getDitChild();
        MorseTreeNode nodeO = nodeM.getDahChild();

        assertTrue("Step 6: Node G (--.) under M must be unlocked at level 7", nodeG.isUnlocked());
        assertTrue("Step 6: Node O (---) under M must be unlocked at level 7", nodeO.isUnlocked());

        // Entire depth 3 is now unlocked [O, G, K, D, W, R, U, S]
        TreeLevel l7 = tree.getLevel(7);
        assertEquals(14, l7.getAllCharacters().size());
        assertTrue(l7.getAllCharacters().containsAll(List.of("O", "G", "K", "D")));
    }

    /**
     * STEP 7: Release of Level 11 unlocks X (-..-) and B (-...) under D.
     */
    @Test
    public void testStep07_ReleaseLevel11_UnlocksUnderD_X_and_B() {
        tree.unlockNodesUpToLevel(11);

        MorseTreeNode nodeD = tree.getRoot().getDahChild().getDitChild().getDitChild();
        MorseTreeNode nodeB = nodeD.getDitChild();
        MorseTreeNode nodeX = nodeD.getDahChild();

        assertNotNull("Node B must exist under D", nodeB);
        assertNotNull("Node X must exist under D", nodeX);
        assertTrue("Node B (-...) must be unlocked at level 11", nodeB.isUnlocked());
        assertTrue("Node X (-..-) must be unlocked at level 11", nodeX.isUnlocked());

        // Node K must also be unlocked
        MorseTreeNode nodeK = tree.getRoot().getDahChild().getDitChild().getDahChild();
        assertTrue("Node K must be unlocked at level 11", nodeK.isUnlocked());
    }

    /**
     * STEP 8: Release of Level 13 unlocks full alphabet (26 letters A-Z).
     */
    @Test
    public void testStep08_ReleaseLevel13_UnlocksFullAlphabet_26Letters() {
        tree.unlockNodesUpToLevel(13);

        TreeLevel l13 = tree.getLevel(13);
        assertEquals(26, l13.getAllCharacters().size());

        for (char c = 'A'; c <= 'Z'; c++) {
            assertTrue("Letter " + c + " must be unlocked at level 13",
                    l13.getAllCharacters().contains(String.valueOf(c)));
        }
    }

    /**
     * STEP 9: Release of Level 17 unlocks digits 7 and 8.
     * Validates that 8 connects directly to O, independent of 9 and 0.
     */
    @Test
    public void testStep09_ReleaseLevel17_UnlocksDigits7_and_8() {
        tree.unlockNodesUpToLevel(17);

        TreeLevel l17 = tree.getLevel(17);
        assertTrue("Digit 7 must be unlocked at level 17", l17.getAllCharacters().contains("7"));
        assertTrue("Digit 8 must be unlocked at level 17", l17.getAllCharacters().contains("8"));

        // Digits 9 and 0 must remain locked until level 18
        assertFalse("Digit 9 must remain locked at level 17", l17.getAllCharacters().contains("9"));
        assertFalse("Digit 0 must remain locked at level 17", l17.getAllCharacters().contains("0"));

        // Verify that 8 is a Dit child of O
        MorseTreeNode nodeO = tree.getRoot().getDahChild().getDahChild().getDahChild();
        MorseTreeNode node8 = nodeO.getDitChild();
        assertNotNull("Node 8 must be Dit child of O", node8);
        assertEquals("8", node8.getCharacter());
        assertTrue("Node 8 must be unlocked at level 17", node8.isUnlocked());
        assertTrue("Node O (parent of 8) must be unlocked", nodeO.isUnlocked());
    }

    /**
     * STEP 10: Release of Level 21 completes the entire tree.
     */
    @Test
    public void testStep10_ReleaseLevel21_FullTreeComplete() {
        tree.unlockNodesUpToLevel(21);

        TreeLevel l21 = tree.getLevel(21);
        assertEquals(42, l21.getAllCharacters().size());
        assertTrue(l21.getAllCharacters().contains("SOS"));
        assertTrue(l21.getAllCharacters().contains("SK"));
    }
}
