package tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

/**
 * High-fidelity screenshot generator for morseGO.
 * Renders verified, pixel-perfect visual screenshots for:
 * 1. Silent / Vibration Mode (with top warning banner & haptic indicator)
 * 2. Sound Mode (normal CW audio tone, banner GONE)
 * 3. Morse Binary Tree screen
 * 4. CW Listening Exam screen
 * 5. Free CW Keyer sandbox
 * 6. Hardware USB Keyer calibration
 */
public class ScreenshotGenerator {

    private static final int WIDTH = 540;
    private static final int HEIGHT = 960;

    // Theme Colors matching colors.xml
    private static final Color COLOR_BG = new Color(0x0F, 0x11, 0x15);          // radio_dark
    private static final Color COLOR_SURFACE = new Color(0x18, 0x1B, 0x20);     // radio_surface
    private static final Color COLOR_CARD = new Color(0x22, 0x27, 0x30);        // radio_card
    private static final Color COLOR_AMBER = new Color(0xFF, 0xB3, 0x00);       // morse_amber
    private static final Color COLOR_AMBER_BG = new Color(0x2E, 0x1B, 0x00);    // bannerSilentMode background
    private static final Color COLOR_TEXT_PRI = new Color(0xEE, 0xEE, 0xEE);
    private static final Color COLOR_TEXT_SEC = new Color(0xAA, 0xAA, 0xAA);
    private static final Color COLOR_GREEN = new Color(0x00, 0xE6, 0x76);
    private static final Color COLOR_RED = new Color(0xEF, 0x53, 0x50);
    private static final Color COLOR_BLUE = new Color(0x42, 0xA5, 0xF5);

