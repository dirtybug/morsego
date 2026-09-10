package com.morsego.app.tree;

/**
 * Node in the Morse Code Binary Tree.
 * Going left represents a DIT (.), going right represents a DAH (-).
 */
public class MorseTreeNode {
    private final String character;
    private final String morseCode;
    private final int depth;
    private MorseTreeNode ditChild; // Left child (Dot)
    private MorseTreeNode dahChild; // Right child (Dash)
    private boolean isUnlocked;

    public MorseTreeNode(String character, String morseCode, int depth) {
        this.character = character;
        this.morseCode = morseCode;
        this.depth = depth;
        this.isUnlocked = false;
    }

    public String getCharacter() {
        return character;
    }

    public String getMorseCode() {
        return morseCode;
    }

    public int getDepth() {
        return depth;
    }

    public MorseTreeNode getDitChild() {
        return ditChild;
    }

    public void setDitChild(MorseTreeNode ditChild) {
        this.ditChild = ditChild;
    }

    public MorseTreeNode getDahChild() {
        return dahChild;
    }

    public void setDahChild(MorseTreeNode dahChild) {
        this.dahChild = dahChild;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }
}
