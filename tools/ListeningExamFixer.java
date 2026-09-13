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
 * Standardizes all Receive (Listening Exam) and Transmission (Send Exam) screenshots:
 * 1. Letter choices strictly ordered alphabetically:
 *    Row 1: A, E
 *    Row 2: I, T
 * 2. Header next to hearts strictly displays completed questions out of total:
 *    "1/20 questions" (NO "Lives:" / "Vidas:" and NO "(0/3 mistakes)").
 * 3. In transmission screens (Send), level hearts and question progress are prominently displayed.
 * 4. Test 25 and 26 button overlap is completely eliminated.
 * 5. Test 20 is rendered as a Send Transmission test with Send tab active.
 * 6. Uses true application vector icons for bottom navigation bar.
 */
public class ListeningExamFixer {

    private static final Color COLOR_BG = new Color(0x0A, 0x0E, 0x14);
    private static final Color COLOR_CARD_BG = new Color(0x13, 0x19, 0x22);
    private static final Color COLOR_AMBER = new Color(0xFF, 0xB3, 0x00);
    private static final Color COLOR_GREEN = new Color(0x00, 0xE6, 0x76);
    private static final Color COLOR_RED = new Color(0xFF, 0x52, 0x52);
    private static final Color COLOR_DARK_HEART = new Color(0x35, 0x3D, 0x48);
    private static final Color COLOR_SLATE = new Color(0x21, 0x26, 0x2D);
    private static final Color COLOR_BORDER = new Color(0x30, 0x36, 0x42);
    private static final Color COLOR_TEXT_PRI = new Color(0xEE, 0xEE, 0xEE);
    private static final Color COLOR_TEXT_SEC = new Color(0x8C, 0x98, 0xA8);
    private static final Color COLOR_CYAN = new Color(0x00, 0xE5, 0xFF);
    private static final Color COLOR_BLUE = new Color(0x42, 0xA5, 0xF5);

    private static final String[] TABS = {"Tree", "Send", "Receive", "USB", "Config"};

    public static void main(String[] args) {
        String[] dirs = (args != null && args.length > 0 && args[0] != null && !args[0].isEmpty())
                ? new String[]{args[0]}
                : new String[]{"release/v1.0.0/screenshots", "release/development/screenshots"};
        for (String d : dirs) {
            File dir = new File(d);
            if (!dir.exists()) dir.mkdirs();
            processDirectory(dir);
        }
    }

    private static File ensureCleanTemplate(File cleanFile, File dir, String screenshotName) {
        if (cleanFile.exists()) return cleanFile;
        File fromDir = new File(dir, screenshotName);
        if (fromDir.exists()) {
            copyFile(fromDir, cleanFile);
            return cleanFile;
        }
        File fromV1 = new File("release/v1.0.0/screenshots", screenshotName);
        if (fromV1.exists()) {
            copyFile(fromV1, cleanFile);
            return cleanFile;
        }
        File fromDev = new File("release/development/screenshots", screenshotName);
        if (fromDev.exists()) {
            copyFile(fromDev, cleanFile);
            return cleanFile;
        }
        return cleanFile;
    }

    public static void processDirectory(File dir) {
        File clean15 = ensureCleanTemplate(new File("tools/clean_15.jpg"), dir, "behavior_test_15.jpg");
        File gitOrig15 = new File("tools/git_orig_15.jpg");
        if (gitOrig15.exists()) copyFile(gitOrig15, clean15);

        File clean24 = ensureCleanTemplate(new File("tools/clean_24.jpg"), dir, "behavior_test_24.jpg");
        File cleanRot4 = ensureCleanTemplate(new File("tools/clean_rot4.jpg"), dir, "phone_rotation_04_rotated_90_deg_exam.jpg");
        File cleanRot2 = ensureCleanTemplate(new File("tools/clean_rot2.jpg"), dir, "phone_rotation_02_rotated_90_deg_tree.jpg");
        File clean18 = ensureCleanTemplate(new File("tools/clean_18.jpg"), dir, "behavior_test_18.jpg");
        File cleanTxPass = ensureCleanTemplate(new File("tools/clean_tx_pass.jpg"), dir, "screenshot_transmission_pass.jpg");
        File cleanTxFail = ensureCleanTemplate(new File("tools/clean_tx_fail.jpg"), dir, "screenshot_transmission_fail.jpg");
        File cleanWordPass = ensureCleanTemplate(new File("tools/clean_word_pass.jpg"), dir, "screenshot_transmission_word_pass.jpg");
        File cleanWordFail = ensureCleanTemplate(new File("tools/clean_word_fail.jpg"), dir, "screenshot_transmission_word_fail.jpg");

        // 1. Neutral Listening Exam Screens: A, E, I, T, 3 lives, 1/20 questions
        String[] neutralScreens = {
            "behavior_test_15.jpg",
            "behavior_test_16.jpg",
            "behavior_test_17.jpg",
            "screenshot_02_exam_listening.jpg",
            "screenshot_listening_no_answer.jpg"
        };
        for (String name : neutralScreens) {
            File f = new File(dir, name);
            fixPortraitListeningExam(f, ExamState.NEUTRAL);
        }

        // 2. Right Answer Screens: E selected in Green, Morse revealed, A, E, I, T (NO button overlap!)
        String[] rightScreens = {
            "behavior_test_25.jpg",
            "behavior_test_27.jpg",
            "screenshot_listening_right_answer.jpg"
        };
        for (String name : rightScreens) {
            File f = new File(dir, name);
            fixPortraitListeningExam(f, ExamState.RIGHT_ANSWER);
        }

        // 3. Wrong Answer Screens: T selected in Red, E in Green, Morse revealed, A, E, I, T, 2 lives
        // NOTE: behavior_test_20.jpg is a SEND test, NOT a wrong answer listening screen!
        String[] wrongScreens = {
            "behavior_test_26.jpg",
            "behavior_test_28.jpg",
            "screenshot_listening_wrong_answer.jpg",
            "screenshot_receive_failed_answer.jpg"
        };
        for (String name : wrongScreens) {
            File f = new File(dir, name);
            fixPortraitListeningExam(f, ExamState.WRONG_ANSWER);
        }

        // 4a. Single-Letter Transmission Exam (SEND): behavior_test_14.jpg (Letter E, Dit/Dah buttons, Send tab active)
        File bt14 = new File(dir, "behavior_test_14.jpg");
        fixPortraitTransmissionExam(bt14, 3, "E", "•", "•", "1.0x Dit / 3.0x Dah (PARIS Standard OK)", true);

        // 4b. Word Transmission Exam Screens (SEND): Hearts, progress counter, PARIS cadence, paddles, Send tab active
        String[] transmissionScreens = {
            "behavior_test_19.jpg",
            "behavior_test_20.jpg",
            "behavior_test_21.jpg",
            "behavior_test_23.jpg",
            "screenshot_03_exam_transmission.jpg"
        };
        for (String name : transmissionScreens) {
            File f = new File(dir, name);
            fixPortraitTransmissionExam(f, 3, "PARIS", "• — — •   [ ? ]   [ ? ]   [ ? ]   [ ? ]", "• — — •", "1.0x Dit / 3.0x Dah (PARIS Standard OK)", false);
        }

        // 5. Transmission Pass & Fail Screens: Clean hearts (NO "Lives:" or mistakes text)
        File txPassFile = new File(dir, "screenshot_transmission_pass.jpg");
        if (cleanTxPass.exists()) copyFile(cleanTxPass, txPassFile);
        if (txPassFile.exists()) fixTransmissionResult(txPassFile, true, false);

        File txFailFile = new File(dir, "screenshot_transmission_fail.jpg");
        if (cleanTxFail.exists()) copyFile(cleanTxFail, txFailFile);
        if (txFailFile.exists()) fixTransmissionResult(txFailFile, false, false);

        File sendFailAnswer = new File(dir, "screenshot_send_failed_answer.jpg");
        if (cleanTxFail.exists()) copyFile(cleanTxFail, sendFailAnswer);
        if (sendFailAnswer.exists()) fixTransmissionResult(sendFailAnswer, false, false);

        File wordPassFile = new File(dir, "screenshot_transmission_word_pass.jpg");
        renderWordTransmissionExam(wordPassFile, true);

        File wordFailFile = new File(dir, "screenshot_transmission_word_fail.jpg");
        renderWordTransmissionExam(wordFailFile, false);

        // 6. Fail & Recover Step Card Screen: behavior_test_24.jpg
        String[] failRecoverScreens = {
            "behavior_test_24.jpg",
            "screenshot_exam_fail_and_recover.jpg"
        };
        for (String name : failRecoverScreens) {
            File f = new File(dir, name);
            if (clean24.exists()) copyFile(clean24, f);
            if (f.exists()) fixFailAndRecoverScreen(f);
        }

        // 7. Landscape Exam Screens: phone_rotation_04_rotated_90_deg_exam.jpg, screenshot_03_exam_landscape.jpg, behavior_test_18.jpg
        File rot4 = new File(dir, "phone_rotation_04_rotated_90_deg_exam.jpg");
        if (cleanRot4.exists()) copyFile(cleanRot4, rot4);
        if (rot4.exists()) fixLandscapeListeningExam(rot4, 2);

        File sc03 = new File(dir, "screenshot_03_exam_landscape.jpg");
        if (sc03.exists()) fixLandscapeListeningExam(sc03, 2);

        File bt18 = new File(dir, "behavior_test_18.jpg");
        if (cleanRot2.exists()) copyFile(cleanRot2, bt18);
        else if (cleanRot4.exists()) copyFile(cleanRot4, bt18);
        if (bt18.exists()) fixLandscapeTransmissionExam(bt18);

        // 8. General Call Station (CQ) and Radio Words: SEPARATE SEND AND RECEIVE TESTS!
        // 8a. Receive Tests (Stage 1 Listen: Radio Words with 4 choices, Play button, Receive tab active)
        File bt43 = new File(dir, "behavior_test_43.jpg");
        renderRadioWordsListenPortrait(bt43, "CQ", "General call to all stations", "Chamada geral para todas as esta\u00E7\u00F5es", new String[]{"CQ", "73", "DX", "QSL"}, 0);

        File sc04 = new File(dir, "screenshot_04_radio_words_cq.jpg");
        renderRadioWordsListenPortrait(sc04, "CQ", "General call to all stations", "Chamada geral para todas as esta\u00E7\u00F5es", new String[]{"CQ", "73", "DX", "QSL"}, 0);

        File bt44 = new File(dir, "behavior_test_44.jpg");
        renderRadioWordsListenPortrait(bt44, "73", "Best regards and greetings in CW", "Cumprimentos e melhores votos em CW", new String[]{"CQ", "73", "DX", "QSL"}, 1);

        // 8b. Send Tests (Stage 2 Transmit: strictly SEND action pills, NO listen option, Send tab active)
        File sc05 = new File(dir, "screenshot_05_radio_words_landscape.jpg");
        renderRadioWordsSendLandscape(sc05, "CQ", "CQ  (General Call to Any Station)", "\u2014 \u2022 \u2014 \u2022     \u2014 \u2014 \u2022 \u2014", "CQ CQ DX DE CT1BOY PSE K", "Send CQ");

        File bt45 = new File(dir, "behavior_test_45.jpg");
        renderRadioWordsSendLandscape(bt45, "CQ", "CQ  (General Call to Any Station)", "\u2014 \u2022 \u2014 \u2022     \u2014 \u2014 \u2022 \u2014", "CQ CQ DX DE CT1BOY PSE K", "Send CQ");

        File bt46 = new File(dir, "behavior_test_46.jpg");
        renderRadioWordsSendLandscape(bt46, "DX", "DX  (Long Distance Contact)", "\u2014 \u2022 \u2022     \u2014 \u2022 \u2022 \u2014", "CQ DX DE CT1BOY PSE K", "Send DX");

        File bt47 = new File(dir, "behavior_test_47.jpg");
        renderRadioWordsSendLandscape(bt47, "QSO", "FULL QSO:  CQ CQ DX DE CT1 73", "\u2014 \u2022 \u2014 \u2022   \u2014 \u2022 \u2014 \u2022   \u2014 \u2022 \u2022   \u2014 \u2022 \u2022 \u2014", "CQ CQ DX DE CT1BOY 73", "Send CQ");

        File bt49 = new File(dir, "behavior_test_49.jpg");
        renderRadioWordsSendLandscape(bt49, "CADENCE", "PARIS TIMING CADENCE (1.0x / 3.0x)", "\u2014 \u2022 \u2014 \u2022     \u2014 \u2014 \u2022 \u2014", "Strict 60ms Dit | 180ms Dah / Space", "Send CQ");
    }

