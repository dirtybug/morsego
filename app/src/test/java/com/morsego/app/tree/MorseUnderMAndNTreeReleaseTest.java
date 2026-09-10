package com.morsego.app.tree;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Testes Unitários dedicados à validação e libertação dos nós POR BAIXO DO M E DO N:
 *
 * Estrutura da Subárvore do T (-):
 *           T (-)
 *          /     \
 *       M (--)   N (-.)
 *       /    \   /    \
 *     O(---) G(--.) K(-.-) D(-..)
 *            /   \   /   \   /   \
 *          Q    Z   Y     C X     B
 *
 * Valida:
 * 1. Ramos diretos sob N: D (-..) e K (-.-) no Nível 6.
 * 2. Ramos diretos sob M: G (--.) e O (---) no Nível 7.
 * 3. Descendentes de D (sob N): B (-...) e X (-..-) no Nível 11.
 * 4. Descendentes de K (sob N): C (-.-.) e Y (-.--) no Nível 12.
 * 5. Descendentes de G (sob M): Z (--..) e Q (--.-) no Nível 13.
 * 6. Descendentes de O (sob M): Números 9 e 0 na profundidade 5.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MorseUnderMAndNTreeReleaseTest {

    private MorseBinaryTree tree;
    private MorseTreeNode nodeT;
    private MorseTreeNode nodeM;
    private MorseTreeNode nodeN;

    @Before
    public void setUp() {
        tree = MorseBinaryTree.getInstance();
        MorseTreeNode root = tree.getRoot();
        assertNotNull("Raiz não pode ser nula", root);

        nodeT = root.getDahChild();
        assertNotNull("Nó T (-) deve existir sob a raiz", nodeT);

        nodeM = nodeT.getDahChild();
        nodeN = nodeT.getDitChild();
        assertNotNull("Nó M (--) deve existir sob T", nodeM);
        assertNotNull("Nó N (-.) deve existir sob T", nodeN);
    }

    /**
     * TESTE 1: Filhos diretos sob N (-.): D (-..) e K (-.-) [NÍVEL 6]
     * Valida que D é o filho Dit (.) de N e K é o filho Dah (-) de N.
     * Valida que no Nível 5 estão bloqueados e no Nível 6 passam a desbloqueados.
     */
    @Test
    public void test01_UnderN_DirectChildren_D_and_K_ReleaseLevel6() {
        MorseTreeNode nodeD = nodeN.getDitChild();
        MorseTreeNode nodeK = nodeN.getDahChild();

        assertNotNull("Nó D deve existir sob N como filho DIT", nodeD);
        assertNotNull("Nó K deve existir sob N como filho DAH", nodeK);
        assertEquals("D", nodeD.getCharacter());
        assertEquals("-..", nodeD.getMorseCode());
        assertEquals("K", nodeK.getCharacter());
        assertEquals("-.-", nodeK.getMorseCode());

        // No Nível 5, D e K devem estar BLOQUEADOS
        tree.unlockNodesUpToLevel(5);
        assertFalse("D sob N deve estar bloqueado no nível 5", nodeD.isUnlocked());
        assertFalse("K sob N deve estar bloqueado no nível 5", nodeK.isUnlocked());

        // No Nível 6, D e K são LIBERTADOS
        tree.unlockNodesUpToLevel(6);
        assertTrue("D sob N deve estar DESBLOQUEADO no nível 6", nodeD.isUnlocked());
        assertTrue("K sob N deve estar DESBLOQUEADO no nível 6", nodeK.isUnlocked());

        TreeLevel l6 = tree.getLevel(6);
        assertEquals("D", l6.getChar1());
        assertEquals("K", l6.getChar2());
        assertTrue(l6.getAllCharacters().contains("D"));
        assertTrue(l6.getAllCharacters().contains("K"));
    }

    /**
     * TESTE 2: Filhos diretos sob M (--): G (--.) e O (---) [NÍVEL 7]
     * Valida que G é o filho Dit (.) de M e O é o filho Dah (-) de M.
     * Valida que no Nível 6 estão bloqueados e no Nível 7 passam a desbloqueados.
     */
    @Test
    public void test02_UnderM_DirectChildren_G_and_O_ReleaseLevel7() {
        MorseTreeNode nodeG = nodeM.getDitChild();
        MorseTreeNode nodeO = nodeM.getDahChild();

        assertNotNull("Nó G deve existir sob M como filho DIT", nodeG);
        assertNotNull("Nó O deve existir sob M como filho DAH", nodeO);
        assertEquals("G", nodeG.getCharacter());
        assertEquals("--.", nodeG.getMorseCode());
        assertEquals("O", nodeO.getCharacter());
        assertEquals("---", nodeO.getMorseCode());

        // No Nível 6, G e O sob M devem estar BLOQUEADOS
        tree.unlockNodesUpToLevel(6);
        assertFalse("G sob M deve estar bloqueado no nível 6", nodeG.isUnlocked());
        assertFalse("O sob M deve estar bloqueado no nível 6", nodeO.isUnlocked());

        // No Nível 7, G e O são LIBERTADOS
        tree.unlockNodesUpToLevel(7);
        assertTrue("G sob M deve estar DESBLOQUEADO no nível 7", nodeG.isUnlocked());
        assertTrue("O sob M deve estar DESBLOQUEADO no nível 7", nodeO.isUnlocked());

        TreeLevel l7 = tree.getLevel(7);
        assertEquals("G", l7.getChar1());
        assertEquals("O", l7.getChar2());
        assertTrue(l7.getAllCharacters().contains("G"));
        assertTrue(l7.getAllCharacters().contains("O"));
    }

    /**
     * TESTE 3: Sub-ramo sob D (que está sob N): B (-...) e X (-..-) [NÍVEL 11]
     * Valida descendência: T -> N -> D -> B (dit) e X (dah).
     * Valida bloqueio até ao Nível 10 e libertação no Nível 11.
     */
    @Test
    public void test03_UnderN_SubBranchD_Children_B_and_X_ReleaseLevel11() {
        MorseTreeNode nodeD = nodeN.getDitChild();
        assertNotNull(nodeD);

        MorseTreeNode nodeB = nodeD.getDitChild();
        MorseTreeNode nodeX = nodeD.getDahChild();

        assertNotNull("B deve existir sob D (sub-ramo de N)", nodeB);
        assertNotNull("X deve existir sob D (sub-ramo de N)", nodeX);
        assertEquals("B", nodeB.getCharacter());
        assertEquals("-...", nodeB.getMorseCode());
        assertEquals("X", nodeX.getCharacter());
        assertEquals("-..-", nodeX.getMorseCode());

        // No Nível 10, B e X devem estar bloqueados
        tree.unlockNodesUpToLevel(10);
        assertFalse("B sob D (N) deve estar bloqueado no nível 10", nodeB.isUnlocked());
        assertFalse("X sob D (N) deve estar bloqueado no nível 10", nodeX.isUnlocked());

        // No Nível 11, B e X são libertados
        tree.unlockNodesUpToLevel(11);
        assertTrue("B sob D (N) deve estar DESBLOQUEADO no nível 11", nodeB.isUnlocked());
        assertTrue("X sob D (N) deve estar DESBLOQUEADO no nível 11", nodeX.isUnlocked());

        TreeLevel l11 = tree.getLevel(11);
        assertEquals("B", l11.getChar1());
        assertEquals("X", l11.getChar2());
    }

    /**
     * TESTE 4: Sub-ramo sob K (que está sob N): C (-.-.) e Y (-.--) [NÍVEL 12]
     * Valida descendência: T -> N -> K -> C (dit) e Y (dah).
     * Valida bloqueio até ao Nível 11 e libertação no Nível 12.
     */
    @Test
    public void test04_UnderN_SubBranchK_Children_C_and_Y_ReleaseLevel12() {
        MorseTreeNode nodeK = nodeN.getDahChild();
        assertNotNull(nodeK);

        MorseTreeNode nodeC = nodeK.getDitChild();
        MorseTreeNode nodeY = nodeK.getDahChild();

        assertNotNull("C deve existir sob K (sub-ramo de N)", nodeC);
        assertNotNull("Y deve existir sob K (sub-ramo de N)", nodeY);
        assertEquals("C", nodeC.getCharacter());
        assertEquals("-.-.", nodeC.getMorseCode());
        assertEquals("Y", nodeY.getCharacter());
        assertEquals("-.--", nodeY.getMorseCode());

        // No Nível 11, C e Y devem estar bloqueados
        tree.unlockNodesUpToLevel(11);
        assertFalse("C sob K (N) deve estar bloqueado no nível 11", nodeC.isUnlocked());
        assertFalse("Y sob K (N) deve estar bloqueado no nível 11", nodeY.isUnlocked());

        // No Nível 12, C e Y são libertados
        tree.unlockNodesUpToLevel(12);
        assertTrue("C sob K (N) deve estar DESBLOQUEADO no nível 12", nodeC.isUnlocked());
        assertTrue("Y sob K (N) deve estar DESBLOQUEADO no nível 12", nodeY.isUnlocked());

        TreeLevel l12 = tree.getLevel(12);
        assertEquals("C", l12.getChar1());
        assertEquals("Y", l12.getChar2());
    }

    /**
     * TESTE 5: Sub-ramo sob G (que está sob M): Z (--..) e Q (--.-) [NÍVEL 13]
     * Valida descendência: T -> M -> G -> Z (dit) e Q (dah).
     * Valida bloqueio até ao Nível 12 e libertação no Nível 13.
     */
    @Test
    public void test05_UnderM_SubBranchG_Children_Z_and_Q_ReleaseLevel13() {
        MorseTreeNode nodeG = nodeM.getDitChild();
        assertNotNull(nodeG);

        MorseTreeNode nodeZ = nodeG.getDitChild();
        MorseTreeNode nodeQ = nodeG.getDahChild();

        assertNotNull("Z deve existir sob G (sub-ramo de M)", nodeZ);
        assertNotNull("Q deve existir sob G (sub-ramo de M)", nodeQ);
        assertEquals("Z", nodeZ.getCharacter());
        assertEquals("--..", nodeZ.getMorseCode());
        assertEquals("Q", nodeQ.getCharacter());
        assertEquals("--.-", nodeQ.getMorseCode());

        // No Nível 12, Z e Q devem estar bloqueados
        tree.unlockNodesUpToLevel(12);
        assertFalse("Z sob G (M) deve estar bloqueado no nível 12", nodeZ.isUnlocked());
        assertFalse("Q sob G (M) deve estar bloqueado no nível 12", nodeQ.isUnlocked());

        // No Nível 13, Z e Q são libertados
        tree.unlockNodesUpToLevel(13);
        assertTrue("Z sob G (M) deve estar DESBLOQUEADO no nível 13", nodeZ.isUnlocked());
        assertTrue("Q sob G (M) deve estar DESBLOQUEADO no nível 13", nodeQ.isUnlocked());

        TreeLevel l13 = tree.getLevel(13);
        assertEquals("Z", l13.getChar1());
        assertEquals("Q", l13.getChar2());
    }

    /**
     * TESTE 6: Sub-ramo sob O (que está sob M): Números 9 (----.) e 0 (-----)
     * Valida descendência de numeração telegráfica:
     * START -> T (-) -> M (--) -> O (---) -> 9 / 0
     */
    @Test
    public void test06_UnderM_SubBranchO_Numbers_9_and_0() {
        MorseTreeNode nodeO = nodeM.getDahChild();
        assertNotNull(nodeO);

        MorseTreeNode node9 = nodeO.getDitChild();
        MorseTreeNode node0 = nodeO.getDahChild();

        assertNotNull("9 deve existir sob O (sub-ramo de M)", node9);
        assertNotNull("0 deve existir sob O (sub-ramo de M)", node0);
        assertEquals("9", node9.getCharacter());
        assertEquals("----.", node9.getMorseCode());
        assertEquals("0", node0.getCharacter());
        assertEquals("-----", node0.getMorseCode());
    }

    /**
     * TESTE 7: Isolamento e Integridade M vs N:
     * Garante que desbloquear ramos de N (D, K) NÃO desbloqueia inadvertidamente ramos de M (G, O).
     */
    @Test
    public void test07_IsolationBetweenBranchM_and_BranchN() {
        // No Nível 6: Desbloqueia ramos de N (D e K), mas ramos de M (G e O) DEVEM continuar bloqueados
        tree.unlockNodesUpToLevel(6);

        MorseTreeNode nodeD = nodeN.getDitChild();
        MorseTreeNode nodeK = nodeN.getDahChild();
        MorseTreeNode nodeG = nodeM.getDitChild();
        MorseTreeNode nodeO = nodeM.getDahChild();

        assertTrue("Ramo de N (D) desbloqueado no nível 6", nodeD.isUnlocked());
        assertTrue("Ramo de N (K) desbloqueado no nível 6", nodeK.isUnlocked());

        assertFalse("Ramo de M (G) DEVE continuar BLOQUEADO no nível 6", nodeG.isUnlocked());
        assertFalse("Ramo de M (O) DEVE continuar BLOQUEADO no nível 6", nodeO.isUnlocked());
    }
}
