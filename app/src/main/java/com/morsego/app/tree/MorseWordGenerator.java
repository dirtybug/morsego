package com.morsego.app.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MorseWordGenerator {

    // Common real words and ham radio vocabulary in Portuguese and international CW
    private static final List<String> DICTIONARY = Arrays.asList(
            // E, T
            "ET", "TE", "TEE", "TET", "ETTE", "TETE",
            // + I, A
            "AT", "IT", "TEA", "EAT", "ATE", "TIE", "TAI", "AIT", "TATE", "TA", "AI",
            // + N, M
            "NO", "ON", "MAN", "MEN", "NET", "TEN", "NAME", "MAIN", "MINT",
            "TEAM", "MINE", "TIME", "NOTE", "MATE", "MEAT", "NINE", "ITEM",
            "MAE", "MIM", "TEM", "TOM", "MAO", "NAO",
            // + S, U
            "SUN", "SET", "USE", "SUIT", "MUST", "SITE", "SAME", "MUSE",
            "TENT", "SEAT", "EAST", "STEM", "SUET", "MIST", "TEST", "SATE",
            "SIM", "SEM", "MAS", "TUA", "SEU", "MES", "SUMO",
            // + R, W
            "WAR", "RAW", "WIRE", "STAR", "REST", "TRUE", "WATER", "WRITE",
            "RAIN", "WEST", "WEAR", "WAIT", "WINE", "RATE", "RUST", "ROSE",
            "RUA", "REI", "RATO", "REDE", "RODA", "ROMA",
            // + D, K
            "DAY", "DARK", "DEAR", "KITE", "KING", "KNOW", "KIND",
            "ROAD", "DATE", "DINE", "DESK", "RISK", "DUST", "MAKE", "MARK",
            "DAR", "DIA", "DADO", "DEDO", "DOR",
            // + G, O
            "GOOD", "GOAT", "GAME", "GOLD", "OPEN", "OVER", "MOON", "ROOM",
            "MORE", "GONE", "DOG", "GATO", "GOTA", "OVO", "ORA", "ONDA",
            // + H, V
            "HOME", "HOPE", "HAVE", "VOTE", "VIEW", "VENTO", "VIDA", "HORA", "HOJE", "VELA",
            // + F, L
            "FIVE", "FAST", "FISH", "LOVE", "LINE", "LATE", "LAKE", "LAMP",
            "FALL", "LEAF", "LIFE", "FOGO", "FALA", "LUA", "LUZ", "LIVRO",
            // + P, J
            "PARK", "PORT", "PAGE", "JOIN", "JUMP", "JUST", "JULY", "JUNE",
            "PAZ", "PAI", "PAO", "PATO", "JOGO", "JATO",
            // + B, X
            "BOAT", "BLUE", "BEST", "BIRD", "BELL", "BOX", "NEXT", "TAXI",
            "BOCA", "BEM", "BOM", "BELO", "BAIXO",
            // + C, Y
            "CITY", "CALL", "COLD", "CARE", "YEAR", "YARD", "YES", "YOU",
            "CASA", "CAFE", "CEU", "COR", "CIMA",
            // + Z, Q
            "ZERO", "ZONE", "ZOOM", "QUIZ", "QUIT", "QUICK", "QUEEN",
            "QUE", "QUASE", "QUEM", "ZEBRA", "ZONA",
            // CW abbreviations
            "CQ", "DX", "QSL", "QTH", "RST", "TU", "TNX", "SOS", "SK", "AR", "GM", "GA", "GE", "RIG", "ANT", "DIT", "DAH"
    );

    /**
     * Checks if all letters in 'word' belong to 'allowedLetters'.
     */
    public static boolean canFormWord(String word, Set<String> allowedLetters) {
        for (int i = 0; i < word.length(); i++) {
            String c = String.valueOf(word.charAt(i)).toUpperCase();
            if (!allowedLetters.contains(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets valid words formed ONLY using the allowed letters.
     */
    public static List<String> getWordsForLetters(List<String> allowedLetters) {
        Set<String> set = new HashSet<>();
        for (String l : allowedLetters) {
            set.add(l.toUpperCase());
        }

        List<String> matches = new ArrayList<>();
        for (String w : DICTIONARY) {
            if (canFormWord(w, set)) {
                matches.add(w);
            }
        }

        // If no dictionary words or very few, generate synthetic words
        if (matches.size() < 4) {
            matches.addAll(generateSyntheticWords(allowedLetters, 6));
        }

        return matches;
    }

    /**
     * Generates synthetic combinations of 2 to 3 characters from allowed letters.
     */
    public static List<String> generateSyntheticWords(List<String> allowedLetters, int count) {
        List<String> list = new ArrayList<>();
        if (allowedLetters.isEmpty()) return list;

        int attempts = 0;
        while (list.size() < count && attempts < 100) {
            attempts++;
            int len = 2 + (int) (Math.random() * 2); // 2 or 3 letters
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < len; i++) {
                sb.append(allowedLetters.get((int) (Math.random() * allowedLetters.size())));
            }
            String w = sb.toString();
            if (!list.contains(w)) {
                list.add(w);
            }
        }
        return list;
    }

    /**
     * Generates an exam word sequence that strictly satisfies:
     * 1. Considers every previous & current unlocked letter in 'pool' at least once.
     * 2. Adds 25% extra words incorporating the user's most-failed letters.
     * 3. All words are randomized.
     */
    public static List<String> generateExamWordSequence(List<String> pool, List<String> mostFailedLetters) {
        List<String> validWords = getWordsForLetters(pool);
        Collections.shuffle(validWords);

        Set<String> uncovered = new HashSet<>(pool);
        List<String> chosenWords = new ArrayList<>();

        // 1. Pick words from dictionary that cover as many uncovered letters as possible
        for (String w : validWords) {
            boolean coversNew = false;
            for (int i = 0; i < w.length(); i++) {
                String c = String.valueOf(w.charAt(i)).toUpperCase();
                if (uncovered.contains(c)) {
                    coversNew = true;
                    break;
                }
            }
            if (coversNew) {
                chosenWords.add(w);
                for (int i = 0; i < w.length(); i++) {
                    uncovered.remove(String.valueOf(w.charAt(i)).toUpperCase());
                }
            }
            if (uncovered.isEmpty()) {
                break;
            }
        }

        // If any letters remain uncovered, synthesize words that specifically include them
        for (String missingChar : new ArrayList<>(uncovered)) {
            StringBuilder sb = new StringBuilder();
            sb.append(missingChar);
            int len = 2 + (int) (Math.random() * 2);
            for (int i = 1; i < len; i++) {
                sb.append(pool.get((int) (Math.random() * pool.size())));
            }
            List<Character> chars = new ArrayList<>();
            for (char c : sb.toString().toCharArray()) chars.add(c);
            Collections.shuffle(chars);
            StringBuilder shuffledWord = new StringBuilder();
            for (char c : chars) shuffledWord.append(c);

            chosenWords.add(shuffledWord.toString());
            uncovered.remove(missingChar);
        }

        // Ensure minimum sensible word count
        while (chosenWords.size() < 4) {
            if (!validWords.isEmpty()) {
                String extra = validWords.get((int) (Math.random() * validWords.size()));
                if (!chosenWords.contains(extra)) {
                    chosenWords.add(extra);
                } else {
                    chosenWords.add(generateSyntheticWords(pool, 1).get(0));
                }
            } else {
                chosenWords.add(generateSyntheticWords(pool, 1).get(0));
            }
        }

        // 2. Add 25% extra words targeting the most failed letters
        int extra25Percent = Math.max(1, (int) Math.round(chosenWords.size() * 0.25));
        for (int i = 0; i < extra25Percent; i++) {
            String targetFailedLetter = (mostFailedLetters != null && !mostFailedLetters.isEmpty())
                    ? mostFailedLetters.get(i % mostFailedLetters.size())
                    : pool.get((int) (Math.random() * pool.size()));

            String wordWithFailed = null;
            for (String w : validWords) {
                if (w.contains(targetFailedLetter) && !chosenWords.contains(w)) {
                    wordWithFailed = w;
                    break;
                }
            }
            if (wordWithFailed == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(targetFailedLetter);
                int len = 2 + (int) (Math.random() * 2);
                for (int j = 1; j < len; j++) {
                    sb.append(pool.get((int) (Math.random() * pool.size())));
                }
                wordWithFailed = sb.toString();
            }
            chosenWords.add(wordWithFailed);
        }

        Collections.shuffle(chosenWords);
        return chosenWords;
    }

    /**
     * Generates alternative word choices strictly from allowed letters for listening tests.
     */
    public static List<String> generateWordChoices(String targetWord, List<String> allowedLetters, int count) {
        List<String> validWords = getWordsForLetters(allowedLetters);
        List<String> options = new ArrayList<>();
        options.add(targetWord);

        Collections.shuffle(validWords);
        for (String w : validWords) {
            if (!options.contains(w) && options.size() < count) {
                options.add(w);
            }
        }

        // Fill remaining with synthetic words from allowed letters if needed
        while (options.size() < count) {
            List<String> syn = generateSyntheticWords(allowedLetters, 1);
            if (!syn.isEmpty() && !options.contains(syn.get(0))) {
                options.add(syn.get(0));
            } else {
                break;
            }
        }

        Collections.shuffle(options);
        return options;
    }
}
