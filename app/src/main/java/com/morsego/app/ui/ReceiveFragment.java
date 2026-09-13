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
import com.morsego.app.R;
import com.morsego.app.databinding.FragmentReceiveBinding;
import com.morsego.app.tree.MorseBinaryTree;
import com.morsego.app.tree.TreeLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ReceiveFragment handles acoustic Morse CW listening practice (Receive / Ouvir).
 */
public class ReceiveFragment extends Fragment {

    private static final int MAX_LIVES = 3;
    private static final int DEFAULT_TOTAL_QUESTIONS = 20;

    private FragmentReceiveBinding binding;
    private int currentLevelNumber = 1;
    private TreeLevel currentLevel;
    private String currentAnswer = "E";
    private int scoreCorrect = 0;
    private int questionsAnswered = 0;
    private int totalQuestions = DEFAULT_TOTAL_QUESTIONS;
    private int currentFailures = 0;
    private boolean testFinished = false;
    private final List<Button> optionButtons = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentReceiveBinding.inflate(inflater, container, false);
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

        binding.btnNextQuestion.setOnClickListener(v -> onNextQuestionClicked());

        binding.btnPrevLevel.setOnClickListener(v -> {
            if (currentLevelNumber > 1) {
                loadLevel(currentLevelNumber - 1);
            }
        });

        binding.btnNextLevel.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            int maxLevels = MorseBinaryTree.getInstance().getTotalLevels();
            if (currentLevelNumber < maxLevels) {
                boolean nextUnlocked = activity != null && activity.getSettings().isLevelUnlocked(currentLevelNumber + 1);
                if (nextUnlocked) {
                    loadLevel(currentLevelNumber + 1);
                } else if (activity != null) {
                    android.widget.Toast.makeText(activity, R.string.toast_level_locked, android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });

        applyLocalization();
        MainActivity activity = (MainActivity) getActivity();
        int initialLevel = (activity != null) ? activity.getSettings().getCurrentUnlockedLevel() : 1;
        if (getArguments() != null && getArguments().containsKey("target_level")) {
            initialLevel = getArguments().getInt("target_level");
        }
        loadLevel(initialLevel);
    }

    public void loadLevel(int levelNum) {
        applyLocalization();
        this.currentLevelNumber = levelNum;
        this.currentLevel = MorseBinaryTree.getInstance().getLevel(levelNum);

        MainActivity activity = (MainActivity) getActivity();
        boolean isUnlocked = activity != null && activity.getSettings().isLevelUnlocked(levelNum);

        if (binding != null) {
            int maxLevels = MorseBinaryTree.getInstance().getTotalLevels();
            binding.tvLevelNumber.setText(getString(R.string.level_number_format, currentLevel.getLevelNumber(), maxLevels) + (isUnlocked ? "" : " 🔒"));
            String morse1Visual = currentLevel.getMorse1().replace('.', '•').replace('-', '—');
            String morse2Visual = currentLevel.getMorse2().replace('.', '•').replace('-', '—');
            binding.tvNewCharacters.setText(getString(R.string.new_characters_format,
                    currentLevel.getChar1(), morse1Visual,
                    currentLevel.getChar2(), morse2Visual));

            int nextLevel = levelNum + 1;
            boolean nextUnlocked = activity != null && activity.getSettings().isLevelUnlocked(nextLevel);
            binding.btnNextLevel.setAlpha(nextUnlocked ? 1.0f : 0.4f);
            binding.btnPrevLevel.setAlpha(levelNum > 1 ? 1.0f : 0.4f);
        }

        resetTest();
    }

    private void resetTest() {
        currentFailures = 0;
        questionsAnswered = 0;
        scoreCorrect = 0;
        testFinished = false;
        if (binding != null) {
            binding.btnNextQuestion.setVisibility(View.GONE);
            binding.tvQuizResult.setVisibility(View.GONE);
            binding.btnNextQuestion.setText(R.string.practice_next_question);
            binding.btnNextQuestion.setOnClickListener(v -> onNextQuestionClicked());
        }
        enableOptionButtons(true);
        setupNewQuestion();
        updateLivesAndProgressUi();
    }

