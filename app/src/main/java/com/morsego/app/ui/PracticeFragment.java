package com.morsego.app.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.morsego.app.MainActivity;
import com.morsego.app.databinding.FragmentPracticeBinding;
import com.morsego.app.tree.MorseBinaryTree;
import com.morsego.app.tree.TreeLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PracticeFragment extends Fragment {

    private FragmentPracticeBinding binding;
    private String currentAnswer = "E";
    private int scoreCorrect = 0;
    private int scoreTotal = 0;
    private final List<Button> optionButtons = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPracticeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        optionButtons.clear();
        optionButtons.add(binding.btnOpt1);
        optionButtons.add(binding.btnOpt2);
        optionButtons.add(binding.btnOpt3);
        optionButtons.add(binding.btnOpt4);

        for (Button btn : optionButtons) {
            btn.setOnClickListener(v -> handleOptionClicked(btn.getText().toString()));
        }

        binding.btnPlayQuestionAudio.setOnClickListener(v -> playQuestionAudio());

        binding.btnNextQuestion.setOnClickListener(v -> {
            binding.btnNextQuestion.setVisibility(View.GONE);
            binding.tvQuizResult.setVisibility(View.GONE);
            enableOptionButtons(true);
            setupNewQuestion();
        });

        setupNewQuestion();
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null && binding != null) {
            int currentLevel = activity.getSettings().getCurrentUnlockedLevel();
            TreeLevel level = MorseBinaryTree.getInstance().getLevel(currentLevel);
            binding.tvPracticeUnlockedInfo.setText("Testando letras desbloqueadas na árvore (Nível " + currentLevel + "): " + level.getAllCharacters().size() + " caracteres");
        }
    }

    private void setupNewQuestion() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        int currentLevel = activity.getSettings().getCurrentUnlockedLevel();
        TreeLevel level = MorseBinaryTree.getInstance().getLevel(currentLevel);
        List<String> pool = new ArrayList<>(level.getAllCharacters());

        binding.tvPracticeUnlockedInfo.setText("Testando letras desbloqueadas na árvore (Nível " + currentLevel + "): " + pool.size() + " caracteres");

        // Pick random answer from pool
        Collections.shuffle(pool);
        currentAnswer = pool.get(0);

        // Generate 3 wrong options
        List<String> options = new ArrayList<>();
        options.add(currentAnswer);

        // Fill remaining with other pool characters or other alphabet letters
        for (String c : pool) {
            if (!options.contains(c) && options.size() < 4) {
                options.add(c);
            }
        }
        while (options.size() < 4) {
            char randomLetter = (char) ('A' + (int) (Math.random() * 26));
            String randStr = String.valueOf(randomLetter);
            if (!options.contains(randStr)) {
                options.add(randStr);
            }
        }

        Collections.shuffle(options);

        for (int i = 0; i < optionButtons.size(); i++) {
            optionButtons.get(i).setText(options.get(i));
            optionButtons.get(i).setBackgroundColor(Color.parseColor("#21262D"));
        }

        playQuestionAudio();
    }

    private void playQuestionAudio() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        String morse = MorseBinaryTree.getInstance().getMorse(currentAnswer);
        if (morse != null) {
            activity.getSynthesizer().playMorsePattern(morse, activity.getSettings().getWpm(), null);
        }
    }

    private void handleOptionClicked(String selected) {
        enableOptionButtons(false);
        scoreTotal++;

        boolean isCorrect = selected.equalsIgnoreCase(currentAnswer);
        if (isCorrect) {
            scoreCorrect++;
            binding.tvQuizResult.setText("✓ Correto! A letra transmitida era '" + currentAnswer + "'.");
            binding.tvQuizResult.setTextColor(Color.parseColor("#00E676"));
        } else {
            binding.tvQuizResult.setText("✗ Incorreto. Escolheu '" + selected + "', mas a letra era '" + currentAnswer + "'.");
            binding.tvQuizResult.setTextColor(Color.parseColor("#FF5252"));
        }

        // Highlight correct button
        for (Button btn : optionButtons) {
            if (btn.getText().toString().equalsIgnoreCase(currentAnswer)) {
                btn.setBackgroundColor(Color.parseColor("#00E676"));
            } else if (btn.getText().toString().equalsIgnoreCase(selected) && !isCorrect) {
                btn.setBackgroundColor(Color.parseColor("#FF5252"));
            }
        }

        binding.tvPracticeScore.setText("Pontos: " + scoreCorrect + " / " + scoreTotal);
        binding.tvQuizResult.setVisibility(View.VISIBLE);
        binding.btnNextQuestion.setVisibility(View.VISIBLE);
    }

    private void enableOptionButtons(boolean enable) {
        for (Button btn : optionButtons) {
            btn.setEnabled(enable);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
