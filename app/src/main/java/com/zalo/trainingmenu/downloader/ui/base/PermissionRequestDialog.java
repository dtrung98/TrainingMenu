package com.zalo.trainingmenu.downloader.ui.base;

import android.content.DialogInterface;
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

public class PermissionRequestDialog extends DialogFragment implements View.OnClickListener {
    public static final String TAG = "PermissionRequestDialog";

    public static PermissionRequestDialog newInstance() {
        return new PermissionRequestDialog();
    }

    @Override
    public int getTheme() {
        return R.style.DialogDimDisabled;
    }

    private boolean mResult = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.permission_request,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bind(view);
    }


    @Override
    public void show(@NonNull FragmentManager manager, String tag) {
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.add(this, TAG);
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void bind(View root) {
     View closeButton = root.findViewById(R.id.close);
     View okButton = root.findViewById(R.id.button);

     if(closeButton!=null) closeButton.setOnClickListener(this);
     if(okButton!=null) okButton.setOnClickListener(this);
    }

    interface RequestResultCallback {
        void onRequestResult(boolean result);
    }

    public PermissionRequestDialog setRequestResultCallback(RequestResultCallback requestResultCallback) {
        mRequestResultCallback = requestResultCallback;
        return this;
    }

    private RequestResultCallback mRequestResultCallback;

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if(mRequestResultCallback !=null) mRequestResultCallback.onRequestResult(mResult);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.close:
                mResult = false;
                dismiss();
                break;
            case R.id.button:
                mResult = true;
                dismiss();
                break;

        }
    }
}
