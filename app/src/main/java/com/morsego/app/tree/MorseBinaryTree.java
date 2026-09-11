package com.morsego.app.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Full International Morse Binary Tree data structure and level generator.
 * Traversing left corresponds to DIT (.), traversing right corresponds to DAH (-).
 */
public class MorseBinaryTree {
    private static MorseBinaryTree instance;

    private final MorseTreeNode root;
    private final Map<String, String> charToMorseMap;
    private final Map<String, String> morseToCharMap;
    private final List<TreeLevel> levels;

    private MorseBinaryTree() {
        this.root = new MorseTreeNode("START", "", 0);
        this.charToMorseMap = new HashMap<>();
        this.morseToCharMap = new HashMap<>();
        this.levels = new ArrayList<>();

        buildTree();
        buildLevels();
    }

    public static synchronized MorseBinaryTree getInstance() {
        if (instance == null) {
            instance = new MorseBinaryTree();
        }
        return instance;
    }

    private void buildTree() {
        // Depth 1
        MorseTreeNode nodeE = addNode(root, true, "E", ".");
        MorseTreeNode nodeT = addNode(root, false, "T", "-");

        // Depth 2 (under E and T)
        MorseTreeNode nodeI = addNode(nodeE, true, "I", "..");
        MorseTreeNode nodeA = addNode(nodeE, false, "A", ".-");
        MorseTreeNode nodeN = addNode(nodeT, true, "N", "-.");
        MorseTreeNode nodeM = addNode(nodeT, false, "M", "--");

        // Depth 3 (under I, A, N, M)
        MorseTreeNode nodeS = addNode(nodeI, true, "S", "...");
        MorseTreeNode nodeU = addNode(nodeI, false, "U", "..-");
        MorseTreeNode nodeR = addNode(nodeA, true, "R", ".-.");
        MorseTreeNode nodeW = addNode(nodeA, false, "W", ".--");
        MorseTreeNode nodeD = addNode(nodeN, true, "D", "-..");
        MorseTreeNode nodeK = addNode(nodeN, false, "K", "-.-");
        MorseTreeNode nodeG = addNode(nodeM, true, "G", "--.");
        MorseTreeNode nodeO = addNode(nodeM, false, "O", "---");

        // Depth 4
        MorseTreeNode nodeH = addNode(nodeS, true, "H", "....");
        MorseTreeNode nodeV = addNode(nodeS, false, "V", "...-");
        MorseTreeNode nodeF = addNode(nodeU, true, "F", "..-.");
        MorseTreeNode nodeL = addNode(nodeR, true, "L", ".-..");
        MorseTreeNode nodeP = addNode(nodeW, true, "P", ".--.");
        MorseTreeNode nodeJ = addNode(nodeW, false, "J", ".---");
        MorseTreeNode nodeB = addNode(nodeD, true, "B", "-...");
        MorseTreeNode nodeX = addNode(nodeD, false, "X", "-..-");
        MorseTreeNode nodeC = addNode(nodeK, true, "C", "-.-.");
        MorseTreeNode nodeY = addNode(nodeK, false, "Y", "-.--");
        MorseTreeNode nodeZ = addNode(nodeG, true, "Z", "--..");
        MorseTreeNode nodeQ = addNode(nodeG, false, "Q", "--.-");

        // Depth 5 (Numbers)
        addNode(nodeH, true, "5", ".....");
        addNode(nodeH, false, "4", "....-");
        addNode(nodeV, false, "3", "...--");
        addNode(nodeU, false, "2", "..---"); // standard morse maps 2 under ..---
        addNode(nodeJ, false, "1", ".----");
        addNode(nodeB, true, "6", "-....");
        addNode(nodeZ, true, "7", "--...");
        addNode(nodeO, true, "8", "---..");
        MorseTreeNode node9 = addNode(nodeO, false, "9", "----.");
        addNode(node9, false, "0", "-----");

        // Additional symbols & prosigns in lookup map
        registerChar(".", ".-.-.-");
        registerChar(",", "--..--");
        registerChar("?", "..--..");
        registerChar("/", "-..-.");
        registerChar("SOS", "...---...");
        registerChar("AR", ".-.-.");
        registerChar("SK", "...-.-");
        registerChar("BT", "-...-");
    }

