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
import com.morsego.app.tree.MorseTreeNode;
import com.morsego.app.tree.TreeLevel;

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
        TreeLevel treeLevel = MorseBinaryTree.getInstance().getLevel(currentLevel);

        binding.tvTreeLevelTitle.setText("ÁRVORE BINÁRIA (NÍVEL " + currentLevel + " DE " + MorseBinaryTree.getInstance().getTotalLevels() + ")");
        binding.tvTreeLevelSub.setText(treeLevel.getTitle() + "\nNovas letras: " + treeLevel.getChar1() + " (" + treeLevel.getMorse1() + ") e " + treeLevel.getChar2() + " (" + treeLevel.getMorse2() + ")");

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
            TreeLevel treeLevel = MorseBinaryTree.getInstance().getLevel(currentLevel);

            binding.tvTreeLevelTitle.setText("ÁRVORE BINÁRIA (DESBLOQUEADO ATÉ NÍVEL " + currentLevel + " DE " + MorseBinaryTree.getInstance().getTotalLevels() + ")");
            binding.tvTreeLevelSub.setText(treeLevel.getTitle() + "\nNovas letras: " + treeLevel.getChar1() + " (" + treeLevel.getMorse1() + ") e " + treeLevel.getChar2() + " (" + treeLevel.getMorse2() + ")");
            binding.morseTreeView.setUnlockedLevel(currentLevel);
        }
    }

    private void updateSelectedNode(String character, String morse) {
        this.selectedChar = character;
        binding.tvSelectedChar.setText(character);
        binding.tvSelectedMorse.setText("Código: " + morse);

        StringBuilder path = new StringBuilder("Raiz");
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
