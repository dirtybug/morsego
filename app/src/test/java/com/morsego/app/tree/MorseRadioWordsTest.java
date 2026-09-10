package com.morsego.app.tree;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class MorseRadioWordsTest {

    @Test
    public void testRadioWordsLockedUntilAllLettersUnlocked() {
        // Levels 1 through 12 have not unlocked all 26 letters yet (Z and Q are unlocked at Level 13)
        for (int level = 1; level <= 12; level++) {
            assertFalse("Radio words must be locked at level " + level,
                    MorseRadioWords.isUnlocked(level));
        }

        // At level 13 (where Z and Q complete the alphabet) and higher, radio words are unlocked
        assertTrue("Radio words must be unlocked at level 13", MorseRadioWords.isUnlocked(13));
        assertTrue("Radio words must be unlocked at level 14", MorseRadioWords.isUnlocked(14));
        assertTrue("Radio words must be unlocked at level 21", MorseRadioWords.isUnlocked(21));
    }

    @Test
    public void testLevel13HasAllLetters() {
        MorseBinaryTree tree = MorseBinaryTree.getInstance();
        TreeLevel level13 = tree.getLevel(13);
        List<String> pool = level13.getAllCharacters();

        // Level 13 pool must contain all 26 letters of the English alphabet
        assertTrue("Level 13 must satisfy hasAllLettersUnlocked",
                MorseRadioWords.hasAllLettersUnlocked(pool));

        // Levels prior to 13 must NOT have all letters
        TreeLevel level12 = tree.getLevel(12);
        assertFalse("Level 12 does not have Z or Q yet",
                MorseRadioWords.hasAllLettersUnlocked(level12.getAllCharacters()));
    }

    @Test
    public void testRadioWordsListIntegrity() {
        List<MorseRadioWords.RadioWordItem> words = MorseRadioWords.getRadioWords();
        assertNotNull(words);
        assertTrue(words.size() >= 8);

        // Verify common radio words exist
        boolean hasCQ = false, has73 = false, hasDX = false, hasQSL = false, hasSOS = false;
        for (MorseRadioWords.RadioWordItem item : words) {
            if ("CQ".equals(item.word)) hasCQ = true;
            if ("73".equals(item.word)) has73 = true;
            if ("DX".equals(item.word)) hasDX = true;
            if ("QSL".equals(item.word)) hasQSL = true;
            if ("SOS".equals(item.word)) hasSOS = true;
        }

        assertTrue(hasCQ);
        assertTrue(has73);
        assertTrue(hasDX);
        assertTrue(hasQSL);
        assertTrue(hasSOS);
    }

    @Test
    public void testGenerateListeningChoices() {
        List<String> choices = MorseRadioWords.generateListeningChoices("CQ", 4);
        assertEquals(4, choices.size());
        assertTrue("Choices must contain the target radio word 'CQ'", choices.contains("CQ"));
    }
}