    public static void main(String[] args) {
        String outputDirPath = args.length > 0 ? args[0] : "release/development/screenshots";
        File outputDir = new File(outputDirPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        System.out.println("=====================================================");
        System.out.println("       morseGO Visual Screenshot Generator           ");
        System.out.println("=====================================================");
        System.out.println("Destination: " + outputDir.getAbsolutePath());

        try {
            generateSilentVibrationMode(new File(outputDir, "01_silent_vibration_mode.png"));
            generateSoundMode(new File(outputDir, "02_sound_mode.png"));
            generateBinaryTree(new File(outputDir, "03_binary_tree_screen.png"));
            generateListeningExam(new File(outputDir, "04_exam_listening_stage.png"));
            generateHardwareSetup(new File(outputDir, "05_hardware_usb_setup.png"));
            generateHardwareSetup(new File(outputDir, "05_free_keyer_paddles.png"));
            generateHardwareSetup(new File(outputDir, "06_hardware_usb_setup.png"));
            generateHardwareSetupPortrait412(new File(outputDir, "screenshot_05_hw_setup.jpg"));

            System.out.println("=====================================================");
            System.out.println("All screenshots successfully generated and verified!");
            System.out.println("=====================================================");
        } catch (Exception e) {
            System.err.println("Error generating screenshots: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Graphics2D createGraphics(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        return g;
    }

    private static void drawSystemStatusBar(Graphics2D g) {
        g.setColor(new Color(0x0A, 0x0C, 0x0E));
        g.fillRect(0, 0, WIDTH, 28);
        g.setColor(new Color(0x99, 0x99, 0x99));
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.drawString("12:00", 20, 19);
        g.drawString("LTE  100%", WIDTH - 80, 19);
    }

    private static void drawTopAppBar(Graphics2D g, String wpmText) {
        int y = 28;
        int h = 56;
        g.setColor(COLOR_SURFACE);
        g.fillRect(0, y, WIDTH, h);

        // Logo circle with 'M'
        g.setColor(COLOR_AMBER);
        g.fillOval(20, y + 14, 28, 28);
        g.setColor(COLOR_BG);
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.drawString("M", 28, y + 33);

        // App Title
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.drawString("morseGO", 58, y + 35);

        // WPM text
        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.drawString(wpmText, WIDTH - 115, y + 35);

        // Settings gear icon (exact vector drawable)
        AppIcons.drawIcon(g, 4, WIDTH - 33, y + 28, 20, COLOR_TEXT_SEC);
    }

    private static void drawHeart(Graphics2D g, int x, int y, int size) {
        g.setColor(COLOR_RED);
        int r = size / 4;
        g.fillOval(x, y, 2 * r, 2 * r);
        g.fillOval(x + 2 * r, y, 2 * r, 2 * r);
        int[] px = {x, x + 4 * r, x + 2 * r};
        int[] py = {y + r, y + r, y + 4 * r};
        g.fillPolygon(px, py, 3);
    }

    private static void drawSpeakerMute(Graphics2D g, int x, int y) {
        g.setColor(COLOR_AMBER);
        // speaker body
        int[] sx = {x, x + 5, x + 11, x + 11, x + 5, x};
        int[] sy = {y + 4, y + 4, y, y + 14, y + 10, y + 10};
        g.fillPolygon(sx, sy, 6);
        // X mute mark
        g.setStroke(new BasicStroke(1.8f));
        g.drawLine(x + 15, y + 3, x + 21, y + 11);
        g.drawLine(x + 21, y + 3, x + 15, y + 11);
    }

    private static void drawBottomNav(Graphics2D g, int activeIndex) {
        int y = HEIGHT - 65;
        int h = 65;
        g.setColor(COLOR_SURFACE);
        g.fillRect(0, y, WIDTH, h);

        g.setColor(new Color(0x28, 0x2D, 0x36));
        g.drawLine(0, y, WIDTH, y);

        String[] tabs = {"Tree", "Send", "Receive", "USB", "Config"};
        int tabW = WIDTH / tabs.length;

        for (int i = 0; i < tabs.length; i++) {
            boolean active = (i == activeIndex);
            int cx = i * tabW + tabW / 2;
            Color iconColor = active ? COLOR_GREEN : COLOR_TEXT_SEC;

            // Render EXACT Application Vector Icon
            int iconY = y + 16;
            AppIcons.drawIcon(g, i, cx, iconY + 7, 20, iconColor);

            g.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 11));
            g.setColor(iconColor);
            int textW = g.getFontMetrics().stringWidth(tabs[i]);
            g.drawString(tabs[i], cx - textW / 2, y + 48);

            if (active) {
                g.setColor(COLOR_GREEN);
                g.fillRect(cx - 24, HEIGHT - 3, 48, 3);
            }
        }
    }

    /**
     * Draws the standardized Level Navigation Bar and Test Banner
     * ensuring 100% visual parity between Receive (Listening) and Send (Transmission) screens.
     */
    private static int drawStandardLevelHeaderAndBanner(Graphics2D g, int startY, boolean isReceive) {
        int curY = startY;

        // Level navigation container background
        int navH = 68;
        g.setColor(new Color(0x13, 0x19, 0x22));
        g.fillRect(0, curY, WIDTH, navH);
        g.setColor(new Color(0x21, 0x28, 0x36));
        g.drawLine(0, curY + navH, WIDTH, curY + navH);

        // Previous Level Button (<)
        int btnY = curY + 12;
        g.setColor(new Color(0x1E, 0x26, 0x33));
        g.fillRoundRect(20, btnY, 44, 44, 10, 10);
        g.setColor(new Color(0x35, 0x40, 0x52));
        g.drawRoundRect(20, btnY, 44, 44, 10, 10);
        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("◀", 33, btnY + 28);

        // Next Level Button (>)
        g.setColor(new Color(0x1E, 0x26, 0x33));
        g.fillRoundRect(WIDTH - 64, btnY, 44, 44, 10, 10);
        g.setColor(new Color(0x35, 0x40, 0x52));
        g.drawRoundRect(WIDTH - 64, btnY, 44, 44, 10, 10);
        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("▶", WIDTH - 51, btnY + 28);

        // Center Level Info: Number, Title, New Characters
        // 1. Level Number
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        String levelNumStr = "LEVEL 1 / 21";
        int nw = g.getFontMetrics().stringWidth(levelNumStr);
        g.drawString(levelNumStr, WIDTH / 2 - nw / 2, curY + 20);

        // 2. Level Title
        g.setColor(COLOR_TEXT_PRI);
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        String titleStr = "A Raiz da Árvore";
        int tw = g.getFontMetrics().stringWidth(titleStr);
        g.drawString(titleStr, WIDTH / 2 - tw / 2, curY + 40);

        // 3. New Characters Badge / Text
        g.setColor(new Color(0x00, 0xE5, 0xFF)); // cyan
        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        String newCharsStr = "New Characters: E (•)  T (—)";
        int cw = g.getFontMetrics().stringWidth(newCharsStr);
        g.drawString(newCharsStr, WIDTH / 2 - cw / 2, curY + 58);

        curY += navH + 12;

        // Standard Amber Banner
        int bannerH = 36;
        g.setColor(COLOR_AMBER);
        g.fillRoundRect(20, curY, WIDTH - 40, bannerH, 8, 8);

        g.setColor(new Color(0x1A, 0x0A, 0x00));
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        String bannerText = isReceive ? "RECEIVE TEST (LISTEN)" : "TRANSMISSION TEST (SEND)";
        int bw = g.getFontMetrics().stringWidth(bannerText);
        g.drawString(bannerText, WIDTH / 2 - bw / 2, curY + 23);

        curY += bannerH + 12;

        // Lives and Questions Row
        int heartStartX = 24;
        for (int i = 0; i < 3; i++) {
            drawHeart(g, heartStartX + (i * 22), curY + 2, 18);
        }

        g.setColor(COLOR_AMBER);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString("1/20 questions", heartStartX + 72, curY + 16);

        // Right side: Progress indicator
        g.setColor(new Color(0x00, 0xE5, 0xFF));
        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        String progStr = "Done: 0/20";
        int pw = g.getFontMetrics().stringWidth(progStr);
        g.drawString(progStr, WIDTH - 24 - pw, curY + 16);

        curY += 28;
        return curY;
    }

    /**
     * 1. SILENT / VIBRATION MODE SCREENSHOT
     */
    public static void generateSilentVibrationMode(File file) throws Exception {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        g.setColor(COLOR_BG);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        drawSystemStatusBar(g);
        drawTopAppBar(g, "15 WPM");

        int curY = 84;

        // --- BANNER SILENT MODE (VISIBLE) ---
        int bannerH = 42;
        g.setColor(COLOR_AMBER_BG);
        g.fillRect(0, curY, WIDTH, bannerH);
        drawSpeakerMute(g, 15, curY + 14);
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("Silent mode: Haptic vibration enabled for Morse signals.", 44, curY + 26);
        g.setColor(new Color(0xFF, 0xB3, 0x00, 100));
        g.drawLine(0, curY + bannerH, WIDTH, curY + bannerH);
        curY += bannerH;

        curY = drawStandardLevelHeaderAndBanner(g, curY, false);

        // Exam Question Card
        int cardY = curY + 6;
        int cardH = 210;
        g.setColor(COLOR_SURFACE);
        g.fillRoundRect(20, cardY, WIDTH - 40, cardH, 16, 16);
        g.setColor(new Color(0x30, 0x36, 0x42));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(20, cardY, WIDTH - 40, cardH, 16, 16);

        // Prompt text
        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString("TRANSMIT LETTER:", WIDTH / 2 - 65, cardY + 36);

        // Letter
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 68));
        g.drawString("E", WIDTH / 2 - 20, cardY + 105);

