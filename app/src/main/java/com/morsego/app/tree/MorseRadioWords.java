package com.morsego.app.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Common Ham Radio Words (Termos Comuns de Rádio CW).
 * Regras:
 * 1. Só fica disponível APÓS desbloquear todas as 26 letras do alfabeto (Nível 13 da Árvore Morse).
 * 2. Dividido estritamente em duas etapas separadas: OUVIR (Escuta) e MANDAR (Transmissão).
 */
public class MorseRadioWords {

    public static final int REQUIRED_LEVEL_FOR_RADIO_WORDS = 13; // Level 13 reaches Z and Q, completing A-Z

    public static final List<RadioWordItem> RADIO_WORDS = Collections.unmodifiableList(Arrays.asList(
            new RadioWordItem("CQ", "-.-. --.-", "Chamada geral para todas as estações"),
            new RadioWordItem("73", "--... ...--", "Cumprimentos e melhores votos em CW"),
            new RadioWordItem("DX", "-.. -..-", "Contacto com estação a longa distância"),
            new RadioWordItem("QSL", "--.- ... .-..", "Confirmação de receção / cartão QSL"),
            new RadioWordItem("QTH", "--.- - ....", "Localização geográfica da estação"),
            new RadioWordItem("RST", ".-. ... -", "Relatório de sinal (Readability-Signal-Tone)"),
            new RadioWordItem("SOS", "... --- ...", "Sinal internacional de socorro"),
            new RadioWordItem("TU", "- ..-", "Agradecimento telegráfico (Thank You)")
    ));

    public static class RadioWordItem {
        public final String word;
        public final String morse;
        public final String description;

        public RadioWordItem(String word, String morse, String description) {
            this.word = word;
            this.morse = morse;
            this.description = description;
        }
    }

    /**
     * Determines whether radio words are unlocked.
     * Must strictly have unlocked all letters in the Morse tree (Level >= 13).
     */
    public static boolean isUnlocked(int currentUnlockedLevel) {
        return currentUnlockedLevel >= REQUIRED_LEVEL_FOR_RADIO_WORDS;
    }

    /**
     * Verifies that the pool contains all 26 letters of the English alphabet.
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

    /**
     * Generates choices for the Ouvir (Listening) radio words test.
     */
    public static List<String> generateListeningChoices(String targetWord, int count) {
        List<String> choices = new ArrayList<>();
        choices.add(targetWord);

        List<RadioWordItem> shuffled = new ArrayList<>(RADIO_WORDS);
        Collections.shuffle(shuffled);

        for (RadioWordItem item : shuffled) {
            if (!choices.contains(item.word) && choices.size() < count) {
                choices.add(item.word);
            }
        }

        Collections.shuffle(choices);
        return choices;
    }
}
