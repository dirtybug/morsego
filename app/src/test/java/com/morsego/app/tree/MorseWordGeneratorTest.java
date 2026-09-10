package com.morsego.app.tree;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class MorseWordGeneratorTest {

    @Test
    public void testDynamicWordGenerationOnlyUsesPool() {
        List<String> pool = Arrays.asList("E", "T");
        for (int i = 0; i < 20; i++) {
            String word = MorseWordGenerator.generateDynamicWord(pool, 2, 4);
            assertNotNull(word);
            assertTrue(word.length() >= 2 && word.length() <= 4);
            for (char c : word.toCharArray()) {
                assertTrue("Character " + c + " must be in pool", pool.contains(String.valueOf(c)));
            }
        }
    }

    @Test
    public void testWordWithLetterGuaranteesLetter() {
        List<String> pool = Arrays.asList("E", "T", "I", "A");
        for (int i = 0; i < 20; i++) {
            String word = MorseWordGenerator.generateWordWithLetter("A", pool, 3, 5);
            assertNotNull(word);
            assertTrue("Word must contain 'A'", word.contains("A"));
            for (char c : word.toCharArray()) {
                assertTrue("Character " + c + " must be in pool", pool.contains(String.valueOf(c)));
            }
        }
    }

    @Test
    public void testGenerateWordsWithNewLetters() {
        List<String> pool = Arrays.asList("E", "T", "I", "A");
        List<String> words = MorseWordGenerator.generateWordsWithNewLetters("I", "A", pool, 4);
        assertEquals(4, words.size());
        for (String w : words) {
            assertTrue("Each word must contain either I or A", w.contains("I") || w.contains("A"));
        }
    }

    @Test
    public void testGenerateExamWordSequenceCoversAllPool() {
        List<String> pool = Arrays.asList("E", "T", "I", "A");
        List<String> examWords = MorseWordGenerator.generateExamWordSequence(pool, Arrays.asList("I"));
        assertNotNull(examWords);
        assertTrue(examWords.size() >= pool.size());

        // Verify that all pool characters appear at least once across generated words
        for (String charInPool : pool) {
            boolean found = false;
            for (String word : examWords) {
                if (word.contains(charInPool)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Letter " + charInPool + " must be covered in exam sequence", found);
        }
    }

    @Test
    public void testGenerateWordChoices() {
        List<String> pool = Arrays.asList("E", "T", "I", "A");
        List<String> choices = MorseWordGenerator.generateWordChoices("TEA", pool, 4);
        assertEquals(4, choices.size());
        assertTrue("Choices must contain the target word", choices.contains("TEA"));
    }

    @Test
    public void testGenerateRandomPenaltyItem() {
        List<String> pool = Arrays.asList("E", "T", "I");
        String penaltyChar = MorseWordGenerator.generateRandomPenaltyItem(pool, false);
        assertTrue(pool.contains(penaltyChar));

        String penaltyWord = MorseWordGenerator.generateRandomPenaltyItem(pool, true);
        assertNotNull(penaltyWord);
        assertTrue(penaltyWord.length() >= 2);
    }
}
