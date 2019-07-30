package com.zalo.servicetraining.downloader.model;

import android.os.Environment;
import android.webkit.URLUtil;

public class DownloadItem {
    private final String mUrl;
    private final String mFileTitle;
    private final String mDirectoryPath;

    public DownloadItem(String mUrlConnection, String mTitle, String directoryPath) {
        this.mUrl = mUrlConnection;
        this.mFileTitle = mTitle;
        this.mDirectoryPath = directoryPath;
    }

    public DownloadItem(String mUrlConnection) {
        this.mUrl = mUrlConnection;
        this.mFileTitle = "";
        this.mDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
    }

    public DownloadItem(String mUrlConnection, String mTitle) {
        this.mUrl = mUrlConnection;
        this.mFileTitle = mTitle;
        this.mDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
    }

    public String getUrlString() {
        return mUrl;
    }

    public String getFileTitle() {
        if(!mFileTitle.isEmpty())
        return mFileTitle;

        String autoTitle = URLUtil.guessFileName(mUrl, null, null);
        if(autoTitle!=null&&!autoTitle.isEmpty())
            return autoTitle;
        return "Unknown.std";
    }


    public DownloadItem(DownloadItem item) {
        this.mUrl = item.getUrlString();
        this.mFileTitle = item.getFileTitle();
        this.mDirectoryPath = item.mDirectoryPath;
    }

    public String getDirectoryPath() {
        return mDirectoryPath;
    }

}
