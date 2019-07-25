package com.zalo.servicetraining.downloader.model;

import android.os.Environment;
import android.webkit.URLUtil;

public class DownloadItem {
    private final String mUrlConnection;
    private final String mTitle;
    private final boolean mNotifyOnFinish;
    private final String mDirectoryPath;

    public boolean isAppendIfExist() {
        return mAppendIfExist;
    }

    public void setAppendIfExist(boolean appendIfExist) {
        mAppendIfExist = appendIfExist;
    }

    private boolean mAppendIfExist = false;


    public boolean isForceDownload() {
        return mForceDownload;
    }

    private final boolean mForceDownload;

    public DownloadItem(String mUrlConnection, String mTitle, boolean mNotifyOnFinish, boolean mForceDownload, String directoryPath) {
        this.mUrlConnection = mUrlConnection;
        this.mTitle = mTitle;
        this.mNotifyOnFinish = mNotifyOnFinish;
        this.mForceDownload = mForceDownload;
        this.mDirectoryPath = directoryPath;
    }

    public DownloadItem(String mUrlConnection) {
        this.mUrlConnection = mUrlConnection;
        this.mTitle = "";
        mNotifyOnFinish = false;
        mForceDownload = false;
        this.mDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
    }

    public DownloadItem(String mUrlConnection, String mTitle) {
        this.mUrlConnection = mUrlConnection;
        this.mTitle = mTitle;
        this.mNotifyOnFinish = false;
        this.mForceDownload = false;
        this.mDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
    }

    public String getUrlString() {
        return mUrlConnection;
    }

    public String getTitle() {
        if(!mTitle.isEmpty())
        return mTitle;

        String autoTitle = URLUtil.guessFileName(mUrlConnection, null, null);
        if(autoTitle!=null&&!autoTitle.isEmpty())
            return autoTitle;
        return "Unknown.std";
    }

    public boolean isNotifyOnFinish() {
        return mNotifyOnFinish;
    }

    public DownloadItem(DownloadItem item) {
        this.mUrlConnection = item.getUrlString();
        this.mTitle = item.getTitle();
        this.mNotifyOnFinish = item.isNotifyOnFinish();
        this.mForceDownload = item.mForceDownload;
        this.mDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
    }

    public String getDirectoryPath() {
        return mDirectoryPath;
    }

}
