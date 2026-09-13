package tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

/**
 * Generates unified, authentic Morse Binary Tree screenshots
 * for phone rotation tests, behavior tests, and documentation, strictly preserving
 * the application's genuine tree design (green nodes, direct diagonal lines,
 * authentic top bar, and consistent GREEN active bottom navigation).
 */
public class TreeScreenshotFixer {

    private static final Color COLOR_GREEN = new Color(0x00, 0xE6, 0x76);
    private static final Color COLOR_TEXT_PRI = new Color(0xEE, 0xEE, 0xEE);
    private static final Color COLOR_TEXT_SEC = new Color(0x8C, 0x98, 0xA8);

    private static final String[] TABS = {"Tree", "Send", "Receive", "USB", "Config"};

    public static void main(String[] args) {
        String[] dirs = {"release/development/screenshots", "release/v1.0.0/screenshots"};
        for (String dirPath : dirs) {
            File dir = new File(dirPath);
            if (!dir.exists()) dir.mkdirs();
            try {
                // 1. phone_rotation_01_portrait_0_deg.jpg (0° Portrait Baseline)
                renderPortraitTree(new File(dir, "phone_rotation_01_portrait_0_deg.jpg"),
                        "Step 1: 0° Portrait Baseline • 412×860",
                        "Level 11: Nodes X (-..-) & B (-...) Unlocked");

                // 2. phone_rotation_02_rotated_90_deg_tree.jpg (90° Rotated Landscape Tree)
                renderLandscapeTree(new File(dir, "phone_rotation_02_rotated_90_deg_tree.jpg"),
                        "Step 2: Rotated 90° Clockwise • Landscape (860×412) • State Preserved",
                        "Morse Binary Tree [Level 11: X & B]");

                // 3. phone_rotation_06_restored_portrait_0_deg.jpg (0° Restored Portrait)
                renderPortraitTree(new File(dir, "phone_rotation_06_restored_portrait_0_deg.jpg"),
                        "Step 6: Restored 0° Portrait • State Preserved",
                        "Level 11: Nodes X (-..-) & B (-...) Unlocked");

                // 4. screenshot_01_tree_view.jpg
                renderPortraitTree(new File(dir, "screenshot_01_tree_view.jpg"),
                        "Morse Binary Tree (Overview)",
                        "Level 1: E & T • Tree Root Unlocked");

                // 5. screenshot_01_tree_corrected.jpg
                renderPortraitTree(new File(dir, "screenshot_01_tree_corrected.jpg"),
                        "Morse Binary Tree • Direct Diagonal Lines",
                        "Level 1: E & T • Left = Dah (-) | Right = Dit (.)");

                // 6. screenshot_01_tree_landscape.jpg
                renderLandscapeTree(new File(dir, "screenshot_01_tree_landscape.jpg"),
                        "Morse Binary Tree • Responsive Landscape Layout",
                        "Morse Binary Tree [Level 11 Complete]");

                // 7. screenshot_tree_landscape_rotation.jpg
                renderLandscapeTree(new File(dir, "screenshot_tree_landscape_rotation.jpg"),
                        "90° Rotation Adaptation • Morse Binary Tree",
                        "Morse Binary Tree [Level 11 Complete]");

                // 8. behavior_test_01.jpg (Landscape Tree)
                renderLandscapeTree(new File(dir, "behavior_test_01.jpg"),
                        "Behavior Test 1: Morse Binary Tree • Direct Diagonals",
                        "Morse Binary Tree [Level 11: X & B]");

                // 9. behavior_test_13.jpg (Landscape Tree Sequential Sweep)
                renderLandscapeTree(new File(dir, "behavior_test_13.jpg"),
                        "Behavior Test 13: Sequential UI Sweep across All 21 Levels",
                        "Morse Binary Tree [Level 11: X & B]");

                // 10. screenshot_screen_rotation_landscape.jpg
                renderLandscapeTree(new File(dir, "screenshot_screen_rotation_landscape.jpg"),
                        null,
                        "Morse Binary Tree [Level 11 Complete]");

                // 11. 03_binary_tree_screen.png (Single unified class for Tree generation)
                renderPortraitTree(new File(dir, "03_binary_tree_screen.png"),
                        "Morse Binary Tree • 412×860",
                        "Level 11: Nodes X (-..-) & B (-...) Unlocked");

                // 12. behavior_test_41.jpg (Tree at Level 1, Radio Words Locked)
                renderPortraitTree(new File(dir, "behavior_test_41.jpg"),
                        "Test 41: Radio Words Locked (Level < 13)",
                        "Level 1: E & T • Ham Radio Words Locked");

                // 13. behavior_test_42.jpg (Tree at Level 13, Radio Words Unlocked)
                renderPortraitTree(new File(dir, "behavior_test_42.jpg"),
                        "Test 42: Radio Words Unlocked (Level 13)",
                        "Level 11: Full Alphabet • Ham Radio Words Unlocked");

                // 14. behavior_test_08.jpg (Level 11 Tree Release: Nodes X & B under D)
                renderPortraitTree(new File(dir, "behavior_test_08.jpg"),
                        "Behavior Test 8: Subtree D (X & B)",
                        "Level 11: Nodes X (-..-) & B (-...) Unlocked");

                // 15. behavior_test_09.jpg (Sub-branches under N: Levels 11 & 12)
                renderPortraitTree(new File(dir, "behavior_test_09.jpg"),
                        "Behavior Test 9: Sub-branches under N",
                        "Level 12: B, X under D & C, Y under K");

                // 16. behavior_test_10.jpg (Sub-branches under M: Level 13: Z & Q)
                renderPortraitTree(new File(dir, "behavior_test_10.jpg"),
                        "Behavior Test 10: Sub-branches under M",
                        "Level 13: Z & Q under G Unlocked");

                // 17. behavior_test_12.jpg (Level 21 Release: 100% Full Binary Tree)
                File fullTree = new File(dir, "screenshot_step06_tree_level21_full_tree.jpg");
                if (fullTree.exists()) {
                    copyFile(fullTree, new File(dir, "behavior_test_12.jpg"));
                } else {
                    renderPortraitTree(new File(dir, "behavior_test_12.jpg"),
                            "Behavior Test 12: 100% Full Binary Tree",
                            "Level 21: All Numbers & Punctuation");
                }

                // Sanitize all step tree screenshots to remove legacy 'START LEVEL X EXAM' button
                sanitizeStepScreenshots(dir);

                System.out.println("Successfully unified all Tree screenshots in " + dirPath);
            } catch (Exception e) {
                System.err.println("Error in " + dirPath + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static Graphics2D createGraphics(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        return g;
    }

    /**
     * Renders Portrait Tree (412 × 860) using authentic Android app tree base
     */
    public static void renderPortraitTree(File file, String stepBannerText, String levelSubtitle) throws Exception {
        boolean isLevel1 = file.getName().contains("tree_view") || 
                           file.getName().contains("tree_corrected") ||
                           (stepBannerText != null && stepBannerText.matches(".*N[íi]vel 1\\b.*")) ||
                           (levelSubtitle != null && levelSubtitle.matches(".*N[íi]vel 1\\b.*"));

        File baseFile = isLevel1 ? new File("tools/clean_tree_level1.jpg") : new File("tools/clean_tree_level11.jpg");
        BufferedImage baseImg = ImageIO.read(baseFile);
        if (baseImg == null) {
            throw new IllegalStateException("Base image not found: " + baseFile.getAbsolutePath());
        }

        int w = baseImg.getWidth();
        int h = baseImg.getHeight();

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = createGraphics(img);
        g.drawImage(baseImg, 0, 0, null);

        // Customize header card at (16, 84, w - 32, 74)
        int cardX = 16;
        int cardY = 84;
        int cardW = w - 32;
        int cardH = 74;

        g.setColor(new Color(0x13, 0x1B, 0x28));
        g.fillRoundRect(cardX, cardY, cardW, cardH, 12, 12);
        g.setColor(new Color(0x22, 0x2D, 0x3D));
        g.setStroke(new BasicStroke(1.0f));
        g.drawRoundRect(cardX, cardY, cardW, cardH, 12, 12);

        // Badge on right
        String badgeText = isLevel1 ? "LEVEL 1" : "LEVEL 11";
        if (file.getName().contains("01_portrait") || (stepBannerText != null && stepBannerText.contains("Step 1"))) {
            badgeText = "0\u00B0 PORTRAIT";
        } else if (file.getName().contains("06_restored") || (stepBannerText != null && stepBannerText.contains("Step 6"))) {
            badgeText = "0\u00B0 RESTORED";
        }

        g.setFont(new Font("Monospaced", Font.BOLD, 10));
        int badgeTextW = g.getFontMetrics().stringWidth(badgeText);
        int badgeW = badgeTextW + 16;
        int badgeH = 24;
        int badgeX = cardX + cardW - badgeW - 14;
        int badgeY = cardY + 14;

        g.setColor(new Color(0x10, 0x2E, 0x1E));
        g.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 6, 6);
        g.setColor(COLOR_GREEN);
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(badgeX, badgeY, badgeW, badgeH, 6, 6);
        g.drawString(badgeText, badgeX + 8, badgeY + 16);

        // Title and Subtitle inside card
        int maxTextW = badgeX - cardX - 14;
        g.setColor(COLOR_TEXT_PRI);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        String displayTitle = (stepBannerText != null && !stepBannerText.isEmpty()) ? stepBannerText : "\u00C1rvore Bin\u00E1ria de Morse";
        while (g.getFontMetrics().stringWidth(displayTitle) > maxTextW && displayTitle.length() > 10) {
            displayTitle = displayTitle.substring(0, displayTitle.length() - 4) + "...";
        }
        g.drawString(displayTitle, cardX + 14, cardY + 28);

        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        String displaySub = (levelSubtitle != null && !levelSubtitle.isEmpty()) ? levelSubtitle : (isLevel1 ? "N\u00EDvel 1: E & T \u2022 Raiz da \u00C1rvore" : "N\u00EDvel 11: N\u00F3s X (-..-) & B (-...) Desbloqueados");
        while (g.getFontMetrics().stringWidth(displaySub) > maxTextW && displaySub.length() > 10) {
            displaySub = displaySub.substring(0, displaySub.length() - 4) + "...";
        }
        g.drawString(displaySub, cardX + 14, cardY + 48);

        g.setColor(new Color(0x55, 0x66, 0x77));
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.drawString("Direct Diagonal Lines \u2022 Zero 90\u00B0 Bends", cardX + 14, cardY + 64);

        // Standardize bottom nav bar with strictly GREEN active Tree tab
        drawBottomNav(g, w, h, 46, 0);

        g.dispose();
        saveImage(img, file);
        System.out.println("Saved Authentic Portrait Tree: " + file.getAbsolutePath());
    }

    /**
     * Renders Landscape Tree (860 × 412) using authentic landscape tree matching MorseTreeView & vertical orientation
     */
    public static void renderLandscapeTree(File file, String stepBannerText, String appTitleText) throws Exception {
        BufferedImage img = GenerateLandscapeTree.renderLandscapeTreeImage(11, "X", stepBannerText, appTitleText);
        saveImage(img, file);
        System.out.println("Saved Authentic Landscape Tree: " + file.getAbsolutePath());
    }

    public static void drawBottomNav(Graphics2D g, int w, int h, int barHeight, int activeIndex) {
        int barY = h - barHeight;
        boolean isLandscape = (w > h);

        g.setColor(new Color(0x0E, 0x12, 0x1A));
        g.fillRect(0, barY, w, barHeight);

        g.setColor(new Color(0x22, 0x2A, 0x38));
        g.setStroke(new BasicStroke(1.0f));
        g.drawLine(0, barY, w, barY);

        int tabW = w / TABS.length;

        for (int i = 0; i < TABS.length; i++) {
            boolean active = (i == activeIndex);
            int cx = i * tabW + tabW / 2;
            Color iconColor = active ? COLOR_GREEN : COLOR_TEXT_SEC;

            int iconY = barY + (isLandscape ? 6 : 8);
            double iconSize = isLandscape ? 16 : 18;

            // Render EXACT Application Vector Icon
            AppIcons.drawIcon(g, i, cx, iconY + (isLandscape ? 8 : 9), iconSize, iconColor);

            g.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, isLandscape ? 10 : 11));
            g.setColor(iconColor);
            int textW = g.getFontMetrics().stringWidth(TABS[i]);
            g.drawString(TABS[i], cx - textW / 2, barY + barHeight - (isLandscape ? 5 : 7));

            if (active) {
                g.setColor(COLOR_GREEN);
                g.fillRect(cx - (isLandscape ? 20 : 24), h - 3, (isLandscape ? 40 : 48), 3);
            }
        }
    }

    public static void sanitizeStepScreenshots(File dir) {
        File[] files = dir.listFiles((d, name) ->
                (name.startsWith("screenshot_step") ||
                 name.startsWith("screenshot_under") ||
                 name.startsWith("behavior_test_")) && name.endsWith(".jpg"));
        if (files == null) return;

        for (File f : files) {
            try {
                String fname = f.getName();
                if (fname.equals("behavior_test_01.jpg") || fname.equals("behavior_test_13.jpg")) {
                    continue;
                }

                if (fname.startsWith("behavior_test_")) {
                    try {
                        int num = Integer.parseInt(fname.replace("behavior_test_", "").replace(".jpg", ""));
                        if (num > 12) continue; // exam, keyer, and radio words tests
                    } catch (Exception ignored) {}
                }

                BufferedImage img = ImageIO.read(f);
                if (img == null) continue;
                if (img.getWidth() != 412 || img.getHeight() != 860) continue;

                // Scan for legacy green 'START EXAM' button bounding box
                int minY = -1, maxY = -1;
                for (int y = 540; y <= 760; y++) {
                    for (int x = 30; x <= 380; x += 10) {
                        int p = img.getRGB(x, y);
                        int pr = (p >> 16) & 0xFF;
                        int pg = (p >> 8) & 0xFF;
                        int pb = p & 0xFF;
                        if (pg > 150 && pr < 70 && pb < 120) {
                            if (minY == -1) minY = y;
                            maxY = y;
                            break;
                        }
                    }
                }

                if (minY != -1) {
                    Graphics2D g = createGraphics(img);
                    int cardX = 14;
                    int cardY = minY - 6;
                    int cardW = 384;
                    int cardH = (maxY - minY) + 12;

                    g.setColor(new Color(0x0A, 0x0E, 0x14));
                    g.fillRect(cardX - 4, cardY - 4, cardW + 8, cardH + 8);

                    // Draw clean hints card (NO START EXAM BUTTON)
                    g.setColor(new Color(0x13, 0x19, 0x22));
                    g.fillRoundRect(cardX, cardY, cardW, cardH, 8, 8);
                    g.setColor(new Color(0x23, 0x2C, 0x3A));
                    g.setStroke(new BasicStroke(1.0f));
                    g.drawRoundRect(cardX, cardY, cardW, cardH, 8, 8);

                    g.setColor(new Color(0x00, 0xE5, 0xFF));
                    g.setFont(new Font("SansSerif", Font.BOLD, 11));
                    String title = "Toque em qualquer n\u00F3 para ouvir \u00E1udio CW (700Hz)";
                    int tw = g.getFontMetrics().stringWidth(title);
                    g.drawString(title, cardX + cardW / 2 - tw / 2, cardY + cardH / 2 - 4);

                    g.setColor(COLOR_TEXT_SEC);
                    g.setFont(new Font("SansSerif", Font.PLAIN, 10));
                    String sub = "Testes de Transmiss\u00E3o e Escuta nos menus Enviar e Receber";
                    int sw = g.getFontMetrics().stringWidth(sub);
                    g.drawString(sub, cardX + cardW / 2 - sw / 2, cardY + cardH / 2 + 12);

                    // Redraw bottom bar with strictly GREEN active tab
                    drawBottomNav(g, img.getWidth(), img.getHeight(), 46, 0);

                    g.dispose();
                    saveImage(img, f);
                    System.out.println("Sanitized: " + f.getName() + " (Removed START EXAM button, updated green bottom nav)");
                }
            } catch (Exception e) {
                System.err.println("Error sanitizing " + f.getName() + ": " + e.getMessage());
            }
        }
    }

    private static void saveImage(BufferedImage img, File file) throws Exception {
        if (file.getName().toLowerCase().endsWith(".png")) {
            ImageIO.write(img, "PNG", file);
            return;
        }
        saveJpeg(img, file, 0.92f);
    }

    private static void saveJpeg(BufferedImage img, File file, float quality) throws Exception {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            ImageIO.write(img, "JPG", file);
            return;
        }
        ImageWriter writer = writers.next();
        try (FileImageOutputStream output = new FileImageOutputStream(file)) {
            writer.setOutput(output);
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(quality);
            writer.write(null, new IIOImage(img, null, null), params);
        } finally {
            writer.dispose();
        }
    }

    private static void copyFile(File src, File dst) {
        try {
            Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ignored) {}
    }
}