    private MorseTreeNode addNode(MorseTreeNode parent, boolean isDit, String character, String morse) {
        int depth = parent.getDepth() + 1;
        MorseTreeNode node = new MorseTreeNode(character, morse, depth);
        if (isDit) {
            parent.setDitChild(node);
        } else {
            parent.setDahChild(node);
        }
        registerChar(character, morse);
        return node;
    }

    private void registerChar(String character, String morse) {
        charToMorseMap.put(character.toUpperCase(), morse);
        morseToCharMap.put(morse, character.toUpperCase());
    }

    private void buildLevels() {
        List<String> pool = new ArrayList<>();

        // Level 1: Root branches (depth 1): T (-) and E (.)
        TreeLevel l1 = new TreeLevel(1, "T", "-", "E", ".", pool,
                "Level 1: Tree Root (T and E)",
                "Begin with the two fundamental root blocks: T (-) and E (.).",
                "Nível 1: A Raiz da Árvore (T e E)",
                "Comece com os dois blocos fundamentais: T (-) e E (.).");
        levels.add(l1);
        pool = l1.getAllCharacters();

        // Level 2: Sub-branches under E: A (.-) and I (..)
        TreeLevel l2 = new TreeLevel(2, "A", ".-", "I", "..", pool,
                "Level 2: Branches of E (A and I)",
                "Traversing branches under E (.): A (.-) and I (..).",
                "Nível 2: Ramos do E (A e I)",
                "Descendo pelos ramos do E (.): A (.-) e I (..).");
        levels.add(l2);
        pool = l2.getAllCharacters();

        // Level 3: Sub-branches under T: M (--) and N (-.)
        TreeLevel l3 = new TreeLevel(3, "M", "--", "N", "-.", pool,
                "Level 3: Branches of T (M and N)",
                "Traversing branches under T (-): M (--) and N (-.).",
                "Nível 3: Ramos do T (M e N)",
                "Descendo pelos ramos do T (-): M (--) e N (-.).");
        levels.add(l3);
        pool = l3.getAllCharacters();

        // Level 4: Sub-branch under I (..)
        TreeLevel l4 = new TreeLevel(4, "S", "...", "U", "..-", pool,
                "Level 4: Branch of I (S and U)",
                "Expanding to depth 3: S (...) and U (..-).",
                "Nível 4: Ramo do I (S e U)",
                "Expandindo para profundidade 3: S (...) e U (..-).");
        levels.add(l4);
        pool = l4.getAllCharacters();

        // Level 5: Sub-branch under A (.-)
        TreeLevel l5 = new TreeLevel(5, "R", ".-.", "W", ".--", pool,
                "Level 5: Branch of A (R and W)",
                "Adding R (.-.) and W (.--).",
                "Nível 5: Ramo do A (R e W)",
                "Adicionando R (.-.) e W (.--).");
        levels.add(l5);
        pool = l5.getAllCharacters();

        // Level 6: Sub-branch under N (-.)
        TreeLevel l6 = new TreeLevel(6, "D", "-..", "K", "-.-", pool,
                "Level 6: Branch of N (D and K)",
                "Adding D (-..) and K (-.-).",
                "Nível 6: Ramo do N (D e K)",
                "Adicionando D (-..) e K (-.-).");
        levels.add(l6);
        pool = l6.getAllCharacters();

        // Level 7: Sub-branch under M (--)
        TreeLevel l7 = new TreeLevel(7, "G", "--.", "O", "---", pool,
                "Level 7: Branch of M (G and O)",
                "Completing depth 3: G (--.) and O (---).",
                "Nível 7: Ramo do M (G e O)",
                "Completando a profundidade 3: G (--.) e O (---).");
        levels.add(l7);
        pool = l7.getAllCharacters();

        // Level 8: Sub-branch under S (...)
        TreeLevel l8 = new TreeLevel(8, "H", "....", "V", "...-", pool,
                "Level 8: Branch of S (H and V)",
                "Starting depth 4: H (....) and V (...-).",
                "Nível 8: Ramo do S (H e V)",
                "Iniciando a profundidade 4: H (....) e V (...-).");
        levels.add(l8);
        pool = l8.getAllCharacters();

        // Level 9: Sub-branches under U (..-) and R (.-.)
        TreeLevel l9 = new TreeLevel(9, "F", "..-.", "L", ".-..", pool,
                "Level 9: Branches of U and R (F and L)",
                "Adding F (..-.) and L (.-..).",
                "Nível 9: Ramos de U e R (F e L)",
                "Adicionando F (..-.) e L (.-..).");
        levels.add(l9);
        pool = l9.getAllCharacters();

        // Level 10: Sub-branch under W (.--)
        TreeLevel l10 = new TreeLevel(10, "P", ".--.", "J", ".---", pool,
                "Level 10: Branch of W (P and J)",
                "Adding P (.--.) and J (.---).",
                "Nível 10: Ramo do W (P e J)",
                "Adicionando P (.--.) e J (.---).");
        levels.add(l10);
        pool = l10.getAllCharacters();

        // Level 11: Sub-branch under D (-..)
        TreeLevel l11 = new TreeLevel(11, "B", "-...", "X", "-..-", pool,
                "Level 11: Branch of D (B and X)",
                "Adding B (-...) and X (-..-).",
                "Nível 11: Ramo do D (B e X)",
                "Adicionando B (-...) e X (-..-).");
        levels.add(l11);
        pool = l11.getAllCharacters();

        // Level 12: Sub-branch under K (-.-)
        TreeLevel l12 = new TreeLevel(12, "C", "-.-.", "Y", "-.--", pool,
                "Level 12: Branch of K (C and Y)",
                "Adding C (-.-.) and Y (-.--).",
                "Nível 12: Ramo do K (C e Y)",
                "Adicionando C (-.-.) e Y (-.--).");
        levels.add(l12);
        pool = l12.getAllCharacters();

        // Level 13: Sub-branch under G (--.)
        TreeLevel l13 = new TreeLevel(13, "Z", "--..", "Q", "--.-", pool,
                "Level 13: Branch of G (Z and Q) - Full Alphabet Complete!",
                "Completing the entire A-Z alphabet: Z (--..) and Q (--.-).",
                "Nível 13: Ramo do G (Z e Q) - Alfabeto Completo!",
                "Completando todo o alfabeto (A-Z): Z (--..) e Q (--.-).");
        levels.add(l13);
        pool = l13.getAllCharacters();

        // Level 14: Numbers 5 and 4
        TreeLevel l14 = new TreeLevel(14, "5", ".....", "4", "....-", pool,
                "Level 14: Digits (5 and 4)",
                "Beginning 5-element digits: 5 (.....) and 4 (....-).",
                "Nível 14: Números (5 e 4)",
                "Iniciando os números de 5 elementos: 5 (.....) e 4 (....-).");
        levels.add(l14);
        pool = l14.getAllCharacters();

        // Level 15: Numbers 3 and 2
        TreeLevel l15 = new TreeLevel(15, "3", "...--", "2", "..---", pool,
                "Level 15: Digits (3 and 2)",
                "Adding digits: 3 (...--) and 2 (..---).",
                "Nível 15: Números (3 e 2)",
                "Adicionando 3 (...--) e 2 (..---).");
        levels.add(l15);
        pool = l15.getAllCharacters();

        // Level 16: Numbers 1 and 6
        TreeLevel l16 = new TreeLevel(16, "1", ".----", "6", "-....", pool,
                "Level 16: Digits (1 and 6)",
                "Adding digits: 1 (.----) and 6 (-....).",
                "Nível 16: Números (1 e 6)",
                "Adicionando 1 (.----) e 6 (-....).");
        levels.add(l16);
        pool = l16.getAllCharacters();

        // Level 17: Numbers 7 and 8
        TreeLevel l17 = new TreeLevel(17, "7", "--...", "8", "---..", pool,
                "Level 17: Digits (7 and 8)",
                "Adding digits: 7 (--...) and 8 (---..).",
                "Nível 17: Números (7 e 8)",
                "Adicionando 7 (--...) e 8 (---..).");
        levels.add(l17);
        pool = l17.getAllCharacters();

        // Level 18: Numbers 9 and 0
        TreeLevel l18 = new TreeLevel(18, "9", "----.", "0", "-----", pool,
                "Level 18: Digits (9 and 0) - All Digits Complete!",
                "Completing all 10 digits: 9 (----.) and 0 (-----).",
                "Nível 18: Números (9 e 0) - Algarismos Completos!",
                "Completando todos os 10 algarismos: 9 (----.) e 0 (-----).");
        levels.add(l18);
        pool = l18.getAllCharacters();

        // Level 19: Punctuation . and ,
        TreeLevel l19 = new TreeLevel(19, ".", ".-.-.-", ",", "--..--", pool,
                "Level 19: Punctuation (. and ,)",
                "Telegraphic periods and commas: . (.-.-.-) and , (--..--).",
                "Nível 19: Pontuação (. e ,)",
                "Pontos e vírgulas telegráficas: . (.-.-.-) e , (--..--).");
        levels.add(l19);
        pool = l19.getAllCharacters();

        // Level 20: Punctuation ? and /
        TreeLevel l20 = new TreeLevel(20, "?", "..--..", "/", "-..-.", pool,
                "Level 20: Punctuation (? and /)",
                "Question mark (..--..) and slash (-..-.).",
                "Nível 20: Pontuação (? e /)",
                "Interrogação (..--..) e barra (-..-.).");
        levels.add(l20);
        pool = l20.getAllCharacters();

        // Level 21: Prosigns SOS and SK
        TreeLevel l21 = new TreeLevel(21, "SOS", "...---...", "SK", "...-.-", pool,
                "Level 21: Prosigns (SOS and SK) - Full Tree Complete!",
                "Distress call SOS (...---...) and End of Work SK (...-.-).",
                "Nível 21: Sinais Especiais (SOS e SK) - Árvore 100% Completa!",
                "Chamada de socorro SOS (...---...) e fim de transmissão SK (...-.-).");
        levels.add(l21);
    }

