package com.morsego.app.ui;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.morsego.app.MainActivity;
import com.morsego.app.databinding.FragmentKeyerBinding;
import com.morsego.app.keyer.MorseDecoder;

public class FreeKeyerFragment extends Fragment implements MorseDecoder.DecoderListener {

    private FragmentKeyerBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentKeyerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) requireActivity();

        binding.tvTxStatus.setText("CW KEYER (" + activity.getSettings().getMode().getLabel().toUpperCase() + ")");
        binding.tvKeyerWpm.setText(activity.getSettings().getWpm() + " WPM");
        binding.tvSeekWpmValue.setText(String.valueOf(activity.getSettings().getWpm()));
        binding.seekWpm.setProgress(activity.getSettings().getWpm() - 5);

        binding.seekWpm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int wpm = progress + 5;
                    activity.getSettings().setWpm(wpm);
                    binding.tvKeyerWpm.setText(wpm + " WPM");
                    binding.tvSeekWpmValue.setText(String.valueOf(wpm));
                    activity.updateTopWpm(wpm);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.btnCopyText.setOnClickListener(v -> {
            String text = activity.getDecoder().getDecodedText();
            if (!text.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("MorseGO", text));
                Toast.makeText(getContext(), "Texto copiado!", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnBackspace.setOnClickListener(v -> activity.getDecoder().backspace());
        binding.btnClearText.setOnClickListener(v -> activity.getDecoder().clear());

        // Touch Paddle controls
        binding.btnFreeDit.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                activity.getInputManager().setTouchDit(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                activity.getInputManager().setTouchDit(false);
            }
            return true;
        });

        binding.btnFreeDah.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                activity.getInputManager().setTouchDah(true);
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                activity.getInputManager().setTouchDah(false);
            }
            return true;
        });
    }

    public void setLedActive(boolean active) {
        if (binding != null) {
            binding.ledTxIndicator.setBackgroundColor(active ? Color.parseColor("#FFB300") : Color.parseColor("#0E1117"));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.getDecoder().setListener(this);
            binding.tvBuffer.setText("BUFFER: " + (activity.getDecoder().getCurrentPattern().isEmpty() ? "—" : activity.getDecoder().getCurrentPattern()));
            binding.tvDecodedOutput.setText(activity.getDecoder().getDecodedText());
        }
    }

    @Override
    public void onPatternChanged(String currentPattern) {
        if (binding != null) {
            binding.tvBuffer.setText("BUFFER: " + (currentPattern.isEmpty() ? "—" : currentPattern));
        }
    }

    @Override
    public void onTextUpdated(String fullText) {
        if (binding != null) {
            binding.tvDecodedOutput.setText(fullText);
        }
    }

    @Override
    public void onCharacterDecoded(char character) {
        // Updated in onTextUpdated
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
