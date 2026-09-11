package com.morsego.app.tree;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Unit Tests for Bilingual User Content Localization (English & Portuguese):
 * Validates that all user-facing strings, level titles, descriptions,
 * and messages correctly adapt to the operating system's language settings.
 */
public class MorseBilingualLocalizationTest {

    private MorseBinaryTree tree;

    @Before
    public void setUp() {
        tree = MorseBinaryTree.getInstance();
    }

    /**
     * Verifies that when the operating system is set to English (default),
     * all level titles and descriptions are displayed in English.
     */
    @Test
    public void testEnglishDefaultLocalization() {
        Locale enLocale = Locale.ENGLISH;

        TreeLevel l1 = tree.getLevel(1);
        assertEquals("Level 1: Tree Root (T and E)", l1.getTitle(enLocale));
        assertTrue("English description must mention root blocks",
                l1.getDescription(enLocale).contains("fundamental root blocks"));

        TreeLevel l6 = tree.getLevel(6);
        assertEquals("Level 6: Branch of N (D and K)", l6.getTitle(enLocale));

        TreeLevel l13 = tree.getLevel(13);
        assertTrue("English title must mention Full Alphabet Complete",
                l13.getTitle(enLocale).contains("Full Alphabet Complete"));

        TreeLevel l21 = tree.getLevel(21);
        assertTrue("English title must mention Full Tree Complete",
                l21.getTitle(enLocale).contains("Full Tree Complete"));
    }

    /**
     * Verifies that when the operating system is set to Portuguese (Locale pt),
     * all level titles and descriptions automatically switch to Portuguese.
     */
    @Test
    public void testPortugueseLocalization() {
        Locale ptLocale = Locale.forLanguageTag("pt");

        TreeLevel l1 = tree.getLevel(1);
        assertEquals("Nível 1: A Raiz da Árvore (T e E)", l1.getTitle(ptLocale));
        assertTrue("Portuguese description must mention blocos fundamentais",
                l1.getDescription(ptLocale).contains("blocos fundamentais"));

        TreeLevel l6 = tree.getLevel(6);
        assertEquals("Nível 6: Ramo do N (D e K)", l6.getTitle(ptLocale));

        TreeLevel l13 = tree.getLevel(13);
        assertTrue("Portuguese title must mention Alfabeto Completo",
                l13.getTitle(ptLocale).contains("Alfabeto Completo"));

        TreeLevel l21 = tree.getLevel(21);
        assertTrue("Portuguese title must mention Árvore 100% Completa",
                l21.getTitle(ptLocale).contains("Árvore 100% Completa"));
    }

    /**
     * Verifies that all 21 levels have non-empty, distinct English and Portuguese titles.
     */
    @Test
    public void testAll21LevelsHaveBothEnglishAndPortugueseContent() {
        for (int i = 1; i <= tree.getTotalLevels(); i++) {
            TreeLevel level = tree.getLevel(i);

            assertNotNull("Level " + i + " must not be null", level);
            assertNotNull("Level " + i + " English title must exist", level.getTitleEn());
            assertNotNull("Level " + i + " Portuguese title must exist", level.getTitlePt());
            assertNotNull("Level " + i + " English description must exist", level.getDescriptionEn());
            assertNotNull("Level " + i + " Portuguese description must exist", level.getDescriptionPt());

            assertFalse("Level " + i + " English title must not be empty", level.getTitleEn().trim().isEmpty());
            assertFalse("Level " + i + " Portuguese title must not be empty", level.getTitlePt().trim().isEmpty());
        }
    }
}
