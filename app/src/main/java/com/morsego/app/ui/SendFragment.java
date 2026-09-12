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
import com.morsego.app.databinding.FragmentSendBinding;
import com.morsego.app.keyer.KeyerSettings;
import com.morsego.app.keyer.MorseDecoder;
import com.morsego.app.keyer.MorseTiming;
import com.morsego.app.tree.MorseBinaryTree;
import com.morsego.app.tree.MorseWordGenerator;
import com.morsego.app.tree.TreeLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SendFragment extends Fragment implements MorseDecoder.DecoderListener {

    private static final int MAX_ALLOWED_FAILURES = 3;
    private static final int NUM_NEW_LETTER_QUESTIONS = 4;

    private enum TestStage {
        STUDY,
        LISTENING,
        SENDING,
        RESULT
    }

    private FragmentSendBinding binding;

    private int currentLevelNumber = 1;
    private TreeLevel currentLevel;

    private TestStage currentStage = TestStage.STUDY;

    // Active test queue & state
    private final LinkedList<String> currentQueue = new LinkedList<>();
    private int questionsAnsweredInStage = 0;
    private int currentStageFailures = 0;

    // Listening test state
    private String currentListeningTarget = "";
    private final List<Button> testOptionButtons = new ArrayList<>();

    // Sending test state
    private String currentSendingTarget = "";
    private boolean sendingWaitingForInput = false;
    private final StringBuilder currentWordKeyed = new StringBuilder();

    // Overall test results
    private int listeningTotalFailures = 0;
    private int sendingTotalFailures = 0;
    private boolean listeningStagePassed = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSendBinding.inflate(inflater, container, false);
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
            btn.setOnClickListener(v -> {
                Object tag = btn.getTag();
                String val = (tag instanceof String) ? (String) tag : btn.getText().toString();
                handleListeningOptionClicked(val);
            });
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
                    boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
                    Toast.makeText(getContext(), isPt ? "🔒 Nível bloqueado! Passe o teste deste nível para desbloquear." : "🔒 Level locked! Pass this level's test to unlock.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Sound previews for 2 new letters
        binding.btnPlayChar1.setOnClickListener(v -> {
            if (currentLevel != null) {
                activity.playMorse(currentLevel.getMorse1(), activity.getSettings().getWpm(), null);
            }
        });

        binding.btnPlayChar2.setOnClickListener(v -> {
            if (currentLevel != null) {
                activity.playMorse(currentLevel.getMorse2(), activity.getSettings().getWpm(), null);
            }
        });

        binding.btnStartLevelTest.setOnClickListener(v -> {
            startExam();
        });
        binding.btnCancelTest.setOnClickListener(v -> cancelExam());
        binding.btnReplayTestAudio.setOnClickListener(v -> playCurrentListeningAudio());

        binding.btnResultAction.setOnClickListener(v -> {
            boolean passed = (sendingTotalFailures < MAX_ALLOWED_FAILURES);
            if (passed) {
                int nextLevel = currentLevelNumber + 1;
                if (nextLevel <= MorseBinaryTree.getInstance().getTotalLevels()) {
                    loadLevel(nextLevel);
                } else {
                    boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
                    Toast.makeText(getContext(), isPt ? "🏆 Parabéns! Concluiu todos os níveis da Árvore de Morse!" : "🏆 Congratulations! You completed all levels of the Morse Tree!", Toast.LENGTH_LONG).show();
                    loadLevel(currentLevelNumber);
                }
            } else {
                startExam();
            }
        });

        binding.btnReviewLevel.setOnClickListener(v -> showStudyView());

        binding.btnResetSendingAttempt.setOnClickListener(v -> {
            currentWordKeyed.setLength(0);
            updateSendingMorseProgress();
            boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
            binding.tvSendingBuffer.setText(isPt ? "A introduzir: —" : "Input: —");
            MainActivity act = (MainActivity) getActivity();
            if (act != null) {
                act.getDecoder().clear();
            }
        });

        // Dedicated bottom paddle buttons: DIT (•) and DAH (—)
        binding.btnTouchDit.setOnTouchListener((v, event) -> {
            if (!sendingWaitingForInput && currentStage == TestStage.SENDING) {
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                activity.getInputManager().setTouchDit(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                activity.getInputManager().setTouchDit(false);
            }
            return true;
        });

        binding.btnTouchDah.setOnTouchListener((v, event) -> {
            if (!sendingWaitingForInput && currentStage == TestStage.SENDING) {
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                activity.getInputManager().setTouchDah(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                activity.getInputManager().setTouchDah(false);
            }
            return true;
        });

        applyLocalization();
        loadLevel(currentLevelNumber);
    }

    private void setSendingPaddlesEnabled(boolean enabled) {
        if (binding == null) return;
        binding.btnTouchDit.setEnabled(enabled);
        binding.btnTouchDah.setEnabled(enabled);
        float alpha = enabled ? 1.0f : 0.35f;
        binding.btnTouchDit.setAlpha(alpha);
        binding.btnTouchDah.setAlpha(alpha);
    }

    private void prepareForNextSendingQuestion(long delayMs) {
        sendingWaitingForInput = false;
        setSendingPaddlesEnabled(false);

        if (binding != null) {
            // Hide/dim previous letter and show transition indicator so user clearly sees the change
            binding.tvSendingPrompt.setText("· · ·");
            binding.tvSendingPrompt.setAlpha(0.35f);
            binding.tvSendingMorseProgress.setText("—");
            boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
            binding.tvSendingBuffer.setText(isPt ? "A preparar próxima letra..." : "Loading next character...");
            binding.getRoot().postDelayed(this::nextSendingQuestion, delayMs);
        }
    }

    private void applyLocalization() {
        if (binding == null) return;
        boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
        binding.tvPaddleHeaderLabel.setText(isPt ?
                "PÁS DE TRANSMISSÃO (DI / DAH OU MANIPULADOR USB):" :
                "KEYER PADDLES (DIT / DAH OR USB KEYER):");
        binding.btnTouchDit.setText(isPt ? "DI (•)\n[ESQ]" : "DIT (•)\n[LEFT]");
        binding.btnTouchDah.setText(isPt ? "DAH (—)\n[DIR]" : "DAH (—)\n[RIGHT]");
        binding.btnCancelTest.setText(isPt ? "Reiniciar Teste" : "Restart Test");
        binding.btnResetSendingAttempt.setText(isPt ? "🔄 Limpar / Recomeçar Palavra" : "🔄 Clear / Restart Word");
        binding.tvListeningInstruction.setText(isPt ? "Toque para ouvir novamente" : "Tap to listen again");
        binding.tvNewLettersLabel.setText(isPt ? "2 NOVOS CARACTERES DESTE NÍVEL:" : "2 NEW CHARACTERS IN THIS LEVEL:");
        binding.tvPlayChar1Label.setText(isPt ? "Ouvir som 🔊" : "Listen 🔊");
        binding.tvPlayChar2Label.setText(isPt ? "Ouvir som 🔊" : "Listen 🔊");
        binding.tvRequirementsTitle.setText(isPt ? "REQUISITOS DO TESTE DE SEND (TRANSMISSÃO):" : "SEND TEST ADVANCEMENT REQUIREMENTS:");
        binding.tvRequirementsDesc.setText(isPt ?
                "• Caracteres no teste: todos os caracteres disponíveis × 4.\n• 4 palavras com os novos caracteres.\n• 4 palavras aleatórias formadas com o vocabulário disponível.\n• Perguntas 100% dinâmicas e aleatórias.\n• Só pode falhar no máximo 3 vezes (3 vidas ❤️❤️❤️).\n• Cada falha adiciona o item + 2 itens aleatórios à fila do teste!\n• Complete a transmissão com sucesso para desbloquear o próximo nível." :
                "• Characters in test: all available characters × 4.\n• 4 words featuring new characters.\n• 4 random words from the learned pool.\n• 100% dynamic & randomized questions.\n• Up to 3 mistakes allowed (3 lives ❤️❤️❤️).\n• Each mistake queues the item + 2 random items!\n• Pass the transmission test to unlock the next level.");
        binding.btnStartLevelTest.setText(isPt ? "INICIAR TESTE DE SEND ▶" : "START SEND TEST ▶");
        binding.btnReviewLevel.setText(isPt ? "Rever Este Nível" : "Review This Level");
    }

    public void loadLevel(int levelNum) {
        applyLocalization();
        this.currentLevelNumber = levelNum;
        this.currentLevel = MorseBinaryTree.getInstance().getLevel(levelNum);
        this.listeningStagePassed = false;

        MainActivity activity = (MainActivity) getActivity();
        boolean isUnlocked = activity != null && activity.getSettings().isLevelUnlocked(levelNum);
        boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");

        int maxLevels = MorseBinaryTree.getInstance().getTotalLevels();
        String levelPrefix = isPt ? "NÍVEL " : "LEVEL ";
        binding.tvLevelNumber.setText(levelPrefix + currentLevel.getLevelNumber() + " / " + maxLevels + (isUnlocked ? "" : " 🔒"));
        binding.tvLevelTitle.setText(currentLevel.getTitle());

        binding.tvNewChar1.setText(currentLevel.getChar1());
        binding.tvNewMorse1.setText(currentLevel.getMorse1());

        binding.tvNewChar2.setText(currentLevel.getChar2());
        binding.tvNewMorse2.setText(currentLevel.getMorse2());

        List<String> pool = currentLevel.getAllCharacters();
        String poolPrefix = isPt ? "Letras no teste (" : "Characters in test (";
        StringBuilder poolSb = new StringBuilder(poolPrefix + pool.size() + "): ");
        for (int i = 0; i < pool.size(); i++) {
            poolSb.append(pool.get(i));
            if (i < pool.size() - 1) poolSb.append(", ");
        }
        binding.tvPoolDescription.setText(poolSb.toString());

        // Update Next button indicator
        int nextLevel = levelNum + 1;
        boolean nextUnlocked = activity != null && activity.getSettings().isLevelUnlocked(nextLevel);
        binding.btnNextLevel.setAlpha(nextUnlocked ? 1.0f : 0.4f);

        // Go directly to the transmission test in Send tab!
        startExam();
    }

    private void showStudyView() {
        startExam();
    }

    // ================= EXAM WORKFLOW =================

    private void startExam() {
        sendingTotalFailures = 0;
        listeningTotalFailures = 0;

        binding.layoutStudyView.setVisibility(View.GONE);
        binding.layoutTestView.setVisibility(View.VISIBLE);
        binding.layoutResultView.setVisibility(View.GONE);

        startSendingStage();
    }

    private void startSendingStageDirectly() {
        startExam();
    }

    private void cancelExam() {
        startExam();
    }

    /**
     * Builds the exam queue following the exact specification:
     * 1. Total exam questions strictly capped at 40 max.
     * 2. The 2 new characters of this level appear exactly 4 times each (8 questions).
     * 3. Previous characters appear at least once each (e.g. at level 2: 8 new + at least 2 previous).
     * 4. Words featuring new letters and pool (up to capacity, total <= 40).
     */
    public static LinkedList<String> generateExamQueueForLevel(TreeLevel level, KeyerSettings settings) {
        if (level == null) return new LinkedList<>();

        List<String> pool = new ArrayList<>(level.getAllCharacters());
        List<String> newLetters = new ArrayList<>();
        if (level.getChar1() != null && !level.getChar1().isEmpty()) {
            newLetters.add(level.getChar1());
        }
        if (level.getChar2() != null && !level.getChar2().isEmpty() && !newLetters.contains(level.getChar2())) {
            newLetters.add(level.getChar2());
        }

        List<String> previousLetters = new ArrayList<>();
        for (String c : pool) {
            if (!newLetters.contains(c)) {
                previousLetters.add(c);
            }
        }

        List<String> letterQuestions = new ArrayList<>();
        // 1. New letters appear 4 times each
        for (String letter : newLetters) {
            for (int i = 0; i < 4; i++) {
                letterQuestions.add(letter);
            }
        }

        // 2. Previous letters appear at least once (or 2 times if space permits)
        if (!previousLetters.isEmpty()) {
            List<String> prevShuffled = new ArrayList<>(previousLetters);
            Collections.shuffle(prevShuffled);

            // Reserve room so letters + words <= 40
            int maxSlotForPrev = 32 - letterQuestions.size();
            int reps = (previousLetters.size() * 2 <= maxSlotForPrev) ? 2 : 1;

            for (int r = 0; r < reps; r++) {
                for (String prev : prevShuffled) {
                    if (letterQuestions.size() < 34) {
                        letterQuestions.add(prev);
                    }
                }
            }
        }

        Collections.shuffle(letterQuestions);

        // 3. Word questions (up to remaining space, ensuring total <= 40)
        List<String> wordQuestions = new ArrayList<>();
        int maxWords = Math.min(6, 40 - letterQuestions.size());
        if (maxWords > 0 && level.getLevelNumber() > 1) {
            List<String> newLetterWords = MorseWordGenerator.generateWordsWithNewLetters(
                    level.getChar1(), level.getChar2(), pool, Math.min(4, maxWords));
            wordQuestions.addAll(newLetterWords);

            int remainingForWords = maxWords - wordQuestions.size();
            if (remainingForWords > 0 && settings != null) {
                List<String> mostFailed = settings.getMostFailedLetters(pool, remainingForWords);
                List<String> randomWords = MorseWordGenerator.generateRandomWords(pool, mostFailed, remainingForWords);
                wordQuestions.addAll(randomWords);
            }

            if (com.morsego.app.tree.MorseRadioWords.isUnlocked(level.getLevelNumber())) {
                List<com.morsego.app.tree.MorseRadioWords.RadioWordItem> radioList = new ArrayList<>(com.morsego.app.tree.MorseRadioWords.RADIO_WORDS);
                Collections.shuffle(radioList);
                int addCount = Math.min(2, Math.min(radioList.size(), 40 - (letterQuestions.size() + wordQuestions.size())));
                for (int i = 0; i < addCount; i++) {
                    wordQuestions.add(radioList.get(i).word);
                }
            }
        }

        Collections.shuffle(wordQuestions);

        LinkedList<String> queue = new LinkedList<>();
        queue.addAll(letterQuestions);
        queue.addAll(wordQuestions);

        // Hard guarantee: maximum number of questions is strictly <= 40
        while (queue.size() > 40) {
            queue.removeLast();
        }

        return queue;
    }

    private LinkedList<String> generateExamQueue() {
        MainActivity activity = (MainActivity) getActivity();
        KeyerSettings settings = activity != null ? activity.getSettings() : null;
        return generateExamQueueForLevel(currentLevel, settings);
    }

    private void updateLivesUi() {
        if (binding == null) return;
        boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
        int remaining = MAX_ALLOWED_FAILURES - currentStageFailures;
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < MAX_ALLOWED_FAILURES; i++) {
            hearts.append(i < remaining ? "❤️ " : "🖤 ");
        }
        binding.tvTestLives.setText(hearts.toString().trim());
        binding.tvTestLives.setVisibility(View.VISIBLE);

        int totalLetters = (currentLevel != null) ? Math.min(40, currentLevel.getAllCharacters().size() * 4) : 8;
        String typeStr = (questionsAnsweredInStage < totalLetters) ?
                (isPt ? "Letras (" + (questionsAnsweredInStage + 1) + "/" + totalLetters + ")" : "Characters (" + (questionsAnsweredInStage + 1) + "/" + totalLetters + ")") :
                (isPt ? "Palavras" : "Words");
        if (binding.tvTestProgress != null) {
            binding.tvTestProgress.setText("[" + typeStr + "] " +
                    (isPt ? "Restam: " + currentQueue.size() + " na fila | Feitas: " + questionsAnsweredInStage :
                            "Remaining: " + currentQueue.size() + " in queue | Done: " + questionsAnsweredInStage));
        }
    }

    // --- STAGE 1: LISTENING (OUVIR / RECEIVE) ---

    private void startListeningStage() {
        currentStage = TestStage.LISTENING;
        boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
        binding.tvTestPhaseBanner.setText(isPt ? "ETAPA 1 DE 2: TREINO DE ESCUTA EM CW (OUVIR)" : "STAGE 1 OF 2: CW LISTENING TRAINING (RECEIVE)");
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
            listeningTotalFailures = currentStageFailures;
            listeningStagePassed = true;
            startSendingStage();
            return;
        }

        currentListeningTarget = currentQueue.poll();
        updateLivesUi();

        List<String> pool = new ArrayList<>(currentLevel.getAllCharacters());
        boolean isSingleLetter = (currentListeningTarget.length() == 1);

        List<String> options;
        if (isSingleLetter) {
            // Options strictly from the level's pool!
            options = new ArrayList<>();
            options.add(currentListeningTarget);
            List<String> otherChars = new ArrayList<>(pool);
            Collections.shuffle(otherChars);
            for (String c : otherChars) {
                if (!options.contains(c) && options.size() < 4) {
                    options.add(c);
                }
            }
        } else {
            // Word question: choices strictly formed by level's pool!
            options = MorseWordGenerator.generateWordChoices(currentListeningTarget, pool, 4);
        }

        // Put letter options in alphabetical order as required
        Collections.sort(options);

        // Bind options to buttons; hide extra buttons if pool has fewer options (e.g. Level 1 only has 2 letters)
        for (int i = 0; i < testOptionButtons.size(); i++) {
            Button btn = testOptionButtons.get(i);
            if (i < options.size()) {
                String opt = options.get(i);
                btn.setVisibility(View.VISIBLE);
                btn.setTag(opt);
                btn.setText(opt);
                btn.setBackgroundColor(Color.parseColor("#21262D"));
                btn.setTextColor(Color.WHITE);
                btn.setEnabled(true);
            } else {
                btn.setVisibility(View.GONE);
            }
        }

        playCurrentListeningAudio();
    }

    private String formatOptionWithMorse(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder(text);
        sb.append("  [ ");
        for (int i = 0; i < text.length(); i++) {
            if (i > 0) sb.append(" ");
            String m = MorseBinaryTree.getInstance().getMorse(String.valueOf(text.charAt(i)));
            if (m != null) {
                sb.append(m.replace("-", "—").replace(".", "•"));
            }
        }
        sb.append(" ]");
        return sb.toString();
    }

    private void playCurrentListeningAudio() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        // Convert word or letter to morse
        StringBuilder morse = new StringBuilder();
        for (int i = 0; i < currentListeningTarget.length(); i++) {
            String c = String.valueOf(currentListeningTarget.charAt(i));
            String m = MorseBinaryTree.getInstance().getMorse(c);
            if (m != null) {
                if (morse.length() > 0) morse.append(" ");
                morse.append(m);
            }
        }

        activity.playMorse(morse.toString(), activity.getSettings().getWpm(), null);
    }

    private void handleListeningOptionClicked(String selected) {
        for (Button btn : testOptionButtons) {
            btn.setEnabled(false);
            Object tag = btn.getTag();
            String opt = (tag instanceof String) ? (String) tag : btn.getText().toString();
            btn.setText(formatOptionWithMorse(opt));
        }

        questionsAnsweredInStage++;
        boolean isCorrect = selected.equalsIgnoreCase(currentListeningTarget);

        if (isCorrect) {
            binding.tvPenaltyNotice.setVisibility(View.GONE);
            for (Button btn : testOptionButtons) {
                Object tag = btn.getTag();
                String opt = (tag instanceof String) ? (String) tag : "";
                if (opt.equalsIgnoreCase(currentListeningTarget)) {
                    btn.setBackgroundColor(Color.parseColor("#00E676"));
                    btn.setTextColor(Color.parseColor("#05080E"));
                }
            }
            binding.getRoot().postDelayed(this::nextListeningQuestion, 800);
        } else {
            // FAILED!
            currentStageFailures++;
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                for (int i = 0; i < currentListeningTarget.length(); i++) {
                    activity.getSettings().recordLetterFailure(String.valueOf(currentListeningTarget.charAt(i)));
                }
            }

            applyFailurePenalty(currentListeningTarget);

            for (Button btn : testOptionButtons) {
                Object tag = btn.getTag();
                String opt = (tag instanceof String) ? (String) tag : "";
                if (opt.equalsIgnoreCase(currentListeningTarget)) {
                    btn.setBackgroundColor(Color.parseColor("#00E676"));
                    btn.setTextColor(Color.parseColor("#05080E"));
                } else if (opt.equalsIgnoreCase(selected)) {
                    btn.setBackgroundColor(Color.parseColor("#FF5252"));
                    btn.setTextColor(Color.WHITE);
                }
            }

            updateLivesUi();

            if (currentStageFailures >= MAX_ALLOWED_FAILURES) {
                listeningTotalFailures = currentStageFailures;
                boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
                String failMsg = isPt ? "Excedeu o limite de 3 falhas no Teste de Escuta (Ouvir)." : "Exceeded limit of 3 failures in Listening Stage.";
                binding.getRoot().postDelayed(() -> showExamResults(false, failMsg), 1200);
            } else {
                binding.getRoot().postDelayed(this::nextListeningQuestion, 1200);
            }
        }
    }

    // --- STAGE 2: SENDING (MANDAR / TRANSMISSION) ---

    private void startSendingStage() {
        currentStage = TestStage.SENDING;
        boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
        binding.tvTestPhaseBanner.setText(isPt ? "TESTE DE SEND (TRANSMISSÃO)" : "SEND TEST (TRANSMISSION)");
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
            sendingTotalFailures = currentStageFailures;
            boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
            String passMsg = isPt ? "Parabéns! Passou no teste de Send!" : "Congratulations! Passed the Send Test!";
            showExamResults(true, passMsg);
            return;
        }

        sendingWaitingForInput = true;
        setSendingPaddlesEnabled(true);
        currentSendingTarget = currentQueue.poll();
        currentWordKeyed.setLength(0);
        updateLivesUi();

        boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
        boolean isSingleLetter = (currentSendingTarget.length() == 1);
        binding.tvSendingPromptLabel.setText(isPt ?
                (isSingleLetter ? "TRANSMITA A LETRA:" : "TRANSMITA A PALAVRA (LETRAS SEPARADAS):") :
                (isSingleLetter ? "TRANSMIT LETTER:" : "TRANSMIT WORD (SEPARATED LETTERS):"));
        binding.tvSendingPrompt.setAlpha(1.0f);
        binding.tvSendingPrompt.setText(currentSendingTarget);
        updateSendingMorseProgress();

        binding.tvSendingBuffer.setText(isPt ? "A introduzir: —" : "Input: —");
        binding.tvTimingFeedback.setText(isPt ? "Cadência de pausa: —" : "Pause cadence: —");
        binding.tvSendingFeedback.setText(isPt ?
                "Use os botões DI e DAH abaixo para transmitir '" + currentSendingTarget + "'" :
                "Use the DIT and DAH buttons below to transmit '" + currentSendingTarget + "'");
        binding.tvSendingFeedback.setTextColor(Color.parseColor("#8B949E"));

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.getDecoder().clear();
        }
    }

    private void updateSendingMorseProgress() {
        if (binding == null || currentSendingTarget == null || currentSendingTarget.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        int targetLen = currentSendingTarget.length();
        int keyedLen = Math.min(currentWordKeyed.length(), targetLen);

        for (int i = 0; i < targetLen; i++) {
            if (i > 0) sb.append("   ");
            if (i < keyedLen) {
                // Character already emitted: reveal dots and dashes
                String c = String.valueOf(currentSendingTarget.charAt(i));
                String m = MorseBinaryTree.getInstance().getMorse(c);
                if (m != null) {
                    sb.append(m.replace("-", "—").replace(".", "•"));
                } else {
                    sb.append(c);
                }
            } else {
                // Not yet emitted: hidden!
                sb.append("[ ? ]");
            }
        }
        binding.tvSendingMorseProgress.setText(sb.toString());
    }

    private void revealFullSendingMorsePattern() {
        if (binding == null || currentSendingTarget == null) return;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currentSendingTarget.length(); i++) {
            if (i > 0) sb.append("   ");
            String c = String.valueOf(currentSendingTarget.charAt(i));
            String m = MorseBinaryTree.getInstance().getMorse(c);
            if (m != null) {
                sb.append(m.replace("-", "—").replace(".", "•"));
            } else {
                sb.append(c);
            }
        }
        binding.tvSendingMorseProgress.setText(sb.toString());
    }

    private void playFailureFeedbackAudioAndVibrate(String target) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null || target == null || target.isEmpty()) return;

        StringBuilder morse = new StringBuilder();
        for (int i = 0; i < target.length(); i++) {
            String c = String.valueOf(target.charAt(i));
            String m = MorseBinaryTree.getInstance().getMorse(c);
            if (m != null) {
                if (morse.length() > 0) morse.append(" ");
                morse.append(m);
            }
        }
        String pattern = morse.toString();
        int wpm = activity.getSettings().getWpm();
        activity.playMorse(pattern, wpm, null);
    }

    @Override
    public void onCharacterDecoded(char character) {
        if (binding == null || currentStage != TestStage.SENDING || !sendingWaitingForInput) return;

        char upperChar = Character.toUpperCase(character);
        currentWordKeyed.append(upperChar);
        boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
        binding.tvSendingBuffer.setText((isPt ? "A introduzir: " : "Input: ") + currentWordKeyed.toString());

        updateSendingMorseProgress();

        // Check against target
        if (currentSendingTarget.length() == 1) {
            // Single letter question
            evaluateSendingResult(currentWordKeyed.toString());
        } else {
            // Word question: check if current prefix is valid
            if (currentWordKeyed.length() == currentSendingTarget.length()) {
                evaluateSendingResult(currentWordKeyed.toString());
            } else if (!currentSendingTarget.startsWith(currentWordKeyed.toString())) {
                // Keyed wrong character in sequence!
                evaluateSendingResult(currentWordKeyed.toString());
            }
        }
    }

    private void evaluateSendingResult(String keyedText) {
        sendingWaitingForInput = false;
        questionsAnsweredInStage++;

        boolean isCorrect = keyedText.equalsIgnoreCase(currentSendingTarget);

        if (isCorrect) {
            binding.tvPenaltyNotice.setVisibility(View.GONE);
            boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
            binding.tvSendingFeedback.setText(isPt ?
                    "✓ Correto! Transmitiu '" + keyedText + "' com sucesso!" :
                    "✓ Correct! Transmitted '" + keyedText + "' successfully!");
            binding.tvSendingFeedback.setTextColor(Color.parseColor("#00E676"));
            revealFullSendingMorsePattern();
            prepareForNextSendingQuestion(450L);
        } else {
            // FAILED SENDING QUESTION (WRONG LETTER ERROR)!
            currentStageFailures++;
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                for (int i = 0; i < currentSendingTarget.length(); i++) {
                    activity.getSettings().recordLetterFailure(String.valueOf(currentSendingTarget.charAt(i)));
                }
            }

            applyFailurePenalty(currentSendingTarget);

            boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
            String errorMsg = isPt ?
                    "❌ Erro de Letra Errada! Transmitiu '" + keyedText + "', esperado '" + currentSendingTarget + "'." :
                    "❌ Wrong Letter Error! Keyed '" + keyedText + "', expected '" + currentSendingTarget + "'.";
            binding.tvSendingFeedback.setText(errorMsg);
            binding.tvSendingFeedback.setTextColor(Color.parseColor("#FF5252"));

            // Reveal correct pattern and play sound & vibration so user learns
            revealFullSendingMorsePattern();
            playFailureFeedbackAudioAndVibrate(currentSendingTarget);

            updateLivesUi();

            if (currentStageFailures >= MAX_ALLOWED_FAILURES) {
                sendingTotalFailures = currentStageFailures;
                String failMsg = isPt ?
                        "Excedeu o limite de 3 falhas no Teste de Envio." :
                        "Exceeded limit of 3 failures in Transmission Test.";
                binding.getRoot().postDelayed(() -> showExamResults(false, failMsg), 1200);
            } else {
                prepareForNextSendingQuestion(900L);
            }
        }
    }

    private String currentSendingPromptOrTarget() {
        return currentSendingTarget;
    }

    /**
     * Live rhythm & pause verification feedback from MorseDecoder
     */
    @Override
    public void onTimingFeedback(MorseTiming.PauseEvaluation eval) {
        if (binding == null || currentStage != TestStage.SENDING) return;
        if (!eval.isTimingFailure) {
            binding.tvTimingFeedback.setText(eval.feedback);
            binding.tvTimingFeedback.setTextColor(eval.isGood ? Color.parseColor("#00E676") : Color.parseColor("#FFB300"));
        }
    }

    /**
     * Timing failure rule: informs the user clearly of timing/cadence failure
     */
    @Override
    public void onTimingFailure(MorseTiming.PauseEvaluation eval) {
        if (binding == null || currentStage != TestStage.SENDING || !sendingWaitingForInput) return;

        // If the timing evaluation is an intra-element cadence warning (within the same letter),
        // show feedback in the timing view without aborting/destroying the student's in-progress letter!
        if (eval != null && eval.feedback != null &&
                (eval.feedback.contains("ponto/traço") || eval.feedback.contains("dit/dah") ||
                 eval.feedback.contains("mesma letra") || eval.feedback.contains("same character"))) {
            binding.tvTimingFeedback.setText(eval.feedback);
            binding.tvTimingFeedback.setTextColor(Color.parseColor("#FF5252"));
            return;
        }

        sendingWaitingForInput = false;
        questionsAnsweredInStage++;
        currentStageFailures++;

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            for (int i = 0; i < currentSendingTarget.length(); i++) {
                activity.getSettings().recordLetterFailure(String.valueOf(currentSendingTarget.charAt(i)));
            }
            activity.getDecoder().clear();
        }

        applyFailurePenalty(currentSendingTarget);

        boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
        String errorMsg = isPt ?
                "❌ Erro de Tempo! Cadência de pausa fora dos limites: " + eval.feedback :
                "❌ Timing Error! Pause cadence out of bounds: " + eval.feedback;
        binding.tvSendingFeedback.setText(errorMsg);
        binding.tvSendingFeedback.setTextColor(Color.parseColor("#FF5252"));
        binding.tvTimingFeedback.setText(errorMsg);
        binding.tvTimingFeedback.setTextColor(Color.parseColor("#FF5252"));

        // Reveal correct pattern and play sound & vibration so user learns cadence
        revealFullSendingMorsePattern();
        playFailureFeedbackAudioAndVibrate(currentSendingTarget);

        updateLivesUi();

        if (currentStageFailures >= MAX_ALLOWED_FAILURES) {
            sendingTotalFailures = currentStageFailures;
            String failMsg = isPt ?
                    "Excedeu o limite de 3 falhas no Teste de Envio (desrespeito dos tempos mín/máx de pausa)." :
                    "Exceeded limit of 3 failures in Transmission Test (pause timing violated).";
            binding.getRoot().postDelayed(() -> showExamResults(false, failMsg), 1200);
        } else {
            prepareForNextSendingQuestion(950L);
        }
    }

    /**
     * Penalty rule: "cada falha adiciona uma letra que falhou mais duas aleatoreas"
     */
    private void applyFailurePenalty(String failedItem) {
        List<String> pool = currentLevel.getAllCharacters();
        boolean isWord = failedItem.length() > 1;

        boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
        if (isWord) {
            String rand1 = MorseWordGenerator.generateRandomPenaltyItem(pool, true);
            String rand2 = MorseWordGenerator.generateRandomPenaltyItem(pool, true);

            currentQueue.add(failedItem);
            currentQueue.add(rand1);
            currentQueue.add(rand2);
            String notice = isPt ?
                    "⚠️ Falha! Adicionada a palavra '" + failedItem + "' + 2 aleatórias (" + rand1 + ", " + rand2 + ")!" :
                    "⚠️ Mistake! Added word '" + failedItem + "' + 2 random words (" + rand1 + ", " + rand2 + ")!";
            binding.tvPenaltyNotice.setText(notice);
        } else {
            String rand1 = MorseWordGenerator.generateRandomPenaltyItem(pool, false);
            String rand2 = MorseWordGenerator.generateRandomPenaltyItem(pool, false);

            currentQueue.add(failedItem);
            currentQueue.add(rand1);
            currentQueue.add(rand2);
            String notice = isPt ?
                    "⚠️ Falha! Adicionada a letra '" + failedItem + "' + 2 aleatórias (" + rand1 + ", " + rand2 + ")!" :
                    "⚠️ Mistake! Added character '" + failedItem + "' + 2 random characters (" + rand1 + ", " + rand2 + ")!";
            binding.tvPenaltyNotice.setText(notice);
        }

        Collections.shuffle(currentQueue);
        binding.tvPenaltyNotice.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPatternChanged(String currentPattern) {
        if (binding != null && currentStage == TestStage.SENDING) {
            boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
            String bufferText = currentWordKeyed.toString() + (currentPattern.isEmpty() ? "" : " [" + currentPattern + "]");
            String prefix = isPt ? "A introduzir: " : "Input: ";
            binding.tvSendingBuffer.setText(prefix + (bufferText.isEmpty() ? "—" : bufferText));
        }
    }

    @Override
    public void onTextUpdated(String fullText) {}

    // --- STAGE 3: RESULTS & UNLOCKING ---

    /**
     * Helper for behavior testing: simulates student completing the exam with zero failures
     * to verify the level release and unlock flow.
     */
    public void simulateExamPassForTesting() {
        listeningTotalFailures = 0;
        sendingTotalFailures = 0;
        boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
        showExamResults(true, isPt ? "Aprovado com sucesso em ambas as etapas." : "Successfully passed both stages.");
    }

    /**
     * Helper for behavior testing: simulates student failing a question and losing a life.
     */
    public void simulateQuestionMistakeForTesting() {
        currentStageFailures++;
        updateLivesUi();
        boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
        binding.tvPenaltyNotice.setVisibility(View.VISIBLE);
        binding.tvPenaltyNotice.setText(isPt ? "Penalização: perdeu 1 vida!" : "Penalty: lost 1 life!");
    }

    /**
     * Helper for behavior testing: simulates student recovering and answering correctly on retry.
     */
    public void simulateQuestionSuccessForTesting() {
        questionsAnsweredInStage++;
        binding.tvPenaltyNotice.setVisibility(View.GONE);
        boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
        if (currentStage == TestStage.LISTENING) {
            binding.tvListeningInstruction.setText(isPt ? "✓ Correto! Recuperação com sucesso." : "✓ Correct! Recovered successfully.");
            binding.tvListeningInstruction.setTextColor(Color.parseColor("#00E676"));
        } else {
            binding.tvSendingFeedback.setText(isPt ? "✓ Correto! Recuperação com sucesso." : "✓ Correct! Recovered successfully.");
            binding.tvSendingFeedback.setTextColor(Color.parseColor("#00E676"));
        }
        updateLivesUi();
    }

    public void triggerListeningOptionForTesting(String selected) {
        handleListeningOptionClicked(selected);
    }

    public String getCurrentListeningTargetForTesting() {
        return currentListeningTarget;
    }

    public List<Button> getTestOptionButtonsForTesting() {
        return testOptionButtons;
    }

    public String getCurrentSendingTargetForTesting() {
        return currentSendingTarget;
    }

    public void startSendingStageForTesting() {
        startSendingStage();
    }

    public void simulateSendingFailureForTesting(String wrongKeyed) {
        evaluateSendingResult(wrongKeyed);
    }

    public void simulateSendingSuccessForTesting(String correctKeyed) {
        evaluateSendingResult(correctKeyed != null ? correctKeyed : currentSendingTarget);
    }

    public void simulateTimingFailureForTesting(String reason) {
        String feedback = reason != null ? reason : "Pause gap exceeded maximum PARIS cadence threshold";
        MorseTiming.PauseEvaluation eval = new MorseTiming.PauseEvaluation(false, true, feedback, 2.5f);
        onTimingFailure(eval);
    }

    public void setSendingTargetForTesting(String target) {
        this.currentSendingTarget = target;
        this.currentWordKeyed.setLength(0);
        this.sendingWaitingForInput = true;
        setSendingPaddlesEnabled(true);
        if (binding != null) {
            boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");
            boolean isSingleLetter = (target.length() == 1);
            binding.tvSendingPromptLabel.setText(isPt ?
                    (isSingleLetter ? "TRANSMITA A LETRA:" : "TRANSMITA A PALAVRA (LETRAS SEPARADAS):") :
                    (isSingleLetter ? "TRANSMIT LETTER:" : "TRANSMIT WORD (SEPARATED LETTERS):"));
            binding.tvSendingPrompt.setAlpha(1.0f);
            binding.tvSendingPrompt.setText(target);
            updateSendingMorseProgress();
        }
    }

    public void simulateKeyCharacterForTesting(char c) {
        onCharacterDecoded(c);
    }

    public String getCurrentWordKeyedForTesting() {
        return currentWordKeyed.toString();
    }

    public void showExamResults(boolean passed, String detailMessage) {
        currentStage = TestStage.RESULT;
        binding.layoutStudyView.setVisibility(View.GONE);
        binding.layoutTestView.setVisibility(View.GONE);
        binding.layoutResultView.setVisibility(View.VISIBLE);

        MainActivity activity = (MainActivity) getActivity();
        boolean isPt = java.util.Locale.getDefault().getLanguage().equalsIgnoreCase("pt");

        if (passed) {
            String resultTitle = isPt ? "🎉 NÍVEL " + currentLevelNumber + " CONCLUÍDO!" : "🎉 LEVEL " + currentLevelNumber + " COMPLETED!";
            binding.tvResultTitle.setText(resultTitle);
            binding.tvResultTitle.setTextColor(Color.parseColor("#00E676"));

            String summary = isPt ?
                    ("Teste de Send (Transmissão): Aprovado (Falhas: " + sendingTotalFailures + "/3)\n\n" +
                            "Dominou as letras novas e palavras formadas com o vocabulário deste nível!") :
                    ("Send Test (Transmission): Passed (Mistakes: " + sendingTotalFailures + "/3)\n\n" +
                            "Mastered the new characters and vocabulary of this level!");
            binding.tvResultSummary.setText(summary);

            if (activity != null) {
                boolean newlyUnlocked = activity.getSettings().unlockNextLevel(currentLevelNumber);
                int nextLevel = currentLevelNumber + 1;
                if (newlyUnlocked && nextLevel <= MorseBinaryTree.getInstance().getTotalLevels()) {
                    String unlockMsg = isPt ?
                            "🔓 Desbloqueou o Nível " + nextLevel + " na Árvore Binária de Morse!" :
                            "🔓 Unlocked Level " + nextLevel + " in the Morse Binary Tree!";
                    binding.tvResultUnlockMsg.setText(unlockMsg);
                    binding.tvResultUnlockMsg.setVisibility(View.VISIBLE);
                } else {
                    String unlockMsg = isPt ? "Nível já desbloqueado anteriormente." : "Level previously unlocked.";
                    binding.tvResultUnlockMsg.setText(unlockMsg);
                    binding.tvResultUnlockMsg.setVisibility(View.VISIBLE);
                }
            }

            binding.btnResultAction.setText(isPt ? "AVANÇAR PARA O PRÓXIMO NÍVEL ▶" : "ADVANCE TO NEXT LEVEL ▶");
            binding.btnResultAction.setBackgroundColor(Color.parseColor("#FFB300"));
        } else {
            binding.tvResultTitle.setText(isPt ? "❌ TESTE DE SEND FALHADO" : "❌ SEND TEST FAILED");
            binding.tvResultTitle.setTextColor(Color.parseColor("#FF5252"));

            String summary = detailMessage + "\n\n"
                    + (isPt ? "Regra: O teste de envio tolera até 3 falhas no total.\nPratique a cadência de pausa e tente novamente!" :
                    "Rule: The transmission test allows up to 3 mistakes total.\nPractice your timing cadence and try again!");
            binding.tvResultSummary.setText(summary);
            binding.tvResultUnlockMsg.setVisibility(View.GONE);
            binding.btnResultAction.setText(isPt ? "REPETIR SEND (TRANSMISSÃO) 🔄" : "RETRY SEND (TRANSMISSION) 🔄");
            binding.btnResultAction.setBackgroundColor(Color.parseColor("#FF5252"));
        }
    }

    public boolean isListeningStagePassed() {
        return listeningStagePassed;
    }

    public void setListeningStagePassedForTesting(boolean passed) {
        this.listeningStagePassed = passed;
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.getDecoder().setListener(this);
            int unlocked = activity.getSettings().getCurrentUnlockedLevel();
            if (currentLevel != null) {
                boolean isUnlocked = activity.getSettings().isLevelUnlocked(currentLevelNumber);
                binding.tvLevelNumber.setText("NÍVEL " + currentLevel.getLevelNumber() + " / " + MorseBinaryTree.getInstance().getTotalLevels() + (isUnlocked ? "" : " 🔒"));
                int nextLevel = currentLevelNumber + 1;
                boolean nextUnlocked = activity.getSettings().isLevelUnlocked(nextLevel);
                binding.btnNextLevel.setAlpha(nextUnlocked ? 1.0f : 0.4f);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

