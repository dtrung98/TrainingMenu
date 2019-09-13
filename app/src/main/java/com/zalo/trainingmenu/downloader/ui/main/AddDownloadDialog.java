package com.zalo.trainingmenu.downloader.ui.main;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.zalo.trainingmenu.App;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.downloader.base.BaseTask;
import com.zalo.trainingmenu.downloader.base.Task;
import com.zalo.trainingmenu.downloader.model.DownloadItem;


import es.dmoral.toasty.Toasty;

public class AddDownloadDialog extends DialogFragment implements View.OnClickListener, ClipboardManager.OnPrimaryClipChangedListener, TextWatcher {
    public static final String TAG = "AddDownloadDialog";

    private TextInputEditText mUrlEditText;
    private TextView mDownloadButton;
    private View mCloseButton;
    private TextView mPasteAndGoButton;
    private ImageView mPasteIcon;
    private String mIntentUrl;

    public static AddDownloadDialog newInstance() {
        return new AddDownloadDialog();
    }

    public static AddDownloadDialog newInstance(String url) {

        Bundle args = new Bundle();
        args.putString(Task.EXTRA_URL,url);
        AddDownloadDialog fragment = new AddDownloadDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_download_layout,container,false);
    }

    @Override
    public int getTheme() {
        return R.style.DialogDimDisabled;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bind(view);
            ClipboardManager clipboardManager = (ClipboardManager) App.getInstance().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null) {
                clipboardManager.addPrimaryClipChangedListener(this);
            }

           Bundle bundle = getArguments();
            if(bundle!=null) {
               String url = bundle.getString(Task.EXTRA_URL);
               if(url!=null && !url.isEmpty()) mUrlEditText.setText(url);
            }
        updateDownloadButton();
        updatePasteButton();
        showKeyboard();

    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        closeKeyboard();
        super.onPause();
    }

    private void bind(View root) {
        mDownloadButton = root.findViewById(R.id.download_button);
        mCloseButton = root.findViewById(R.id.close);
        mPasteAndGoButton = root.findViewById(R.id.paste_and_go_button);
        mPasteIcon = root.findViewById(R.id.paste_icon);
        TextInputLayout urlTextInputLayout = root.findViewById(R.id.url_input_layout);
        mUrlEditText = urlTextInputLayout.findViewById(R.id.url_input_edit_text);
        mUrlEditText.addTextChangedListener(this);
        root.findViewById(R.id.panel).setOnClickListener(this);
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
                closeKeyboard();
                dismiss();
                break;
            case R.id.paste_and_go_button:
                pasteAndGo();
                break;
            case R.id.panel:
                closeKeyboard();
                break;
        }
    }

    private void showKeyboard(){
        mUrlEditText.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) App.getInstance().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(inputMethodManager!=null&&!inputMethodManager.isAcceptingText())
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void closeKeyboard(){
            InputMethodManager inputMethodManager = (InputMethodManager) App.getInstance().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(inputMethodManager!=null&&inputMethodManager.isAcceptingText())
            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    private void addToDownload() {
        Editable editable = mUrlEditText.getText();
        if(editable!=null) {
            String text = editable.toString();
            if(!text.isEmpty()&& URLUtil.isValidUrl(text)) {
                append(mUrlEditText.getText().toString());
                dismiss();
                Toasty.info(App.getInstance().getApplicationContext(),R.string.add_new_download).show();
            } else Toasty.error(App.getInstance().getApplicationContext(),R.string.invalid_url).show();
        }
    }

    private void pasteAndGo() {
            ClipboardManager clipboardManager = (ClipboardManager)App.getInstance().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if(clipboardManager !=null&& clipboardManager.getPrimaryClip()!=null) {
                ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);

                CharSequence pasteData = item.getText();

                if (pasteData == null) {
                    Uri pasteUri = item.getUri();

                    if (pasteUri != null) {

                        pasteData = pasteUri.toString();
                    } else {
                        Log.d(TAG, "Clipboard contains an invalid data type");
                    }
                }
                if(pasteData!=null&&URLUtil.isValidUrl(pasteData.toString())) {
                    closeKeyboard();
                    append(pasteData.toString());
                    dismiss();
                    Toasty.info(App.getInstance().getApplicationContext(),R.string.add_new_download).show();
                } else Toasty.error(App.getInstance().getApplicationContext(),R.string.invalid_url).show();
            }

    }
    private void append(String url) {
        DownloadItem item = new DownloadItem(url);
        if(getActivity() instanceof DownloadActivity) {
            Intent intent = new Intent(DownloadActivity.ACTION_APPEND_TASK);
            intent.putExtra(BaseTask.EXTRA_DOWNLOAD_ITEM,item);
            ((DownloadActivity) getActivity()).executeWriteStorageAction(intent);
        }
    }

    @Override
    public void show(@NonNull FragmentManager manager, String tag) {
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.add(this, TAG);
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void updateDownloadButton() {
        Editable editable = mUrlEditText.getText();
        if(editable!=null) {
            String text = editable.toString();
            if(!text.isEmpty()&& URLUtil.isValidUrl(text)) {
                mDownloadButton.setBackgroundResource(R.drawable.background_round_green);
                mDownloadButton.setEnabled(true);
             return;
            }
        }

        // else
        mDownloadButton.setBackgroundResource(R.drawable.background_round_dark);
        mDownloadButton.setEnabled(false);

    }

    private void updatePasteButton() {
        ClipboardManager clipboardManager = (ClipboardManager) App.getInstance().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if(clipboardManager !=null&& clipboardManager.getPrimaryClip()!=null) {
            ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);

            CharSequence pasteData = item.getText();

            if (pasteData == null) {
                Uri pasteUri = item.getUri();

                if (pasteUri != null) {

                    pasteData = pasteUri.toString();
                }
            }
            if(pasteData!=null && URLUtil.isValidUrl(pasteData.toString())) {
                mPasteIcon.setColorFilter(getResources().getColor(R.color.FlatTealBlue));
                mPasteAndGoButton.setBackgroundResource(R.drawable.background_round_teal_blue_border);
                mPasteAndGoButton.setTextColor(getResources().getColor(R.color.FlatTealBlue));
                mPasteAndGoButton.setEnabled(true);
                return;
            }
        }

        // else
        mPasteIcon.setColorFilter(getResources().getColor(R.color.dark_666));
        mPasteAndGoButton.setBackgroundResource(R.drawable.background_round_dark_border);
        mPasteAndGoButton.setTextColor(getResources().getColor(R.color.dark_666));
        mPasteAndGoButton.setEnabled(false);
    }


    @Override
    public void onPrimaryClipChanged() {
        updatePasteButton();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        updateDownloadButton();
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onDestroyView() {
        ClipboardManager clipboardManager = (ClipboardManager) App.getInstance().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if(clipboardManager!=null)
            clipboardManager.removePrimaryClipChangedListener(this);
        mDownloadButton.setOnClickListener(null);
        mCloseButton.setOnClickListener(null);
        mPasteAndGoButton.setOnClickListener(null);
        super.onDestroyView();
    }
}
