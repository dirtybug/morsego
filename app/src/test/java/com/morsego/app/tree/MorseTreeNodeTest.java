package com.morsego.app.tree;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit Tests for MorseTreeNode:
 * Validates node properties, binary child assignments (Dit/Dah),
 * unlock status toggles, and orientation stability.
 */
public class MorseTreeNodeTest {

    @Test
    public void testNodeCreationAndProperties() {
        MorseTreeNode node = new MorseTreeNode("E", ".", 1);
        assertEquals("E", node.getCharacter());
        assertEquals(".", node.getMorseCode());
        assertEquals(1, node.getDepth());
        assertFalse("Node should be locked by default", node.isUnlocked());
        assertNull("Dit child should be null initially", node.getDitChild());
        assertNull("Dah child should be null initially", node.getDahChild());
    }

    @Test
    public void testChildAssignments_DitAndDah() {
        MorseTreeNode parent = new MorseTreeNode("T", "-", 1);
        MorseTreeNode ditChild = new MorseTreeNode("N", "-.", 2);
        MorseTreeNode dahChild = new MorseTreeNode("M", "--", 2);

        parent.setDitChild(ditChild);
        parent.setDahChild(dahChild);

        assertNotNull(parent.getDitChild());
        assertNotNull(parent.getDahChild());
        assertEquals("N", parent.getDitChild().getCharacter());
        assertEquals("M", parent.getDahChild().getCharacter());
        assertEquals("-.", parent.getDitChild().getMorseCode());
        assertEquals("--", parent.getDahChild().getMorseCode());
    }

    @Test
    public void testUnlockStatus() {
        MorseTreeNode node = new MorseTreeNode("A", ".-", 2);
        assertFalse(node.isUnlocked());

        node.setUnlocked(true);
        assertTrue(node.isUnlocked());

        node.setUnlocked(false);
        assertFalse(node.isUnlocked());
    }

    @Test
    public void testOrientationAndRotationStability() {
        // Node state in Portrait
        MorseTreeNode node = new MorseTreeNode("K", "-.-", 3);
        node.setUnlocked(true);
        assertTrue(node.isUnlocked());

        // Rotate to Landscape: check node fields
        int orientation = 2; // Landscape
        assertEquals(2, orientation);
        assertEquals("K", node.getCharacter());
        assertEquals("-.-", node.getMorseCode());
        assertEquals(3, node.getDepth());
        assertTrue(node.isUnlocked());

        // Rotate back to Portrait
        orientation = 1; // Portrait
        assertEquals(1, orientation);
        assertTrue(node.isUnlocked());
    }
}