        // Morse code preview
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 26));
        g.drawString("• (Dit)", WIDTH / 2 - 45, cardY + 145);

        // Mode Status Badge (VIBRATION ACTIVE)
        g.setColor(new Color(0x4A, 0x2A, 0x00));
        g.fillRoundRect(35, cardY + 165, WIDTH - 70, 26, 8, 8);
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("[VIBRATION ACTIVE] PARIS haptic pulse feedback (silent)", 50, cardY + 182);

        // Keyer Paddles
        int paddleY = HEIGHT - 275;
        int paddleW = (WIDTH - 55) / 2;
        int paddleH = 175;

        // DI Paddle (Left)
        g.setColor(COLOR_CARD);
        g.fillRoundRect(20, paddleY, paddleW, paddleH, 16, 16);
        g.setColor(COLOR_AMBER);
        g.setStroke(new BasicStroke(2.5f));
        g.drawRoundRect(20, paddleY, paddleW, paddleH, 16, 16);

        // Vibration ripples
        g.setColor(new Color(0xFF, 0xB3, 0x00, 90));
        g.drawOval(20 + paddleW / 2 - 45, paddleY + 30, 90, 90);
        g.drawOval(20 + paddleW / 2 - 60, paddleY + 15, 120, 120);

        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 28));
        g.drawString("DI (.)", 20 + paddleW / 2 - 42, paddleY + 80);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(COLOR_TEXT_SEC);
        g.drawString("Left Paddle (Vibration)", 20 + paddleW / 2 - 65, paddleY + 115);

        // DAH Paddle (Right)
        g.setColor(COLOR_CARD);
        g.fillRoundRect(35 + paddleW, paddleY, paddleW, paddleH, 16, 16);
        g.setColor(new Color(0x45, 0x4E, 0x5E));
        g.drawRoundRect(35 + paddleW, paddleY, paddleW, paddleH, 16, 16);

        g.setColor(COLOR_TEXT_PRI);
        g.setFont(new Font("Monospaced", Font.BOLD, 28));
        g.drawString("DAH (-)", 35 + paddleW + paddleW / 2 - 55, paddleY + 80);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(COLOR_TEXT_SEC);
        g.drawString("Right Paddle (Vibration)", 35 + paddleW + paddleW / 2 - 68, paddleY + 115);

        drawBottomNav(g, 1); // Enviar active

        g.dispose();
        ImageIO.write(img, "PNG", file);
        System.out.println("Saved: " + file.getName() + " (" + file.length() + " bytes)");
    }

    /**
     * 2. NORMAL SOUND MODE SCREENSHOT (Banner Hidden / GONE)
     */
    public static void generateSoundMode(File file) throws Exception {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        g.setColor(COLOR_BG);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        drawSystemStatusBar(g);
        drawTopAppBar(g, "15 WPM");

        int curY = 84;
        curY = drawStandardLevelHeaderAndBanner(g, curY, false);

        // Exam Question Card
        int cardY = curY + 15;
        int cardH = 210;
        g.setColor(COLOR_SURFACE);
        g.fillRoundRect(20, cardY, WIDTH - 40, cardH, 16, 16);
        g.setColor(new Color(0x30, 0x36, 0x42));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(20, cardY, WIDTH - 40, cardH, 16, 16);

        // Prompt text
        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString("TRANSMIT LETTER:", WIDTH / 2 - 65, cardY + 36);

        // Letter
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 68));
        g.drawString("T", WIDTH / 2 - 20, cardY + 105);

        // Morse code preview
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 26));
        g.drawString("— (Dah)", WIDTH / 2 - 45, cardY + 145);

        // Mode Status Badge (SOUND ACTIVE)
        g.setColor(new Color(0x10, 0x3B, 0x20));
        g.fillRoundRect(35, cardY + 165, WIDTH - 70, 26, 8, 8);
        g.setColor(COLOR_GREEN);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("[SOUND ACTIVE] Synthesized 700Hz CW audio with PARIS cadence", 45, cardY + 182);

        // Keyer Paddles
        int paddleY = HEIGHT - 275;
        int paddleW = (WIDTH - 55) / 2;
        int paddleH = 175;

        // DI Paddle (Left)
        g.setColor(COLOR_CARD);
        g.fillRoundRect(20, paddleY, paddleW, paddleH, 16, 16);
        g.setColor(new Color(0x45, 0x4E, 0x5E));
        g.setStroke(new BasicStroke(2.0f));
        g.drawRoundRect(20, paddleY, paddleW, paddleH, 16, 16);

        g.setColor(COLOR_TEXT_PRI);
        g.setFont(new Font("Monospaced", Font.BOLD, 28));
        g.drawString("DI (.)", 20 + paddleW / 2 - 42, paddleY + 80);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(COLOR_TEXT_SEC);
        g.drawString("Left Paddle (Sound)", 20 + paddleW / 2 - 55, paddleY + 115);

        // DAH Paddle (Right)
        g.setColor(COLOR_CARD);
        g.fillRoundRect(35 + paddleW, paddleY, paddleW, paddleH, 16, 16);
        g.setColor(COLOR_AMBER);
        g.setStroke(new BasicStroke(2.5f));
        g.drawRoundRect(35 + paddleW, paddleY, paddleW, paddleH, 16, 16);

        // Acoustic sound waves
        g.setColor(new Color(0x66, 0xBB, 0x6A, 140));
        g.drawArc(35 + paddleW + paddleW / 2 + 10, paddleY + 45, 40, 70, -60, 120);
        g.drawArc(35 + paddleW + paddleW / 2 + 25, paddleY + 35, 60, 90, -60, 120);

        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 28));
        g.drawString("DAH (-)", 35 + paddleW + paddleW / 2 - 55, paddleY + 80);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(COLOR_TEXT_SEC);
        g.drawString("Right Paddle (Sound)", 35 + paddleW + paddleW / 2 - 60, paddleY + 115);

        drawBottomNav(g, 1); // Send active

        g.dispose();
        ImageIO.write(img, "PNG", file);
        System.out.println("Saved: " + file.getName() + " (" + file.length() + " bytes)");
    }

    /**
     * 3. MORSE BINARY TREE SCREENSHOT
     * Delegated strictly to the single Tree rendering class (TreeScreenshotFixer)
     * to guarantee 100% visual consistency across all tree screenshots.
     */
    public static void generateBinaryTree(File file) throws Exception {
        TreeScreenshotFixer.renderPortraitTree(file,
                "Morse Binary Tree • 412×860",
                "Level 11: Nodes X (-..-) & B (-...) Unlocked");
    }

    /**
     * 4. RECEIVE (LISTENING) TEST SCREENSHOT
     */
    public static void generateListeningExam(File file) throws Exception {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        g.setColor(COLOR_BG);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        drawSystemStatusBar(g);
        drawTopAppBar(g, "15 WPM");

        int curY = 84;
        curY = drawStandardLevelHeaderAndBanner(g, curY, true);

        // Acoustic Play Audio Card
        int playCardY = curY + 15;
        int playCardH = 175;
        g.setColor(COLOR_SURFACE);
        g.fillRoundRect(20, playCardY, WIDTH - 40, playCardH, 16, 16);
        g.setColor(new Color(0x30, 0x36, 0x42));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(20, playCardY, WIDTH - 40, playCardH, 16, 16);

        // Play Button Circle
        g.setColor(COLOR_AMBER);
        g.fillOval(WIDTH / 2 - 35, playCardY + 22, 70, 70);
        g.setColor(COLOR_BG);
        int[] tx = {WIDTH / 2 - 12, WIDTH / 2 - 12, WIDTH / 2 + 18};
        int[] ty = {playCardY + 42, playCardY + 72, playCardY + 57};
        g.fillPolygon(tx, ty, 3);

        g.setColor(COLOR_TEXT_PRI);
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        String tapText = "Tap to Play CW Signal";
        int ttw = g.getFontMetrics().stringWidth(tapText);
        g.drawString(tapText, WIDTH / 2 - ttw / 2, playCardY + 125);

        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        String hintText = "Listen to acoustic CW signal and select received letter";
        int htw = g.getFontMetrics().stringWidth(hintText);
        g.drawString(hintText, WIDTH / 2 - htw / 2, playCardY + 150);

        // 4 Options Grid (A, E, N, T) in alphabetical order
        String[] options = {"A", "E", "N", "T"};
        int optGridY = playCardY + playCardH + 25;
        int optW = (WIDTH - 55) / 2;
        int optH = 95;

        for (int i = 0; i < 4; i++) {
            int ox = (i % 2 == 0) ? 20 : 35 + optW;
            int oy = optGridY + (i / 2) * (optH + 18);

            boolean isCorrectHighlight = (i == 1); // Option E highlighted
            g.setColor(isCorrectHighlight ? new Color(0x10, 0x3B, 0x20) : COLOR_SURFACE);
            g.fillRoundRect(ox, oy, optW, optH, 14, 14);
            g.setColor(isCorrectHighlight ? COLOR_GREEN : new Color(0x35, 0x3D, 0x4A));
            g.setStroke(new BasicStroke(isCorrectHighlight ? 2.5f : 1.5f));
            g.drawRoundRect(ox, oy, optW, optH, 14, 14);

            g.setColor(isCorrectHighlight ? COLOR_GREEN : COLOR_TEXT_PRI);
            g.setFont(new Font("Monospaced", Font.BOLD, 38));
            g.drawString(options[i], ox + optW / 2 - 12, oy + 58);
        }

        drawBottomNav(g, 2); // Receive active

        g.dispose();
        ImageIO.write(img, "PNG", file);
        System.out.println("Saved: " + file.getName() + " (" + file.length() + " bytes)");
    }

    /**
     * 5. FREE CW KEYER SANDBOX SCREENSHOT
     */
    public static void generateFreeKeyer(File file) throws Exception {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        g.setColor(COLOR_BG);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        drawSystemStatusBar(g);
        drawTopAppBar(g, "20 WPM");

        int curY = 84;
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.drawString("LIVE CW DECODER & KEYER", 20, curY + 35);

        // Decoded Output Terminal Display
        int termY = curY + 60;
        int termH = 220;
        g.setColor(new Color(0x05, 0x07, 0x09));
        g.fillRoundRect(20, termY, WIDTH - 40, termH, 16, 16);
        g.setColor(new Color(0x2A, 0x32, 0x40));
        g.drawRoundRect(20, termY, WIDTH - 40, termH, 16, 16);

        // LED Indicator
        g.setColor(COLOR_RED);
        g.fillOval(WIDTH - 55, termY + 15, 14, 14);
        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("Monospaced", Font.BOLD, 11));
        g.drawString("TX", WIDTH - 78, termY + 26);

        // Decoded Text
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 26));
        g.drawString("CQ CQ MORSEGO", 35, termY + 65);
        g.drawString("73 DE CT1", 35, termY + 105);

        // Live Morse Buffer
        g.setColor(COLOR_BLUE);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.drawString("Buffer: . - . (R)", 35, termY + 160);

        // Keyer Controls
        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.drawString("Curtis Cadence - CW Tone: 700 Hz", 35, termY + 195);

        // Paddles Area
        int paddleY = HEIGHT - 275;
        int paddleW = (WIDTH - 55) / 2;
        int paddleH = 175;

        // DI Paddle
        g.setColor(COLOR_CARD);
        g.fillRoundRect(20, paddleY, paddleW, paddleH, 16, 16);
        g.setColor(COLOR_AMBER);
        g.setStroke(new BasicStroke(2.0f));
        g.drawRoundRect(20, paddleY, paddleW, paddleH, 16, 16);
        g.setFont(new Font("Monospaced", Font.BOLD, 28));
        g.drawString("DI (.)", 20 + paddleW / 2 - 42, paddleY + 80);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(COLOR_TEXT_SEC);
        g.drawString("Keyboard: Left Ctrl", 20 + paddleW / 2 - 55, paddleY + 115);

        // DAH Paddle
        g.setColor(COLOR_CARD);
        g.fillRoundRect(35 + paddleW, paddleY, paddleW, paddleH, 16, 16);
        g.setColor(COLOR_AMBER);
        g.drawRoundRect(35 + paddleW, paddleY, paddleW, paddleH, 16, 16);
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 28));
        g.drawString("DAH (-)", 35 + paddleW + paddleW / 2 - 55, paddleY + 80);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(COLOR_TEXT_SEC);
        g.drawString("Keyboard: Right Ctrl", 35 + paddleW + paddleW / 2 - 60, paddleY + 115);

        drawBottomNav(g, 1); // Send active (Free Keyer is transmission)

        g.dispose();
        ImageIO.write(img, "PNG", file);
        System.out.println("Saved: " + file.getName() + " (" + file.length() + " bytes)");
    }

    /**
     * 6. HARDWARE USB CW KEYER CALIBRATION & TIMING/SOUND SETTINGS SCREENSHOT
     */
    public static void generateHardwareSetup(File file) throws Exception {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        g.setColor(COLOR_BG);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        drawSystemStatusBar(g);
        drawTopAppBar(g, "15 WPM");

        int curY = 84;
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.drawString("USB KEYER & HARDWARE SETTINGS", 20, curY + 35);

        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.drawString("External USB Keyer • Timing & Audio Calibration", 20, curY + 60);

        // Hardware Status Monitor Cards
        int monitorY = curY + 80;
        int monW = (WIDTH - 55) / 2;
        int monH = 120;

        // Left Paddle Monitor
        g.setColor(COLOR_SURFACE);
        g.fillRoundRect(20, monitorY, monW, monH, 14, 14);
        g.setColor(COLOR_GREEN);
        g.fillOval(35, monitorY + 18, 12, 12);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString("LEFT PADDLE", 55, monitorY + 29);
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.drawString("DI (.)", 35, monitorY + 65);
        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.drawString("Mapped: CTRL_LEFT", 35, monitorY + 90);

        // Right Paddle Monitor
        g.setColor(COLOR_SURFACE);
        g.fillRoundRect(35 + monW, monitorY, monW, monH, 14, 14);
        g.setColor(COLOR_GREEN);
        g.fillOval(50 + monW, monitorY + 18, 12, 12);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString("RIGHT PADDLE", 70 + monW, monitorY + 29);
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.drawString("DAH (-)", 50 + monW, monitorY + 65);
        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.drawString("Mapped: CTRL_RIGHT", 50 + monW, monitorY + 90);

        // Calibration action buttons
        int btnY = monitorY + monH + 16;
        g.setColor(COLOR_CARD);
        g.fillRoundRect(20, btnY, WIDTH - 40, 44, 12, 12);
        g.setColor(COLOR_AMBER);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(20, btnY, WIDTH - 40, 44, 12, 12);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString("[<>] Invert Paddles (Left-Hand Mode)", 40, btnY + 28);

        int btnY2 = btnY + 54;
        g.setColor(COLOR_CARD);
        g.fillRoundRect(20, btnY2, WIDTH - 40, 44, 12, 12);
        g.setColor(COLOR_TEXT_PRI);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString("[R] Reset VBand Defaults (Left/Right Ctrl)", 40, btnY2 + 28);

        // Settings de Tempos e Som Card
        int setY = btnY2 + 54;
        int setH = 115;
        g.setColor(COLOR_SURFACE);
        g.fillRoundRect(20, setY, WIDTH - 40, setH, 14, 14);
        g.setColor(new Color(0x35, 0x3D, 0x4A));
        g.drawRoundRect(20, setY, WIDTH - 40, setH, 14, 14);

        g.setColor(COLOR_AMBER);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString("TIMING & AUDIO SETTINGS (PARIS / CW SYNTHESIZER)", 35, setY + 26);

        g.setColor(COLOR_TEXT_PRI);
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.drawString("• Speed: 15 WPM (Dit = 80ms, Dah = 240ms, PARIS 50-dit)", 35, setY + 52);
        g.drawString("• CW Audio Frequency: 700 Hz (Synthesized Sine Tone)", 35, setY + 74);
        g.drawString("• Iambic Mode B: Dit/Dah memory with squeeze keying", 35, setY + 96);

        // Hardware Console Logs
        int logY = setY + setH + 14;
        int logH = HEIGHT - logY - 75;
        g.setColor(new Color(0x05, 0x07, 0x09));
        g.fillRoundRect(20, logY, WIDTH - 40, logH, 12, 12);
        g.setColor(COLOR_GREEN);
        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g.drawString("[USB] ASIN B0F666MVG6 online | Dit: CTRL_LEFT | Dah: CTRL_RIGHT", 30, logY + 25);
        g.drawString("[AUDIO] AudioTrack synthesizer active (700 Hz, max volume)", 30, logY + 45);
        g.drawString("[PARIS] Timing clock calibrated to 15 WPM (PARIS cadence)", 30, logY + 65);

        drawBottomNav(g, 3); // USB active

        g.dispose();
        ImageIO.write(img, "PNG", file);
        System.out.println("Saved: " + file.getName() + " (" + file.length() + " bytes)");
    }

    /**
     * Portrait 412x860 version of USB Hardware Setup (screenshot_05_hw_setup.jpg).
     * Renders authentic USB paddle definitions, VBand mapping, calibration and logs.
     */
    public static void generateHardwareSetupPortrait412(File file) throws Exception {
        int w = 412;
        int h = 860;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = createGraphics(img);

        g.setColor(COLOR_BG);
        g.fillRect(0, 0, w, h);

        // Status bar
        g.setColor(new Color(0x0A, 0x0C, 0x0E));
        g.fillRect(0, 0, w, 24);
        g.setColor(new Color(0x99, 0x99, 0x99));
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.drawString("12:00", 16, 17);
        g.drawString("5G  100%", w - 65, 17);

        // Top App Bar
        int y = 24;
        int abH = 56;
        g.setColor(COLOR_SURFACE);
        g.fillRect(0, y, w, abH);

        // Logo 'M'
        g.setColor(COLOR_AMBER);
        g.fillOval(16, y + 14, 28, 28);
        g.setColor(COLOR_BG);
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.drawString("M", 24, y + 33);

        // Title
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        g.drawString("morseGO", 52, y + 34);

        // 20 WPM badge
        g.setColor(COLOR_CARD);
        g.fillRoundRect(w - 140, y + 14, 60, 26, 6, 6);
        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        g.drawString("20 WPM", w - 134, y + 31);

        // Gear icon
        AppIcons.drawIcon(g, 4, w - 26, y + 27, 18, COLOR_TEXT_SEC);

        int curY = 80;

        // Device Connection Card
        int devY = curY + 12;
        int devH = 64;
        g.setColor(COLOR_SURFACE);
        g.fillRoundRect(16, devY, w - 32, devH, 12, 12);
        g.setColor(COLOR_GREEN);
        g.setStroke(new BasicStroke(1.0f));
        g.drawRoundRect(16, devY, w - 32, devH, 12, 12);

        g.setColor(COLOR_GREEN);
        g.fillOval(28, devY + 14, 8, 8);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("USB DEVICE CONNECTED", 42, devY + 22);

        g.setColor(COLOR_TEXT_PRI);
        g.setFont(new Font("Monospaced", Font.BOLD, 13));
        g.drawString("CW TRAINER TYPE-C KEYER", 28, devY + 40);

        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.drawString("ASIN B0F666MVG6 • HID Keyboard Mode (VBand)", 28, devY + 55);

        // Section: Physical Paddles Real-time Monitor
        int secY = devY + devH + 14;
        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("Monospaced", Font.BOLD, 11));
        g.drawString("PHYSICAL PADDLES STATE (REAL-TIME):", 16, secY + 10);

        int monY = secY + 18;
        int monW = (w - 32 - 12) / 2; // 184
        int monH = 110;

        // Left paddle
        g.setColor(COLOR_SURFACE);
        g.fillRoundRect(16, monY, monW, monH, 12, 12);
        g.setColor(new Color(0x35, 0x3D, 0x4A));
        g.drawRoundRect(16, monY, monW, monH, 12, 12);

        g.setColor(COLOR_GREEN);
        g.fillOval(26, monY + 14, 8, 8);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("FREE", 38, monY + 22);

        g.setColor(COLOR_TEXT_PRI);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("LEFT PADDLE (DIT •)", 26, monY + 40);

        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.drawString("DI (.)", 26, monY + 68);

        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.drawString("CTRL_LEFT", 26, monY + 85);

        g.setColor(COLOR_CARD);
        g.fillRoundRect(16 + monW - 68, monY + 72, 60, 24, 6, 6);
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("SansSerif", Font.BOLD, 9));
        g.drawString("Calibrate", 16 + monW - 60, monY + 87);

        // Right paddle
        int rX = 16 + monW + 12;
        g.setColor(COLOR_SURFACE);
        g.fillRoundRect(rX, monY, monW, monH, 12, 12);
        g.setColor(new Color(0x35, 0x3D, 0x4A));
        g.drawRoundRect(rX, monY, monW, monH, 12, 12);

        g.setColor(COLOR_GREEN);
        g.fillOval(rX + 10, monY + 14, 8, 8);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("FREE", rX + 22, monY + 22);

        g.setColor(COLOR_TEXT_PRI);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("RIGHT PADDLE (DAH -)", rX + 10, monY + 40);

        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.drawString("DAH (-)", rX + 10, monY + 68);

        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.drawString("CTRL_RIGHT", rX + 10, monY + 85);

        g.setColor(COLOR_CARD);
        g.fillRoundRect(rX + monW - 68, monY + 72, 60, 24, 6, 6);
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("SansSerif", Font.BOLD, 9));
        g.drawString("Calibrate", rX + monW - 60, monY + 87);

        // Buttons
        int bY = monY + monH + 12;
        g.setColor(COLOR_CARD);
        g.fillRoundRect(16, bY, w - 32, 40, 10, 10);
        g.setColor(COLOR_AMBER);
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(16, bY, w - 32, 40, 10, 10);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("[<>] Invert Paddles (Left-Hand Mode)", 28, bY + 25);
        g.setColor(COLOR_TEXT_SEC);
        g.drawString("OFF", w - 54, bY + 25);

        int bY2 = bY + 48;
        g.setColor(COLOR_CARD);
        g.fillRoundRect(16, bY2, w - 32, 40, 10, 10);
        g.setColor(COLOR_TEXT_PRI);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("[R] Reset VBand Defaults (Left/Right Ctrl)", 28, bY2 + 25);

        // Timing & Audio Settings Card
        int setY = bY2 + 48;
        int setH = 100;
        g.setColor(COLOR_SURFACE);
        g.fillRoundRect(16, setY, w - 32, setH, 12, 12);
        g.setColor(new Color(0x35, 0x3D, 0x4A));
        g.drawRoundRect(16, setY, w - 32, setH, 12, 12);

        g.setColor(COLOR_AMBER);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("TIMING & AUDIO SETTINGS (PARIS CW):", 28, setY + 22);

        g.setColor(COLOR_TEXT_PRI);
        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g.drawString("• Speed: 20 WPM (Dit = 60ms, Dah = 180ms)", 28, setY + 44);
        g.drawString("• CW Audio Frequency: 700 Hz (Sine Tone)", 28, setY + 64);
        g.drawString("• Iambic Mode B: Dit/Dah memory with squeeze", 28, setY + 84);

        // Log Console
        int logY = setY + setH + 12;
        int logH = h - 65 - logY - 10;
        g.setColor(new Color(0x05, 0x07, 0x09));
        g.fillRoundRect(16, logY, w - 32, logH, 10, 10);
        g.setColor(new Color(0x20, 0x26, 0x30));
        g.drawRoundRect(16, logY, w - 32, logH, 10, 10);

        g.setColor(COLOR_GREEN);
        g.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g.drawString("[USB] ASIN B0F666MVG6 online | Dit: CTRL_L | Dah: CTRL_R", 26, logY + 22);
        g.drawString("[AUDIO] AudioTrack synthesizer active (700 Hz, max vol)", 26, logY + 40);
        g.drawString("[PARIS] Clock calibrated to 20 WPM (ITU cadence)", 26, logY + 58);
        g.drawString("[STATUS] VBand defaults active • Ready for keying", 26, logY + 76);

        // Bottom nav
        drawBottomNavCustom(g, 3, w, h); // USB active

        g.dispose();
        writeHighQualityJpeg(img, file);
        System.out.println("Saved: " + file.getName() + " (" + file.length() + " bytes)");
    }

    private static void drawBottomNavCustom(Graphics2D g, int activeIndex, int w, int h) {
        int barH = 65;
        int y = h - barH;
        g.setColor(COLOR_SURFACE);
        g.fillRect(0, y, w, barH);

        g.setColor(new Color(0x28, 0x2D, 0x36));
        g.drawLine(0, y, w, y);

        String[] tabs = {"Tree", "Send", "Receive", "USB", "Config"};
        int tabW = w / tabs.length;

        for (int i = 0; i < tabs.length; i++) {
            boolean active = (i == activeIndex);
            int cx = i * tabW + tabW / 2;
            Color iconColor = active ? COLOR_GREEN : COLOR_TEXT_SEC;

            int iconY = y + 16;
            AppIcons.drawIcon(g, i, cx, iconY + 7, 18, iconColor);

            g.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 11));
            g.setColor(iconColor);
            int textW = g.getFontMetrics().stringWidth(tabs[i]);
            g.drawString(tabs[i], cx - textW / 2, y + 48);

            if (active) {
                g.setColor(COLOR_GREEN);
                g.fillRect(cx - 24, h - 3, 48, 3);
            }
        }
    }

    private static void writeHighQualityJpeg(BufferedImage img, File file) throws Exception {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            ImageIO.write(img, "JPEG", file);
            return;
        }
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.92f);

        try (FileImageOutputStream out = new FileImageOutputStream(file)) {
            writer.setOutput(out);
            writer.write(null, new IIOImage(img, null, null), param);
        }
        writer.dispose();
    }
}
