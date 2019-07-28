package com.zalo.servicetraining.downloader.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.zalo.servicetraining.R;
import com.zalo.servicetraining.downloader.model.DownloadItem;
import com.zalo.servicetraining.downloader.service.DownloaderRemote;

import es.dmoral.toasty.Toasty;

public class AddDownloadDialog extends DialogFragment implements View.OnClickListener, ClipboardManager.OnPrimaryClipChangedListener, TextWatcher {
    public static final String TAG = "AddDownloadDialog";

    private TextInputLayout mUrlTextInputLayout;
    private TextInputEditText mUrlEditText;
    private TextView mDownloadButton;
    private TextView mPasteAndGoButton;
    private ImageView mPasteIcon;

    public static AddDownloadDialog newInstance() {

        Bundle args = new Bundle();

        AddDownloadDialog fragment = new AddDownloadDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private ClipboardManager mClipboardManager;


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
        mClipboardManager = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if(mClipboardManager!=null) {
            mClipboardManager.addPrimaryClipChangedListener(this);
        }
        updateDownloadButton();
        updatePasteButton();
        showKeyboard();

    }

    @Override
    public void onPause() {
        closeKeyboard();
        super.onPause();
    }

    @Override
    public void dismiss() {
        closeKeyboard();
        super.dismiss();
    }

    @Override
    public void onDestroyView() {
        mClipboardManager.removePrimaryClipChangedListener(this);
        mClipboardManager = null;
        super.onDestroyView();
    }

    private void bind(View root) {
        mDownloadButton = root.findViewById(R.id.download_button);
        View mCloseButton = root.findViewById(R.id.close);
        mPasteAndGoButton = root.findViewById(R.id.paste_and_go_button);
        mPasteIcon = root.findViewById(R.id.paste_icon);
        mUrlTextInputLayout = root.findViewById(R.id.url_input_layout);
        mUrlEditText = mUrlTextInputLayout.findViewById(R.id.url_input_edit_text);
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

    public void showKeyboard(){
        mUrlEditText.requestFocus();
        if(getContext()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(inputMethodManager!=null&&!inputMethodManager.isAcceptingText())
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    public void closeKeyboard(){
        if(getContext()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(inputMethodManager!=null&&inputMethodManager.isAcceptingText())
            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }
    }

    private void addToDownload() {
        Editable editable = mUrlEditText.getText();
        if(editable!=null) {
            String text = editable.toString();
            if(!text.isEmpty()&& URLUtil.isValidUrl(text)) {
                DownloaderRemote.appendTask(new DownloadItem(mUrlEditText.getText().toString()));
                dismiss();
                Toasty.info(mUrlEditText.getContext(),R.string.add_new_download).show();
            } else Toasty.error(mUrlEditText.getContext(),R.string.invalid_url).show();
        }
    }

    private void pasteAndGo() {
        if(getContext()!=null) {
            if(mClipboardManager !=null&& mClipboardManager.getPrimaryClip()!=null) {
                ClipData.Item item = mClipboardManager.getPrimaryClip().getItemAt(0);

                CharSequence pasteData = item.getText();

                if (pasteData == null) {
                    Uri pasteUri = item.getUri();

                    if (pasteUri != null) {

                        pasteData = pasteUri.toString();
                    } else {
                        Log.e(TAG, "Clipboard contains an invalid data type");
                    }
                }
                if(pasteData!=null&&URLUtil.isValidUrl(pasteData.toString())) {
                    closeKeyboard();
                    DownloaderRemote.appendTask(new DownloadItem(pasteData.toString()));
                    dismiss();
                    Toasty.info(mUrlEditText.getContext(),R.string.add_new_download).show();
                } else Toasty.error(mUrlEditText.getContext(),R.string.invalid_url).show();
            }
        }

    }
    @Override
    public void show(FragmentManager manager, String tag) {
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
        if(getContext()!=null) {
            if(mClipboardManager !=null&& mClipboardManager.getPrimaryClip()!=null) {
                ClipData.Item item = mClipboardManager.getPrimaryClip().getItemAt(0);

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
}
