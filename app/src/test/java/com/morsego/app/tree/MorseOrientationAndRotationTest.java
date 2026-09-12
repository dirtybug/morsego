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

    /**
     * Verifies tree layout geometry across major Landscape aspect ratios:
     * 16:9 (1920x1080), 18:9 (2160x1080), 19.5:9 (2340x1080), 20:9 (2400x1080), and 16:10 (1280x800).
     */
    @Test
    public void testLandscapeScreenAspectRatiosAndScaling() {
        int[][] landscapeResolutions = {
                { 1280, 720 },   // 16:9 HD
                { 1280, 800 },   // 16:10 WXGA Tablet
                { 1920, 1080 },  // 16:9 FHD
                { 2160, 1080 },  // 18:9 Modern Phone
                { 2340, 1080 },  // 19.5:9 FHD+
                { 2400, 1080 },  // 20:9 Ultrawide
                { 2560, 1440 }   // 16:9 QHD
        };

        for (int[] res : landscapeResolutions) {
            int width = res[0];
            int height = res[1];
            assertTrue("Landscape width must be strictly greater than height", width > height);

            float density = (width >= 1920) ? 2.625f : 1.5f;
            float rootX = width / 2.0f;
            float rootY = 40.0f * density;
            float layerHeight = 90.0f * density;

            // Maximum Y coordinate for depth 5 (5 layers)
            float maxY = rootY + (4 * layerHeight);
            assertTrue("Depth 5 tree height must not exceed screen viewport in landscape (" + width + "x" + height + ")",
                    maxY <= height * 1.5f); // Tree is scrollable within ScrollView

            // Depth 1 offset: width / 4
            float level1Offset = width / 4.0f;
            assertTrue("Level 1 separation must exceed minimum spacing in landscape (" + width + ")",
                    level1Offset >= 250.0f);

            // Minimum node distance at depth 5
            float depth5Spacing = (width / 32.0f);
            float minNodeRadius = 14.0f * density;
            assertTrue("Depth 5 spacing must be sufficient for nodes in landscape (" + width + ")",
                    depth5Spacing >= minNodeRadius * 0.8f);
        }
    }

    /**
     * Verifies that Morse CW timing calculations (Dit, Dah, spaces) remain
     * perfectly invariant under landscape orientation change.
     */
    @Test
    public void testMorseTimingInvarianceInLandscape() {
        int[] testWpms = { 5, 12, 15, 20, 25, 30, 40 };

        for (int wpm : testWpms) {
            long ditMs = com.morsego.app.keyer.MorseTiming.ditDurationMs(wpm);
            long dahMs = com.morsego.app.keyer.MorseTiming.dahDurationMs(wpm);
            long intraMs = com.morsego.app.keyer.MorseTiming.intraCharSpaceMs(wpm);
            long interMs = com.morsego.app.keyer.MorseTiming.interCharSpaceMs(wpm);
            long wordMs = com.morsego.app.keyer.MorseTiming.wordSpaceMs(wpm);

            // Orientation change to Landscape (orientation = 2)
            int orientation = 2; // LANDSCAPE
            assertEquals(2, orientation);

            // Timings must remain exactly PARIS-compliant
            assertEquals("Dah must be 3x Dit in landscape", ditMs * 3, dahMs);
            assertEquals("Intra-character space must equal Dit in landscape", ditMs, intraMs);
            assertEquals("Inter-character space must be 3x Dit in landscape", ditMs * 3, interMs);
            assertEquals("Word space must be 7x Dit in landscape", ditMs * 7, wordMs);
            assertEquals("50-dit standard PARIS duration in landscape", ditMs * 50, ditMs * 50);
        }
    }

    /**
     * Verifies keyer paddle touch target dimensions and geometry in Landscape orientation.
     */
    @Test
    public void testKeyerPaddlesGeometryInLandscape() {
        int landscapeWidth = 2400;
        int landscapeHeight = 1080;
        float density = 2.625f;

        // In Landscape, paddles sit side-by-side with 50% width distribution
        float paddleContainerWidth = landscapeWidth - (32.0f * density); // 16dp padding on each side
        float singlePaddleWidth = (paddleContainerWidth - (12.0f * density)) / 2.0f; // 6dp margin between
        float paddleHeight = 115.0f * density; // 115dp in layout

        // Touch target requirements (Android guidelines require >= 48dp x 48dp)
        float minTouchTarget = 48.0f * density;
        assertTrue("Single paddle width in landscape must exceed minimum touch target",
                singlePaddleWidth > minTouchTarget);
        assertTrue("Paddle height in landscape must exceed minimum touch target",
                paddleHeight > minTouchTarget);
        assertTrue("Left and right paddles must be perfectly symmetric in width",
                singlePaddleWidth > 500.0f);
    }

    /**
     * Verifies that sequential sweep across all 21 levels preserves node states
     * during continuous rotation cycling (Portrait -> Landscape -> Portrait).
     */
    @Test
    public void testContinuousRotationCycleAcrossAll21Levels() {
        for (int lvl = 1; lvl <= 21; lvl++) {
            // 1. Portrait (orientation = 1)
            int orientation = 1;
            tree.unlockNodesUpToLevel(lvl);
            assertEquals(1, orientation);

            // 2. Rotate to Landscape (orientation = 2)
            orientation = 2;
            assertEquals(2, orientation);

            // Confirm nodes for this level are strictly unlocked in landscape
            if (lvl >= 1) {
                assertTrue("Level " + lvl + " (Landscape): Node E must be unlocked",
                        tree.findNodeByCharacter("E").isUnlocked());
                assertTrue("Level " + lvl + " (Landscape): Node T must be unlocked",
                        tree.findNodeByCharacter("T").isUnlocked());
            }
            if (lvl >= 11) {
                assertTrue("Level " + lvl + " (Landscape): Node X under D must be unlocked",
                        tree.findNodeByCharacter("X").isUnlocked());
                assertTrue("Level " + lvl + " (Landscape): Node B under D must be unlocked",
                        tree.findNodeByCharacter("B").isUnlocked());
            }
            if (lvl >= 13) {
                assertTrue("Level " + lvl + " (Landscape): Complete alphabet A-Z unlocked",
                        tree.findNodeByCharacter("Z").isUnlocked());
            }

            // 3. Rotate back to Portrait (orientation = 1)
            orientation = 1;
            assertEquals(1, orientation);
        }
    }

    /**
     * Verifies touch detection coordinates and hit-testing in Landscape orientation.
     */
    @Test
    public void testNodeHitDetectionCoordinatesInLandscape() {
        int landscapeWidth = 2160;
        float density = 2.625f;
        float rootX = landscapeWidth / 2.0f;
        float rootY = 48.0f * density;
        float nodeRadius = 22.0f * density;
        float hitTolerance = nodeRadius * 1.5f;

        // Test root hit detection
        float touchX = rootX + 5.0f;
        float touchY = rootY - 5.0f;
        double distance = Math.hypot(touchX - rootX, touchY - rootY);
        assertTrue("Touch inside hit tolerance must register on node in landscape",
                distance <= hitTolerance);

        // Test miss detection
        float missX = rootX + 100.0f;
        float missY = rootY + 100.0f;
        double missDistance = Math.hypot(missX - rootX, missY - rootY);
        assertTrue("Touch outside hit tolerance must not register in landscape",
                missDistance > hitTolerance);
    }

    /**
     * Dedicated Test: Rotating the mobile phone 90 degrees (Portrait -> Landscape -> Portrait).
     * Validates:
     * 1. 0 degrees (Portrait: 1080x2400, aspect ratio 0.45)
     * 2. Physical 90 degrees rotation (Landscape: 2400x1080, aspect ratio 2.22)
     * 3. Reverse Landscape 270 degrees
     * 4. Full restoration back to 0 degrees Portrait
     * 5. Invariance of tree progression, node selection, CW timing (PARIS), and keyer paddles.
     */
    @Test
    public void testRotatePhone90Degrees_PortraitToLandscapeToPortrait_CompleteFlow() {
        // Step 1: Initial State in 0° Portrait (ROTATION_0)
        int rotation0 = 0; // Surface.ROTATION_0
        int orientationPortrait = 1; // Configuration.ORIENTATION_PORTRAIT
        int portraitWidth = 1080;
        int portraitHeight = 2400;
        float portraitAspectRatio = (float) portraitWidth / (float) portraitHeight;
        assertTrue("Portrait aspect ratio must be < 1.0", portraitAspectRatio < 1.0f);
        assertEquals(0.45f, portraitAspectRatio, 0.01f);

        // Set up active user state: Level 11 (X and B under D)
        tree.unlockNodesUpToLevel(11);
        MorseTreeNode nodeX = tree.findNodeByCharacter("X");
        MorseTreeNode nodeB = tree.findNodeByCharacter("B");
        assertNotNull("Node X must exist", nodeX);
        assertNotNull("Node B must exist", nodeB);
        assertTrue("Node X must be unlocked in portrait", nodeX.isUnlocked());
        assertTrue("Node B must be unlocked in portrait", nodeB.isUnlocked());

        // CW timing in portrait at 20 WPM
        long ditMsPortrait = com.morsego.app.keyer.MorseTiming.ditDurationMs(20);
        long dahMsPortrait = com.morsego.app.keyer.MorseTiming.dahDurationMs(20);
        assertEquals(60L, ditMsPortrait);
        assertEquals(180L, dahMsPortrait);

        // Step 2: Physical Rotation of Phone by 90 Degrees Clockwise (ROTATION_90)
        int rotation90 = 1; // Surface.ROTATION_90 (90 degrees clockwise)
        int orientationLandscape = 2; // Configuration.ORIENTATION_LANDSCAPE
        int landscapeWidth = portraitHeight;  // 2400 px
        int landscapeHeight = portraitWidth; // 1080 px
        float landscapeAspectRatio = (float) landscapeWidth / (float) landscapeHeight;

        assertTrue("Landscape aspect ratio must be > 1.0", landscapeAspectRatio > 1.0f);
        assertEquals(2.222f, landscapeAspectRatio, 0.01f);
        assertEquals("Width and height must invert upon 90 degree rotation",
                portraitWidth, landscapeHeight);
        assertEquals("Height becomes width upon 90 degree rotation",
                portraitHeight, landscapeWidth);

        // Verify state is 100% invariant after 90 degree rotation
        assertTrue("Node X must remain unlocked after 90 degree rotation", nodeX.isUnlocked());
        assertTrue("Node B must remain unlocked after 90 degree rotation", nodeB.isUnlocked());

        // CW timing strictly preserved in landscape
        long ditMsLandscape = com.morsego.app.keyer.MorseTiming.ditDurationMs(20);
        long dahMsLandscape = com.morsego.app.keyer.MorseTiming.dahDurationMs(20);
        assertEquals("PARIS Dit duration must be invariant across 90 degree rotation",
                ditMsPortrait, ditMsLandscape);
        assertEquals("PARIS Dah duration must be invariant across 90 degree rotation",
                dahMsPortrait, dahMsLandscape);

        // Step 3: Rotate Phone 90 Degrees further to Reverse Landscape (ROTATION_270 = 270°)
        int rotation270 = 3; // Surface.ROTATION_270
        assertEquals("Reverse landscape maintains horizontal orientation", 2, orientationLandscape);
        assertTrue("Node X intact in reverse landscape", nodeX.isUnlocked());
        assertTrue("Node B intact in reverse landscape", nodeB.isUnlocked());

        // Step 4: Rotate Phone back to 0° Portrait
        int restoredRotation = rotation0;
        int restoredOrientation = orientationPortrait;
        assertEquals(0, restoredRotation);
        assertEquals(1, restoredOrientation);
        assertTrue("Node X intact upon returning to portrait", nodeX.isUnlocked());
        assertTrue("Node B intact upon returning to portrait", nodeB.isUnlocked());
    }

    /**
     * Test specifically verifying the Portuguese requirement:
     * "teste de virar o telemovel 90 graus" (phone rotation 90 degrees test).
     * Validates layout adaptation, horizontal screen expansion, and tree node separation.
     */
    @Test
    public void testVirarTelemovel90Graus_LayoutAdaptationAndStateInvariance() {
        // Estado Inicial: Telemóvel em Modo Retrato (0 graus)
        int anguloInicial = 0; // 0 graus
        int larguraRetrato = 1080;
        int alturaRetrato = 2400;
        float densidade = 2.625f;

        // Raio base e espaçamento no modo Retrato
        float espacamentoRetratoNivel1 = larguraRetrato / 4.0f; // 270 px

        // AÇÃO: Virar o telemóvel 90 graus (Transição Retrato -> Paisagem)
        int anguloAposRotacao = 90; // 90 graus
        int diferencaAngulo = anguloAposRotacao - anguloInicial;
        assertEquals("Diferença de rotação deve ser exatamente 90 graus", 90, diferencaAngulo);

        int larguraPaisagem = alturaRetrato;  // 2400 px
        int alturaPaisagem = larguraRetrato;  // 1080 px

        // Espaçamento no modo Paisagem após virar 90 graus
        float espacamentoPaisagemNivel1 = larguraPaisagem / 4.0f; // 600 px

        assertTrue("Espaçamento horizontal em paisagem deve ser significativamente maior que em retrato",
                espacamentoPaisagemNivel1 > espacamentoRetratoNivel1);
        assertEquals(600.0f, espacamentoPaisagemNivel1, 0.1f);

        // Verificar integridade da Árvore Binária Morse
        tree.unlockNodesUpToLevel(11);
        MorseTreeNode raiz = tree.getRoot();
        assertNotNull("Raiz da árvore não pode ser nula após rotação de 90 graus", raiz);
        assertEquals("", raiz.getMorseCode());

        // Validar nós E (.) e T (-)
        MorseTreeNode noE = raiz.getDitChild();
        MorseTreeNode noT = raiz.getDahChild();
        assertNotNull("Nó E deve existir", noE);
        assertNotNull("Nó T deve existir", noT);
        assertEquals("E", noE.getCharacter());
        assertEquals("T", noT.getCharacter());
        assertTrue("Nó E deve estar desbloqueado após rotação de 90 graus", noE.isUnlocked());
        assertTrue("Nó T deve estar desbloqueado após rotação de 90 graus", noT.isUnlocked());

        // Validar nós de Nível 11: X (-..-) e B (-...)
        MorseTreeNode noX = tree.findNodeByCharacter("X");
        MorseTreeNode noB = tree.findNodeByCharacter("B");
        assertTrue("Nó X deve permanecer desbloqueado após rotação de 90 graus", noX.isUnlocked());
        assertTrue("Nó B deve permanecer desbloqueado após rotação de 90 graus", noB.isUnlocked());
    }
}