    public MorseTreeNode getRoot() {
        return root;
    }

    public List<TreeLevel> getLevels() {
        return levels;
    }

    public TreeLevel getLevel(int levelNumber) {
        if (levelNumber >= 1 && levelNumber <= levels.size()) {
            return levels.get(levelNumber - 1);
        }
        return levels.get(0);
    }

    public int getTotalLevels() {
        return levels.size();
    }

    public String getMorse(String character) {
        return charToMorseMap.get(character.toUpperCase());
    }

    public String getChar(String morse) {
        return morseToCharMap.get(morse.trim());
    }

    /**
     * Traverses the tree based on a Morse sequence (e.g. ".-")
     */
    public MorseTreeNode traverse(String morsePattern) {
        MorseTreeNode current = root;
        for (int i = 0; i < morsePattern.length(); i++) {
            char c = morsePattern.charAt(i);
            if (c == '.') {
                if (current.getDitChild() != null) {
                    current = current.getDitChild();
                } else {
                    return null;
                }
            } else if (c == '-') {
                if (current.getDahChild() != null) {
                    current = current.getDahChild();
                } else {
                    return null;
                }
            }
        }
        return current;
    }

    /**
     * Finds a tree node by its character representation.
     */
    public MorseTreeNode findNodeByCharacter(String character) {
        if (character == null) return null;
        if ("START".equalsIgnoreCase(character)) return root;
        String morse = getMorse(character);
        if (morse == null) return null;
        return traverse(morse);
    }

    /**
     * Updates unlocked flag for nodes based on user's current level
     */
    public void unlockNodesUpToLevel(int level) {
        if (level < 1) level = 1;
        TreeLevel treeLevel = getLevel(Math.min(level, levels.size()));
        List<String> unlockedChars = treeLevel.getAllCharacters();

        unlockRecursive(root, unlockedChars);
    }

    private void unlockRecursive(MorseTreeNode node, List<String> unlockedChars) {
        if (node == null) return;
        node.setUnlocked(unlockedChars.contains(node.getCharacter()));
        unlockRecursive(node.getDitChild(), unlockedChars);
        unlockRecursive(node.getDahChild(), unlockedChars);
    }
}
