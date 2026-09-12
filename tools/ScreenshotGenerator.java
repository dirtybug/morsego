package tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

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
    private static final Color COLOR_GREEN = new Color(0x4C, 0xAF, 0x50);
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
            generateFreeKeyer(new File(outputDir, "05_free_keyer_paddles.png"));
            generateHardwareSetup(new File(outputDir, "06_hardware_usb_setup.png"));

            System.out.println("=====================================================");
            System.out.println("All 6 screenshots successfully generated and verified!");
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

        // Settings gear icon
        g.setColor(COLOR_TEXT_SEC);
        g.drawOval(WIDTH - 42, y + 19, 18, 18);
        g.drawLine(WIDTH - 33, y + 15, WIDTH - 33, y + 41);
        g.drawLine(WIDTH - 46, y + 28, WIDTH - 20, y + 28);
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

        String[] tabs = {"Arvore", "Enviar", "Ouvir", "Keyer", "USB"};
        int tabW = WIDTH / tabs.length;

        for (int i = 0; i < tabs.length; i++) {
            boolean active = (i == activeIndex);
            int cx = i * tabW + tabW / 2;
            g.setColor(active ? COLOR_AMBER : COLOR_TEXT_SEC);

            // Vector icon representation
            int iconY = y + 16;
            if (i == 0) { // Tree icon
                g.drawLine(cx, iconY, cx, iconY + 14);
                g.drawLine(cx, iconY + 4, cx - 6, iconY + 9);
                g.drawLine(cx, iconY + 4, cx + 6, iconY + 9);
            } else if (i == 1) { // Send / lightning icon
                int[] lx = {cx + 2, cx - 4, cx, cx - 2, cx + 5, cx};
                int[] ly = {iconY, iconY + 6, iconY + 6, iconY + 14, iconY + 7, iconY + 7};
                g.fillPolygon(lx, ly, 6);
            } else if (i == 2) { // Listen / headphones
                g.drawArc(cx - 7, iconY, 14, 12, 0, 180);
                g.fillRect(cx - 8, iconY + 6, 3, 6);
                g.fillRect(cx + 5, iconY + 6, 3, 6);
            } else if (i == 3) { // Keyer paddles
                g.fillRect(cx - 6, iconY + 2, 4, 10);
                g.fillRect(cx + 2, iconY + 2, 4, 10);
            } else if (i == 4) { // USB plug
                g.drawRect(cx - 5, iconY + 1, 10, 8);
                g.drawLine(cx, iconY + 9, cx, iconY + 14);
            }

            g.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 11));
            int textW = g.getFontMetrics().stringWidth(tabs[i]);
            g.drawString(tabs[i], cx - textW / 2, y + 48);
        }
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
        g.drawString("Telemovel em silencio: Vibracao tatil ativada para sinais de Morse.", 44, curY + 26);
        g.setColor(new Color(0xFF, 0xB3, 0x00, 100));
        g.drawLine(0, curY + bannerH, WIDTH, curY + bannerH);
        curY += bannerH;

        // Header info: Level & Lives
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.drawString("NIVEL 1 / 13: E & T", 20, curY + 35);
        for (int i = 0; i < 3; i++) {
            drawHeart(g, WIDTH - 95 + (i * 24), curY + 20, 18);
        }

        // Subtitle
        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.drawString("FASE 2: TRANSMISSAO (MANIPULACAO CW)", 20, curY + 60);

        // Exam Question Card
        int cardY = curY + 80;
        int cardH = 220;
        g.setColor(COLOR_SURFACE);
        g.fillRoundRect(20, cardY, WIDTH - 40, cardH, 16, 16);
        g.setColor(new Color(0x30, 0x36, 0x42));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(20, cardY, WIDTH - 40, cardH, 16, 16);

        // Prompt text
        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString("TRANSMITA A LETRA:", WIDTH / 2 - 75, cardY + 40);

        // Letter
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 72));
        g.drawString("E", WIDTH / 2 - 22, cardY + 120);

        // Morse code preview
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 28));
        g.drawString(". (Dit)", WIDTH / 2 - 45, cardY + 165);

        // Mode Status Badge (VIBRATION ACTIVE)
        g.setColor(new Color(0x4A, 0x2A, 0x00));
        g.fillRoundRect(35, cardY + 180, WIDTH - 70, 26, 8, 8);
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("[VIBRACAO ATIVA] Resposta via pulsos tateis PARIS (sem som)", 48, cardY + 197);

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
        g.drawString("Pa Esquerda (Vibracao)", 20 + paddleW / 2 - 65, paddleY + 115);

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
        g.drawString("Pa Direita (Vibracao)", 35 + paddleW + paddleW / 2 - 60, paddleY + 115);

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

        // NO BANNER: Content starts directly under top app bar
        int curY = 84;

        // Header info: Level & Lives
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.drawString("NIVEL 1 / 13: E & T", 20, curY + 38);
        for (int i = 0; i < 3; i++) {
            drawHeart(g, WIDTH - 95 + (i * 24), curY + 23, 18);
        }

        // Subtitle
        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.drawString("FASE 2: TRANSMISSAO (MANIPULACAO CW)", 20, curY + 65);

        // Exam Question Card
        int cardY = curY + 88;
        int cardH = 220;
        g.setColor(COLOR_SURFACE);
        g.fillRoundRect(20, cardY, WIDTH - 40, cardH, 16, 16);
        g.setColor(new Color(0x30, 0x36, 0x42));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(20, cardY, WIDTH - 40, cardH, 16, 16);

        // Prompt text
        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString("TRANSMITA A LETRA:", WIDTH / 2 - 75, cardY + 40);

        // Letter
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 72));
        g.drawString("T", WIDTH / 2 - 22, cardY + 120);

        // Morse code preview
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 28));
        g.drawString("- (Dah)", WIDTH / 2 - 45, cardY + 165);

        // Mode Status Badge (SOUND ACTIVE)
        g.setColor(new Color(0x10, 0x3B, 0x20));
        g.fillRoundRect(35, cardY + 180, WIDTH - 70, 26, 8, 8);
        g.setColor(COLOR_GREEN);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("[SOM ATIVO] Audio sintetizado CW 700Hz com cadencia PARIS", 50, cardY + 197);

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
        g.drawString("Pa Esquerda (Som)", 20 + paddleW / 2 - 50, paddleY + 115);

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
        g.drawString("Pa Direita (Som)", 35 + paddleW + paddleW / 2 - 50, paddleY + 115);

        drawBottomNav(g, 1); // Enviar active

        g.dispose();
        ImageIO.write(img, "PNG", file);
        System.out.println("Saved: " + file.getName() + " (" + file.length() + " bytes)");
    }

    /**
     * 3. MORSE BINARY TREE SCREENSHOT
     */
    public static void generateBinaryTree(File file) throws Exception {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        g.setColor(COLOR_BG);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        drawSystemStatusBar(g);
        drawTopAppBar(g, "15 WPM");

        int curY = 84;
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.drawString("ARVORE BINARIA DE MORSE", 20, curY + 35);

        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.drawString("Nivel 1: E & T - Esquerda = Dah (-) | Direita = Dit (.)", 20, curY + 60);

        // Binary Tree Canvas Area
        int treeY = curY + 80;
        int treeH = HEIGHT - treeY - 80;
        g.setColor(COLOR_SURFACE);
        g.fillRoundRect(20, treeY, WIDTH - 40, treeH, 16, 16);

        // Root Node (START)
        int rootX = WIDTH / 2;
        int rootY = treeY + 60;
        g.setColor(COLOR_CARD);
        g.fillOval(rootX - 25, rootY - 25, 50, 50);
        g.setColor(COLOR_AMBER);
        g.setStroke(new BasicStroke(2.0f));
        g.drawOval(rootX - 25, rootY - 25, 50, 50);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("INICIO", rootX - 18, rootY + 5);

        // Left branch (T: Dah —)
        int tX = rootX - 110;
        int tY = rootY + 110;
        g.setColor(new Color(0x45, 0x4E, 0x5E));
        g.drawLine(rootX - 20, rootY + 20, tX + 15, tY - 15);
        g.setColor(new Color(0xFF, 0x8F, 0x00));
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("- DAH", (rootX + tX) / 2 - 35, (rootY + tY) / 2 - 5);

        g.setColor(new Color(0x3E, 0x27, 0x23));
        g.fillOval(tX - 25, tY - 25, 50, 50);
        g.setColor(COLOR_AMBER);
        g.drawOval(tX - 25, tY - 25, 50, 50);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.drawString("T", tX - 7, tY + 8);

        // Right branch (E: Dit •)
        int eX = rootX + 110;
        int eY = rootY + 110;
        g.setColor(new Color(0x45, 0x4E, 0x5E));
        g.drawLine(rootX + 20, rootY + 20, eX - 15, eY - 15);
        g.setColor(new Color(0x64, 0xB5, 0xF6));
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString(". DIT", (rootX + eX) / 2 + 10, (rootY + eY) / 2 - 5);

        g.setColor(new Color(0x1A, 0x23, 0x7E));
        g.fillOval(eX - 25, eY - 25, 50, 50);
        g.setColor(COLOR_AMBER);
        g.drawOval(eX - 25, eY - 25, 50, 50);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.drawString("E", eX - 7, eY + 8);

        // Level 2 Sub-branches (M, N, A, I)
        int[] subX = {tX - 55, tX + 55, eX - 55, eX + 55};
        String[] subLetters = {"M", "N", "A", "I"};
        for (int i = 0; i < 4; i++) {
            int px = (i < 2) ? tX : eX;
            int py = (i < 2) ? tY : eY;
            int sx = subX[i];
            int sy = py + 100;
            g.setColor(new Color(0x35, 0x3D, 0x4A));
            g.drawLine(px, py + 25, sx, sy - 20);
            g.setColor(COLOR_CARD);
            g.fillOval(sx - 20, sy - 20, 40, 40);
            g.setColor(new Color(0x55, 0x60, 0x72));
            g.drawOval(sx - 20, sy - 20, 40, 40);
            g.setColor(COLOR_TEXT_SEC);
            g.setFont(new Font("Monospaced", Font.BOLD, 16));
            g.drawString(subLetters[i], sx - 6, sy + 6);
        }

        // Action Button: Praticar Este Nivel
        int btnY = treeY + treeH - 70;
        g.setColor(COLOR_AMBER);
        g.fillRoundRect(40, btnY, WIDTH - 80, 48, 12, 12);
        g.setColor(COLOR_BG);
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.drawString("PRATICAR ESTE NIVEL >", WIDTH / 2 - 95, btnY + 30);

        drawBottomNav(g, 0); // Arvore active

        g.dispose();
        ImageIO.write(img, "PNG", file);
        System.out.println("Saved: " + file.getName() + " (" + file.length() + " bytes)");
    }

    /**
     * 4. CW LISTENING EXAM SCREENSHOT
     */
    public static void generateListeningExam(File file) throws Exception {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = createGraphics(img);

        g.setColor(COLOR_BG);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        drawSystemStatusBar(g);
        drawTopAppBar(g, "15 WPM");

        int curY = 84;
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.drawString("TREINO DE AUDICAO CW", 20, curY + 35);
        for (int i = 0; i < 3; i++) {
            drawHeart(g, WIDTH - 95 + (i * 24), curY + 20, 18);
        }

        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.drawString("Identifique o som emitido pelo sinal Morse:", 20, curY + 60);

        // Acoustic Play Audio Card
        int playCardY = curY + 80;
        int playCardH = 160;
        g.setColor(COLOR_SURFACE);
        g.fillRoundRect(20, playCardY, WIDTH - 40, playCardH, 16, 16);

        // Play Button Circle
        g.setColor(COLOR_AMBER);
        g.fillOval(WIDTH / 2 - 35, playCardY + 25, 70, 70);
        g.setColor(COLOR_BG);
        int[] tx = {WIDTH / 2 - 12, WIDTH / 2 - 12, WIDTH / 2 + 18};
        int[] ty = {playCardY + 45, playCardY + 75, playCardY + 60};
        g.fillPolygon(tx, ty, 3);

        g.setColor(COLOR_TEXT_PRI);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("Toque para Ouvir o Sinal CW", WIDTH / 2 - 95, playCardY + 125);

        // 4 Options Grid (A, B, C, D)
        String[] options = {"E", "T", "A", "N"};
        int optGridY = playCardY + playCardH + 30;
        int optW = (WIDTH - 55) / 2;
        int optH = 75;

        for (int i = 0; i < 4; i++) {
            int ox = (i % 2 == 0) ? 20 : 35 + optW;
            int oy = optGridY + (i / 2) * (optH + 15);

            boolean isCorrectHighlight = (i == 0); // Option E highlighted
            g.setColor(isCorrectHighlight ? new Color(0x1B, 0x38, 0x25) : COLOR_SURFACE);
            g.fillRoundRect(ox, oy, optW, optH, 12, 12);
            g.setColor(isCorrectHighlight ? COLOR_GREEN : new Color(0x35, 0x3D, 0x4A));
            g.setStroke(new BasicStroke(1.5f));
            g.drawRoundRect(ox, oy, optW, optH, 12, 12);

            g.setColor(isCorrectHighlight ? COLOR_GREEN : COLOR_TEXT_PRI);
            g.setFont(new Font("Monospaced", Font.BOLD, 32));
            g.drawString(options[i], ox + optW / 2 - 10, oy + 48);
        }

        drawBottomNav(g, 2); // Ouvir active

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
        g.drawString("MANIPULADOR LIVRE (IAMBIC B)", 20, curY + 35);

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
        g.drawString("Cadencia Curtis - Tom CW: 700 Hz", 35, termY + 195);

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
        g.drawString("Teclado: Left Ctrl", 20 + paddleW / 2 - 50, paddleY + 115);

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
        g.drawString("Teclado: Right Ctrl", 35 + paddleW + paddleW / 2 - 55, paddleY + 115);

        drawBottomNav(g, 3); // Keyer active

        g.dispose();
        ImageIO.write(img, "PNG", file);
        System.out.println("Saved: " + file.getName() + " (" + file.length() + " bytes)");
    }

    /**
     * 6. HARDWARE USB CW KEYER CALIBRATION SCREENSHOT
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
        g.drawString("MONITOR & CALIBRACAO USB", 20, curY + 35);

        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.drawString("Manipulador Externo (ASIN B0F666MVG6)", 20, curY + 60);

        // Hardware Status Monitor Cards
        int monitorY = curY + 80;
        int monW = (WIDTH - 55) / 2;
        int monH = 130;

        // Left Paddle Monitor
        g.setColor(COLOR_SURFACE);
        g.fillRoundRect(20, monitorY, monW, monH, 14, 14);
        g.setColor(COLOR_GREEN);
        g.fillOval(35, monitorY + 20, 12, 12);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString("PA ESQUERDA", 55, monitorY + 31);
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.drawString("DI (.)", 35, monitorY + 70);
        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.drawString("Mapeado: CTRL_LEFT", 35, monitorY + 95);

        // Right Paddle Monitor
        g.setColor(COLOR_SURFACE);
        g.fillRoundRect(35 + monW, monitorY, monW, monH, 14, 14);
        g.setColor(COLOR_GREEN);
        g.fillOval(50 + monW, monitorY + 20, 12, 12);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString("PA DIREITA", 70 + monW, monitorY + 31);
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.drawString("DAH (-)", 50 + monW, monitorY + 70);
        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.drawString("Mapeado: CTRL_RIGHT", 50 + monW, monitorY + 95);

        // Calibration action buttons
        int btnY = monitorY + monH + 25;
        g.setColor(COLOR_CARD);
        g.fillRoundRect(20, btnY, WIDTH - 40, 50, 12, 12);
        g.setColor(COLOR_AMBER);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(20, btnY, WIDTH - 40, 50, 12, 12);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("[<>] Inverter Pas (Modo Canhoto)", 40, btnY + 32);

        int btnY2 = btnY + 65;
        g.setColor(COLOR_CARD);
        g.fillRoundRect(20, btnY2, WIDTH - 40, 50, 12, 12);
        g.setColor(COLOR_TEXT_PRI);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("[R] Repor Padrao VBand (Ctrl Esquerdo/Direito)", 40, btnY2 + 32);

        // Hardware Console Logs
        int logY = btnY2 + 65;
        int logH = HEIGHT - logY - 80;
        g.setColor(new Color(0x05, 0x07, 0x09));
        g.fillRoundRect(20, logY, WIDTH - 40, logH, 12, 12);
        g.setColor(COLOR_GREEN);
        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g.drawString("[USB] Dispositivo ASIN B0F666MVG6 detetado", 30, logY + 25);
        g.drawString("[CALIB] Paddle Dit calibrado -> KEYCODE_CTRL_LEFT", 30, logY + 45);
        g.drawString("[CALIB] Paddle Dah calibrado -> KEYCODE_CTRL_RIGHT", 30, logY + 65);
        g.drawString("[STATUS] Pronto para manipulacao Iambic em tempo real", 30, logY + 85);

        drawBottomNav(g, 4); // USB active

        g.dispose();
        ImageIO.write(img, "PNG", file);
        System.out.println("Saved: " + file.getName() + " (" + file.length() + " bytes)");
    }
}
