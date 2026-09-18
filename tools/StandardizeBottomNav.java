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

/**
 * Standardizes the bottom navigation bar across ALL screenshots (both landscape and portrait)
 * to strictly have the exact same 5 items:
 * [Tree] [Send] [Receive] [USB] [Config]
 */
public class StandardizeBottomNav {

    private static final String[] TABS = {"Tree", "Send", "Receive", "Keyer", "USB"};

    public static void main(String[] args) {
        String[] dirs = (args != null && args.length > 0 && args[0] != null && !args[0].isEmpty())
                ? new String[]{args[0]}
                : new String[]{"release/development/screenshots"};
        for (String dirPath : dirs) {
            File dir = new File(dirPath);
            if (!dir.exists() || !dir.isDirectory()) continue;

            File[] files = dir.listFiles((d, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
            if (files == null) continue;

            System.out.println("Processing " + files.length + " screenshots in " + dirPath);

            int updatedCount = 0;
            for (File file : files) {
                try {
                    if (processScreenshot(file)) {
                        updatedCount++;
                    }
                } catch (Exception e) {
                    System.err.println("Error processing " + file.getName() + ": " + e.getMessage());
                }
            }

            System.out.println("Standardized bottom navigation in " + updatedCount + " screenshots in " + dirPath);
        }
    }

    public static boolean processScreenshot(File file) throws Exception {
        BufferedImage img = ImageIO.read(file);
        if (img == null) return false;

        int w = img.getWidth();
        int h = img.getHeight();

        // 01 to 06 pngs are handled by ScreenshotGenerator directly
        String name = file.getName().toLowerCase();
        if (name.startsWith("01_") || name.startsWith("02_") || name.startsWith("03_") ||
            name.startsWith("04_") || name.startsWith("05_") || name.startsWith("06_")) {
            return false;
        }

        int activeIndex = determineActiveTab(name);

        boolean isLandscape = w > h;
        int barHeight = isLandscape ? 40 : 46;
        int barY = h - barHeight;

        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color bgColor = new Color(0x0E, 0x12, 0x1A);
        Color borderColor = new Color(0x22, 0x2A, 0x38);
        Color accentColor = new Color(0x00, 0xE6, 0x76);
        Color textSec = new Color(0x7E, 0x8A, 0x9B);

        // Fill bottom bar background
        g.setColor(bgColor);
        g.fillRect(0, barY, w, barHeight);

        // Top border line
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(1.0f));
        g.drawLine(0, barY, w, barY);

        int tabW = w / TABS.length;

        for (int i = 0; i < TABS.length; i++) {
            boolean active = (i == activeIndex);
            int cx = i * tabW + tabW / 2;
            Color iconColor = active ? accentColor : textSec;

            int iconY = barY + (isLandscape ? 6 : 8);
            double iconSize = isLandscape ? 16 : 18;

            // Render EXACT Application Vector Icon
            AppIcons.drawIcon(g, i, cx, iconY + (isLandscape ? 8 : 9), iconSize, iconColor);

            // Tab label text
            g.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, isLandscape ? 11 : 12));
            g.setColor(iconColor);
            int textW = g.getFontMetrics().stringWidth(TABS[i]);
            g.drawString(TABS[i], cx - textW / 2, barY + (isLandscape ? 33 : 36));

            // Active underline indicator
            if (active) {
                g.setColor(accentColor);
                g.fillRect(cx - (isLandscape ? 22 : 26), h - 3, (isLandscape ? 44 : 52), 3);
            }
        }

        g.dispose();

        // Save back image
        if (file.getName().endsWith(".jpg")) {
            writeHighQualityJpeg(img, file);
        } else {
            ImageIO.write(img, "PNG", file);
        }

        return true;
    }

    private static int determineActiveTab(String name) {
        // 1. Behavior test exact numbers
        if (name.startsWith("behavior_test_")) {
            try {
                int num = Integer.parseInt(name.replace("behavior_test_", "").replace(".jpg", ""));
                if (num <= 13 || num == 41 || num == 42) return 0; // Tree
                if (num == 14 || num == 18 || (num >= 19 && num <= 23) || (num >= 29 && num <= 33) || (num >= 45 && num <= 47) || num == 49) return 1; // Send
                if ((num >= 15 && num <= 17) || (num >= 24 && num <= 28) || num == 38 || (num >= 43 && num <= 44) || num == 48) return 2; // Receive
                if (num == 34 || num == 35) return 3; // Keyer (Free Keyer Sandbox)
                if (num == 36 || num == 37) return 4; // USB / Hardware Keyer
                if (num == 39 || num == 40) return 0; // Settings dialog modal (tree background)
            } catch (Exception ignored) {}
        }

        if (name.contains("05_free_keyer") || name.contains("free_keyer") || name.contains("keyer_landscape") || name.contains("rotation_03") || name.contains("screenshot_02_keyer")) {
            return 3; // Keyer
        }

        if (name.contains("05_hardware") || name.contains("06_hardware") || name.contains("05_hw_setup") || name.contains("hardware") || name.contains("usb") || name.contains("hw") || name.contains("rotation_05") || name.contains("screenshot_04_hardware")) {
            return 4; // USB
        }

        if (name.contains("tree") || name.contains("rotation_01") || name.contains("rotation_02") || name.contains("rotation_06")) {
            return 0; // Tree
        }

        if (name.contains("screenshot_05_radio_words") || name.contains("radio_words_landscape") || name.contains("radio_words_stage2") || name.contains("radio_words_transmit") || name.contains("send") || name.contains("transmi") || name.contains("paddles")) {
            return 1; // Send
        }

        if (name.contains("receive") || name.contains("listen") || name.contains("hear") || name.contains("exam") || name.contains("quiz") || name.contains("radio") || name.contains("rotation_04")) {
            return 2; // Receive
        }

        return 0;
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
        } finally {
            writer.dispose();
        }
    }
}
