package com.morsego.app.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Represents a progression level in the Morse Binary Tree.
 * Every level adds exactly 2 new characters following the binary tree branches,
 * and maintains the cumulative pool of all characters learned so far.
 * Supports bilingual user content (English default and Portuguese localization).
 */
public class TreeLevel {
    private final int levelNumber;
    private final String char1;
    private final String morse1;
    private final String char2;
    private final String morse2;
    private final List<String> newCharacters;
    private final List<String> allCharacters;
    private final String titleEn;
    private final String descriptionEn;
    private final String titlePt;
    private final String descriptionPt;

    public TreeLevel(int levelNumber, String char1, String morse1, String char2, String morse2,
                     List<String> previousCharacters,
                     String titleEn, String descriptionEn,
                     String titlePt, String descriptionPt) {
        this.levelNumber = levelNumber;
        this.char1 = char1;
        this.morse1 = morse1;
        this.char2 = char2;
        this.morse2 = morse2;

        this.newCharacters = new ArrayList<>();
        this.newCharacters.add(char1);
        this.newCharacters.add(char2);

        this.allCharacters = new ArrayList<>(previousCharacters);
        this.allCharacters.add(char1);
        this.allCharacters.add(char2);

        this.titleEn = titleEn;
        this.descriptionEn = descriptionEn;
        this.titlePt = titlePt;
        this.descriptionPt = descriptionPt;
    }

    public TreeLevel(int levelNumber, String char1, String morse1, String char2, String morse2,
                     List<String> previousCharacters, String title, String description) {
        this(levelNumber, char1, morse1, char2, morse2, previousCharacters,
                title, description, title, description);
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public String getChar1() {
        return char1;
    }

    public String getNewChar1() {
        return char1;
    }

    public String getMorse1() {
        return morse1;
    }

    public String getChar2() {
        return char2;
    }

    public String getNewChar2() {
        return char2;
    }

    public String getMorse2() {
        return morse2;
    }

    public List<String> getNewCharacters() {
        return Collections.unmodifiableList(newCharacters);
    }

    public List<String> getAllCharacters() {
        return Collections.unmodifiableList(allCharacters);
    }

    /**
     * Returns the level title localized according to the system default Locale.
     */
    public String getTitle() {
        return isPortuguese(Locale.getDefault()) ? titlePt : titleEn;
    }

    public String getTitle(Locale locale) {
        return isPortuguese(locale) ? titlePt : titleEn;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public String getTitlePt() {
        return titlePt;
    }

    /**
     * Returns the level description localized according to the system default Locale.
     */
    public String getDescription() {
        return isPortuguese(Locale.getDefault()) ? descriptionPt : descriptionEn;
    }

    public String getDescription(Locale locale) {
        return isPortuguese(locale) ? descriptionPt : descriptionEn;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public String getDescriptionPt() {
        return descriptionPt;
    }

    private boolean isPortuguese(Locale locale) {
        return locale != null && locale.getLanguage().equalsIgnoreCase("pt");
    }

    public String getRandomCharacterFromPool() {
        if (allCharacters.isEmpty()) return char1;
        int idx = (int) (Math.random() * allCharacters.size());
        return allCharacters.get(idx);
    }
}