    private void onNextQuestionClicked() {
        if (binding == null) return;
        binding.btnNextQuestion.setVisibility(View.GONE);
        binding.tvQuizResult.setVisibility(View.GONE);
        enableOptionButtons(true);
        setupNewQuestion();
        updateLivesAndProgressUi();
    }

    private void updateLivesAndProgressUi() {
        if (binding == null) return;
        int remaining = Math.max(0, MAX_LIVES - currentFailures);
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < MAX_LIVES; i++) {
            hearts.append(i < remaining ? "❤️ " : "🖤 ");
        }

        int currentQ = Math.min(questionsAnswered + 1, totalQuestions);

        binding.tvReceiveLives.setText(hearts.toString().trim());
        binding.tvReceiveProgress.setText(getString(R.string.test_progress_format, currentQ, totalQuestions, questionsAnswered));
        binding.pbReceiveProgress.setMax(totalQuestions);
        binding.pbReceiveProgress.setProgress(questionsAnswered);
        binding.tvPracticeScore.setText(getString(R.string.receive_score_format, scoreCorrect, totalQuestions));
    }

    private void applyLocalization() {
        if (binding == null) return;
        binding.tvPracticeHeaderTitle.setText(R.string.receive_test_header);
        binding.tvPracticeTapPrompt.setText(R.string.practice_tap_to_listen);
        if (testFinished) {
            binding.btnNextQuestion.setText(R.string.btn_restart_receive);
        } else {
            binding.btnNextQuestion.setText(R.string.practice_next_question);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        applyLocalization();
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null && currentLevel == null) {
            int unlocked = activity.getSettings().getCurrentUnlockedLevel();
            loadLevel(unlocked);
        }
    }

    private void setupNewQuestion() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null || currentLevel == null) return;

        int currentLevelNum = currentLevel.getLevelNumber();
        List<String> pool = new ArrayList<>(currentLevel.getAllCharacters());

        boolean radioUnlocked = com.morsego.app.tree.MorseRadioWords.isUnlocked(currentLevelNum);
        com.morsego.app.keyer.KeyerSettings settings = activity.getSettings();
        String recvStatus = (settings != null && settings.isReceivePassed(currentLevelNum)) ? getString(R.string.status_passed) : getString(R.string.status_pending);
        String sendStatus = (settings != null && settings.isSendPassed(currentLevelNum)) ? getString(R.string.status_passed) : getString(R.string.status_pending);
        String statusStr = getString(R.string.level_mode_status_format, recvStatus, sendStatus);

        String info = (radioUnlocked ?
                getString(R.string.receive_unlocked_info_radio_format, currentLevelNum, pool.size()) :
                getString(R.string.receive_unlocked_info_format, currentLevelNum, pool.size()))
                + " • " + statusStr;
        binding.tvPracticeUnlockedInfo.setText(info);

        boolean pickRadioWord = radioUnlocked && (Math.random() < 0.35);
        List<String> options = new ArrayList<>();

        if (pickRadioWord) {
            com.morsego.app.tree.MorseRadioWords.RadioWordItem item = com.morsego.app.tree.MorseRadioWords.getRandomWord();
            currentAnswer = item.word;
            options = com.morsego.app.tree.MorseRadioWords.generateListeningChoices(currentAnswer, 4);
        } else {
            // Pick random answer from pool
            Collections.shuffle(pool);
            currentAnswer = pool.get(0);

            // Generate 3 wrong options
            options.add(currentAnswer);

            // Fill remaining ONLY with characters from the level's pool
            for (String c : pool) {
                if (!options.contains(c) && options.size() < 4) {
                    options.add(c);
                }
            }
        }

        // Put letter options in alphabetical order as required
        Collections.sort(options);

        for (int i = 0; i < optionButtons.size(); i++) {
            Button btn = optionButtons.get(i);
            if (i < options.size()) {
                btn.setVisibility(View.VISIBLE);
                btn.setText(options.get(i));
                btn.setBackgroundColor(Color.parseColor("#21262D"));
            } else {
                btn.setVisibility(View.GONE);
            }
        }

        playQuestionAudio();
    }

    private void playQuestionAudio() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        if (currentAnswer.length() == 1) {
            String morse = MorseBinaryTree.getInstance().getMorse(currentAnswer);
            if (morse != null) {
                activity.playMorse(morse, activity.getSettings().getWpm(), null);
            }
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < currentAnswer.length(); i++) {
                if (i > 0) sb.append(" ");
                String m = MorseBinaryTree.getInstance().getMorse(String.valueOf(currentAnswer.charAt(i)));
                if (m != null) sb.append(m);
            }
            activity.playMorse(sb.toString(), activity.getSettings().getWpm(), null);
        }
    }

    private void handleOptionClicked(String selected) {
        enableOptionButtons(false);
        questionsAnswered++;

        boolean isCorrect = selected.equalsIgnoreCase(currentAnswer);
        if (isCorrect) {
            scoreCorrect++;
            binding.tvQuizResult.setText(getString(R.string.receive_correct_result, currentAnswer));
            binding.tvQuizResult.setTextColor(Color.parseColor("#00E676"));
        } else {
            currentFailures++;
            binding.tvQuizResult.setText(getString(R.string.receive_incorrect_result, selected, currentAnswer));
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

        updateLivesAndProgressUi();

        if (currentFailures >= MAX_LIVES) {
            testFinished = true;
            binding.tvQuizResult.setText(R.string.receive_test_failed);
            binding.tvQuizResult.setTextColor(Color.parseColor("#FF5252"));
            binding.btnNextQuestion.setText(R.string.btn_restart_receive);
            binding.btnNextQuestion.setOnClickListener(v -> resetTest());
            binding.tvQuizResult.setVisibility(View.VISIBLE);
            binding.btnNextQuestion.setVisibility(View.VISIBLE);
        } else if (questionsAnswered >= totalQuestions) {
            testFinished = true;
            MainActivity activity = (MainActivity) getActivity();
            boolean isSendPassed = false;
            if (activity != null) {
                activity.getSettings().setReceivePassed(currentLevelNumber, true);
                isSendPassed = activity.getSettings().isSendPassed(currentLevelNumber);
            }

            binding.tvQuizResult.setVisibility(View.VISIBLE);
            binding.btnNextQuestion.setVisibility(View.VISIBLE);

            if (!isSendPassed) {
                binding.tvQuizResult.setText(getString(R.string.receive_passed_need_send, currentLevelNumber));
                binding.tvQuizResult.setTextColor(Color.parseColor("#00E676"));
                binding.btnNextQuestion.setText(R.string.btn_go_to_send);
                binding.btnNextQuestion.setOnClickListener(v -> {
                    if (activity != null) {
                        activity.navigateToSend(currentLevelNumber);
                    }
                });

                final int lvl = currentLevelNumber;
                binding.getRoot().postDelayed(() -> {
                    if (isAdded() && testFinished && activity != null) {
                        activity.navigateToSend(lvl);
                    }
                }, 2500);
            } else {
                if (activity != null) {
                    activity.getSettings().unlockNextLevel(currentLevelNumber);
                }
                int maxLevels = MorseBinaryTree.getInstance().getTotalLevels();
                int nextLevel = currentLevelNumber + 1;
                if (nextLevel <= maxLevels) {
                    binding.tvQuizResult.setText(getString(R.string.level_completed_both_passed, currentLevelNumber, nextLevel));
                    binding.tvQuizResult.setTextColor(Color.parseColor("#00E676"));
                    binding.btnNextQuestion.setText(getString(R.string.btn_next_level, nextLevel));
                    binding.btnNextQuestion.setOnClickListener(v -> loadLevel(nextLevel));
                } else {
                    binding.tvQuizResult.setText(R.string.toast_all_levels_completed);
                    binding.tvQuizResult.setTextColor(Color.parseColor("#00E676"));
                    binding.btnNextQuestion.setText(R.string.btn_restart_receive);
                    binding.btnNextQuestion.setOnClickListener(v -> resetTest());
                }
            }
        } else {
            binding.btnNextQuestion.setText(R.string.practice_next_question);
            binding.btnNextQuestion.setOnClickListener(v -> onNextQuestionClicked());
            binding.tvQuizResult.setVisibility(View.VISIBLE);
            binding.btnNextQuestion.setVisibility(View.VISIBLE);
        }
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
