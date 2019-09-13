package com.zalo.trainingmenu.downloader.helper;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.URLUtil;

import com.zalo.trainingmenu.App;

public class URLCopyBoardHelper implements ClipboardManager.OnPrimaryClipChangedListener {
     public static final String TAG = "URLCopyBoardHelper";
     public interface CopyBoardCallback {
         void onNewURLCopyBoard(String url);
     }

     public URLCopyBoardHelper() {
     }

     private boolean mRegistered = false;

    public void init(CopyBoardCallback callback) {
        mCallback = callback;
        register();
    }

    private CopyBoardCallback mCallback;

    public synchronized void register() {
        if(mRegistered) unregister();

        ClipboardManager clipboardManager = (ClipboardManager) App.getInstance().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if(clipboardManager!=null) {
            clipboardManager.addPrimaryClipChangedListener(this);
            mRegistered = true;
        }
    }

    public void unregister() {
        if(mRegistered) {
            ClipboardManager clipboardManager = (ClipboardManager) App.getInstance().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager != null)
                clipboardManager.removePrimaryClipChangedListener(this);
        }
    }

    public void destroy() {
        unregister();
        mCallback = null;
    }

    @Override
    public void onPrimaryClipChanged() {
        Log.d(TAG, "onPrimaryClipChanged");
        ClipboardManager clipboardManager = (ClipboardManager) App.getInstance().getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if(clipboardManager !=null&& clipboardManager.getPrimaryClip()!=null) {
            ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);

            CharSequence pasteSequence = item.getText();
            String pasteData = null;

            if (pasteSequence == null) {
                Uri pasteUri = item.getUri();

                if (pasteUri != null) {

                    pasteData = pasteUri.toString();
                }
            } else pasteData = pasteSequence.toString();

            if(pasteData!=null && !pasteData.isEmpty() && !mLastUrl.equals(pasteData)) {
                mLastUrl = pasteData;
                if(URLUtil.isValidUrl(pasteData) && mCallback != null) {
                   notifyThisDownloadLink(pasteData);
                 }
            }
        }
    }

    private String mLastUrl = "";

    public void notifyThisDownloadLink(String url) {
        if(url!=null && mCallback!=null && !url.isEmpty()) {
            mCallback.onNewURLCopyBoard(url);
        }
    }
}
