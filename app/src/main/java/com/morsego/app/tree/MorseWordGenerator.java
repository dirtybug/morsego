package com.morsego.app.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Generates dynamic, randomized Morse words and code sequences on the fly.
 * Completely avoids static lists so students cannot memorize patterns, ensuring
 * authentic acoustic Morse decoding and transmission rhythm training.
 */
public class MorseWordGenerator {

    private static final Random RNG = new Random();

    /**
     * Generates a single dynamic word of random length between minLen and maxLen
     * formed strictly from allowed letters.
     */
    public static String generateDynamicWord(List<String> pool, int minLen, int maxLen) {
        if (pool == null || pool.isEmpty()) return "E";
        int len = minLen + RNG.nextInt(Math.max(1, maxLen - minLen + 1));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(pool.get(RNG.nextInt(pool.size())));
        }
        return sb.toString();
    }

    /**
     * Generates a dynamic word that is guaranteed to contain 'requiredChar'
     * at a randomized position, with other letters drawn randomly from 'pool'.
     */
    public static String generateWordWithLetter(String requiredChar, List<String> pool, int minLen, int maxLen) {
        if (pool == null || pool.isEmpty()) return requiredChar;
        int len = minLen + RNG.nextInt(Math.max(1, maxLen - minLen + 1));
        List<String> chars = new ArrayList<>();
        chars.add(requiredChar.toUpperCase());
        for (int i = 1; i < len; i++) {
            chars.add(pool.get(RNG.nextInt(pool.size())).toUpperCase());
        }
        Collections.shuffle(chars, RNG);
        StringBuilder sb = new StringBuilder();
        for (String c : chars) {
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Generates an exam word sequence dynamically satisfying:
     * 1. Never static: newly generated on every test run so user cannot memorize.
     * 2. Covers EVERY letter in 'pool' (all previous levels + current) at least once.
     * 3. Adds 25% extra dynamic words incorporating the user's most failed letters.
     * 4. Shuffles the final sequence.
     */
    public static List<String> generateExamWordSequence(List<String> pool, List<String> mostFailedLetters) {
        if (pool == null || pool.isEmpty()) {
            List<String> fallback = new ArrayList<>();
            fallback.add("ET");
            return fallback;
        }

        Set<String> uniqueWords = new HashSet<>();

        // Part 1: Ensure every single character in the level's pool appears at least once
        for (String letter : pool) {
            int attempts = 0;
            String word;
            do {
                int maxL = pool.size() <= 4 ? 3 : 4;
                word = generateWordWithLetter(letter, pool, 2, maxL);
                attempts++;
            } while (uniqueWords.contains(word) && attempts < 30);
            uniqueWords.add(word);
        }

        // Guarantee a minimum of 4 base words even on Level 1
        while (uniqueWords.size() < 4) {
            int maxL = pool.size() <= 2 ? 3 : 4;
            uniqueWords.add(generateDynamicWord(pool, 2, maxL));
        }

        // Part 2: Add 25% extra words based on the user's most-failed letters
        int baseCount = uniqueWords.size();
        int extraCount = Math.max(1, (int) Math.round(baseCount * 0.25));

        for (int i = 0; i < extraCount; i++) {
            String failedChar;
            if (mostFailedLetters != null && !mostFailedLetters.isEmpty()) {
                failedChar = mostFailedLetters.get(i % mostFailedLetters.size());
            } else {
                failedChar = pool.get(RNG.nextInt(pool.size()));
            }

            int attempts = 0;
            String extraWord;
            do {
                int maxL = pool.size() <= 4 ? 3 : 4;
                extraWord = generateWordWithLetter(failedChar, pool, 2, maxL);
                attempts++;
            } while (uniqueWords.contains(extraWord) && attempts < 30);
            uniqueWords.add(extraWord);
        }

        List<String> result = new ArrayList<>(uniqueWords);
        Collections.shuffle(result, RNG);
        return result;
    }

    /**
     * Generates alternative word choices for listening tests dynamically on the fly.
     * All distractors are formed strictly from 'allowedLetters' and have similar lengths,
     * randomized so the user cannot guess by pattern elimination or memorize button positions.
     */
    public static List<String> generateWordChoices(String targetWord, List<String> allowedLetters, int count) {
        List<String> choices = new ArrayList<>();
        choices.add(targetWord);

        int targetLen = targetWord.length();
        int attempts = 0;

        while (choices.size() < count && attempts < 150) {
            attempts++;
            int len = Math.max(2, targetLen + (RNG.nextBoolean() ? 0 : (RNG.nextBoolean() ? -1 : 1)));
            String distractor = generateDynamicWord(allowedLetters, len, len);
            if (!choices.contains(distractor)) {
                choices.add(distractor);
            }
        }

        Collections.shuffle(choices, RNG);
        return choices;
    }

    /**
     * Generates a random dynamic item (letter or word) from pool for penalty additions.
     */
    public static String generateRandomPenaltyItem(List<String> pool, boolean asWord) {
        if (asWord) {
            int maxL = pool.size() <= 4 ? 3 : 4;
            return generateDynamicWord(pool, 2, maxL);
        } else {
            return pool.get(RNG.nextInt(pool.size()));
        }
    }
}
