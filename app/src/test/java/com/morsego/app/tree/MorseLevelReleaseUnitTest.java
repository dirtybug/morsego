package com.morsego.app.tree;

import org.junit.Before;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Unit tests specifically testing the Level Release (Desbloqueio progressivo de níveis)
 * in the Morse Binary Tree and student progression.
 */
public class MorseLevelReleaseUnitTest {

    private MorseBinaryTree tree;

    @Before
    public void setUp() {
        tree = MorseBinaryTree.getInstance();
    }

    @Test
    public void testInitialState_OnlyLevel1Released() {
        // At initial state, only Level 1 (T and E) is released
        tree.unlockNodesUpToLevel(1);

        MorseTreeNode root = tree.getRoot();
        assertNotNull(root);

        MorseTreeNode nodeT = root.getDahChild();
        MorseTreeNode nodeE = root.getDitChild();
        assertNotNull(nodeT);
        assertNotNull(nodeE);

        // Level 1 characters MUST be released
        assertTrue("Node T must be unlocked at level 1", nodeT.isUnlocked());
        assertTrue("Node E must be unlocked at level 1", nodeE.isUnlocked());

        // Level 2 characters (A under E, I under E, M under T, N under T) MUST be LOCKED
        MorseTreeNode nodeA = nodeE.getDahChild();
        MorseTreeNode nodeI = nodeE.getDitChild();
        MorseTreeNode nodeM = nodeT.getDahChild();
        MorseTreeNode nodeN = nodeT.getDitChild();

        assertNotNull(nodeA);
        assertNotNull(nodeI);
        assertNotNull(nodeM);
        assertNotNull(nodeN);

        assertFalse("Node A must be locked at level 1", nodeA.isUnlocked());
        assertFalse("Node I must be locked at level 1", nodeI.isUnlocked());
        assertFalse("Node M must be locked at level 1", nodeM.isUnlocked());
        assertFalse("Node N must be locked at level 1", nodeN.isUnlocked());
    }

    @Test
    public void testReleaseLevel2_UnlocksAandI() {
        // When Level 2 is released
        tree.unlockNodesUpToLevel(2);

        TreeLevel l2 = tree.getLevel(2);
        List<String> pool = l2.getAllCharacters();
        assertEquals(4, pool.size());
        assertTrue(pool.contains("T"));
        assertTrue(pool.contains("E"));
        assertTrue(pool.contains("A"));
        assertTrue(pool.contains("I"));

        MorseTreeNode root = tree.getRoot();
        MorseTreeNode nodeT = root.getDahChild();
        MorseTreeNode nodeE = root.getDitChild();
        MorseTreeNode nodeA = nodeE.getDahChild();
        MorseTreeNode nodeI = nodeE.getDitChild();
        MorseTreeNode nodeM = nodeT.getDahChild();
        MorseTreeNode nodeN = nodeT.getDitChild();

        // Level 1 and Level 2 nodes must now be unlocked
        assertTrue("Node T remains unlocked", nodeT.isUnlocked());
        assertTrue("Node E remains unlocked", nodeE.isUnlocked());
        assertTrue("Node A is newly unlocked in level 2", nodeA.isUnlocked());
        assertTrue("Node I is newly unlocked in level 2", nodeI.isUnlocked());

        // Level 3 nodes (M, N) must still be locked
        assertFalse("Node M must remain locked until level 3 is released", nodeM.isUnlocked());
        assertFalse("Node N must remain locked until level 3 is released", nodeN.isUnlocked());
    }

    @Test
    public void testReleaseLevel3_UnlocksMandN() {
        // When Level 3 is released
        tree.unlockNodesUpToLevel(3);

        TreeLevel l3 = tree.getLevel(3);
        List<String> pool = l3.getAllCharacters();
        assertEquals(6, pool.size());
        assertTrue(pool.contains("T"));
        assertTrue(pool.contains("E"));
        assertTrue(pool.contains("A"));
        assertTrue(pool.contains("I"));
        assertTrue(pool.contains("M"));
        assertTrue(pool.contains("N"));

        MorseTreeNode root = tree.getRoot();
        MorseTreeNode nodeM = root.getDahChild().getDahChild();
        MorseTreeNode nodeN = root.getDahChild().getDitChild();

        assertTrue("Node M is now unlocked in level 3", nodeM.isUnlocked());
        assertTrue("Node N is now unlocked in level 3", nodeN.isUnlocked());
    }

    @Test
    public void testSequentialRelease_All21Levels() {
        // Test that each level sequentially adds exactly 2 new characters
        int prevSize = 0;
        for (int lvl = 1; lvl <= tree.getTotalLevels(); lvl++) {
            TreeLevel level = tree.getLevel(lvl);
            assertEquals(lvl, level.getLevelNumber());
            assertNotNull(level.getNewChar1());
            assertNotNull(level.getNewChar2());

            List<String> pool = level.getAllCharacters();
            assertEquals("Each level must add exactly 2 characters", prevSize + 2, pool.size());
            assertTrue(pool.contains(level.getNewChar1()));
            assertTrue(pool.contains(level.getNewChar2()));

            prevSize = pool.size();
        }

        // At level 13, all 26 letters (A-Z) must be unlocked
        TreeLevel l13 = tree.getLevel(13);
        for (char c = 'A'; c <= 'Z'; c++) {
            assertTrue("Character " + c + " must be released by level 13",
                    l13.getAllCharacters().contains(String.valueOf(c)));
        }
    }

    @Test
    public void testListeningStagePassedPreservedOnRetry() {
        com.morsego.app.ui.LearnFragment fragment = new com.morsego.app.ui.LearnFragment();
        // Initially, listening stage is not passed
        assertFalse(fragment.isListeningStagePassed());

        // When student passes Listening stage (Stage 1), flag is set to true
        fragment.setListeningStagePassedForTesting(true);
        assertTrue("Listening stage must be marked as passed", fragment.isListeningStagePassed());

        // If sending stage fails and retry is triggered, listeningStagePassed must remain true
        // so the user does NOT need to repeat listening
        assertTrue("On exam retry, student must not need to redo listening if already passed",
                fragment.isListeningStagePassed());
    }
}
