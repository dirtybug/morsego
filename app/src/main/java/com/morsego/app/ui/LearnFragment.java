package com.morsego.app.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.morsego.app.MainActivity;
import com.morsego.app.databinding.FragmentLearnBinding;
import com.morsego.app.keyer.MorseDecoder;
import com.morsego.app.tree.MorseBinaryTree;
import com.morsego.app.tree.TreeLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LearnFragment extends Fragment implements MorseDecoder.DecoderListener {

    private static final int TOTAL_QUESTIONS_PER_STAGE = 5;
    private static final int PASSING_SCORE = 4; // 80% passing threshold

    private enum TestStage {
        STUDY,
        LISTENING,
        SENDING,
        RESULT
    }

    private FragmentLearnBinding binding;

    private int currentLevelNumber = 1;
    private TreeLevel currentLevel;

    private TestStage currentStage = TestStage.STUDY;

    // Listening test state
    private int listeningIndex = 0;
    private int listeningScore = 0;
    private String currentListeningAnswer = "E";
    private final List<Button> testOptionButtons = new ArrayList<>();

    // Sending test state
    private int sendingIndex = 0;
    private int sendingScore = 0;
    private String currentSendingPrompt = "E";
    private boolean sendingWaitingForInput = false;

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

        testOptionButtons.clear();
        testOptionButtons.add(binding.btnTestOpt1);
        testOptionButtons.add(binding.btnTestOpt2);
        testOptionButtons.add(binding.btnTestOpt3);
        testOptionButtons.add(binding.btnTestOpt4);

        for (Button btn : testOptionButtons) {
            btn.setOnClickListener(v -> handleListeningOptionClicked(btn.getText().toString()));
        }

        binding.btnPrevLevel.setOnClickListener(v -> {
            if (currentLevelNumber > 1) {
                loadLevel(currentLevelNumber - 1);
            }
        });

        binding.btnNextLevel.setOnClickListener(v -> {
            int next = currentLevelNumber + 1;
            if (next <= MorseBinaryTree.getInstance().getTotalLevels()) {
                if (activity.getSettings().isLevelUnlocked(next)) {
                    loadLevel(next);
                } else {
                    Toast.makeText(getContext(), "🔒 Nível bloqueado! Passe o teste de Ouvir e Mandar deste nível para desbloquear.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Sound previews for 2 new letters
        binding.btnPlayChar1.setOnClickListener(v -> {
            if (currentLevel != null) {
                activity.getSynthesizer().playMorsePattern(currentLevel.getMorse1(), activity.getSettings().getWpm(), null);
            }
        });

        binding.btnPlayChar2.setOnClickListener(v -> {
            if (currentLevel != null) {
                activity.getSynthesizer().playMorsePattern(currentLevel.getMorse2(), activity.getSettings().getWpm(), null);
            }
        });

        binding.btnStartLevelTest.setOnClickListener(v -> startExam());
        binding.btnCancelTest.setOnClickListener(v -> cancelExam());

        binding.btnReplayTestAudio.setOnClickListener(v -> playCurrentListeningAudio());

        binding.btnResultAction.setOnClickListener(v -> {
            boolean passed = (listeningScore >= PASSING_SCORE) && (sendingScore >= PASSING_SCORE);
            if (passed) {
                int nextLevel = currentLevelNumber + 1;
                if (nextLevel <= MorseBinaryTree.getInstance().getTotalLevels()) {
                    loadLevel(nextLevel);
                } else {
                    Toast.makeText(getContext(), "🏆 Parabéns! Concluiu todos os níveis da Árvore de Morse!", Toast.LENGTH_LONG).show();
                    loadLevel(currentLevelNumber);
                }
            } else {
                startExam();
            }
        });

        binding.btnReviewLevel.setOnClickListener(v -> showStudyView());

        // Dedicated bottom paddle buttons: DI (• PONTO) and DAH (— TRAÇO)
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

        loadLevel(currentLevelNumber);
    }

    public void loadLevel(int levelNum) {
        this.currentLevelNumber = levelNum;
        this.currentLevel = MorseBinaryTree.getInstance().getLevel(levelNum);

        MainActivity activity = (MainActivity) getActivity();
        boolean isUnlocked = activity != null && activity.getSettings().isLevelUnlocked(levelNum);

        int maxLevels = MorseBinaryTree.getInstance().getTotalLevels();
        binding.tvLevelNumber.setText("NÍVEL " + currentLevel.getLevelNumber() + " / " + maxLevels + (isUnlocked ? "" : " 🔒"));
        binding.tvLevelTitle.setText(currentLevel.getTitle());

        binding.tvNewChar1.setText(currentLevel.getChar1());
        binding.tvNewMorse1.setText(currentLevel.getMorse1());

        binding.tvNewChar2.setText(currentLevel.getChar2());
        binding.tvNewMorse2.setText(currentLevel.getMorse2());

        List<String> pool = currentLevel.getAllCharacters();
        StringBuilder poolSb = new StringBuilder("Letras que vão sair no teste (" + pool.size() + "): ");
        for (int i = 0; i < pool.size(); i++) {
            poolSb.append(pool.get(i));
            if (i < pool.size() - 1) poolSb.append(", ");
        }
        binding.tvPoolDescription.setText(poolSb.toString());

        // Update Next button indicator
        int nextLevel = levelNum + 1;
        boolean nextUnlocked = activity != null && activity.getSettings().isLevelUnlocked(nextLevel);
        binding.btnNextLevel.setAlpha(nextUnlocked ? 1.0f : 0.4f);

        showStudyView();
    }

    private void showStudyView() {
        currentStage = TestStage.STUDY;
        binding.layoutStudyView.setVisibility(View.VISIBLE);
        binding.layoutTestView.setVisibility(View.GONE);
        binding.layoutResultView.setVisibility(View.GONE);
    }

    // ================= EXAM WORKFLOW =================

    private void startExam() {
        currentStage = TestStage.LISTENING;
        listeningIndex = 0;
        listeningScore = 0;
        sendingIndex = 0;
        sendingScore = 0;

        binding.layoutStudyView.setVisibility(View.GONE);
        binding.layoutTestView.setVisibility(View.VISIBLE);
        binding.layoutResultView.setVisibility(View.GONE);

        startListeningQuestion();
    }

    private void cancelExam() {
        showStudyView();
    }

    // --- STAGE 1: LISTENING (OUVIR) ---

    private void startListeningQuestion() {
        currentStage = TestStage.LISTENING;
        binding.tvTestPhaseBanner.setText("PARTE 1 DE 2: TESTE DE ESCUTA (OUVIR)");
        binding.tvTestPhaseBanner.setBackgroundColor(Color.parseColor("#00E5FF"));
        binding.layoutStageListening.setVisibility(View.VISIBLE);
        binding.layoutStageSending.setVisibility(View.GONE);

        binding.tvTestProgress.setText("Pergunta " + (listeningIndex + 1) + " de " + TOTAL_QUESTIONS_PER_STAGE + " | Acertos: " + listeningScore);

        // Pick random answer from pool
        List<String> pool = new ArrayList<>(currentLevel.getAllCharacters());
        Collections.shuffle(pool);
        currentListeningAnswer = pool.get(0);

        // 4 options
        List<String> options = new ArrayList<>();
        options.add(currentListeningAnswer);
        for (String c : pool) {
            if (!options.contains(c) && options.size() < 4) {
                options.add(c);
            }
        }
        while (options.size() < 4) {
            char r = (char) ('A' + (int) (Math.random() * 26));
            String rStr = String.valueOf(r);
            if (!options.contains(rStr)) options.add(rStr);
        }
        Collections.shuffle(options);

        for (int i = 0; i < testOptionButtons.size(); i++) {
            Button btn = testOptionButtons.get(i);
            btn.setText(options.get(i));
            btn.setBackgroundColor(Color.parseColor("#21262D"));
            btn.setEnabled(true);
        }

        playCurrentListeningAudio();
    }

    private void playCurrentListeningAudio() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;
        String morse = MorseBinaryTree.getInstance().getMorse(currentListeningAnswer);
        if (morse != null) {
            activity.getSynthesizer().playMorsePattern(morse, activity.getSettings().getWpm(), null);
        }
    }

    private void handleListeningOptionClicked(String selected) {
        for (Button btn : testOptionButtons) {
            btn.setEnabled(false);
        }

        boolean isCorrect = selected.equalsIgnoreCase(currentListeningAnswer);
        if (isCorrect) {
            listeningScore++;
        }

        for (Button btn : testOptionButtons) {
            if (btn.getText().toString().equalsIgnoreCase(currentListeningAnswer)) {
                btn.setBackgroundColor(Color.parseColor("#00E676"));
            } else if (btn.getText().toString().equalsIgnoreCase(selected) && !isCorrect) {
                btn.setBackgroundColor(Color.parseColor("#FF5252"));
            }
        }

        listeningIndex++;
        binding.tvTestProgress.setText("Pergunta " + listeningIndex + " de " + TOTAL_QUESTIONS_PER_STAGE + " | Acertos: " + listeningScore);

        binding.getRoot().postDelayed(() -> {
            if (listeningIndex < TOTAL_QUESTIONS_PER_STAGE) {
                startListeningQuestion();
            } else {
                // Listening stage complete! Proceed to Sending Stage
                startSendingStage();
            }
        }, 900);
    }

    // --- STAGE 2: SENDING (MANDAR) ---

    private void startSendingStage() {
        currentStage = TestStage.SENDING;
        binding.tvTestPhaseBanner.setText("PARTE 2 DE 2: TESTE DE ENVIO (MANDAR)");
        binding.tvTestPhaseBanner.setBackgroundColor(Color.parseColor("#FFB300"));
        binding.layoutStageListening.setVisibility(View.GONE);
        binding.layoutStageSending.setVisibility(View.VISIBLE);

        sendingIndex = 0;
        sendingScore = 0;
        nextSendingQuestion();
    }

    private void nextSendingQuestion() {
        sendingWaitingForInput = true;
        binding.tvTestProgress.setText("Desafio " + (sendingIndex + 1) + " de " + TOTAL_QUESTIONS_PER_STAGE + " | Acertos: " + sendingScore);

        List<String> pool = new ArrayList<>(currentLevel.getAllCharacters());
        Collections.shuffle(pool);
        currentSendingPrompt = pool.get(0);

        binding.tvSendingPrompt.setText(currentSendingPrompt);
        binding.tvSendingBuffer.setText("A introduzir: —");
        binding.tvSendingFeedback.setText("Use os botões DI e DAH abaixo para transmitir '" + currentSendingPrompt + "'");
        binding.tvSendingFeedback.setTextColor(Color.parseColor("#8B949E"));

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.getDecoder().clear();
        }
    }

    @Override
    public void onCharacterDecoded(char character) {
        if (binding == null || currentStage != TestStage.SENDING || !sendingWaitingForInput) return;

        sendingWaitingForInput = false;
        String decodedStr = String.valueOf(character).toUpperCase();
        boolean isCorrect = decodedStr.equalsIgnoreCase(currentSendingPrompt);

        if (isCorrect) {
            sendingScore++;
            binding.tvSendingFeedback.setText("✓ Correto! Transmitiu '" + character + "' perfeitamente!");
            binding.tvSendingFeedback.setTextColor(Color.parseColor("#00E676"));
        } else {
            binding.tvSendingFeedback.setText("✗ Incorreto: Transmitiu '" + character + "', mas o pedido era '" + currentSendingPrompt + "'.");
            binding.tvSendingFeedback.setTextColor(Color.parseColor("#FF5252"));
        }

        sendingIndex++;
        binding.tvTestProgress.setText("Desafio " + sendingIndex + " de " + TOTAL_QUESTIONS_PER_STAGE + " | Acertos: " + sendingScore);

        binding.getRoot().postDelayed(() -> {
            if (sendingIndex < TOTAL_QUESTIONS_PER_STAGE) {
                nextSendingQuestion();
            } else {
                // Exam complete! Show Results
                showExamResults();
            }
        }, 1100);
    }

    @Override
    public void onPatternChanged(String currentPattern) {
        if (binding != null && currentStage == TestStage.SENDING) {
            binding.tvSendingBuffer.setText("A introduzir: " + (currentPattern.isEmpty() ? "—" : currentPattern));
        }
    }

    @Override
    public void onTextUpdated(String fullText) {}

    // --- STAGE 3: RESULTS & UNLOCKING ---

    private void showExamResults() {
        currentStage = TestStage.RESULT;
        binding.layoutStudyView.setVisibility(View.GONE);
        binding.layoutTestView.setVisibility(View.GONE);
        binding.layoutResultView.setVisibility(View.VISIBLE);

        boolean passedListening = listeningScore >= PASSING_SCORE;
        boolean passedSending = sendingScore >= PASSING_SCORE;
        boolean overallPass = passedListening && passedSending;

        MainActivity activity = (MainActivity) getActivity();

        if (overallPass) {
            binding.tvResultTitle.setText("🎉 NÍVEL " + currentLevelNumber + " CONCLUÍDO!");
            binding.tvResultTitle.setTextColor(Color.parseColor("#00E676"));

            StringBuilder sb = new StringBuilder();
            sb.append("Teste de Escuta (Ouvir): ").append(listeningScore).append(" / ").append(TOTAL_QUESTIONS_PER_STAGE).append(" ✓\n");
            sb.append("Teste de Envio (Mandar): ").append(sendingScore).append(" / ").append(TOTAL_QUESTIONS_PER_STAGE).append(" ✓\n");
            int totalCorrect = listeningScore + sendingScore;
            int totalPossible = TOTAL_QUESTIONS_PER_STAGE * 2;
            int percent = (totalCorrect * 100) / totalPossible;
            sb.append("Aproveitamento Final: ").append(percent).append("%");
            binding.tvResultSummary.setText(sb.toString());

            if (activity != null) {
                boolean newlyUnlocked = activity.getSettings().unlockNextLevel(currentLevelNumber);
                int nextLevel = currentLevelNumber + 1;
                if (newlyUnlocked && nextLevel <= MorseBinaryTree.getInstance().getTotalLevels()) {
                    binding.tvResultUnlockMsg.setText("🔓 Desbloqueou o Nível " + nextLevel + " na Árvore Binária de Morse!");
                    binding.tvResultUnlockMsg.setVisibility(View.VISIBLE);
                } else {
                    binding.tvResultUnlockMsg.setText("Nível já desbloqueado anteriormente.");
                    binding.tvResultUnlockMsg.setVisibility(View.VISIBLE);
                }
            }

            binding.btnResultAction.setText("AVANÇAR PARA O PRÓXIMO NÍVEL ▶");
            binding.btnResultAction.setBackgroundColor(Color.parseColor("#FFB300"));
        } else {
            binding.tvResultTitle.setText("❌ NÃO PASSOU NO TESTE");
            binding.tvResultTitle.setTextColor(Color.parseColor("#FF5252"));

            StringBuilder sb = new StringBuilder();
            sb.append("Teste de Escuta (Ouvir): ").append(listeningScore).append(" / ").append(TOTAL_QUESTIONS_PER_STAGE)
                    .append(passedListening ? " ✓\n" : " (Mínimo: " + PASSING_SCORE + ")\n");
            sb.append("Teste de Envio (Mandar): ").append(sendingScore).append(" / ").append(TOTAL_QUESTIONS_PER_STAGE)
                    .append(passedSending ? " ✓\n" : " (Mínimo: " + PASSING_SCORE + ")\n");
            sb.append("\nÉ necessário acertar pelo menos 80% (4 de 5) em AMBAS as etapas para desbloquear o próximo nível.");
            binding.tvResultSummary.setText(sb.toString());

            binding.tvResultUnlockMsg.setVisibility(View.GONE);

            binding.btnResultAction.setText("REPETIR TESTE 🔄");
            binding.btnResultAction.setBackgroundColor(Color.parseColor("#FF5252"));
        }
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
