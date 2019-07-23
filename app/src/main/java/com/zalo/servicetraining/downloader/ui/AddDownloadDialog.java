package com.zalo.servicetraining.downloader.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.zalo.servicetraining.R;
import com.zalo.servicetraining.downloader.service.DownloaderRemote;

public class AddDownloadDialog extends DialogFragment implements View.OnClickListener {
    public static final String TAG = "AddDownloadDialog";

    private TextInputLayout mUrlTextInputLayout;
    private TextInputEditText mUrlEditText;


    public static AddDownloadDialog newInstance() {

        Bundle args = new Bundle();

        AddDownloadDialog fragment = new AddDownloadDialog();
        fragment.setArguments(args);
        return fragment;
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

   /* public static boolean hasSoftKeys(WindowManager windowManager){
        Display d = windowManager.getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
    }

    public static int getNavigationHeight(Activity activity)
    {

        int navigationBarHeight = 0;
        int resourceId = activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
        }
        if(!hasSoftKeys(activity.getWindowManager())) return 0;
        return  navigationBarHeight;
    }*/

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bind(view);
        showKeyboard();

    }
    private void bind(View root) {
        View mDownloadButton = root.findViewById(R.id.download_button);
        View mCloseButton = root.findViewById(R.id.close);
        View mPasteAndGoButton = root.findViewById(R.id.paste_and_go_button);
        mUrlTextInputLayout = root.findViewById(R.id.url_input_layout);
        mUrlEditText = mUrlTextInputLayout.findViewById(R.id.url_input_edit_text);

        mDownloadButton.setOnClickListener(this);
        mCloseButton.setOnClickListener(this);
        mPasteAndGoButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.download_button:
                addToDownload();
                break;
            case R.id.close:
                dismiss();
                break;
            case R.id.paste_and_go_button:
                pasteAndGo();
                break;
        }
    }

    public void showKeyboard(){
        if(getContext()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(inputMethodManager!=null)
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    public void closeKeyboard(){
        if(getContext()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(inputMethodManager!=null)
            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }
    }

    private void addToDownload() {
        closeKeyboard();
        if(getActivity() instanceof DownloaderActivity)
            ((DownloaderActivity)getActivity()).doSomething();
    }

    private void pasteAndGo() {
        mUrlEditText.clearFocus();

    }


}
