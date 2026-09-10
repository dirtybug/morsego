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
     * PASSO A PASSO COMPLETO DO NÍVEL 1 AO 21:
     * Percorre sequencialmente cada nível, validando:
     * 1. A introdução de exatamente 2 novas letras/símbolos por passo.
     * 2. A acumulação correta de tamanho (2 * nível).
     * 3. Que todos os caracteres dos níveis anteriores se mantêm acessíveis.
     * 4. Que nenhuma letra de níveis posteriores está inadvertidamente aberta.
     */
    @Test
    public void testPasso04_StepByStepProgression_Levels1Through21() {
        for (int step = 1; step <= 21; step++) {
            tree.unlockNodesUpToLevel(step);

            TreeLevel level = tree.getLevel(step);
            assertNotNull("Nível " + step + " deve existir", level);

            // Valida as 2 novas letras deste passo
            String c1 = level.getChar1();
            String c2 = level.getChar2();
            assertNotNull("Caractere 1 do passo " + step + " não pode ser nulo", c1);
            assertNotNull("Caractere 2 do passo " + step + " não pode ser nulo", c2);
            assertFalse(c1.isEmpty());
            assertFalse(c2.isEmpty());

            // Valida tamanho do pool acumulado
            List<String> pool = level.getAllCharacters();
            assertEquals("O pool no passo " + step + " deve ter exatamente " + (step * 2) + " caracteres",
                    step * 2, pool.size());
            assertTrue("Pool do passo " + step + " deve conter " + c1, pool.contains(c1));
            assertTrue("Pool do passo " + step + " deve conter " + c2, pool.contains(c2));

            // Valida que os nós correspondentes na árvore estão desbloqueados
            MorseTreeNode node1 = tree.findNodeByCharacter(c1);
            MorseTreeNode node2 = tree.findNodeByCharacter(c2);
            if (node1 != null) {
                assertTrue("Nó " + c1 + " deve estar desbloqueado no passo " + step, node1.isUnlocked());
            }
            if (node2 != null) {
                assertTrue("Nó " + c2 + " deve estar desbloqueado no passo " + step, node2.isUnlocked());
            }

            // Se ainda houver níveis superiores, valida que os novos caracteres do próximo nível estão bloqueados
            if (step < 21) {
                TreeLevel nextLevel = tree.getLevel(step + 1);
                MorseTreeNode nextNode1 = tree.findNodeByCharacter(nextLevel.getChar1());
                MorseTreeNode nextNode2 = tree.findNodeByCharacter(nextLevel.getChar2());
                if (nextNode1 != null) {
                    assertFalse("Nó " + nextLevel.getChar1() + " do passo " + (step + 1) +
                            " deve estar BLOQUEADO no passo " + step, nextNode1.isUnlocked());
                }
                if (nextNode2 != null) {
                    assertFalse("Nó " + nextLevel.getChar2() + " do passo " + (step + 1) +
                            " deve estar BLOQUEADO no passo " + step, nextNode2.isUnlocked());
                }
            }
        }
    }

    /**
     * PASSO 13: Validação do marco crítico do Nível 13.
     * No Nível 13, Z e Q completam as 26 letras do alfabeto A-Z, libertando as Palavras de Rádio CW.
     */
    @Test
    public void testPasso05_MilestoneLevel13_AlphabetComplete() {
        tree.unlockNodesUpToLevel(13);
        TreeLevel l13 = tree.getLevel(13);
        List<String> pool = l13.getAllCharacters();

        assertEquals(26, pool.size());
        for (char c = 'A'; c <= 'Z'; c++) {
            assertTrue("Alfabeto completo deve conter " + c + " no passo 13",
                    pool.contains(String.valueOf(c)));
        }
    }
}
