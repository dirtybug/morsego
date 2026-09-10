package com.morsego.app.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a progression level in the Morse Binary Tree.
 * Every level adds exactly 2 new characters following the binary tree branches,
 * and maintains the cumulative pool of all characters learned so far.
 */
public class TreeLevel {
    private final int levelNumber;
    private final String char1;
    private final String morse1;
    private final String char2;
    private final String morse2;
    private final List<String> newCharacters;
    private final List<String> allCharacters;
    private final String title;
    private final String description;

    public TreeLevel(int levelNumber, String char1, String morse1, String char2, String morse2,
                     List<String> previousCharacters, String title, String description) {
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

        this.title = title;
        this.description = description;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public String getChar1() {
        return char1;
    }

    public String getMorse1() {
        return morse1;
    }

    public String getChar2() {
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

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getRandomCharacterFromPool() {
        if (allCharacters.isEmpty()) return char1;
        int idx = (int) (Math.random() * allCharacters.size());
        return allCharacters.get(idx);
    }
}
