package tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

public class GenerateLandscapeTree {

    public static class NodeDef {
        public String letter;
        public String morse;
        public int x;
        public int y;
        public float radius;
        public boolean isDah; // true if left child of parent
        public NodeDef parent;
        public boolean unlocked;
        public boolean selected;

        public NodeDef(String letter, String morse, int x, int y, float radius, boolean isDah, NodeDef parent, boolean unlocked) {
            this.letter = letter;
            this.morse = morse;
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.isDah = isDah;
            this.parent = parent;
            this.unlocked = unlocked;
            this.selected = false;
        }
    }

    public static BufferedImage renderLandscapeTreeImage(int level, String selectedLetter, String stepBannerText, String appTitleText) {
        int w = 860;
        int h = 412;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Fill entire canvas with authentic background
        g.setColor(new Color(0x0A, 0x0E, 0x14));
        g.fillRect(0, 0, w, h);

        // 1. Status bar (0..22)
        g.setColor(new Color(0x07, 0x0A, 0x0F));
        g.fillRect(0, 0, w, 22);
        g.setColor(new Color(0x8C, 0x98, 0xA8));
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.drawString("12:45", 16, 15);
        g.drawString("90\u00B0 LANDSCAPE \u2022 860\u00D7412", w / 2 - 60, 15);
        g.drawString("5G 100%", w - 76, 15);

        // Battery icon
        g.setColor(new Color(0x8C, 0x98, 0xA8));
        g.drawRoundRect(w - 28, 7, 18, 9, 2, 2);
        g.fillRect(w - 26, 9, 13, 5);
        g.fillRect(w - 10, 9, 2, 5);

        // 2. Action bar (22..68)
        g.setColor(new Color(0x0E, 0x13, 0x1D));
        g.fillRect(0, 22, w, 44);
        g.setColor(new Color(0x1F, 0x29, 0x37));
        g.drawLine(0, 66, w, 66);

        // morseGO logo
        g.setColor(new Color(0x00, 0xE6, 0x76));
        g.setFont(new Font("SansSerif", Font.BOLD, 17));
        g.drawString("morseGO", 16, 50);

        // Subtitle badge
        g.setColor(new Color(0xEE, 0xEE, 0xEE));
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        String titleStr = (appTitleText != null && !appTitleText.isEmpty()) ? appTitleText : "\u2022 \u00C1rvore Bin\u00E1ria de Morse [N\u00EDvel 11: X & B]";
        if (!titleStr.startsWith("\u2022") && !titleStr.startsWith("morseGO")) {
            titleStr = "\u2022 " + titleStr;
        }
        g.drawString(titleStr, 110, 49);

        // Pills on right
        // 20 WPM
        int pillY = 32;
        int pillH = 24;
        g.setColor(new Color(0x10, 0x2A, 0x45));
        g.fillRoundRect(w - 240, pillY, 65, pillH, 6, 6);
        g.setColor(new Color(0x00, 0xB0, 0xFF));
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("20 WPM", w - 232, pillY + 16);

        // 700 Hz
        g.setColor(new Color(0x2A, 0x22, 0x10));
        g.fillRoundRect(w - 165, pillY, 65, pillH, 6, 6);
        g.setColor(new Color(0xFF, 0xB3, 0x00));
        g.drawString("700 Hz", w - 156, pillY + 16);

        // Settings button
        g.setColor(new Color(0x16, 0x20, 0x2E));
        g.fillRoundRect(w - 90, pillY, 74, pillH, 6, 6);
        g.setColor(new Color(0x37, 0x47, 0x5A));
        g.drawRoundRect(w - 90, pillY, 74, pillH, 6, 6);
        g.setColor(new Color(0xEE, 0xEE, 0xEE));
        g.drawString("\u2699 Settings", w - 84, pillY + 16);

        // 3. Step banner (68..96) if present
        int bannerY = 68;
        int bannerH = 26;
        if (stepBannerText != null && !stepBannerText.trim().isEmpty()) {
            g.setColor(new Color(0x0C, 0x1E, 0x16));
            g.fillRoundRect(14, bannerY, w - 28, bannerH, 6, 6);
            g.setColor(new Color(0x18, 0x4D, 0x2E));
            g.setStroke(new BasicStroke(1.0f));
            g.drawRoundRect(14, bannerY, w - 28, bannerH, 6, 6);

            g.setColor(new Color(0x00, 0xE6, 0x76));
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.drawString(stepBannerText, 24, bannerY + 17);
        }

        // 4. Tree area: y = 98 to 310
        // Define all nodes strictly matching MorseTreeView & vertical screenshots:
        // Left = DAH (—), Right = DIT (•)
        java.util.List<NodeDef> nodes = new java.util.ArrayList<>();

        // Root at (430, 118)
        NodeDef root = new NodeDef("START", "", 430, 118, 14.0f, false, null, true);
        nodes.add(root);

        // Depth 1 (dy = 40)
        // Left child of Root = T (DAH —)
        NodeDef nodeT = new NodeDef("T", "\u2014", 264, 158, 13.5f, true, root, true);
        // Right child of Root = E (DIT •)
        NodeDef nodeE = new NodeDef("E", "\u2022", 596, 158, 13.5f, false, root, true);
        nodes.add(nodeT);
        nodes.add(nodeE);

        // Depth 2 (dy = 42)
        // Under T: Left = M (DAH ——), Right = N (DIT —•)
        NodeDef nodeM = new NodeDef("M", "\u2014\u2014", 180, 200, 13.5f, true, nodeT, level >= 2);
        NodeDef nodeN = new NodeDef("N", "\u2014\u2022", 348, 200, 13.5f, false, nodeT, level >= 2);
        // Under E: Left = A (DAH •—), Right = I (DIT ••)
        NodeDef nodeA = new NodeDef("A", "\u2022\u2014", 512, 200, 13.5f, true, nodeE, level >= 2);
        NodeDef nodeI = new NodeDef("I", "\u2022\u2022", 680, 200, 13.5f, false, nodeE, level >= 2);
        nodes.add(nodeM);
        nodes.add(nodeN);
        nodes.add(nodeA);
        nodes.add(nodeI);

        // Depth 3 (dy = 42)
        // Under M: Left = O (———), Right = G (——•)
        NodeDef nodeO = new NodeDef("O", "\u2014\u2014\u2014", 138, 242, 13.0f, true, nodeM, level >= 3);
        NodeDef nodeG = new NodeDef("G", "\u2014\u2014\u2022", 222, 242, 13.0f, false, nodeM, level >= 3);
        // Under N: Left = K (—•—), Right = D (—••)
        NodeDef nodeK = new NodeDef("K", "\u2014\u2022\u2014", 306, 242, 13.0f, true, nodeN, level >= 3);
        NodeDef nodeD = new NodeDef("D", "\u2014\u2022\u2022", 390, 242, 13.0f, false, nodeN, level >= 3);
        // Under A: Left = W (•——), Right = R (•—•)
        NodeDef nodeW = new NodeDef("W", "\u2022\u2014\u2014", 470, 242, 13.0f, true, nodeA, level >= 13);
        NodeDef nodeR = new NodeDef("R", "\u2022\u2014\u2022", 554, 242, 13.0f, false, nodeA, level >= 13);
        // Under I: Left = U (••—), Right = S (•••)
        NodeDef nodeU = new NodeDef("U", "\u2022\u2022\u2014", 638, 242, 13.0f, true, nodeI, level >= 13);
        NodeDef nodeS = new NodeDef("S", "\u2022\u2022\u2022", 722, 242, 13.0f, false, nodeI, level >= 13);
        nodes.add(nodeO);
        nodes.add(nodeG);
        nodes.add(nodeK);
        nodes.add(nodeD);
        nodes.add(nodeW);
        nodes.add(nodeR);
        nodes.add(nodeU);
        nodes.add(nodeS);

        // Depth 4 (dy = 42)
        // Under G: Left = Q (——•—), Right = Z (——••)
        NodeDef nodeQ = new NodeDef("Q", "\u2014\u2014\u2022\u2014", 201, 284, 12.0f, true, nodeG, false);
        NodeDef nodeZ = new NodeDef("Z", "\u2014\u2014\u2022\u2022", 243, 284, 12.0f, false, nodeG, false);
        // Under K: Left = Y (—•——), Right = C (—•—•)
        NodeDef nodeY = new NodeDef("Y", "\u2014\u2022\u2014\u2014", 285, 284, 12.0f, true, nodeK, false);
        NodeDef nodeC = new NodeDef("C", "\u2014\u2022\u2014\u2022", 327, 284, 12.0f, false, nodeK, false);
        // Under D: Left = X (—••—), Right = B (—•••)  [UNLOCKED IN LEVEL 11!]
        NodeDef nodeX = new NodeDef("X", "\u2014\u2022\u2022\u2014", 369, 284, 12.0f, true, nodeD, level >= 11);
        NodeDef nodeB = new NodeDef("B", "\u2014\u2022\u2022\u2022", 411, 284, 12.0f, false, nodeD, level >= 11);
        // Under W: Left = J (•———), Right = P (•——•)
        NodeDef nodeJ = new NodeDef("J", "\u2022\u2014\u2014\u2014", 449, 284, 12.0f, true, nodeW, false);
        NodeDef nodeP = new NodeDef("P", "\u2022\u2014\u2014\u2022", 491, 284, 12.0f, false, nodeW, false);
        // Under R: Right = L (•—••)
        NodeDef nodeL = new NodeDef("L", "\u2022\u2014\u2022\u2022", 575, 284, 12.0f, false, nodeR, false);
        // Under U: Right = F (••—•)
        NodeDef nodeF = new NodeDef("F", "\u2022\u2022\u2014\u2022", 659, 284, 12.0f, false, nodeU, false);
        // Under S: Left = V (•••—), Right = H (••••)
        NodeDef nodeV = new NodeDef("V", "\u2022\u2022\u2022\u2014", 701, 284, 12.0f, true, nodeS, false);
        NodeDef nodeH = new NodeDef("H", "\u2022\u2022\u2022\u2022", 743, 284, 12.0f, false, nodeS, false);

        nodes.add(nodeQ);
        nodes.add(nodeZ);
        nodes.add(nodeY);
        nodes.add(nodeC);
        nodes.add(nodeX);
        nodes.add(nodeB);
        nodes.add(nodeJ);
        nodes.add(nodeP);
        nodes.add(nodeL);
        nodes.add(nodeF);
        nodes.add(nodeV);
        nodes.add(nodeH);

        // Mark selected node
        if (selectedLetter != null) {
            for (NodeDef n : nodes) {
                if (selectedLetter.equals(n.letter)) {
                    n.selected = true;
                    break;
                }
            }
        }

        // --- DRAW CONNECTING LINES ---
        for (NodeDef n : nodes) {
            if (n.parent == null) continue;
            boolean activeLine = n.unlocked && n.parent.unlocked;

            if (activeLine) {
                // Bright mint/green glow line
                g.setColor(new Color(0x00, 0xE6, 0x76, 0x40));
                g.setStroke(new BasicStroke(4.0f));
                g.drawLine(n.parent.x, n.parent.y, n.x, n.y);

                g.setColor(new Color(0x00, 0xE6, 0x76));
                g.setStroke(new BasicStroke(2.0f));
                g.drawLine(n.parent.x, n.parent.y, n.x, n.y);
            } else {
                // Locked dark line
                g.setColor(new Color(0x1B, 0x22, 0x2D));
                g.setStroke(new BasicStroke(1.2f));
                g.drawLine(n.parent.x, n.parent.y, n.x, n.y);
            }

            // Draw branch indicator: Dah "—" on left, Dit "•" on right
            int midX = (n.parent.x + n.x) / 2;
            int midY = (n.parent.y + n.y) / 2;
            g.setFont(new Font("SansSerif", Font.BOLD, 12));

            if (n.isDah) {
                // Left branch: DAH
                g.setColor(activeLine ? new Color(0x00, 0xE5, 0xFF) : new Color(0x3B, 0x47, 0x58));
                g.drawString("\u2014", midX - 10, midY - 2);
            } else {
                // Right branch: DIT
                g.setColor(activeLine ? new Color(0xFF, 0xB3, 0x00) : new Color(0x3B, 0x47, 0x58));
                g.drawString("\u2022", midX + 6, midY - 2);
            }
        }

        // --- DRAW NODES ---
        for (NodeDef n : nodes) {
            int r = Math.round(n.radius);

            if (n.letter.equals("START")) {
                // START Root Node
                g.setColor(new Color(0x00, 0xE6, 0x76));
                g.fillOval(n.x - r, n.y - r, r * 2, r * 2);
                g.setColor(new Color(0x80, 0xFF, 0xC0));
                g.setStroke(new BasicStroke(1.5f));
                g.drawOval(n.x - r, n.y - r, r * 2, r * 2);

                g.setColor(new Color(0x0A, 0x0E, 0x14));
                g.setFont(new Font("SansSerif", Font.BOLD, 9));
                int tw = g.getFontMetrics().stringWidth("START");
                g.drawString("START", n.x - tw / 2, n.y + 3);
                continue;
            }

            if (n.unlocked) {
                // Active node: Bright Mint Green
                g.setColor(new Color(0x00, 0xE6, 0x76));
                g.fillOval(n.x - r, n.y - r, r * 2, r * 2);
                g.setColor(new Color(0x80, 0xFF, 0xC0));
                g.setStroke(new BasicStroke(1.5f));
                g.drawOval(n.x - r, n.y - r, r * 2, r * 2);

                // Letter inside
                g.setColor(new Color(0x0A, 0x0E, 0x14));
                g.setFont(new Font("SansSerif", Font.BOLD, Math.round(n.radius * 0.95f)));
                int tw = g.getFontMetrics().stringWidth(n.letter);
                g.drawString(n.letter, n.x - tw / 2, n.y + Math.round(n.radius * 0.35f));

                // Morse text underneath
                if (!n.morse.isEmpty()) {
                    g.setColor(new Color(0x00, 0xE5, 0xFF));
                    g.setFont(new Font("SansSerif", Font.BOLD, 10));
                    int mw = g.getFontMetrics().stringWidth(n.morse);
                    g.drawString(n.morse, n.x - mw / 2, n.y + r + 11);
                }

                // If selected (e.g. node X): Draw outer amber glow ring
                if (n.selected) {
                    g.setColor(new Color(0xFF, 0xB3, 0x00));
                    g.setStroke(new BasicStroke(2.0f));
                    g.drawOval(n.x - r - 4, n.y - r - 4, (r + 4) * 2, (r + 4) * 2);
                }
            } else {
                // Locked node: Dark gray
                g.setColor(new Color(0x13, 0x19, 0x22));
                g.fillOval(n.x - r, n.y - r, r * 2, r * 2);
                g.setColor(new Color(0x28, 0x33, 0x44));
                g.setStroke(new BasicStroke(1.0f));
                g.drawOval(n.x - r, n.y - r, r * 2, r * 2);

                // Letter inside
                g.setColor(new Color(0x6E, 0x7B, 0x8D));
                g.setFont(new Font("SansSerif", Font.BOLD, Math.round(n.radius * 0.95f)));
                int tw = g.getFontMetrics().stringWidth(n.letter);
                g.drawString(n.letter, n.x - tw / 2, n.y + Math.round(n.radius * 0.35f));

                // Morse text underneath
                if (!n.morse.isEmpty()) {
                    g.setColor(new Color(0x3B, 0x47, 0x58));
                    g.setFont(new Font("SansSerif", Font.PLAIN, 9));
                    int mw = g.getFontMetrics().stringWidth(n.morse);
                    g.drawString(n.morse, n.x - mw / 2, n.y + r + 10);
                }
            }
        }

        // 5. Footer card: x = 14, y = 312, w = 832, h = 46 (CLEAN, NO PLAY CW BUTTON!)
        int cardX = 14;
        int cardY = 312;
        int cardW = w - 28;
        int cardH = 46;

        g.setColor(new Color(0x0E, 0x14, 0x1E));
        g.fillRoundRect(cardX, cardY, cardW, cardH, 8, 8);
        g.setColor(new Color(0x00, 0xE6, 0x76));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(cardX, cardY, cardW, cardH, 8, 8);

        // Footer text: balanced layout across card without any redundant button
        int textY = cardY + 28;
        int curX = cardX + 24;

        g.setColor(new Color(0xFF, 0xB3, 0x00));
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        String selStr = "Selected Node: X (-\u2022\u2022-)";
        g.drawString(selStr, curX, textY);
        curX += g.getFontMetrics().stringWidth(selStr) + 24;

        g.setColor(new Color(0x8C, 0x98, 0xA8));
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        String lvlStr = "\u2022  Level 11/21 Unlocked (12 Active Letters)";
        g.drawString(lvlStr, curX, textY);
        curX += g.getFontMetrics().stringWidth(lvlStr) + 24;

        g.setColor(new Color(0x00, 0xE6, 0x76));
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        String diagStr = "\u2022  Esquerda = Dah (\u2014) | Direita = Dit (\u2022) \u2022 Direct Diagonals";
        g.drawString(diagStr, curX, textY);

        // 6. Bottom Navigation Bar (Tab 0 Tree active in GREEN)
        TreeScreenshotFixer.drawBottomNav(g, w, h, 40, 0);

        g.dispose();
        return img;
    }

    public static void saveImage(BufferedImage img, File file) throws Exception {
        if (file.getName().toLowerCase().endsWith(".png")) {
            ImageIO.write(img, "PNG", file);
            return;
        }
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
            params.setCompressionQuality(0.92f);
            writer.write(null, new IIOImage(img, null, null), params);
        } finally {
            writer.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedImage testImg = renderLandscapeTreeImage(11, "X", "Behavior Test 1: Morse Binary Tree \u2022 Direct Diagonals", "\u00C1rvore Bin\u00E1ria de Morse [N\u00EDvel 11: X & B]");
        saveImage(testImg, new File("tools/clean_rot2.jpg"));
        System.out.println("Updated tools/clean_rot2.jpg successfully.");
    }
}
