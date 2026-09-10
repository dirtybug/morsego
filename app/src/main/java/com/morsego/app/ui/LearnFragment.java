package com.morsego.app.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.morsego.app.MainActivity;
import com.morsego.app.databinding.FragmentLearnBinding;
import com.morsego.app.keyer.MorseDecoder;
import com.morsego.app.tree.MorseBinaryTree;
import com.morsego.app.tree.TreeLevel;

import java.util.List;

public class LearnFragment extends Fragment implements MorseDecoder.DecoderListener {

    private FragmentLearnBinding binding;
    private int currentLevelNumber = 1;
    private TreeLevel currentLevel;
    private String currentPrompt = "E";
    private int correctStreak = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLearnBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) requireActivity();
        currentLevelNumber = activity.getSettings().getCurrentUnlockedLevel();
        loadLevel(currentLevelNumber);

        binding.btnPrevLevel.setOnClickListener(v -> {
            if (currentLevelNumber > 1) {
                loadLevel(currentLevelNumber - 1);
            }
        });

        binding.btnNextLevel.setOnClickListener(v -> {
            if (currentLevelNumber < MorseBinaryTree.getInstance().getTotalLevels()) {
                loadLevel(currentLevelNumber + 1);
            }
        });

        binding.btnPlayChar1.setOnClickListener(v -> {
            activity.getSynthesizer().playMorsePattern(currentLevel.getMorse1(), activity.getSettings().getWpm(), null);
        });

        binding.btnPlayChar2.setOnClickListener(v -> {
            activity.getSynthesizer().playMorsePattern(currentLevel.getMorse2(), activity.getSettings().getWpm(), null);
        });

        binding.btnListenChallenge.setOnClickListener(v -> {
            String morse = MorseBinaryTree.getInstance().getMorse(currentPrompt);
            if (morse != null) {
                activity.getSynthesizer().playMorsePattern(morse, activity.getSettings().getWpm(), null);
            }
        });

        // Touch Paddle Controls with touch down & up listeners
        binding.btnTouchDit.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                activity.getInputManager().setTouchDit(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                activity.getInputManager().setTouchDit(false);
            }
            return true;
        });

        binding.btnTouchDah.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                activity.getInputManager().setTouchDah(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                activity.getInputManager().setTouchDah(false);
            }
            return true;
        });
    }

    private void loadLevel(int levelNum) {
        this.currentLevelNumber = levelNum;
        this.currentLevel = MorseBinaryTree.getInstance().getLevel(levelNum);
        this.correctStreak = 0;

        binding.tvLevelNumber.setText("NÍVEL " + currentLevel.getLevelNumber() + " / " + MorseBinaryTree.getInstance().getTotalLevels());
        binding.tvLevelTitle.setText(currentLevel.getTitle());

        binding.tvNewChar1.setText(currentLevel.getChar1());
        binding.tvNewMorse1.setText(currentLevel.getMorse1());

        binding.tvNewChar2.setText(currentLevel.getChar2());
        binding.tvNewMorse2.setText(currentLevel.getMorse2());

        List<String> allChars = currentLevel.getAllCharacters();
        StringBuilder poolStr = new StringBuilder("Todas as letras em jogo (" + allChars.size() + "): ");
        for (int i = 0; i < allChars.size(); i++) {
            poolStr.append(allChars.get(i));
            if (i < allChars.size() - 1) poolStr.append(", ");
        }
        binding.tvPoolDescription.setText(poolStr.toString());

        nextChallengePrompt();
    }

    private void nextChallengePrompt() {
        if (currentLevel == null) return;
        this.currentPrompt = currentLevel.getRandomCharacterFromPool();

        binding.tvChallengeChar.setText(currentPrompt);
        String morse = MorseBinaryTree.getInstance().getMorse(currentPrompt);
        binding.tvChallengeMorse.setText("Código: " + (morse != null ? morse : ""));
        binding.tvLiveBuffer.setText("A introduzir: —");
        binding.tvFeedback.setText("Toque nas pás do manipulador para responder");
        binding.tvFeedback.setTextColor(Color.parseColor("#8B949E"));
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.getDecoder().setListener(this);
        }
    }

    @Override
    public void onPatternChanged(String currentPattern) {
        if (binding != null) {
            binding.tvLiveBuffer.setText("A introduzir: " + (currentPattern.isEmpty() ? "—" : currentPattern));
        }
    }

    @Override
    public void onTextUpdated(String fullText) {
        // Handled in character decoded
    }

    @Override
    public void onCharacterDecoded(char character) {
        if (binding == null) return;

        String decodedStr = String.valueOf(character).toUpperCase();
        boolean isCorrect = decodedStr.equalsIgnoreCase(currentPrompt);

        if (isCorrect) {
            correctStreak++;
            binding.tvFeedback.setText("✓ Correto! Transmitiu '" + character + "' com sucesso! (" + correctStreak + "/5)");
            binding.tvFeedback.setTextColor(Color.parseColor("#00E676"));

            // If user gets 5 correct, unlock next level
            if (correctStreak >= 5) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) {
                    int nextLvl = currentLevelNumber + 1;
                    if (nextLvl > activity.getSettings().getCurrentUnlockedLevel() &&
                            nextLvl <= MorseBinaryTree.getInstance().getTotalLevels()) {
                        activity.getSettings().setCurrentUnlockedLevel(nextLvl);
                        binding.tvFeedback.setText("🎉 Parabéns! Desbloqueou o Nível " + nextLvl + " na Árvore!");
                    }
                }
            }

            // Move to next challenge after a short pause
            binding.getRoot().postDelayed(this::nextChallengePrompt, 900);
        } else {
            correctStreak = Math.max(0, correctStreak - 1);
            binding.tvFeedback.setText("✗ Incorreto: Transmitiu '" + character + "', mas era esperado '" + currentPrompt + "'.");
            binding.tvFeedback.setTextColor(Color.parseColor("#FF5252"));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