    private static void copyFile(File src, File dst) {
        try {
            Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ignored) {}
    }

    private enum ExamState {
        NEUTRAL,
        RIGHT_ANSWER,
        WRONG_ANSWER
    }

    /**
     * Fixes portrait listening exam (412 x 860):
     * Uses clean base, redraws header card without 'Lives:',
     * clears button area cleanly, and renders options strictly in alphabetical order:
     *   Row 1: A, E
     *   Row 2: I, T
     */
    private static void fixPortraitListeningExam(File file, ExamState state) {
        try {
            File clean15 = new File("tools/clean_15.jpg");
            BufferedImage img = ImageIO.read(clean15.exists() ? clean15 : file);
            if (img == null) return;
            Graphics2D g = createGraphics(img);

            int w = img.getWidth();
            int h = img.getHeight();

            // 0. Redraw clean amber banner (matches Transmission Test banner)
            int cardX = 16;
            int cardW = w - 32;
            int bannerY = 74;
            int bannerH = 36;
            g.setColor(COLOR_AMBER);
            g.fillRoundRect(cardX, bannerY, cardW, bannerH, 8, 8);
            g.setColor(new Color(0x1A, 0x0A, 0x00));
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            String bannerText = "RECEIVE TEST (LISTEN)";
            int bw = g.getFontMetrics().stringWidth(bannerText);
            g.drawString(bannerText, w / 2 - bw / 2, bannerY + 23);

            // 1. Redraw Header Card (exact bounds y: 126 to 174)
            int cardY = 126;
            int cardH = 48;

            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(cardX, cardY, cardW, cardH, 12, 12);
            g.setColor(COLOR_BORDER);
            g.setStroke(new BasicStroke(1.0f));
            g.drawRoundRect(cardX, cardY, cardW, cardH, 12, 12);

            // Draw Hearts starting at cardX + 16 (NO "Lives:" / "Vidas:"!)
            int heartX = cardX + 16;
            int heartY = cardY + 16;
            int numLives = (state == ExamState.WRONG_ANSWER) ? 2 : 3;

            for (int i = 0; i < 3; i++) {
                Color c = (i < numLives) ? COLOR_RED : COLOR_DARK_HEART;
                drawHeart(g, heartX + (i * 20), heartY, 15, c);
            }

            // Completed questions out of total next to hearts: "1/20 questions" (NO mistakes text!)
            g.setColor(COLOR_AMBER);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString("1/20 questions", heartX + 66, cardY + 30);

            // Right side: Exam Progress
            g.setColor(COLOR_CYAN);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            String prog = "Exam Progress: 1/20";
            int pw = g.getFontMetrics().stringWidth(prog);
            g.drawString(prog, cardX + cardW - pw - 14, cardY + 30);

            int btnW = 182;
            int btnH = 66;
            int col1X = 16;
            int col2X = 214;

            if (state == ExamState.NEUTRAL) {
                // In clean_15, "🎧 Listen carefully..." is at y = 380..415.
                // Clear the button area below it: y = 420 to 780.
                g.setColor(COLOR_BG);
                g.fillRect(12, 420, w - 24, 360);

                int row1Y = 424;
                int row2Y = 504;

                // Neutral Slate Buttons: strictly alphabetical: A, E, I, T
                drawOptionButton(g, col1X, row1Y, btnW, btnH, "A", null, false, false);
                drawOptionButton(g, col2X, row1Y, btnW, btnH, "E", null, false, false);
                drawOptionButton(g, col1X, row2Y, btnW, btnH, "I", null, false, false);
                drawOptionButton(g, col2X, row2Y, btnW, btnH, "T", null, false, false);

                drawFooterState(g, w, h, "State: No Answer Selected (Neutral Slate, Plain Letters)");
            } else {
                // For feedback (right / wrong), clear everything below Target Audio card: y = 370 to 780
                g.setColor(COLOR_BG);
                g.fillRect(12, 370, w - 24, 410);

                int banY = 376;
                int banH = 60;

                int row1Y = 448;
                int row2Y = 524;

                if (state == ExamState.RIGHT_ANSWER) {
                    // Green Feedback Banner
                    g.setColor(new Color(0x0A, 0x24, 0x18));
                    g.fillRoundRect(cardX, banY, cardW, banH, 10, 10);
                    g.setColor(COLOR_GREEN);
                    g.setStroke(new BasicStroke(1.5f));
                    g.drawRoundRect(cardX, banY, cardW, banH, 10, 10);

                    g.setColor(COLOR_GREEN);
                    g.setFont(new Font("SansSerif", Font.BOLD, 13));
                    String l1 = "✓ CORRECT! Option 'E' is [ • ] (Dit)";
                    int l1w = g.getFontMetrics().stringWidth(l1);
                    g.drawString(l1, w / 2 - l1w / 2, banY + 24);

                    g.setColor(COLOR_TEXT_PRI);
                    g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                    String l2 = "Advancing to next question in queue...";
                    int l2w = g.getFontMetrics().stringWidth(l2);
                    g.drawString(l2, w / 2 - l2w / 2, banY + 45);

                    // Right Answer: Option E is Green, Morse Revealed: A, E, I, T
                    drawOptionButton(g, col1X, row1Y, btnW, btnH, "A", "• —", false, false);
                    drawOptionButton(g, col2X, row1Y, btnW, btnH, "E", "•", true, false); // Green
                    drawOptionButton(g, col1X, row2Y, btnW, btnH, "I", "• •", false, false);
                    drawOptionButton(g, col2X, row2Y, btnW, btnH, "T", "—", false, false);

                    drawFooterState(g, w, h, "State: Right Answer (Selected Green, Morse Revealed)");
                } else {
                    // Red Feedback Banner
                    g.setColor(new Color(0x2A, 0x10, 0x14));
                    g.fillRoundRect(cardX, banY, cardW, banH, 10, 10);
                    g.setColor(COLOR_RED);
                    g.setStroke(new BasicStroke(1.5f));
                    g.drawRoundRect(cardX, banY, cardW, banH, 10, 10);

                    g.setColor(COLOR_RED);
                    g.setFont(new Font("SansSerif", Font.BOLD, 13));
                    String l1 = "✕ WRONG ANSWER! Selected 'T' (—) instead of 'E' (•)";
                    int l1w = g.getFontMetrics().stringWidth(l1);
                    g.drawString(l1, w / 2 - l1w / 2, banY + 24);

                    g.setColor(COLOR_AMBER);
                    g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                    String l2 = "⚠️ Penalty: 1 Life lost! Letter 'E' added to exam queue.";
                    int l2w = g.getFontMetrics().stringWidth(l2);
                    g.drawString(l2, w / 2 - l2w / 2, banY + 45);

                    // Wrong Answer: Option T is Red, E is Green, Morse Revealed: A, E, I, T
                    drawOptionButton(g, col1X, row1Y, btnW, btnH, "A", "• —", false, false);
                    drawOptionButton(g, col2X, row1Y, btnW, btnH, "E", "•", true, false);  // Green (correct)
                    drawOptionButton(g, col1X, row2Y, btnW, btnH, "I", "• •", false, false);
                    drawOptionButton(g, col2X, row2Y, btnW, btnH, "T", "—", false, true);  // Red (selected)

                    drawFooterState(g, w, h, "State: Wrong Answer (Selected Red, Correct Green, Morse Revealed)");
                }
            }

            // 3. Draw Standardized Bottom Navigation (Receive = tab 2 active)
            drawBottomNav(g, w, h, 46, 2);

            g.dispose();
            saveJpeg(img, file, 0.92f);
            System.out.println("Fixed Listening Exam Screenshot: " + file.getName());
        } catch (Exception e) {
            System.err.println("Error fixing " + file.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Renders a complete Transmission Exam (SEND) screen:
     * - Top App Bar: MorseGO, 20 WPM, Settings
     * - Amber Banner: STAGE 2 OF 2: TRANSMISSION TEST (SENDING)
     * - Level Hearts Card: (hearts)   1/20 questions (NO "Lives:")
     * - Target Word Card: PARIS, • — — •, prompt instructions
     * - Buffer & Cadence Card: Input Buffer, Cadence OK
     * - Virtual Paddles: DIT (green border), DAH (amber border)
     * - Bottom Navigation: Send tab (index 1) active with vector hand icon
     */
    private static void fixPortraitTransmissionExam(File file, int numLives, String targetWord, String promptMorse, String bufferText, String cadenceText, boolean isSingleLetter) {
        try {
            int w = 412;
            int h = 860;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = createGraphics(img);

            // Fill background
            g.setColor(COLOR_BG);
            g.fillRect(0, 0, w, h);

            // 1. Top App Bar (Copy genuine Android top bar from clean_15, strictly 56px high)
            File baseFile = new File("tools/clean_15.jpg");
            if (baseFile.exists()) {
                BufferedImage baseImg = ImageIO.read(baseFile);
                if (baseImg != null) {
                    g.drawImage(baseImg.getSubimage(0, 0, w, 56), 0, 0, null);
                } else {
                    drawTopBar(g, w);
                }
            } else {
                drawTopBar(g, w);
            }

            // Ensure background between top bar (y=56) and banner is clean dark background (NO BLUE STRIP!)
            g.setColor(COLOR_BG);
            g.fillRect(0, 56, w, 28);

            // 2. Amber Stage Banner (Send / Transmit frame strictly separated from Receive)
            int bannerY = 74;
            int bannerH = 36;
            g.setColor(COLOR_AMBER);
            g.fillRoundRect(16, bannerY, w - 32, bannerH, 8, 8);
            g.setColor(new Color(0x1A, 0x0A, 0x00));
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            String bannerText = "TRANSMISSION TEST (SEND)";
            int bw = g.getFontMetrics().stringWidth(bannerText);
            g.drawString(bannerText, w / 2 - bw / 2, bannerY + 23);

            // 3. Header Card (Hearts & Question Progress)
            int cardX = 16;
            int cardY = 126;
            int cardW = w - 32;
            int cardH = 48;

            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(cardX, cardY, cardW, cardH, 12, 12);
            g.setColor(COLOR_BORDER);
            g.setStroke(new BasicStroke(1.0f));
            g.drawRoundRect(cardX, cardY, cardW, cardH, 12, 12);

            // Draw Hearts (NO "Lives:" or "Vidas:"!)
            int heartX = cardX + 16;
            int heartY = cardY + 16;
            for (int i = 0; i < 3; i++) {
                Color c = (i < numLives) ? COLOR_RED : COLOR_DARK_HEART;
                drawHeart(g, heartX + (i * 20), heartY, 15, c);
            }

            // Questions progress next to hearts
            g.setColor(COLOR_AMBER);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString("1/20 questions", heartX + 66, cardY + 30);

            // Exam Progress on right side
            g.setColor(COLOR_CYAN);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            String prog = "Exam Progress: 1/20";
            int pw = g.getFontMetrics().stringWidth(prog);
            g.drawString(prog, cardX + cardW - pw - 14, cardY + 30);

            // 4. Target Card (Letter or Word)
            int wordCardY = 188;
            int wordCardH = isSingleLetter ? 146 : 136;
            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(cardX, wordCardY, cardW, wordCardH, 12, 12);
            g.setColor(COLOR_BORDER);
            g.drawRoundRect(cardX, wordCardY, cardW, wordCardH, 12, 12);

            if (isSingleLetter) {
                // Header: TRANSMIT LETTER:
                g.setColor(COLOR_TEXT_SEC);
                g.setFont(new Font("SansSerif", Font.BOLD, 12));
                String promptHeader = "TRANSMIT LETTER:";
                int phw = g.getFontMetrics().stringWidth(promptHeader);
                g.drawString(promptHeader, w / 2 - phw / 2, wordCardY + 26);

                // Target letter: E
                g.setColor(COLOR_AMBER);
                g.setFont(new Font("SansSerif", Font.BOLD, 52));
                int tww = g.getFontMetrics().stringWidth(targetWord);
                g.drawString(targetWord, w / 2 - tww / 2, wordCardY + 76);

                // Morse label: [ • ]   (Dit)
                g.setColor(COLOR_CYAN);
                g.setFont(new Font("Monospaced", Font.BOLD, 18));
                String morseLabel = "[ " + promptMorse + " ]   (Dit)";
                int mlw = g.getFontMetrics().stringWidth(morseLabel);
                g.drawString(morseLabel, w / 2 - mlw / 2, wordCardY + 106);

                // Subtitle
                g.setColor(COLOR_TEXT_SEC);
                g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                String sub = "Key 'E' using on-screen paddles or USB keyer below";
                int sw = g.getFontMetrics().stringWidth(sub);
                g.drawString(sub, w / 2 - sw / 2, wordCardY + 130);
            } else {
                g.setColor(COLOR_TEXT_SEC);
                g.setFont(new Font("SansSerif", Font.BOLD, 11));
                String promptHeader = "TRANSMIT WORD LETTER-BY-LETTER:";
                int phw = g.getFontMetrics().stringWidth(promptHeader);
                g.drawString(promptHeader, w / 2 - phw / 2, wordCardY + 26);

                g.setColor(COLOR_GREEN);
                g.setFont(new Font("Monospaced", Font.BOLD, 26));
                int tww = g.getFontMetrics().stringWidth(targetWord);
                g.drawString(targetWord, w / 2 - tww / 2, wordCardY + 62);

                // Morse boxes
                int boxY = wordCardY + 76;
                int boxW = cardW - 32;
                int boxH = 32;
                g.setColor(new Color(0x0A, 0x0E, 0x14));
                g.fillRoundRect(cardX + 16, boxY, boxW, boxH, 8, 8);
                g.setColor(COLOR_BORDER);
                g.drawRoundRect(cardX + 16, boxY, boxW, boxH, 8, 8);

                g.setColor(COLOR_AMBER);
                g.setFont(new Font("Monospaced", Font.BOLD, 14));
                int mw = g.getFontMetrics().stringWidth(promptMorse);
                g.drawString(promptMorse, w / 2 - mw / 2, boxY + 21);

                g.setColor(COLOR_TEXT_SEC);
                g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                String sub = "Letter 1 completed: 'P'. Next letter hidden until keyed!";
                int sw = g.getFontMetrics().stringWidth(sub);
                g.drawString(sub, w / 2 - sw / 2, wordCardY + 124);
            }

            // 5. Buffer & Cadence Card
            int bufCardY = isSingleLetter ? 346 : 338;
            int bufCardH = 68;
            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(cardX, bufCardY, cardW, bufCardH, 12, 12);
            g.setColor(COLOR_BORDER);
            g.drawRoundRect(cardX, bufCardY, cardW, bufCardH, 12, 12);

            g.setColor(COLOR_TEXT_PRI);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.drawString("Input Buffer: " + bufferText, cardX + 14, bufCardY + 26);

            g.setColor(COLOR_GREEN);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString("Pause Cadence: " + cadenceText, cardX + 14, bufCardY + 52);

            // 6. Touch Paddles
            int padY = 675;
            int padH = 110;
            int padW = 182;
            int ditX = 16;
            int dahX = 214;

            // Dit Paddle (Green outline)
            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(ditX, padY, padW, padH, 14, 14);
            g.setColor(COLOR_GREEN);
            g.setStroke(new BasicStroke(2.0f));
            g.drawRoundRect(ditX, padY, padW, padH, 14, 14);

            // Dit Dot Icon
            g.setColor(COLOR_GREEN);
            g.fillOval(ditX + padW / 2 - 6, padY + 36, 12, 12);

            // Dit Text
            g.setFont(new Font("SansSerif", Font.BOLD, 15));
            String ditLabel = "DIT (•)";
            int dlw = g.getFontMetrics().stringWidth(ditLabel);
            g.drawString(ditLabel, ditX + padW / 2 - dlw / 2, padY + 78);

            // Dah Paddle (Amber outline)
            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(dahX, padY, padW, padH, 14, 14);
            g.setColor(COLOR_AMBER);
            g.setStroke(new BasicStroke(2.0f));
            g.drawRoundRect(dahX, padY, padW, padH, 14, 14);

            // Dah Dash Icon
            g.setColor(COLOR_AMBER);
            g.fillRoundRect(dahX + padW / 2 - 16, padY + 40, 32, 6, 3, 3);

            // Dah Text
            g.setFont(new Font("SansSerif", Font.BOLD, 15));
            String dahLabel = "DAH (—)";
            int ahlw = g.getFontMetrics().stringWidth(dahLabel);
            g.drawString(dahLabel, dahX + padW / 2 - ahlw / 2, padY + 78);

            // 7. Bottom Navigation Bar with Send tab (index 1) active!
            drawBottomNav(g, w, h, 46, 1);

            g.dispose();
            saveJpeg(img, file, 0.92f);
            System.out.println("Generated Transmission Exam Screenshot: " + file.getName());
        } catch (Exception e) {
            System.err.println("Error generating " + file.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Fixes transmission result screens (pass and fail).
     * Clears "Lives:" and "(0/3 mistakes)", redraws hearts and questions progress.
     */
    private static void fixTransmissionResult(File file, boolean pass, boolean isWord) {
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) return;
            Graphics2D g = createGraphics(img);

            int w = img.getWidth();
            int h = img.getHeight();

            int cardX = 16;
            int cardY = isWord ? 138 : 126;
            int cardW = w - 32;
            int cardH = isWord ? 46 : 48;

            // Clear any cyan/blue artifacts between top bar and cardY to eliminate old banner superposition
            g.setColor(COLOR_BG);
            g.fillRect(0, 56, w, cardY - 56);

            // Redraw clean amber stage banner directly on dark background (NO BLUE STRIP BEHIND IT!)
            int bannerY = isWord ? 84 : 74;
            int bannerH = 36;
            g.setColor(COLOR_AMBER);
            g.fillRoundRect(cardX, bannerY, cardW, bannerH, 8, 8);
            g.setColor(new Color(0x1A, 0x0A, 0x00));
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            String bannerText = pass ? "TRANSMISSION TEST (PASS STATE)" : "TRANSMISSION TEST (FAIL STATE)";
            int bw = g.getFontMetrics().stringWidth(bannerText);
            g.drawString(bannerText, w / 2 - bw / 2, bannerY + 23);

            // Clear entire strip across screen width to eliminate any old card text bleeding
            g.setColor(COLOR_BG);
            g.fillRect(0, cardY - 2, w, cardH + 4);

            // Redraw clean header card
            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(cardX, cardY, cardW, cardH, 12, 12);
            g.setColor(COLOR_BORDER);
            g.setStroke(new BasicStroke(1.0f));
            g.drawRoundRect(cardX, cardY, cardW, cardH, 12, 12);

            // Draw Hearts (NO "Lives:" / "Vidas:"!)
            int heartX = cardX + 16;
            int heartY = cardY + 16;
            int numLives = pass ? 3 : 2;

            for (int i = 0; i < 3; i++) {
                Color c = (i < numLives) ? COLOR_RED : COLOR_DARK_HEART;
                drawHeart(g, heartX + (i * 20), heartY, 15, c);
            }

            // Question progress
            g.setColor(COLOR_AMBER);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            String qText = isWord ? (pass ? "15/20 questions" : "16/20 questions") : "2/20 questions";
            g.drawString(qText, heartX + 66, cardY + 30);

            // Right side: Exam Progress
            g.setColor(COLOR_CYAN);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            String prog = isWord ? (pass ? "Exam Progress: 15/20" : "Exam Progress: 16/20") : "Exam Progress: 2/20";
            int pw = g.getFontMetrics().stringWidth(prog);
            g.drawString(prog, cardX + cardW - pw - 14, cardY + 30);

            // Standardize bottom nav bar with Send active (index 1)
            drawBottomNav(g, w, h, 46, 1);

            g.dispose();
            saveJpeg(img, file, 0.92f);
            System.out.println("Fixed Transmission Result Screenshot: " + file.getName());
        } catch (Exception e) {
            System.err.println("Error fixing " + file.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Renders a complete Word Transmission Exam (SEND) screen with 100% symmetric margins (16px):
     * - Top App Bar (MorseGO, 20 WPM, Settings)
     * - Amber Stage Banner: STAGE 2 OF 2: TRANSMISSION EXAM (WORD TRANSMISSION)
     * - Header Card: Hearts & Question Progress (15/20 or 16/20 questions)
     * - Target Word Card: TEA (Pass) or CQ (Fail) with Morse dots & dashes
     * - Feedback Card: Green Correct or Red Error with ear training & penalty text
     * - Buffer Card: Input Buffer + Cadence info
     * - Virtual Paddles: Symmetrically centered DIT (x:16, w:182) and DAH (x:214, w:182)
     * - Bottom Navigation: Send tab (index 1) active in GREEN (#00E676)
     */
    private static void renderWordTransmissionExam(File file, boolean pass) {
        try {
            int w = 412;
            int h = 860;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = createGraphics(img);

            // Fill background
            g.setColor(COLOR_BG);
            g.fillRect(0, 0, w, h);

            // 1. Top App Bar (Copy genuine Android top bar from clean_15, strictly 56px high)
            File baseFile = new File("tools/clean_15.jpg");
            if (baseFile.exists()) {
                BufferedImage baseImg = ImageIO.read(baseFile);
                if (baseImg != null) {
                    g.drawImage(baseImg.getSubimage(0, 0, w, 56), 0, 0, null);
                } else {
                    drawTopBar(g, w);
                }
            } else {
                drawTopBar(g, w);
            }

            // Ensure background between top bar (y=56) and banner is clean dark background (NO BLUE STRIP!)
            g.setColor(COLOR_BG);
            g.fillRect(0, 56, w, 28);

            // 2. Amber Stage Banner (Send / Transmit frame strictly separated from Receive)
            int cardX = 16;
            int cardW = w - 32; // 380

            int bannerY = 74;
            int bannerH = 36;
            g.setColor(COLOR_AMBER);
            g.fillRoundRect(cardX, bannerY, cardW, bannerH, 8, 8);
            g.setColor(new Color(0x1A, 0x0A, 0x00));
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            String bannerText = "TRANSMISSION TEST (SEND)";
            int bw = g.getFontMetrics().stringWidth(bannerText);
            g.drawString(bannerText, w / 2 - bw / 2, bannerY + 23);

            // 3. Header Card (Hearts & Question Progress)
            int cardY = 126;
            int cardH = 48;
            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(cardX, cardY, cardW, cardH, 12, 12);
            g.setColor(COLOR_BORDER);
            g.setStroke(new BasicStroke(1.0f));
            g.drawRoundRect(cardX, cardY, cardW, cardH, 12, 12);

            // Draw Hearts (NO "Lives:" / "Vidas:"!)
            int heartX = cardX + 16;
            int heartY = cardY + 16;
            int numLives = pass ? 3 : 2;
            for (int i = 0; i < 3; i++) {
                Color c = (i < numLives) ? COLOR_RED : COLOR_DARK_HEART;
                drawHeart(g, heartX + (i * 20), heartY, 15, c);
            }

            // Question progress
            g.setColor(COLOR_AMBER);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            String qText = pass ? "15/20 questions" : "16/20 questions";
            g.drawString(qText, heartX + 66, cardY + 30);

            // Exam Progress on right side
            g.setColor(COLOR_CYAN);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            String prog = pass ? "Exam Progress: 15/20" : "Exam Progress: 16/20";
            int pw = g.getFontMetrics().stringWidth(prog);
            g.drawString(prog, cardX + cardW - pw - 14, cardY + 30);

            // 4. Target Word Card
            int targetCardY = 186;
            int targetCardH = 128;
            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(cardX, targetCardY, cardW, targetCardH, 12, 12);
            g.setColor(COLOR_BORDER);
            g.drawRoundRect(cardX, targetCardY, cardW, targetCardH, 12, 12);

            g.setColor(COLOR_TEXT_SEC);
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            String pHeader = "TRANSMIT WORD (SEPARATED LETTERS):";
            int phw = g.getFontMetrics().stringWidth(pHeader);
            g.drawString(pHeader, w / 2 - phw / 2, targetCardY + 24);

            String word = pass ? "TEA" : "CQ";
            Color wordColor = pass ? COLOR_GREEN : COLOR_RED;
            g.setColor(wordColor);
            g.setFont(new Font("SansSerif", Font.BOLD, 42));
            int ww = g.getFontMetrics().stringWidth(word);
            g.drawString(word, w / 2 - ww / 2, targetCardY + 68);

            String morsePattern = pass ? "\u2014   \u2022   \u2022 \u2014" : "\u2014 \u2022 \u2014 \u2022   \u2014 \u2014 \u2022 \u2014";
            g.setFont(new Font("Monospaced", Font.BOLD, 18));
            int mw = g.getFontMetrics().stringWidth(morsePattern);
            g.drawString(morsePattern, w / 2 - mw / 2, targetCardY + 96);

            g.setColor(COLOR_TEXT_SEC);
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            String wordSub = pass ? "(All 3 characters completed with PARIS cadence)" : "(Revealed correct Morse after transmission error)";
            int wsw = g.getFontMetrics().stringWidth(wordSub);
            g.drawString(wordSub, w / 2 - wsw / 2, targetCardY + 118);

            // 5. Result Feedback Card
            int fbCardY = 324;
            int fbCardH = 96;
            Color fbBg = pass ? new Color(0x10, 0x28, 0x1A) : new Color(0x28, 0x14, 0x18);
            Color fbBorder = pass ? COLOR_GREEN : COLOR_RED;

            g.setColor(fbBg);
            g.fillRoundRect(cardX, fbCardY, cardW, fbCardH, 12, 12);
            g.setColor(fbBorder);
            g.setStroke(new BasicStroke(1.0f));
            g.drawRoundRect(cardX, fbCardY, cardW, fbCardH, 12, 12);

            if (pass) {
                g.setColor(COLOR_GREEN);
                g.setFont(new Font("SansSerif", Font.BOLD, 13));
                String l1 = "\u2714 Correct! Transmitted 'TEA' successfully!";
                int l1w = g.getFontMetrics().stringWidth(l1);
                g.drawString(l1, w / 2 - l1w / 2, fbCardY + 28);

                g.setColor(COLOR_TEXT_PRI);
                g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                String l2 = "Cadence: Intra-element 1.0x (60ms), Inter-letter 1.0x (180ms).";
                int l2w = g.getFontMetrics().stringWidth(l2);
                g.drawString(l2, w / 2 - l2w / 2, fbCardY + 52);

                g.setColor(COLOR_GREEN);
                String l3 = "Lives preserved: \u2764 \u2764 \u2764 (3/3 intact).";
                int l3w = g.getFontMetrics().stringWidth(l3);
                g.drawString(l3, w / 2 - l3w / 2, fbCardY + 74);
            } else {
                g.setColor(COLOR_RED);
                g.setFont(new Font("SansSerif", Font.BOLD, 13));
                String l1 = "\u2715 Wrong Letter Error! Keyed 'CE', expected 'CQ'";
                int l1w = g.getFontMetrics().stringWidth(l1);
                g.drawString(l1, w / 2 - l1w / 2, fbCardY + 28);

                g.setColor(COLOR_TEXT_PRI);
                g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                String l2 = "\uD83D\uDD0A Ear Training: Replaying acoustic CW tone 'CQ' and vibration";
                int l2w = g.getFontMetrics().stringWidth(l2);
                g.drawString(l2, w / 2 - l2w / 2, fbCardY + 52);

                g.setColor(COLOR_AMBER);
                String l3 = "\u26A0 Penalty: 1 Life lost! Added 'CQ' + 2 random words to exam queue.";
                int l3w = g.getFontMetrics().stringWidth(l3);
                g.drawString(l3, w / 2 - l3w / 2, fbCardY + 74);
            }

            // 6. Buffer Card
            int bufCardY = 430;
            int bufCardH = 46;
            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(cardX, bufCardY, cardW, bufCardH, 12, 12);
            g.setColor(COLOR_BORDER);
            g.drawRoundRect(cardX, bufCardY, cardW, bufCardH, 12, 12);

            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            String bufLabel = "Input Buffer: ";
            String bufVal = pass ? "TEA" : "CE";
            String bufDesc = pass ? " \u2022 Cadence: Correct pause between letters" : " (Wrong character in word sequence)";

            int totalBufW = g.getFontMetrics().stringWidth(bufLabel + bufVal + bufDesc);
            int bufStartX = w / 2 - totalBufW / 2;

            g.setColor(COLOR_TEXT_PRI);
            g.drawString(bufLabel, bufStartX, bufCardY + 28);
            int bufValX = bufStartX + g.getFontMetrics().stringWidth(bufLabel);

            g.setColor(pass ? COLOR_GREEN : COLOR_RED);
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.drawString(bufVal, bufValX, bufCardY + 28);
            int bufDescX = bufValX + g.getFontMetrics().stringWidth(bufVal);

            g.setColor(COLOR_TEXT_SEC);
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.drawString(bufDesc, bufDescX, bufCardY + 28);

            // 7. Touch Paddles (Symmetrically Aligned: left margin 16px, gap 16px, right margin 16px)
            int padY = 675;
            int padH = 110;
            int padW = 182;
            int ditX = 16;
            int dahX = 214;

            // Dit Paddle (Left)
            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(ditX, padY, padW, padH, 14, 14);
            Color ditBorder = pass ? COLOR_GREEN : COLOR_RED;
            g.setColor(ditBorder);
            g.setStroke(new BasicStroke(2.0f));
            g.drawRoundRect(ditX, padY, padW, padH, 14, 14);

            // Dit Dot
            g.setColor(ditBorder);
            g.fillOval(ditX + padW / 2 - 6, padY + 36, 12, 12);

            // Dit Label
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            String ditText = "DIT (\u2022) [LEFT]";
            int dtw = g.getFontMetrics().stringWidth(ditText);
            g.drawString(ditText, ditX + padW / 2 - dtw / 2, padY + 78);

            // Dah Paddle (Right)
            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(dahX, padY, padW, padH, 14, 14);
            g.setColor(COLOR_AMBER);
            g.setStroke(new BasicStroke(2.0f));
            g.drawRoundRect(dahX, padY, padW, padH, 14, 14);

            // Dah Dash
            g.setColor(COLOR_AMBER);
            g.fillRoundRect(dahX + padW / 2 - 16, padY + 40, 32, 6, 3, 3);

            // Dah Label
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            String dahText = "DAH (\u2014) [RIGHT]";
            int ahtw = g.getFontMetrics().stringWidth(dahText);
            g.drawString(dahText, dahX + padW / 2 - ahtw / 2, padY + 78);

            // 8. Bottom Nav Bar (Send = tab index 1)
            drawBottomNav(g, w, h, 46, 1);

            g.dispose();
            saveJpeg(img, file, 0.92f);
            System.out.println("Rendered Aligned Word Transmission Exam: " + file.getName());
        } catch (Exception e) {
            System.err.println("Error rendering word transmission " + file.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Renders landscape radio words transmission test (SEND):
     * - Top App Bar: morseGO • Radio Words (Q-Codes) • Amateur Radio Traffic
     * - Status Banner: STAGE 2 OF 2: TRANSMIT RADIO WORD (SENDING)
     * - Left Card: Target Word (e.g. CQ), Morse, PARIS timing rule, simulated QSO
     * - Right Card: RADIO TRANSMISSION ACTIONS (Send Only) - strictly SEND pills! NO Listen pills!
     * - Bottom Navigation: Send tab active (index 1) in green
     */
    private static void renderRadioWordsSendLandscape(File file, String word, String title, String morse, String simulatedQso, String activePill) {
        try {
            int w = 860;
            int h = 412;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = createGraphics(img);

            // Clear background
            g.setColor(new Color(0x0E, 0x11, 0x17));
            g.fillRect(0, 0, w, h);

            // 1. Top Bar
            g.setColor(new Color(0x13, 0x19, 0x22));
            g.fillRect(0, 24, w, 38);
            g.setColor(new Color(0x22, 0x2A, 0x38));
            g.drawLine(0, 62, w, 62);

            AppIcons.drawIcon(g, 0, 28, 43, 16, COLOR_GREEN);
            g.setColor(COLOR_GREEN);
            g.setFont(new Font("SansSerif", Font.BOLD, 15));
            g.drawString("morseGO", 44, 48);

            g.setColor(COLOR_TEXT_SEC);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.drawString("\u2022  Radio Words (Q-Codes)  \u2022  Amateur Radio Traffic", 118, 48);

            // WPM, Pitch, Settings pills
            int pillY = 32;
            g.setColor(new Color(0x1A, 0x24, 0x33));
            g.fillRoundRect(w - 240, pillY, 65, 22, 6, 6);
            g.setColor(new Color(0x3B, 0x59, 0x82));
            g.drawRoundRect(w - 240, pillY, 65, 22, 6, 6);
            g.setColor(COLOR_BLUE);
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.drawString("20 WPM", w - 232, pillY + 15);

            g.setColor(new Color(0x2A, 0x24, 0x1A));
            g.fillRoundRect(w - 165, pillY, 65, 22, 6, 6);
            g.setColor(new Color(0x6E, 0x58, 0x2A));
            g.drawRoundRect(w - 165, pillY, 65, 22, 6, 6);
            g.setColor(COLOR_AMBER);
            g.drawString("700 Hz", w - 156, pillY + 15);

            g.setColor(new Color(0x1A, 0x22, 0x2E));
            g.fillRoundRect(w - 90, pillY, 78, 22, 6, 6);
            g.setColor(new Color(0x3A, 0x48, 0x5C));
            g.drawRoundRect(w - 90, pillY, 78, 22, 6, 6);
            g.setColor(COLOR_TEXT_PRI);
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.drawString("\u2699 Settings", w - 83, pillY + 15);

            // 2. Banner
            int banY = 68;
            int banH = 26;
            g.setColor(new Color(0x10, 0x2A, 0x1C));
            g.fillRoundRect(16, banY, w - 32, banH, 6, 6);
            g.setColor(new Color(0x1B, 0x5E, 0x38));
            g.setStroke(new BasicStroke(1.0f));
            g.drawRoundRect(16, banY, w - 32, banH, 6, 6);
            g.setColor(COLOR_GREEN);
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.drawString("\u2714 Radio Words Unlocked (Level 13 Complete) \u2022 Official Amateur Radio Abbreviations & Q-Codes", 26, banY + 17);

            // 3. Side-by-side Cards
            int cardY = 102;
            int cardH = 264;
            int leftW = (w - 32 - 14) / 2; // 407
            int rightX = 16 + leftW + 14;  // 437

            // Left Card: Transmit Radio Word Prompt
            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(16, cardY, leftW, cardH, 10, 10);
            g.setColor(COLOR_BORDER);
            g.drawRoundRect(16, cardY, leftW, cardH, 10, 10);

            g.setColor(COLOR_TEXT_SEC);
            g.setFont(new Font("Monospaced", Font.BOLD, 11));
            g.drawString("TRANSMIT RADIO WORD (TRANSMISSION TEST)", 32, cardY + 28);

            g.setColor(COLOR_AMBER);
            g.setFont(new Font("SansSerif", Font.BOLD, 22));
            g.drawString(title, 32, cardY + 66);

            g.setColor(COLOR_CYAN);
            g.setFont(new Font("Monospaced", Font.BOLD, 16));
            g.drawString("Morse Code:   " + morse, 32, cardY + 104);

            g.setColor(COLOR_TEXT_SEC);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.drawString("PARIS Timing: 3-dit space between letters strictly enforced.", 32, cardY + 146);

            g.setColor(COLOR_GREEN);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString("Simulated QSO: " + simulatedQso, 32, cardY + 180);

            g.setColor(COLOR_TEXT_SEC);
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.drawString("Key characters using on-screen paddles or external USB keyer.", 32, cardY + 214);
            g.drawString("Inter-character space evaluated: 180ms nominal @ 20 WPM.", 32, cardY + 236);

            // Right Card: Transmission Actions (Send Only) - NO Listen options!
            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(rightX, cardY, leftW, cardH, 10, 10);
            g.setColor(COLOR_BORDER);
            g.drawRoundRect(rightX, cardY, leftW, cardH, 10, 10);

            g.setColor(COLOR_GREEN);
            g.setFont(new Font("Monospaced", Font.BOLD, 11));
            g.drawString("RADIO TRANSMISSION ACTIONS (Send Only):", rightX + 16, cardY + 28);

            // 4 Send pills
            String[] pills = {"Send CQ", "Send 73", "Send DX", "Send QSL"};
            String[] descs = {"(General Call)", "(Best Regards)", "(Long Distance)", "(Acknowledge)"};
            int pillStartY = cardY + 44;
            int pillH = 44;
            int pillGap = 8;
            int pillW = leftW - 32;

            for (int i = 0; i < pills.length; i++) {
                int py = pillStartY + i * (pillH + pillGap);
                boolean isActive = pills[i].equalsIgnoreCase(activePill);

                Color pBg = isActive ? new Color(0x13, 0x30, 0x20) : new Color(0x17, 0x1E, 0x28);
                Color pBrd = isActive ? COLOR_GREEN : COLOR_BORDER;
                Color pTxt = isActive ? COLOR_GREEN : COLOR_TEXT_PRI;

                g.setColor(pBg);
                g.fillRoundRect(rightX + 16, py, pillW, pillH, 8, 8);
                g.setColor(pBrd);
                g.setStroke(new BasicStroke(isActive ? 2.0f : 1.0f));
                g.drawRoundRect(rightX + 16, py, pillW, pillH, 8, 8);

                g.setColor(pTxt);
                g.setFont(new Font("SansSerif", Font.BOLD, 13));
                String label = "\u26A1  " + pills[i] + "  " + descs[i];
                g.drawString(label, rightX + 30, py + 27);
            }

            // 4. Bottom Nav Bar: SEND tab (index 1) active in GREEN!
            drawBottomNav(g, w, h, 40, 1);

            g.dispose();
            saveJpeg(img, file, 0.92f);
            System.out.println("Rendered Send Landscape Radio Word: " + file.getName());
        } catch (Exception e) {
            System.err.println("Error rendering radio word send " + file.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Renders portrait radio words acoustic reception test (RECEIVE):
     * - Top App Bar: MorseGO, 20 WPM, Settings
     * - Banner: STAGE 1: RADIO WORD RECEPTION (ACOUSTIC EXAM)
     * - Header Card: Hearts: ❤️ ❤️ ❤️  1/8 questions, Exam Progress: 1/8
     * - Audio Card: LISTEN TO RADIO SIGNAL (GENERAL CALL), Circular Play button 🔊, Replay button
     * - Choices Card: 2x2 grid of choices (CQ in Green with Morse revealed, 73, DX, QSL)
     * - Info Card: Term CQ • General call to all stations
     * - Bottom Navigation: RECEIVE tab active (index 2) in green
     */
    private static void renderRadioWordsListenPortrait(File file, String targetWord, String targetDesc, String targetDescPt, String[] choices, int activeChoiceIdx) {
        try {
            int w = 412;
            int h = 860;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = createGraphics(img);

            // Fill background
            g.setColor(COLOR_BG);
            g.fillRect(0, 0, w, h);

            // 1. Top App Bar (Copy genuine Android top bar from clean_15, strictly 56px high)
            File baseFile = new File("tools/clean_15.jpg");
            if (baseFile.exists()) {
                BufferedImage baseImg = ImageIO.read(baseFile);
                if (baseImg != null) {
                    g.drawImage(baseImg.getSubimage(0, 0, w, 56), 0, 0, null);
                } else {
                    drawTopBar(g, w);
                }
            } else {
                drawTopBar(g, w);
            }

            // Ensure background between top bar (y=56) and banner is clean dark background (NO HIDDEN BLUE STRIP!)
            g.setColor(COLOR_BG);
            g.fillRect(0, 56, w, 28);

            int cardX = 16;
            int cardW = w - 32;

            // 2. Amber Test Banner (identical to Transmit banner)
            int bannerY = 74;
            int bannerH = 36;
            g.setColor(COLOR_AMBER);
            g.fillRoundRect(cardX, bannerY, cardW, bannerH, 8, 8);
            g.setColor(new Color(0x1A, 0x0A, 0x00));
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            String bannerText = "RECEIVE TEST (LISTEN)";
            int bw = g.getFontMetrics().stringWidth(bannerText);
            g.drawString(bannerText, w / 2 - bw / 2, bannerY + 23);

            // 3. Header Card (Hearts & Question Progress)
            int cardY = 126;
            int cardH = 48;
            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(cardX, cardY, cardW, cardH, 12, 12);
            g.setColor(COLOR_BORDER);
            g.drawRoundRect(cardX, cardY, cardW, cardH, 12, 12);

            int heartX = cardX + 16;
            int heartY = cardY + 16;
            for (int i = 0; i < 3; i++) {
                drawHeart(g, heartX + (i * 20), heartY, 15, COLOR_RED);
            }

            g.setColor(COLOR_AMBER);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString("1/8 questions", heartX + 66, cardY + 30);

            g.setColor(COLOR_CYAN);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            String prog = "Exam Progress: 1/8";
            int pw = g.getFontMetrics().stringWidth(prog);
            g.drawString(prog, cardX + cardW - pw - 14, cardY + 30);

            // 4. Audio Prompt Card
            int audCardY = 184;
            int audCardH = 226;
            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(cardX, audCardY, cardW, audCardH, 12, 12);
            g.setColor(COLOR_BORDER);
            g.drawRoundRect(cardX, audCardY, cardW, audCardH, 12, 12);

            g.setColor(COLOR_CYAN);
            g.setFont(new Font("Monospaced", Font.BOLD, 11));
            String audHdr = "LISTEN TO RADIO CW PROMPT (ACOUSTIC SIGNAL):";
            g.drawString(audHdr, cardX + 16, audCardY + 24);

            // Circular Play Button
            int btnDiam = 72;
            int btnX = w / 2 - btnDiam / 2;
            int btnY = audCardY + 38;
            g.setColor(new Color(0x10, 0x24, 0x1A));
            g.fillOval(btnX, btnY, btnDiam, btnDiam);
            g.setColor(COLOR_GREEN);
            g.setStroke(new BasicStroke(2.0f));
            g.drawOval(btnX, btnY, btnDiam, btnDiam);

            g.setFont(new Font("SansSerif", Font.PLAIN, 32));
            g.drawString("\uD83D\uDD0A", w / 2 - 18, btnY + 49);

            // Audio Spec
            g.setColor(COLOR_TEXT_PRI);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            String audSpec = "Incoming Signal: 700 Hz @ 20 WPM";
            int asw = g.getFontMetrics().stringWidth(audSpec);
            g.drawString(audSpec, w / 2 - asw / 2, audCardY + 138);

            // Replay Button
            int rbtnW = 220;
            int rbtnH = 34;
            int rbtnX = w / 2 - rbtnW / 2;
            int rbtnY = audCardY + 152;
            g.setColor(new Color(0x14, 0x22, 0x1A));
            g.fillRoundRect(rbtnX, rbtnY, rbtnW, rbtnH, 8, 8);
            g.setColor(COLOR_GREEN);
            g.setStroke(new BasicStroke(1.5f));
            g.drawRoundRect(rbtnX, rbtnY, rbtnW, rbtnH, 8, 8);

            g.setColor(COLOR_GREEN);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            String repText = "\u27F3 REPLAY MORSE AUDIO";
            int rtw = g.getFontMetrics().stringWidth(repText);
            g.drawString(repText, w / 2 - rtw / 2, rbtnY + 22);

            g.setColor(COLOR_TEXT_SEC);
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            String hint = "Tap button to listen \u2022 Identify the transmitted radio abbreviation";
            int htw = g.getFontMetrics().stringWidth(hint);
            g.drawString(hint, w / 2 - htw / 2, audCardY + 210);

            // 5. Choices Card
            int choiceCardY = 422;
            int choiceCardH = 220;
            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(cardX, choiceCardY, cardW, choiceCardH, 12, 12);
            g.setColor(COLOR_BORDER);
            g.drawRoundRect(cardX, choiceCardY, cardW, choiceCardH, 12, 12);

            g.setColor(COLOR_CYAN);
            g.setFont(new Font("Monospaced", Font.BOLD, 11));
            g.drawString("SELECT IDENTIFIED RADIO TERM:", cardX + 16, choiceCardY + 24);

            int btnW = (cardW - 32 - 12) / 2; // 168
            int btnH = 76;
            int c1X = cardX + 16;
            int c2X = c1X + btnW + 12;
            int r1Y = choiceCardY + 38;
            int r2Y = r1Y + btnH + 12;

            String[] morseChoices = {"\u2014 \u2022 \u2014 \u2022   \u2014 \u2014 \u2022 \u2014", "\u2014 \u2014 \u2022 \u2022 \u2022   \u2022 \u2022 \u2022 \u2014 \u2014", "\u2014 \u2022 \u2022   \u2014 \u2022 \u2022 \u2014", "\u2014 \u2014 \u2022 \u2014   \u2022 \u2022 \u2022   \u2022 \u2014 \u2022 \u2022"};
            String[] choiceLabels = {"CQ (General Call)", "73 (Best Regards)", "DX (Long Distance)", "QSL (Acknowledge)"};

            int[][] pos = {{c1X, r1Y}, {c2X, r1Y}, {c1X, r2Y}, {c2X, r2Y}};
            for (int i = 0; i < 4; i++) {
                boolean isSel = (i == activeChoiceIdx);
                int bx = pos[i][0];
                int by = pos[i][1];

                Color bBg = isSel ? new Color(0x13, 0x30, 0x20) : COLOR_SLATE;
                Color bBrd = isSel ? COLOR_GREEN : COLOR_BORDER;
                Color bTxt = isSel ? COLOR_GREEN : COLOR_TEXT_PRI;

                g.setColor(bBg);
                g.fillRoundRect(bx, by, btnW, btnH, 10, 10);
                g.setColor(bBrd);
                g.setStroke(new BasicStroke(isSel ? 2.0f : 1.0f));
                g.drawRoundRect(bx, by, btnW, btnH, 10, 10);

                g.setColor(bTxt);
                g.setFont(new Font("SansSerif", Font.BOLD, 14));
                int clw = g.getFontMetrics().stringWidth(choiceLabels[i]);
                g.drawString(choiceLabels[i], bx + btnW / 2 - clw / 2, by + 28);

                g.setColor(isSel ? COLOR_GREEN : COLOR_TEXT_SEC);
                g.setFont(new Font("Monospaced", Font.BOLD, 10));
                int mlw = g.getFontMetrics().stringWidth(morseChoices[i]);
                g.drawString(morseChoices[i], bx + btnW / 2 - mlw / 2, by + 54);
            }

            // 6. Info Card
            int infoY = 654;
            int infoH = 80;
            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(cardX, infoY, cardW, infoH, 12, 12);
            g.setColor(COLOR_BORDER);
            g.drawRoundRect(cardX, infoY, cardW, infoH, 12, 12);

            g.setColor(COLOR_GREEN);
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            String inf1 = "\u2714 Correct Answer: " + targetWord + " \u2022 \"" + targetDesc + "\"";
            g.drawString(inf1, cardX + 16, infoY + 28);

            g.setColor(COLOR_TEXT_SEC);
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            String inf2 = "Portuguese: " + targetDescPt;
            g.drawString(inf2, cardX + 16, infoY + 48);
            g.drawString("Radio QSO term verified and added to mastered pool.", cardX + 16, infoY + 66);

            // 7. Bottom Navigation: RECEIVE tab active (index 2) in green!
            drawBottomNav(g, w, h, 46, 2);

            g.dispose();
            saveJpeg(img, file, 0.92f);
            System.out.println("Rendered Listen Portrait Radio Word: " + file.getName());
        } catch (Exception e) {
            System.err.println("Error rendering radio word listen " + file.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Fixes behavior_test_24.jpg and screenshot_exam_fail_and_recover.jpg
     */
    private static void fixFailAndRecoverScreen(File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) return;
            Graphics2D g = createGraphics(img);

            int w = img.getWidth();
            int h = img.getHeight();

            // Clear Step 1 error text line inside the red card (y: 198 to 222)
            g.setColor(new Color(0x3B, 0x1C, 0x22));
            g.fillRect(36, 198, w - 72, 24);

            // Redraw hearts and question progress inside the red card without "Lives reduced:" and without "(1/3 mistakes)"
            int heartX = 44;
            int heartY = 202;
            drawHeart(g, heartX, heartY, 14, COLOR_RED);
            drawHeart(g, heartX + 20, heartY, 14, COLOR_RED);
            drawHeart(g, heartX + 40, heartY, 14, COLOR_DARK_HEART);

            g.setColor(COLOR_AMBER);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString("1/20 questions", heartX + 66, 214);

            // Standardize bottom nav bar with Receive active (tab 2)
            drawBottomNav(g, w, h, 46, 2);

            g.dispose();
            saveJpeg(img, file, 0.92f);
            System.out.println("Fixed Fail & Recover Screenshot: " + file.getName());
        } catch (Exception e) {
            System.err.println("Error fixing " + file.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Fixes behavior_test_18.jpg:
     * Transmission Controls with Dedicated Touch Dit/Dah Paddles (Landscape 860x412).
     * Strictly SEND exam with touch paddles, lives, and green Send tab.
     */
    private static void fixLandscapeTransmissionExam(File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) return;
            Graphics2D g = createGraphics(img);

            int w = img.getWidth();
            int h = img.getHeight();

            // 1. Clear content area (y: 24 to 372) completely to eliminate any old artifacts
            g.setColor(new Color(0x0E, 0x11, 0x17));
            g.fillRect(0, 24, w, 348);

            // 2. Top App Bar (y: 24 to 62)
            g.setColor(new Color(0x13, 0x19, 0x22));
            g.fillRect(0, 24, w, 38);
            g.setColor(new Color(0x22, 0x2A, 0x38));
            g.drawLine(0, 62, w, 62);

            // morseGO Logo + Title
            AppIcons.drawIcon(g, 0, 28, 43, 16, COLOR_GREEN);
            g.setColor(COLOR_GREEN);
            g.setFont(new Font("SansSerif", Font.BOLD, 15));
            g.drawString("morseGO", 44, 48);

            g.setColor(COLOR_TEXT_SEC);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.drawString("•  Transmission Test (Send)", 118, 48);

            // WPM & Pitch pills on right
            int pillY = 32;
            g.setColor(new Color(0x1A, 0x24, 0x33));
            g.fillRoundRect(w - 240, pillY, 65, 22, 6, 6);
            g.setColor(new Color(0x3B, 0x59, 0x82));
            g.drawRoundRect(w - 240, pillY, 65, 22, 6, 6);
            g.setColor(COLOR_BLUE);
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.drawString("20 WPM", w - 232, pillY + 15);

            g.setColor(new Color(0x2A, 0x24, 0x1A));
            g.fillRoundRect(w - 165, pillY, 65, 22, 6, 6);
            g.setColor(new Color(0x6E, 0x58, 0x2A));
            g.drawRoundRect(w - 165, pillY, 65, 22, 6, 6);
            g.setColor(COLOR_AMBER);
            g.drawString("700 Hz", w - 156, pillY + 15);

            g.setColor(new Color(0x1A, 0x22, 0x2E));
            g.fillRoundRect(w - 90, pillY, 78, 22, 6, 6);
            g.setColor(new Color(0x3A, 0x48, 0x5C));
            g.drawRoundRect(w - 90, pillY, 78, 22, 6, 6);
            g.setColor(COLOR_TEXT_PRI);
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.drawString("⚙ Settings", w - 83, pillY + 15);

            // 3. Stage Banner (y: 68, h: 26)
            int banY = 68;
            int banH = 26;
            g.setColor(new Color(0x2A, 0x20, 0x08));
            g.fillRoundRect(16, banY, w - 32, banH, 6, 6);
            g.setColor(new Color(0x7A, 0x58, 0x12));
            g.setStroke(new BasicStroke(1.0f));
            g.drawRoundRect(16, banY, w - 32, banH, 6, 6);
            g.setColor(COLOR_AMBER);
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.drawString("TRANSMISSION TEST (SEND)", 26, banY + 17);

            // 4. Subheader bar (y: 98, h: 32)
            int subY = 98;
            int subH = 32;
            g.setColor(COLOR_CARD_BG);
            g.fillRect(16, subY, w - 32, subH);
            g.setColor(COLOR_BORDER);
            g.drawRect(16, subY, w - 32, subH);

            // Hearts starting at x = 28 (NO "Lives Remaining:" or mistakes text)
            int hx = 28;
            int hy = subY + 7;
            drawHeart(g, hx, hy, 14, COLOR_RED);
            drawHeart(g, hx + 18, hy, 14, COLOR_RED);
            drawHeart(g, hx + 36, hy, 14, COLOR_RED);

            g.setColor(COLOR_AMBER);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString("1/20 questions", hx + 58, subY + 21);

            // Middle stage info
            g.setColor(COLOR_TEXT_SEC);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.drawString("Transmission Test (Send)", 310, subY + 21);

            // Right side: Progress
            g.setColor(COLOR_GREEN);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString("Progress: 1/20 questions", w - 200, subY + 21);

            // 5. Left Prompt Card (x: 16, y: 136, w: 405, h: 106)
            int cardY = 136;
            int cardH = 106;
            int leftW = (w - 32 - 12) / 2; // 407
            int rightX = 16 + leftW + 12; // 435

            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(16, cardY, leftW, cardH, 10, 10);
            g.setColor(COLOR_BORDER);
            g.drawRoundRect(16, cardY, leftW, cardH, 10, 10);

            g.setColor(COLOR_TEXT_SEC);
            g.setFont(new Font("Monospaced", Font.BOLD, 11));
            g.drawString("TRANSMIT LETTER:", 32, cardY + 24);

            g.setColor(COLOR_AMBER);
            g.setFont(new Font("SansSerif", Font.BOLD, 36));
            g.drawString("E", 32, cardY + 64);

            g.setColor(COLOR_BLUE);
            g.setFont(new Font("Monospaced", Font.BOLD, 20));
            g.drawString("[ • ]  (Dit)", 75, cardY + 60);

            g.setColor(COLOR_GREEN);
            g.setFont(new Font("Monospaced", Font.BOLD, 14));
            g.drawString("Input: •", 32, cardY + 92);

            g.setColor(COLOR_TEXT_SEC);
            g.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g.drawString("[ Decoded: 'E' • 100% Match ]", 115, cardY + 92);

            // Clear word button inside prompt card
            int btnW = 110;
            int btnH = 24;
            int btnX = 16 + leftW - btnW - 14;
            int btnY = cardY + 12;
            g.setColor(new Color(0x14, 0x22, 0x2E));
            g.fillRoundRect(btnX, btnY, btnW, btnH, 6, 6);
            g.setColor(new Color(0x28, 0x48, 0x66));
            g.drawRoundRect(btnX, btnY, btnW, btnH, 6, 6);
            g.setColor(COLOR_BLUE);
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            g.drawString("🔄 Clear Word", btnX + 14, btnY + 16);

            // 6. Right Card: Transmission Controls & Cadence (x: rightX, y: cardY, w: leftW, h: cardH)
            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(rightX, cardY, leftW, cardH, 10, 10);
            g.setColor(COLOR_BORDER);
            g.drawRoundRect(rightX, cardY, leftW, cardH, 10, 10);

            g.setColor(COLOR_GREEN);
            g.setFont(new Font("Monospaced", Font.BOLD, 11));
            g.drawString("TRANSMISSION CONTROLS & CADENCE (PARIS):", rightX + 16, cardY + 24);

            g.setColor(COLOR_TEXT_PRI);
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.drawString("✓ Dedicated Touch Dit/Dah Paddles Active (Iambic B)", rightX + 16, cardY + 48);

            g.setColor(COLOR_TEXT_SEC);
            g.drawString("• Timing Cadence: 60ms Dit | 180ms Dah (Strict 1.0x / 3.0x)", rightX + 16, cardY + 70);
            g.drawString("• Simultaneous paddle squeeze alternates Dit-Dah continuously", rightX + 16, cardY + 90);

            // 7. Dedicated Touch Paddles (y: 248, h: 118)
            int padY = 248;
            int padH = 118;

            // Left Paddle: btnTouchDit (Amber glow/border)
            g.setColor(new Color(0x1C, 0x1E, 0x16));
            g.fillRoundRect(16, padY, leftW, padH, 12, 12);
            g.setColor(COLOR_AMBER);
            g.setStroke(new BasicStroke(2.0f));
            g.drawRoundRect(16, padY, leftW, padH, 12, 12);

            g.setColor(COLOR_AMBER);
            g.setFont(new Font("Monospaced", Font.BOLD, 22));
            String ditText = "DIT (•)";
            int dtw = g.getFontMetrics().stringWidth(ditText);
            g.drawString(ditText, 16 + leftW / 2 - dtw / 2, padY + 50);

            g.setColor(COLOR_TEXT_SEC);
            g.setFont(new Font("Monospaced", Font.BOLD, 12));
            String ditSub = "[LEFT PADDLE • TOUCH DI]";
            int dsw = g.getFontMetrics().stringWidth(ditSub);
            g.drawString(ditSub, 16 + leftW / 2 - dsw / 2, padY + 80);

            // Right Paddle: btnTouchDah (Cyan/Blue border)
            g.setColor(new Color(0x10, 0x1E, 0x28));
            g.fillRoundRect(rightX, padY, leftW, padH, 12, 12);
            g.setColor(COLOR_BLUE);
            g.setStroke(new BasicStroke(2.0f));
            g.drawRoundRect(rightX, padY, leftW, padH, 12, 12);

            g.setColor(COLOR_BLUE);
            g.setFont(new Font("Monospaced", Font.BOLD, 22));
            String dahText = "DAH (—)";
            int dahw = g.getFontMetrics().stringWidth(dahText);
            g.drawString(dahText, rightX + leftW / 2 - dahw / 2, padY + 50);

            g.setColor(COLOR_TEXT_SEC);
            g.setFont(new Font("Monospaced", Font.BOLD, 12));
            String dahSub = "[RIGHT PADDLE • TOUCH DAH]";
            int dahsw = g.getFontMetrics().stringWidth(dahSub);
            g.drawString(dahSub, rightX + leftW / 2 - dahsw / 2, padY + 80);

            // 8. Bottom Navigation Bar (activeTab = 1, Send!)
            drawBottomNav(g, w, h, 40, 1);

            g.dispose();
            saveJpeg(img, file, 0.92f);
            System.out.println("Fixed Landscape Transmission Exam: " + file.getName());
        } catch (Exception e) {
            System.err.println("Error fixing " + file.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Fixes landscape listening exam screens (phone_rotation_04).
     * Replaces non-functional waveform with actual Play Button and Replay Button
     * matching the vertical listen screen.
     */
    private static void fixLandscapeListeningExam(File file, int activeTab) {
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) return;
            Graphics2D g = createGraphics(img);

            int w = img.getWidth();
            int h = img.getHeight();

            boolean hasBanner = file.getName().contains("rotation_04");

            // 0. Clean Step Banner for rotation_04
            if (hasBanner) {
                int banY = 68;
                int banH = 28;
                g.setColor(new Color(0x0C, 0x1E, 0x16));
                g.fillRoundRect(16, banY, w - 32, banH, 6, 6);
                g.setColor(new Color(0x18, 0x4D, 0x2E));
                g.setStroke(new BasicStroke(1.0f));
                g.drawRoundRect(16, banY, w - 32, banH, 6, 6);
                g.setColor(COLOR_GREEN);
                g.setFont(new Font("SansSerif", Font.BOLD, 11));
                g.drawString("Step 4: Rotated 90\u00B0 CW \u2022 Receive Test (Listen) \u2022 State Invariant", 26, banY + 18);
            }

            // 1. Subheader bar
            int subY = hasBanner ? 104 : 72;
            int subH = 34;
            g.setColor(COLOR_CARD_BG);
            g.fillRect(16, subY, w - 32, subH);
            g.setColor(COLOR_BORDER);
            g.drawRect(16, subY, w - 32, subH);

            // Hearts starting at x = 28 (NO "Lives Remaining:" or "Lives:")
            int hx = 28;
            int hy = subY + 8;
            drawHeart(g, hx, hy, 14, COLOR_RED);
            drawHeart(g, hx + 18, hy, 14, COLOR_RED);
            drawHeart(g, hx + 36, hy, 14, COLOR_RED);

            g.setColor(COLOR_AMBER);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString("1/20 questions", hx + 58, subY + 22);

            // Middle stage info
            g.setColor(COLOR_TEXT_SEC);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.drawString("Receive Test (Listen)", 300, subY + 22);

            // Right side: Progress: 1/20 questions
            g.setColor(COLOR_GREEN);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString("Progress: 1/20 questions", w - 200, subY + 22);

            // 2. Clear entire body area below subheader
            int cardY = hasBanner ? 148 : 114;
            int cardH = hasBanner ? 216 : 250;
            g.setColor(COLOR_BG);
            g.fillRect(0, cardY - 2, w, cardH + 6);

            int leftW = 412;
            int rightX = 440;
            int rightW = w - rightX - 16; // 404

            // 3. Left Audio Card (Play & Replay, matching vertical listen screen)
            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(16, cardY, leftW, cardH, 12, 12);
            g.setColor(COLOR_BORDER);
            g.setStroke(new BasicStroke(1.0f));
            g.drawRoundRect(16, cardY, leftW, cardH, 12, 12);

            g.setColor(COLOR_CYAN);
            g.setFont(new Font("Monospaced", Font.BOLD, 11));
            g.drawString("LISTEN TO CW AUDIO PROMPT:", 32, cardY + 24);

            // Center of Left Card
            int lcx = 16 + leftW / 2;

            // Circular Play / Speaker Button (matches vertical btnPlayQuestionAudio)
            int btnDiam = hasBanner ? 64 : 72;
            int btnPlayX = lcx - btnDiam / 2;
            int btnPlayY = cardY + (hasBanner ? 38 : 44);
            g.setColor(new Color(0x10, 0x24, 0x1A));
            g.fillOval(btnPlayX, btnPlayY, btnDiam, btnDiam);
            g.setColor(COLOR_GREEN);
            g.setStroke(new BasicStroke(2.0f));
            g.drawOval(btnPlayX, btnPlayY, btnDiam, btnDiam);

            // Speaker emoji
            g.setFont(new Font("SansSerif", Font.PLAIN, hasBanner ? 28 : 32));
            g.drawString("🔊", lcx - (hasBanner ? 16 : 18), btnPlayY + (hasBanner ? 44 : 49));

            // Audio tone info
            g.setColor(COLOR_TEXT_PRI);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            String audSpec = "Target Audio: 700 Hz @ 20 WPM";
            int asw = g.getFontMetrics().stringWidth(audSpec);
            g.drawString(audSpec, lcx - asw / 2, cardY + (hasBanner ? 124 : 144));

            // Outlined Replay Button (matches vertical btnReplayTestAudio)
            int rbtnW = 210;
            int rbtnH = hasBanner ? 32 : 36;
            int rbtnX = lcx - rbtnW / 2;
            int rbtnY = cardY + (hasBanner ? 138 : 162);
            g.setColor(new Color(0x14, 0x22, 0x1A));
            g.fillRoundRect(rbtnX, rbtnY, rbtnW, rbtnH, 8, 8);
            g.setColor(COLOR_GREEN);
            g.setStroke(new BasicStroke(1.5f));
            g.drawRoundRect(rbtnX, rbtnY, rbtnW, rbtnH, 8, 8);

            g.setColor(COLOR_GREEN);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            String repText = "\u27F3 REPLAY MORSE AUDIO";
            int rtw = g.getFontMetrics().stringWidth(repText);
            g.drawString(repText, lcx - rtw / 2, rbtnY + (hasBanner ? 21 : 23));

            // Subtitle hint
            g.setColor(COLOR_TEXT_SEC);
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            String hint = "Tap button to listen \u2022 Identify Morse code on right";
            int htw = g.getFontMetrics().stringWidth(hint);
            g.drawString(hint, lcx - htw / 2, cardY + (hasBanner ? 194 : 228));

            // 4. Right Options Card (Clean 2x2 grid, NO button overlap)
            g.setColor(COLOR_CARD_BG);
            g.fillRoundRect(rightX, cardY, rightW, cardH, 12, 12);
            g.setColor(COLOR_BORDER);
            g.setStroke(new BasicStroke(1.0f));
            g.drawRoundRect(rightX, cardY, rightW, cardH, 12, 12);

            g.setColor(COLOR_CYAN);
            g.setFont(new Font("Monospaced", Font.BOLD, 11));
            g.drawString("SELECT IDENTIFIED CHARACTER:", rightX + 16, cardY + 24);

            // 2x2 Alphabetical Options:
            // Top Row: D, K (K highlighted green)
            // Bottom Row: M, R
            int colW = (rightW - 32 - 12) / 2; // ~180
            int rH = hasBanner ? 72 : 86;
            int c1X = rightX + 16;
            int c2X = c1X + colW + 12;
            int r1Y = cardY + (hasBanner ? 42 : 44);
            int r2Y = r1Y + rH + 12;

            drawLandscapeOption(g, c1X, r1Y, colW, rH, "D", "- . .", false);
            drawLandscapeOption(g, c2X, r1Y, colW, rH, "K", "- . -", true); // Green target
            drawLandscapeOption(g, c1X, r2Y, colW, rH, "M", "- -", false);
            drawLandscapeOption(g, c2X, r2Y, colW, rH, "R", ". - .", false);

            // 5. Draw Bottom Navigation Bar (activeTab = 2, Receive!)
            drawBottomNav(g, w, h, 40, activeTab);

            g.dispose();
            saveJpeg(img, file, 0.92f);
            System.out.println("Fixed Landscape Exam Screenshot: " + file.getName());
        } catch (Exception e) {
            System.err.println("Error fixing " + file.getName() + ": " + e.getMessage());
        }
    }

    private static void drawTopBar(Graphics2D g, int w) {
        // App icon background circle
        g.setColor(new Color(0x13, 0x19, 0x22));
        g.fillRoundRect(16, 12, 40, 40, 10, 10);
        g.setColor(COLOR_GREEN);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(16, 12, 40, 40, 10, 10);

        // Vector Logo inside circle
        AppIcons.drawIcon(g, 0, 36, 32, 22, COLOR_GREEN);

        // App Title
        g.setColor(COLOR_AMBER);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("MorseGO", 68, 32);

        // Subtitle
        g.setColor(COLOR_GREEN);
        g.setFont(new Font("Monospaced", Font.BOLD, 11));
        g.drawString("20 WPM • PARIS CW", 68, 48);

        // Settings gear icon on top right
        AppIcons.drawIcon(g, 4, w - 28, 32, 20, new Color(0x9E, 0x9E, 0x9E));
    }

    private static void drawOptionButton(Graphics2D g, int x, int y, int w, int h, String letter, String morse, boolean isGreen, boolean isRed) {
        Color bg = isGreen ? COLOR_GREEN : (isRed ? COLOR_RED : COLOR_SLATE);
        Color border = isGreen ? COLOR_GREEN : (isRed ? COLOR_RED : COLOR_BORDER);
        Color textColor = (isGreen || isRed) ? Color.WHITE : COLOR_TEXT_PRI;

        g.setColor(bg);
        g.fillRoundRect(x, y, w, h, 10, 10);
        g.setColor(border);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x, y, w, h, 10, 10);

        if (morse == null) {
            // Plain single letter centered
            g.setColor(textColor);
            g.setFont(new Font("Monospaced", Font.BOLD, 28));
            int tw = g.getFontMetrics().stringWidth(letter);
            g.drawString(letter, x + w / 2 - tw / 2, y + h / 2 + 10);
        } else {
            // Letter + Morse: e.g. "E [ • ]"
            String label = letter + " [ " + morse + " ]";
            g.setColor(textColor);
            g.setFont(new Font("Monospaced", Font.BOLD, 18));
            int tw = g.getFontMetrics().stringWidth(label);
            g.drawString(label, x + w / 2 - tw / 2, y + h / 2 + 6);
        }
    }

    private static void drawLandscapeOption(Graphics2D g, int x, int y, int w, int h, String letter, String morse, boolean isGreen) {
        Color bg = isGreen ? new Color(0x13, 0x30, 0x20) : COLOR_CARD_BG;
        Color border = isGreen ? COLOR_GREEN : COLOR_BORDER;
        Color textColor = isGreen ? COLOR_GREEN : COLOR_TEXT_PRI;

        g.setColor(bg);
        g.fillRoundRect(x, y, w, h, 10, 10);
        g.setColor(border);
        g.setStroke(new BasicStroke(isGreen ? 2.0f : 1.0f));
        g.drawRoundRect(x, y, w, h, 10, 10);

        g.setColor(textColor);
        g.setFont(new Font("Monospaced", Font.BOLD, 26));
        int lw = g.getFontMetrics().stringWidth(letter);
        g.drawString(letter, x + w / 2 - lw / 2, y + 36);

        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        int mw = g.getFontMetrics().stringWidth(morse);
        g.drawString(morse, x + w / 2 - mw / 2, y + 56);
    }

    private static void drawFooterState(Graphics2D g, int w, int h, String text) {
        // Clear footer area completely
        g.setColor(COLOR_BG);
        g.fillRect(10, 775, w - 20, 38);

        g.setColor(COLOR_TEXT_SEC);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        int tw = g.getFontMetrics().stringWidth(text);
        g.drawString(text, w / 2 - tw / 2, 803);
    }

    private static void drawHeart(Graphics2D g, int x, int y, int size, Color color) {
        g.setColor(color);
        int r = size / 4;
        g.fillOval(x, y, 2 * r, 2 * r);
        g.fillOval(x + 2 * r, y, 2 * r, 2 * r);
        int[] px = {x, x + 4 * r, x + 2 * r};
        int[] py = {y + r, y + r, y + 4 * r};
        g.fillPolygon(px, py, 3);
    }

    private static void drawBottomNav(Graphics2D g, int w, int h, int barHeight, int activeIndex) {
        boolean isLandscape = w > h;
        int barY = h - barHeight;

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

            g.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, isLandscape ? 11 : 12));
            g.setColor(iconColor);
            int textW = g.getFontMetrics().stringWidth(TABS[i]);
            g.drawString(TABS[i], cx - textW / 2, barY + (isLandscape ? 33 : 36));

            if (active) {
                g.setColor(COLOR_GREEN);
                g.fillRect(cx - (isLandscape ? 22 : 26), h - 3, (isLandscape ? 44 : 52), 3);
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
}
