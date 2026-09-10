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
import com.morsego.app.keyer.KeyerSettings;
import com.morsego.app.keyer.MorseDecoder;
import com.morsego.app.tree.MorseBinaryTree;
import com.morsego.app.tree.TreeLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LearnFragment extends Fragment implements MorseDecoder.DecoderListener {

    private static final int MAX_ALLOWED_FAILURES = 3;

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

    // Active test queues & state
    private final LinkedList<String> currentQueue = new LinkedList<>();
    private int questionsAnsweredInStage = 0;
    private int currentStageFailures = 0;

    // Listening test state
    private String currentListeningTarget = "";
    private final List<Button> testOptionButtons = new ArrayList<>();

    // Sending test state
    private String currentSendingTarget = "";
    private boolean sendingWaitingForInput = false;

    // Overall test results
    private int listeningTotalFailures = 0;
    private int sendingTotalFailures = 0;

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
                    Toast.makeText(getContext(), "🔒 Nível bloqueado! Passe o teste deste nível para desbloquear.", Toast.LENGTH_SHORT).show();
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
            boolean passed = (listeningTotalFailures < MAX_ALLOWED_FAILURES) && (sendingTotalFailures < MAX_ALLOWED_FAILURES);
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
        listeningTotalFailures = 0;
        sendingTotalFailures = 0;

        binding.layoutStudyView.setVisibility(View.GONE);
        binding.layoutTestView.setVisibility(View.VISIBLE);
        binding.layoutResultView.setVisibility(View.GONE);

        startListeningStage();
    }

    private void cancelExam() {
        showStudyView();
    }

    /**
     * Builds the test queue:
     * - Every letter from current level's pool (all previous + 2 new letters) included at least once.
     * - Plus 25% extra questions drawn from the most failed letters.
     * - Shuffled randomly!
     */
    private LinkedList<String> generateExamQueue() {
        MainActivity activity = (MainActivity) getActivity();
        KeyerSettings settings = activity != null ? activity.getSettings() : new KeyerSettings(requireContext());

        List<String> pool = new ArrayList<>(currentLevel.getAllCharacters());
        int baseCount = pool.size();
        int extra25Percent = Math.max(1, (int) Math.round(baseCount * 0.25));

        List<String> mostFailed = settings.getMostFailedLetters(pool, extra25Percent);

        List<String> combined = new ArrayList<>(pool);
        combined.addAll(mostFailed);

        Collections.shuffle(combined);
        return new LinkedList<>(combined);
    }

    private void updateLivesUi() {
        if (binding == null) return;
        int remaining = MAX_ALLOWED_FAILURES - currentStageFailures;
        StringBuilder hearts = new StringBuilder("Vidas: ");
        for (int i = 0; i < MAX_ALLOWED_FAILURES; i++) {
            hearts.append(i < remaining ? "❤️" : "🖤");
        }
        hearts.append(" (").append(currentStageFailures).append("/").append(MAX_ALLOWED_FAILURES).append(" falhas)");
        binding.tvTestLives.setText(hearts.toString());
        binding.tvTestLives.setTextColor(remaining > 1 ? Color.parseColor("#FFB300") : Color.parseColor("#FF5252"));

        binding.tvTestProgress.setText("Restam: " + currentQueue.size() + " na fila | Feitas: " + questionsAnsweredInStage);
    }

    // --- STAGE 1: LISTENING (OUVIR) ---

    private void startListeningStage() {
        currentStage = TestStage.LISTENING;
        binding.tvTestPhaseBanner.setText("PARTE 1 DE 2: TESTE DE ESCUTA (OUVIR)");
        binding.tvTestPhaseBanner.setBackgroundColor(Color.parseColor("#00E5FF"));
        binding.layoutStageListening.setVisibility(View.VISIBLE);
        binding.layoutStageSending.setVisibility(View.GONE);
        binding.tvPenaltyNotice.setVisibility(View.GONE);

        currentQueue.clear();
        currentQueue.addAll(generateExamQueue());
        questionsAnsweredInStage = 0;
        currentStageFailures = 0;

        nextListeningQuestion();
    }

    private void nextListeningQuestion() {
        if (currentQueue.isEmpty()) {
            // Stage 1 completed successfully!
            listeningTotalFailures = currentStageFailures;
            startSendingStage();
            return;
        }

        currentListeningTarget = currentQueue.poll();
        updateLivesUi();

        // 4 options
        List<String> pool = new ArrayList<>(currentLevel.getAllCharacters());
        List<String> options = new ArrayList<>();
        options.add(currentListeningTarget);

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
        String morse = MorseBinaryTree.getInstance().getMorse(currentListeningTarget);
        if (morse != null) {
            activity.getSynthesizer().playMorsePattern(morse, activity.getSettings().getWpm(), null);
        }
    }

    private void handleListeningOptionClicked(String selected) {
        for (Button btn : testOptionButtons) {
            btn.setEnabled(false);
        }

        questionsAnsweredInStage++;
        boolean isCorrect = selected.equalsIgnoreCase(currentListeningTarget);

        if (isCorrect) {
            binding.tvPenaltyNotice.setVisibility(View.GONE);
            for (Button btn : testOptionButtons) {
                if (btn.getText().toString().equalsIgnoreCase(currentListeningTarget)) {
                    btn.setBackgroundColor(Color.parseColor("#00E676"));
                }
            }
            binding.getRoot().postDelayed(this::nextListeningQuestion, 700);
        } else {
            // FAILED THIS QUESTION!
            currentStageFailures++;
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                activity.getSettings().recordLetterFailure(currentListeningTarget);
            }

            // Penalty: Add failed letter back + 2 random letters from pool
            applyFailurePenalty(currentListeningTarget);

            for (Button btn : testOptionButtons) {
                if (btn.getText().toString().equalsIgnoreCase(currentListeningTarget)) {
                    btn.setBackgroundColor(Color.parseColor("#00E676"));
                } else if (btn.getText().toString().equalsIgnoreCase(selected)) {
                    btn.setBackgroundColor(Color.parseColor("#FF5252"));
                }
            }

            updateLivesUi();

            if (currentStageFailures >= MAX_ALLOWED_FAILURES) {
                // Game Over / Failed due to strikes!
                listeningTotalFailures = currentStageFailures;
                binding.getRoot().postDelayed(() -> showExamResults(false, "Excedeu o limite de 3 falhas no Teste de Escuta."), 1200);
            } else {
                binding.getRoot().postDelayed(this::nextListeningQuestion, 1200);
            }
        }
    }

    // --- STAGE 2: SENDING (MANDAR) ---

    private void startSendingStage() {
        currentStage = TestStage.SENDING;
        binding.tvTestPhaseBanner.setText("PARTE 2 DE 2: TESTE DE ENVIO (MANDAR)");
        binding.tvTestPhaseBanner.setBackgroundColor(Color.parseColor("#FFB300"));
        binding.layoutStageListening.setVisibility(View.GONE);
        binding.layoutStageSending.setVisibility(View.VISIBLE);
        binding.tvPenaltyNotice.setVisibility(View.GONE);

        currentQueue.clear();
        currentQueue.addAll(generateExamQueue());
        questionsAnsweredInStage = 0;
        currentStageFailures = 0;

        nextSendingQuestion();
    }

    private void nextSendingQuestion() {
        if (currentQueue.isEmpty()) {
            // Stage 2 completed successfully!
            sendingTotalFailures = currentStageFailures;
            showExamResults(true, "Parabéns! Passou no teste de Ouvir e Mandar!");
            return;
        }

        sendingWaitingForInput = true;
        currentSendingPrompt = currentQueue.poll();
        updateLivesUi();

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
        questionsAnsweredInStage++;

        String decodedStr = String.valueOf(character).toUpperCase();
        boolean isCorrect = decodedStr.equalsIgnoreCase(currentSendingPrompt);

        if (isCorrect) {
            binding.tvPenaltyNotice.setVisibility(View.GONE);
            binding.tvSendingFeedback.setText("✓ Correto! Transmitiu '" + character + "' perfeitamente!");
            binding.tvSendingFeedback.setTextColor(Color.parseColor("#00E676"));
            binding.getRoot().postDelayed(this::nextSendingQuestion, 800);
        } else {
            // FAILED SENDING QUESTION!
            currentStageFailures++;
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                activity.getSettings().recordLetterFailure(currentSendingPrompt);
            }

            // Penalty: Add failed letter back + 2 random letters from pool
            applyFailurePenalty(currentSendingPrompt);

            binding.tvSendingFeedback.setText("✗ Incorreto: Transmitiu '" + character + "', mas o pedido era '" + currentSendingPrompt + "'.");
            binding.tvSendingFeedback.setTextColor(Color.parseColor("#FF5252"));

            updateLivesUi();

            if (currentStageFailures >= MAX_ALLOWED_FAILURES) {
                // Game Over in sending stage!
                sendingTotalFailures = currentStageFailures;
                binding.getRoot().postDelayed(() -> showExamResults(false, "Excedeu o limite de 3 falhas no Teste de Envio."), 1200);
            } else {
                binding.getRoot().postDelayed(this::nextSendingQuestion, 1400);
            }
        }
    }

    /**
     * Penalty rule: "cada falha adiciona uma letra que falhou mais duas aleatoreas"
     */
    private void applyFailurePenalty(String failedLetter) {
        List<String> pool = currentLevel.getAllCharacters();
        String rand1 = pool.get((int) (Math.random() * pool.size()));
        String rand2 = pool.get((int) (Math.random() * pool.size()));

        currentQueue.add(failedLetter);
        currentQueue.add(rand1);
        currentQueue.add(rand2);

        // Shuffle queue so repeated letters don't strictly appear in a row
        Collections.shuffle(currentQueue);

        binding.tvPenaltyNotice.setText("⚠️ Falha! Adicionada a letra '" + failedLetter + "' + 2 aleatórias (" + rand1 + ", " + rand2 + ") à fila!");
        binding.tvPenaltyNotice.setVisibility(View.VISIBLE);
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

    private void showExamResults(boolean passed, String detailMessage) {
        currentStage = TestStage.RESULT;
        binding.layoutStudyView.setVisibility(View.GONE);
        binding.layoutTestView.setVisibility(View.GONE);
        binding.layoutResultView.setVisibility(View.VISIBLE);

        MainActivity activity = (MainActivity) getActivity();

        if (passed) {
            binding.tvResultTitle.setText("🎉 NÍVEL " + currentLevelNumber + " CONCLUÍDO!");
            binding.tvResultTitle.setTextColor(Color.parseColor("#00E676"));

            String summary = "Etapa 1 (Ouvir): Aprovado (Falhas: " + listeningTotalFailures + "/3)\n" +
                    "Etapa 2 (Mandar): Aprovado (Falhas: " + sendingTotalFailures + "/3)\n\n" +
                    "Dominou todas as letras anteriores e novas deste nível!";
            binding.tvResultSummary.setText(summary);

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
            binding.tvResultTitle.setText("❌ TESTE FALHADO");
            binding.tvResultTitle.setTextColor(Color.parseColor("#FF5252"));

            String summary = detailMessage + "\n\n" +
                    "Regra: O teste só tolera até 3 falhas no total.\n" +
                    "Pratique as letras com maior taxa de erro e tente novamente!";
            binding.tvResultSummary.setText(summary);
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
