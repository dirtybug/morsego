package com.morsego.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.morsego.app.MainActivity;
import com.morsego.app.databinding.FragmentTreeBinding;
import com.morsego.app.tree.MorseBinaryTree;
import com.morsego.app.tree.TreeLevel;

import java.util.Locale;

/**
 * Fragment that displays the interactive Morse Binary Tree and allows node exploration.
 */
public class TreeFragment extends Fragment {

    private FragmentTreeBinding binding;
    private String selectedChar = "E";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTreeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) requireActivity();
        int currentLevel = activity.getSettings().getCurrentUnlockedLevel();
        updateTreeHeader(currentLevel);

        binding.morseTreeView.setUnlockedLevel(currentLevel);

        // Update initial selected node
        updateSelectedNode("E", ".");

        binding.morseTreeView.setOnNodeClickListener(node -> {
            updateSelectedNode(node.getCharacter(), node.getMorseCode());
            activity.getSynthesizer().playMorsePattern(node.getMorseCode(), activity.getSettings().getWpm(), null);
        });

        binding.btnPlaySelected.setOnClickListener(v -> {
            String morse = MorseBinaryTree.getInstance().getMorse(selectedChar);
            if (morse != null) {
                activity.getSynthesizer().playMorsePattern(morse, activity.getSettings().getWpm(), null);
            }
        });

        binding.btnGoToLesson.setOnClickListener(v -> {
            activity.navigateToTab(MainActivity.TAB_LEARN);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && getActivity() != null) {
            MainActivity activity = (MainActivity) getActivity();
            int currentLevel = activity.getSettings().getCurrentUnlockedLevel();
            updateTreeHeader(currentLevel);
            binding.morseTreeView.setUnlockedLevel(currentLevel);
        }
    }

    private void updateTreeHeader(int currentLevel) {
        boolean isPt = Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
        int totalLevels = MorseBinaryTree.getInstance().getTotalLevels();
        TreeLevel treeLevel = MorseBinaryTree.getInstance().getLevel(currentLevel);

        String title = isPt ?
                "ÁRVORE BINÁRIA (NÍVEL " + currentLevel + " DE " + totalLevels + ")" :
                "MORSE BINARY TREE (LEVEL " + currentLevel + " OF " + totalLevels + ")";
        String newCharsLabel = isPt ? "Novas letras: " : "New characters: ";
        String andLabel = isPt ? " e " : " and ";

        binding.tvTreeLevelTitle.setText(title);
        binding.tvTreeLevelSub.setText(treeLevel.getTitle() + "\n" + newCharsLabel +
                treeLevel.getChar1() + " (" + treeLevel.getMorse1() + ")" + andLabel +
                treeLevel.getChar2() + " (" + treeLevel.getMorse2() + ")");
    }

    private void updateSelectedNode(String character, String morse) {
        this.selectedChar = character;
        boolean isPt = Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
        binding.tvSelectedChar.setText(character);
        binding.tvSelectedMorse.setText((isPt ? "Código: " : "Code: ") + morse);

        StringBuilder path = new StringBuilder(isPt ? "Raiz" : "Root");
        for (int i = 0; i < morse.length(); i++) {
            path.append(morse.charAt(i) == '.' ? " -> DIT (•)" : " -> DAH (—)");
        }
        binding.tvSelectedPath.setText(path.toString());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
