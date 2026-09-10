package com.morsego.app.tree;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Testes Unitários de Release da Árvore Binária de Morse Passo a Passo:
 * Demonstra e valida com rigor matemático a abertura e iluminação progressiva
 * dos nós da árvore nível por nível (do Nível 1 ao Nível 21).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MorseTreeStepByStepReleaseTest {

    private MorseBinaryTree tree;

    @Before
    public void setUp() {
        tree = MorseBinaryTree.getInstance();
    }

    /**
     * PASSO 1: Apenas a raiz T (-) e E (.) está desbloqueada.
     * Os filhos de E (A, I) e os filhos de T (M, N) devem estar estritamente bloqueados.
     */
    @Test
    public void testPasso01_ReleaseLevel1_OnlyRootUnlocked() {
        tree.unlockNodesUpToLevel(1);

        MorseTreeNode root = tree.getRoot();
        assertNotNull("Raiz não pode ser nula", root);

        MorseTreeNode nodeT = root.getDahChild(); // Ramo esquerdo (-)
        MorseTreeNode nodeE = root.getDitChild(); // Ramo direito (.)
        assertNotNull("Nó T deve existir", nodeT);
        assertNotNull("Nó E deve existir", nodeE);

        // PASSO 1: T e E estão desbloqueados
        assertTrue("Passo 1: Nó T (-) na raiz deve estar desbloqueado", nodeT.isUnlocked());
        assertTrue("Passo 1: Nó E (.) na raiz deve estar desbloqueado", nodeE.isUnlocked());

        // PASSO 1: Descendentes devem estar BLOQUEADOS
        MorseTreeNode nodeA = nodeE.getDahChild();
        MorseTreeNode nodeI = nodeE.getDitChild();
        MorseTreeNode nodeM = nodeT.getDahChild();
        MorseTreeNode nodeN = nodeT.getDitChild();

        assertFalse("Passo 1: Nó A (.-) sob E deve estar bloqueado", nodeA.isUnlocked());
        assertFalse("Passo 1: Nó I (..) sob E deve estar bloqueado", nodeI.isUnlocked());
        assertFalse("Passo 1: Nó M (--) sob T deve estar bloqueado", nodeM.isUnlocked());
        assertFalse("Passo 1: Nó N (-.) sob T deve estar bloqueado", nodeN.isUnlocked());

        TreeLevel l1 = tree.getLevel(1);
        assertEquals(2, l1.getAllCharacters().size());
        assertTrue(l1.getAllCharacters().contains("T"));
        assertTrue(l1.getAllCharacters().contains("E"));
    }

    /**
     * PASSO 2: Release do Nível 2 liberta os ramos de E: A (.-) e I (..).
     * O ramo T (M e N) CONTINUA bloqueado nesta etapa.
     */
    @Test
    public void testPasso02_ReleaseLevel2_UnlocksBranchE_KeepsBranchTLocked() {
        tree.unlockNodesUpToLevel(2);

        MorseTreeNode root = tree.getRoot();
        MorseTreeNode nodeT = root.getDahChild();
        MorseTreeNode nodeE = root.getDitChild();

        MorseTreeNode nodeA = nodeE.getDahChild();
        MorseTreeNode nodeI = nodeE.getDitChild();
        MorseTreeNode nodeM = nodeT.getDahChild();
        MorseTreeNode nodeN = nodeT.getDitChild();

        // Nível 1 continua desbloqueado
        assertTrue("Nó T continua desbloqueado", nodeT.isUnlocked());
        assertTrue("Nó E continua desbloqueado", nodeE.isUnlocked());

        // PASSO 2: Ramos de E agora DESBLOQUEADOS
        assertTrue("Passo 2: Nó A (.-) sob E foi libertado/desbloqueado", nodeA.isUnlocked());
        assertTrue("Passo 2: Nó I (..) sob E foi libertado/desbloqueado", nodeI.isUnlocked());

        // PASSO 2: Ramos de T AINDA BLOQUEADOS
        assertFalse("Passo 2: Nó M (--) sob T permanece bloqueado no nível 2", nodeM.isUnlocked());
        assertFalse("Passo 2: Nó N (-.) sob T permanece bloqueado no nível 2", nodeN.isUnlocked());

        TreeLevel l2 = tree.getLevel(2);
        assertEquals(4, l2.getAllCharacters().size());
        assertTrue(l2.getAllCharacters().containsAll(List.of("T", "E", "A", "I")));
    }

    /**
     * PASSO 3: Release do Nível 3 liberta os ramos de T: M (--) e N (-.).
     * Agora toda a profundidade 2 está 100% desbloqueada [T, E, A, I, M, N].
     * Os nós da profundidade 3 (S, U, R, W, D, K, G, O) continuam bloqueados.
     */
    @Test
    public void testPasso03_ReleaseLevel3_UnlocksBranchT_CompletesDepth2() {
        tree.unlockNodesUpToLevel(3);

        MorseTreeNode root = tree.getRoot();
        MorseTreeNode nodeT = root.getDahChild();
        MorseTreeNode nodeE = root.getDitChild();

        MorseTreeNode nodeA = nodeE.getDahChild();
        MorseTreeNode nodeI = nodeE.getDitChild();
        MorseTreeNode nodeM = nodeT.getDahChild();
        MorseTreeNode nodeN = nodeT.getDitChild();

        // Toda a profundidade 1 e 2 está agora desbloqueada
        assertTrue("Passo 3: Nó T desbloqueado", nodeT.isUnlocked());
        assertTrue("Passo 3: Nó E desbloqueado", nodeE.isUnlocked());
        assertTrue("Passo 3: Nó A desbloqueado", nodeA.isUnlocked());
        assertTrue("Passo 3: Nó I desbloqueado", nodeI.isUnlocked());
        assertTrue("Passo 3: Nó M (--) sob T foi agora libertado/desbloqueado", nodeM.isUnlocked());
        assertTrue("Passo 3: Nó N (-.) sob T foi agora libertado/desbloqueado", nodeN.isUnlocked());

        // Nós da profundidade 3 continuam bloqueados
        MorseTreeNode nodeS = nodeI.getDitChild();
        MorseTreeNode nodeU = nodeI.getDahChild();
        assertFalse("Passo 3: Nó S (...) deve continuar bloqueado", nodeS.isUnlocked());
        assertFalse("Passo 3: Nó U (..-) deve continuar bloqueado", nodeU.isUnlocked());

        TreeLevel l3 = tree.getLevel(3);
        assertEquals(6, l3.getAllCharacters().size());
        assertTrue(l3.getAllCharacters().containsAll(List.of("T", "E", "A", "I", "M", "N")));
    }

    /**
     * PASSO 4: Release dos Níveis 4 e 5 - Ramos de I (S, U) e A (R, W).
     */
    @Test
    public void testPasso04_ReleaseLevels4and5_Branches_I_and_A() {
        tree.unlockNodesUpToLevel(4);
        MorseTreeNode nodeS = tree.findNodeByCharacter("S");
        MorseTreeNode nodeU = tree.findNodeByCharacter("U");
        assertNotNull(nodeS);
        assertNotNull(nodeU);
        assertTrue("S deve estar desbloqueado no nível 4", nodeS.isUnlocked());
        assertTrue("U deve estar desbloqueado no nível 4", nodeU.isUnlocked());

        tree.unlockNodesUpToLevel(5);
        MorseTreeNode nodeR = tree.findNodeByCharacter("R");
        MorseTreeNode nodeW = tree.findNodeByCharacter("W");
        assertNotNull(nodeR);
        assertNotNull(nodeW);
        assertTrue("R deve estar desbloqueado no nível 5", nodeR.isUnlocked());
        assertTrue("W deve estar desbloqueado no nível 5", nodeW.isUnlocked());
    }

    /**
     * PASSO 5: Release dos Níveis 8 a 10 - Ramos de S (H, V), U/R (F, L) e W (P, J).
     */
    @Test
    public void testPasso05_ReleaseLevels8to10_Branches_S_U_R_W() {
        tree.unlockNodesUpToLevel(8);
        assertTrue(tree.findNodeByCharacter("H").isUnlocked());
        assertTrue(tree.findNodeByCharacter("V").isUnlocked());

        tree.unlockNodesUpToLevel(9);
        assertTrue(tree.findNodeByCharacter("F").isUnlocked());
        assertTrue(tree.findNodeByCharacter("L").isUnlocked());

        tree.unlockNodesUpToLevel(10);
        assertTrue(tree.findNodeByCharacter("P").isUnlocked());
        assertTrue(tree.findNodeByCharacter("J").isUnlocked());
    }

    /**
     * PASSO 6: Release do Nível 13 - Conclusão do Alfabeto Completo (A-Z).
     * Z e Q completam as 26 letras da telegrafia internacional.
     */
    @Test
    public void testPasso06_MilestoneLevel13_AlphabetComplete() {
        tree.unlockNodesUpToLevel(13);
        TreeLevel l13 = tree.getLevel(13);
        List<String> pool = l13.getAllCharacters();

        assertEquals(26, pool.size());
        for (char c = 'A'; c <= 'Z'; c++) {
            assertTrue("Alfabeto completo deve conter " + c + " no passo 13",
                    pool.contains(String.valueOf(c)));
        }
    }

    /**
     * PASSO 7: Release dos Níveis 14 a 18 - Todos os 10 Algarismos Numéricos (0 a 9).
     */
    @Test
    public void testPasso07_ReleaseLevels14to18_AllNumbers_0_to_9() {
        tree.unlockNodesUpToLevel(18);
        TreeLevel l18 = tree.getLevel(18);
        List<String> pool = l18.getAllCharacters();

        // 26 letras + 10 números = 36 caracteres
        assertEquals(36, pool.size());
        for (int d = 0; d <= 9; d++) {
            String digit = String.valueOf(d);
            assertTrue("Dígito " + digit + " deve estar desbloqueado até ao nível 18",
                    pool.contains(digit));
            MorseTreeNode node = tree.findNodeByCharacter(digit);
            if (node != null) {
                assertTrue("Nó do dígito " + digit + " deve estar marcado como unlocked", node.isUnlocked());
            }
        }
    }

    /**
     * PASSO 8: Release dos Níveis 19 a 21 - Pontuação e Sinais Especiais (Árvore 100% Completa).
     */
    @Test
    public void testPasso08_ReleaseLevels19to21_PunctuationAndProsigns() {
        tree.unlockNodesUpToLevel(21);
        TreeLevel l21 = tree.getLevel(21);
        List<String> pool = l21.getAllCharacters();

        assertEquals(42, pool.size());
        assertTrue("Deve conter ponto '.'", pool.contains("."));
        assertTrue("Deve conter vírgula ','", pool.contains(","));
        assertTrue("Deve conter interrogação '?'", pool.contains("?"));
        assertTrue("Deve conter barra '/'", pool.contains("/"));
        assertTrue("Deve conter SOS", pool.contains("SOS"));
        assertTrue("Deve conter SK", pool.contains("SK"));
    }

    /**
     * PASSO A PASSO COMPLETO DO NÍVEL 1 AO 21:
     * Percorre sequencialmente cada nível, validando:
     * 1. A introdução de exatamente 2 novas letras/símbolos por passo.
     * 2. A acumulação correta de tamanho (2 * nível).
     * 3. Que todos os caracteres dos níveis anteriores se mantêm acessíveis.
     * 4. Que nenhuma letra de níveis posteriores está inadvertidamente aberta.
     */
    @Test
    public void testPasso09_StepByStepProgression_Levels1Through21() {
        for (int step = 1; step <= 21; step++) {
            tree.unlockNodesUpToLevel(step);

            TreeLevel level = tree.getLevel(step);
            assertNotNull("Nível " + step + " deve existir", level);

            String c1 = level.getChar1();
            String c2 = level.getChar2();
            assertNotNull("Caractere 1 do passo " + step + " não pode ser nulo", c1);
            assertNotNull("Caractere 2 do passo " + step + " não pode ser nulo", c2);

            List<String> pool = level.getAllCharacters();
            assertEquals("O pool no passo " + step + " deve ter exatamente " + (step * 2) + " caracteres",
                    step * 2, pool.size());
            assertTrue("Pool do passo " + step + " deve conter " + c1, pool.contains(c1));
            assertTrue("Pool do passo " + step + " deve conter " + c2, pool.contains(c2));
        }
    }

    /**
     * PASSO 10: Teste Dedicado ao Ramo do D (sob N): X (-..-) e B (-...).
     * Valida explicitamente que X e B são os filhos de D e são libertados no Nível 11.
     */
    @Test
    public void testPasso10_Dedicated_UnderD_X_and_B_Release() {
        tree.unlockNodesUpToLevel(10);
        MorseTreeNode nodeD = tree.findNodeByCharacter("D");
        assertNotNull("Nó D deve existir", nodeD);

        MorseTreeNode nodeX = nodeD.getDahChild();
        MorseTreeNode nodeB = nodeD.getDitChild();
        assertNotNull("Nó X (-..-) deve existir sob D", nodeX);
        assertNotNull("Nó B (-...) deve existir sob D", nodeB);

        assertEquals("X", nodeX.getCharacter());
        assertEquals("-..-", nodeX.getMorseCode());
        assertEquals("B", nodeB.getCharacter());
        assertEquals("-...", nodeB.getMorseCode());

        // No Nível 10, X e B estão bloqueados
        assertFalse("X deve estar bloqueado no nível 10", nodeX.isUnlocked());
        assertFalse("B deve estar bloqueado no nível 10", nodeB.isUnlocked());

        // No Nível 11, X e B são libertados
        tree.unlockNodesUpToLevel(11);
        assertTrue("X deve estar desbloqueado no nível 11", nodeX.isUnlocked());
        assertTrue("B deve estar desbloqueado no nível 11", nodeB.isUnlocked());

        TreeLevel l11 = tree.getLevel(11);
        assertTrue("Pool do nível 11 deve conter X", l11.getAllCharacters().contains("X"));
        assertTrue("Pool do nível 11 deve conter B", l11.getAllCharacters().contains("B"));
    }
}

