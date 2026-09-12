package com.morsego.app.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Common Ham Radio Words & Prosigns (CQ, 73, DX, QSL, QTH, RST, SOS, TU).
 * Rules:
 * 1. Only unlocked AFTER mastering all 26 alphabet letters in the Morse tree (Level >= 13).
 * 2. Strictly split into two separated categories: LISTEN (Acoustic) and SEND (Transmission).
 * 3. Supports bilingual user content (English default, Portuguese on pt locale).
 */
public class MorseRadioWords {

    public static final int REQUIRED_LEVEL_FOR_RADIO_WORDS = 13; // Level 13 completes A-Z (Z and Q)

    public static final List<RadioWordItem> RADIO_WORDS = Collections.unmodifiableList(Arrays.asList(
            new RadioWordItem("CQ", "-.-. --.-",
                    "General call to all stations",
                    "Chamada geral para todas as estações"),
            new RadioWordItem("73", "--... ...--",
                    "Best regards and greetings in CW",
                    "Cumprimentos e melhores votos em CW"),
            new RadioWordItem("DX", "-.. -..-",
                    "Long distance station contact",
                    "Contacto com estação a longa distância"),
            new RadioWordItem("QSL", "--.- ... .-..",
                    "Acknowledgement of receipt / QSL card",
                    "Confirmação de receção / cartão QSL"),
            new RadioWordItem("QTH", "--.- - ....",
                    "Geographic location of the station",
                    "Localização geográfica da estação"),
            new RadioWordItem("RST", ".-. ... -",
                    "Signal report (Readability-Signal-Tone)",
                    "Relatório de sinal (Readability-Signal-Tone)"),
            new RadioWordItem("SOS", "... --- ...",
                    "International distress signal",
                    "Sinal internacional de socorro"),
            new RadioWordItem("TU", "- ..-",
                    "Thank You in CW telegraphy",
                    "Agradecimento telegráfico (Thank You)")
    ));

    public static class RadioWordItem {
        public final String word;
        public final String morse;
        public final String descriptionEn;
        public final String descriptionPt;

        public RadioWordItem(String word, String morse, String descriptionEn, String descriptionPt) {
            this.word = word;
            this.morse = morse;
            this.descriptionEn = descriptionEn;
            this.descriptionPt = descriptionPt;
        }

        public String getDescription() {
            boolean isPt = Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
            return isPt ? descriptionPt : descriptionEn;
        }
    }

    /**
     * Determines whether radio words are unlocked.
     * Must strictly have unlocked all 26 letters in the Morse tree (Level >= 13).
     */
    public static boolean isUnlocked(int currentUnlockedLevel) {
        return currentUnlockedLevel >= REQUIRED_LEVEL_FOR_RADIO_WORDS;
    }

    /**
     * Verifies that the pool contains all 26 letters of the alphabet.
     */
    public static boolean hasAllLettersUnlocked(List<String> pool) {
        if (pool == null) return false;
        for (char c = 'A'; c <= 'Z'; c++) {
            if (!pool.contains(String.valueOf(c))) {
                return false;
            }
        }
        return true;
    }

    public static List<RadioWordItem> getRadioWords() {
        return RADIO_WORDS;
    }

    public static RadioWordItem getRandomWord() {
        int idx = (int) (Math.random() * RADIO_WORDS.size());
        return RADIO_WORDS.get(idx);
    }

    public static class RadioPill {
        public enum Mode {
            LISTEN, SEND, OUVIR, ENVIAR;
            public boolean isListening() { return this == LISTEN || this == OUVIR; }
            public boolean isTransmission() { return this == SEND || this == ENVIAR; }
        }

        public final Mode mode;
        public final String word;
        public final String label;

        public RadioPill(Mode mode, String word) {
            this.mode = mode;
            this.word = word;
            boolean isPt = Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
            String prefix;
            if (mode.isListening()) {
                prefix = isPt ? "Ouvir " : "Listen ";
            } else {
                prefix = isPt ? "Enviar " : "Send ";
            }
            this.label = prefix + word;
        }
    }

    /**
     * Returns the pills list strictly separated for the LISTEN section.
     */
    public static List<RadioPill> getListeningPills() {
        List<RadioPill> list = new ArrayList<>();
        for (RadioWordItem item : RADIO_WORDS) {
            list.add(new RadioPill(RadioPill.Mode.OUVIR, item.word));
        }
        return list;
    }

    /**
     * Returns the pills list strictly separated for the SEND / TRANSMISSION section.
     */
    public static List<RadioPill> getTransmissionPills() {
        List<RadioPill> list = new ArrayList<>();
        for (RadioWordItem item : RADIO_WORDS) {
            list.add(new RadioPill(RadioPill.Mode.ENVIAR, item.word));
        }
        return list;
    }

    /**
     * Generates multiple choices for listening evaluation.
     */
    public static List<String> generateListeningChoices(String targetWord, int count) {
        List<String> choices = new ArrayList<>();
        choices.add(targetWord);

        List<RadioWordItem> pool = new ArrayList<>(RADIO_WORDS);
        Collections.shuffle(pool);

        for (RadioWordItem item : pool) {
            if (!item.word.equals(targetWord) && choices.size() < count) {
                choices.add(item.word);
            }
        }

        Collections.shuffle(choices);
        return choices;
    }
}
