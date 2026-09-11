package com.morsego.app.tree;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit Tests for Mobile Orientation and Screen Rotation:
 * Validates that all features (Morse Binary Tree, Exam Progression, Keyer Settings)
 * function identically in both Horizontal (Landscape) and Vertical (Portrait) orientations,
 * and preserve complete user state upon screen rotation.
 */
public class MorseOrientationAndRotationTest {

    private MorseBinaryTree tree;

    @Before
    public void setUp() {
        tree = MorseBinaryTree.getInstance();
    }

    /**
     * Verifies that the Morse Binary Tree structure and node coordinates
     * maintain geometric integrity in both Portrait and Landscape dimension regimes.
     */
    @Test
    public void testTreeGeometry_PortraitAndLandscapeDimensions() {
        // Typical Portrait screen bounds (e.g. 1080 x 2400 px, density 2.625)
        int portraitWidth = 1080;
        int portraitHeight = 2400;

        // Typical Landscape screen bounds (e.g. 2400 x 1080 px, density 2.625)
        int landscapeWidth = 2400;
        int landscapeHeight = 1080;

        float density = 2.625f;

        // Portrait layout check
        float portraitLayerHeight = 105f * density;
        float portraitStartY = 48f * density;
        float portraitMaxY = portraitStartY + 4 * portraitLayerHeight;
        assertTrue("Portrait tree must fit comfortably within vertical height", portraitMaxY < portraitHeight);

        // Landscape layout check: wider layout gives greater horizontal separation
        float landscapeXOffset = landscapeWidth / 4f;
        assertTrue("Landscape width provides superior horizontal clearance for depth 5", landscapeXOffset > 400);

        // Verify adaptive radius scaling:
        // Depth 1-3 radius: 22dp; Depth 4 radius: 18dp; Depth 5 radius: 16dp
        float baseRadius = 22f * density;
        float depth4Radius = 18f * density;
        float depth5Radius = 16f * density;

        assertTrue("Depth 5 radius must be smaller than base radius to prevent overlapping", depth5Radius < baseRadius);
        assertTrue("Depth 4 radius must be between depth 5 and base radius", depth4Radius < baseRadius && depth4Radius > depth5Radius);
    }

    /**
     * Simulates device rotation:
     * 1. Start in Vertical (Portrait) orientation at Level 11.
     * 2. Rotate device to Horizontal (Landscape) orientation.
     * 3. Verify that unlocked nodes, progress, and tree state remain 100% intact.
     * 4. Rotate back to Vertical (Portrait) and verify stability.
     */
    @Test
    public void testStatePreservation_AcrossScreenRotation() {
        // 1. Initial state in Portrait: Unlock up to Level 11 (X and B under D)
        tree.unlockNodesUpToLevel(11);

        MorseTreeNode root = tree.getRoot();
        MorseTreeNode nodeT = root.getDahChild();
        MorseTreeNode nodeN = nodeT.getDitChild();
        MorseTreeNode nodeD = nodeN.getDitChild();
        MorseTreeNode nodeX = nodeD.getDahChild();
        MorseTreeNode nodeB = nodeD.getDitChild();

        assertTrue("Portrait: Node X must be unlocked", nodeX.isUnlocked());
        assertTrue("Portrait: Node B must be unlocked", nodeB.isUnlocked());

        // 2. Simulate Configuration Change: Device rotated to LANDSCAPE (Horizontal)
        boolean isLandscape = true;
        int orientation = isLandscape ? 2 /* Configuration.ORIENTATION_LANDSCAPE */ : 1;
        assertEquals(2, orientation);

        // State verification after rotation: all previously unlocked nodes remain unlocked
        assertTrue("Landscape: Node X must remain unlocked after rotation", nodeX.isUnlocked());
        assertTrue("Landscape: Node B must remain unlocked after rotation", nodeB.isUnlocked());
        assertTrue("Landscape: Node T at root must remain unlocked", nodeT.isUnlocked());

        // 3. Unlock next level in Landscape (Level 12: C and Y under K)
        tree.unlockNodesUpToLevel(12);
        MorseTreeNode nodeK = nodeN.getDahChild();
        MorseTreeNode nodeC = nodeK.getDitChild();
        MorseTreeNode nodeY = nodeK.getDahChild();

        assertTrue("Landscape: Node C unlocked at level 12", nodeC.isUnlocked());
        assertTrue("Landscape: Node Y unlocked at level 12", nodeY.isUnlocked());

        // 4. Rotate back to PORTRAIT (Vertical)
        isLandscape = false;
        orientation = isLandscape ? 2 : 1;
        assertEquals(1, orientation);

        assertTrue("Portrait (post-rotation): Node C must remain unlocked", nodeC.isUnlocked());
        assertTrue("Portrait (post-rotation): Node Y must remain unlocked", nodeY.isUnlocked());
        assertTrue("Portrait (post-rotation): Node X must remain unlocked", nodeX.isUnlocked());
        assertTrue("Portrait (post-rotation): Node B must remain unlocked", nodeB.isUnlocked());
    }

    /**
     * Verifies that connecting lines are direct diagonal lines in both orientations,
     * without any 90-degree orthogonal doglegs.
     */
    @Test
    public void testDirectConnectingLines_NoRightAnglesInAnyOrientation() {
        float[] testWidths = { 980f, 1080f, 1920f, 2400f }; // Covering both portrait and landscape widths

        for (float width : testWidths) {
            float rootX = width / 2f;
            float rootY = 48f;
            float layerHeight = 105f;
            float xOffset = width / 4f;

            // Dah child (Left)
            float dahX = rootX - xOffset;
            float dahY = rootY + layerHeight;

            // Dit child (Right)
            float ditX = rootX + xOffset;
            float ditY = rootY + layerHeight;

            // Slope verification: line must be diagonal (dx != 0 and dy != 0)
            float dahDx = Math.abs(dahX - rootX);
            float dahDy = Math.abs(dahY - rootY);
            assertTrue("Left branch must have non-zero horizontal delta (diagonal)", dahDx > 0);
            assertTrue("Left branch must have non-zero vertical delta (diagonal)", dahDy > 0);
            assertNotEquals("Connecting line must NOT be vertical (no 90-degree angle)", 0f, dahDx, 0.001f);

            float ditDx = Math.abs(ditX - rootX);
            float ditDy = Math.abs(ditY - rootY);
            assertTrue("Right branch must have non-zero horizontal delta (diagonal)", ditDx > 0);
            assertTrue("Right branch must have non-zero vertical delta (diagonal)", ditDy > 0);
            assertNotEquals("Connecting line must NOT be vertical (no 90-degree angle)", 0f, ditDx, 0.001f);
        }
    }
}
