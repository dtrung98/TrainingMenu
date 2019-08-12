package com.zalo.trainingmenu.downloader.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.zalo.trainingmenu.R;

import org.jetbrains.annotations.NotNull;

public class PreferenceDialog extends DialogFragment implements View.OnClickListener {
    private static final String TAG = "PreferenceDialog";

    static PreferenceDialog newInstance() {
        return new PreferenceDialog();
    }

    @Override
    public int getTheme() {
        return R.style.DialogDimDisabled;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_download_layout,container,false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bind(view);
    }


    private void bind(View root) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }


    @Override
    public void show(@NotNull FragmentManager manager, String tag) {
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.add(this, TAG);
        fragmentTransaction.commitAllowingStateLoss();
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
