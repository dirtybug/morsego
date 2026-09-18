package com.morsego.app.ui;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.morsego.app.BuildConfig;
import com.morsego.app.R;
import com.morsego.app.databinding.DialogAboutBinding;

public class AboutDialogFragment extends DialogFragment {

    private DialogAboutBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogAboutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tvAboutVersionBuild.setText("v" + BuildConfig.VERSION_NAME + " • Build: " + BuildConfig.BUILD_TIME);

        View.OnClickListener openGitHub = v -> {
            try {
                String url = getString(R.string.about_repo_url);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } catch (Exception ignored) {
            }
        };

        binding.btnGitHub.setOnClickListener(openGitHub);
        binding.tvRepoUrl.setOnClickListener(openGitHub);

        binding.btnCloseAbout.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
