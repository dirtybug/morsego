package com.morsego.app.tree;

import org.junit.Before;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class MorseBinaryTreeTest {

    private MorseBinaryTree tree;

    @Before
    public void setUp() {
        tree = MorseBinaryTree.getInstance();
    }

    @Test
    public void testSingletonInstance() {
        assertNotNull(tree);
        assertSame(tree, MorseBinaryTree.getInstance());
    }

    @Test
    public void testTotalLevels() {
        assertEquals(21, tree.getTotalLevels());
    }

    @Test
    public void testLevel1Content() {
        TreeLevel l1 = tree.getLevel(1);
        assertEquals(1, l1.getLevelNumber());
        assertEquals("E", l1.getNewChar1());
        assertEquals(".", l1.getMorse1());
        assertEquals("T", l1.getNewChar2());
        assertEquals("-", l1.getMorse2());
        assertEquals(2, l1.getAllCharacters().size());
        assertTrue(l1.getAllCharacters().contains("E"));
        assertTrue(l1.getAllCharacters().contains("T"));
    }

    @Test
    public void testLevel2Content() {
        TreeLevel l2 = tree.getLevel(2);
        assertEquals(2, l2.getLevelNumber());
        assertEquals("I", l2.getNewChar1());
        assertEquals("..", l2.getMorse1());
        assertEquals("A", l2.getNewChar2());
        assertEquals(".-", l2.getMorse2());
        // Level 2 should have E, T, I, A
        assertEquals(4, l2.getAllCharacters().size());
        assertTrue(l2.getAllCharacters().contains("E"));
        assertTrue(l2.getAllCharacters().contains("T"));
        assertTrue(l2.getAllCharacters().contains("I"));
        assertTrue(l2.getAllCharacters().contains("A"));
    }

    @Test
    public void testMorseLookup() {
        assertEquals(".", tree.getMorse("E"));
        assertEquals("-", tree.getMorse("T"));
        assertEquals("..", tree.getMorse("I"));
        assertEquals(".-", tree.getMorse("A"));
        assertEquals("-.", tree.getMorse("N"));
        assertEquals("--", tree.getMorse("M"));
        assertEquals("...---...", tree.getMorse("SOS"));
    }

    @Test
    public void testCharLookup() {
        assertEquals("E", tree.getChar("."));
        assertEquals("T", tree.getChar("-"));
        assertEquals("I", tree.getChar(".."));
        assertEquals("A", tree.getChar(".-"));
        assertEquals("SOS", tree.getChar("...---..."));
    }

    @Test
    public void testTreeTraversal() {
        MorseTreeNode nodeE = tree.traverse(".");
        assertNotNull(nodeE);
        assertEquals("E", nodeE.getCharacter());

        MorseTreeNode nodeA = tree.traverse(".-");
        assertNotNull(nodeA);
        assertEquals("A", nodeA.getCharacter());

        MorseTreeNode nodeInvalid = tree.traverse("........");
        assertNull(nodeInvalid);
    }
}
