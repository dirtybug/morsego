package com.morsego.app.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.morsego.app.MainActivity;
import com.morsego.app.databinding.FragmentHardwareBinding;
import com.morsego.app.keyer.KeyerInputManager;

import java.util.List;

public class HardwareFragment extends Fragment {

    private FragmentHardwareBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHardwareBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) requireActivity();

        updatePaddleVisuals(activity.getInputManager().isDitPressed(), activity.getInputManager().isDahPressed());
        binding.switchReversePaddles.setChecked(activity.getSettings().isReversePaddles());
        updateReverseLabels(activity.getSettings().isReversePaddles());

        binding.switchReversePaddles.setOnCheckedChangeListener((buttonView, isChecked) -> {
            activity.getSettings().setReversePaddles(isChecked);
            updateReverseLabels(isChecked);
        });

        binding.btnCalibrateDit.setOnClickListener(v -> {
            activity.getInputManager().startCalibration(KeyerInputManager.CalibrationState.DIT);
            binding.btnCalibrateDit.setText("Tap DIT paddle...");
        });

        binding.btnCalibrateDah.setOnClickListener(v -> {
            activity.getInputManager().startCalibration(KeyerInputManager.CalibrationState.DAH);
            binding.btnCalibrateDah.setText("Tap DAH paddle...");
        });

        binding.btnResetVband.setOnClickListener(v -> {
            activity.getSettings().setDitKeyCode(KeyEvent.KEYCODE_CTRL_LEFT);
            activity.getSettings().setDahKeyCode(KeyEvent.KEYCODE_CTRL_RIGHT);
            binding.btnCalibrateDit.setText("Calibrate Dit (Ctrl Left)");
            binding.btnCalibrateDah.setText("Calibrate Dah (Ctrl Right)");
        });

        activity.getInputManager().setOnLogUpdated(() -> {
            if (binding != null && getActivity() != null) {
                getActivity().runOnUiThread(this::updateLogs);
            }
        });

        updateLogs();
    }

    public void updatePaddleVisuals(boolean ditDown, boolean dahDown) {
        if (binding == null) return;

        MainActivity activity = (MainActivity) getActivity();
        boolean reversed = activity != null && activity.getSettings().isReversePaddles();

        boolean leftDown = !reversed ? ditDown : dahDown;
        boolean rightDown = !reversed ? dahDown : ditDown;

        binding.cardLeftPaddle.setBackgroundColor(leftDown ? Color.parseColor("#FFB300") : Color.parseColor("#21262D"));
        binding.tvLeftPaddleState.setText(leftDown ? "PRESSED" : "RELEASED");
        binding.tvLeftPaddleState.setTextColor(leftDown ? Color.parseColor("#0E1117") : Color.parseColor("#8B949E"));

        binding.cardRightPaddle.setBackgroundColor(rightDown ? Color.parseColor("#00E5FF") : Color.parseColor("#21262D"));
        binding.tvRightPaddleState.setText(rightDown ? "PRESSED" : "RELEASED");
        binding.tvRightPaddleState.setTextColor(rightDown ? Color.parseColor("#0E1117") : Color.parseColor("#8B949E"));
    }

    private void updateReverseLabels(boolean reversed) {
        if (binding == null) return;
        binding.tvReverseDesc.setText(reversed ?
                "Left = Dah (—) | Right = Dit (•)" :
                "Left = Dit (•) | Right = Dah (—)");

        binding.tvLeftPaddleName.setText(reversed ? "LEFT PADDLE (DAH —)" : "LEFT PADDLE (DIT •)");
        binding.tvRightPaddleName.setText(reversed ? "RIGHT PADDLE (DIT •)" : "RIGHT PADDLE (DAH —)");
    }

    private void updateLogs() {
        if (binding == null) return;
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        List<KeyerInputManager.HardwareLog> logs = activity.getInputManager().getRecentLogs();
        if (logs.isEmpty()) {
            binding.tvHardwareLogs.setText("Waiting for USB keyer events...");
            return;
        }

        StringBuilder sb = new StringBuilder();
        int start = Math.max(0, logs.size() - 6);
        for (int i = logs.size() - 1; i >= start; i--) {
            KeyerInputManager.HardwareLog l = logs.get(i);
            sb.append("[").append(l.type).append("] ")
                    .append(l.name).append(" (#").append(l.keyCode).append(")\n");
        }
        binding.tvHardwareLogs.setText(sb.toString());

        // Also reset calibration button labels if calibration finished
        if (activity.getInputManager().getCalibrationState() == KeyerInputManager.CalibrationState.NONE) {
            binding.btnCalibrateDit.setText("Calibrate Dit (" + KeyEvent.keyCodeToString(activity.getSettings().getDitKeyCode()) + ")");
            binding.btnCalibrateDah.setText("Calibrate Dah (" + KeyEvent.keyCodeToString(activity.getSettings().getDahKeyCode()) + ")");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
