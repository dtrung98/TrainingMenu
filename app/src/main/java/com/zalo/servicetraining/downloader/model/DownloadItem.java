package com.zalo.servicetraining.downloader.model;

public class DownloadItem {
    private final String mUrlConnection;
    private final String mTitle;
    private final boolean mNotifyOnFinish;

    public boolean isForceDownload() {
        return mForceDownload;
    }

    private final boolean mForceDownload;

    public DownloadItem(String mUrlConnection, String mTitle, boolean mNotifyOnFinish, boolean mForceDownload) {
        this.mUrlConnection = mUrlConnection;
        this.mTitle = mTitle;
        this.mNotifyOnFinish = mNotifyOnFinish;
        this.mForceDownload = mForceDownload;
    }
    public DownloadItem(String mUrlConnection) {
        this.mUrlConnection = mUrlConnection;
        this.mTitle = "";
        mNotifyOnFinish = false;
        mForceDownload = false;
    }

    public DownloadItem(String mUrlConnection, String mTitle) {
        this.mUrlConnection = mUrlConnection;
        this.mTitle = mTitle;
        this.mNotifyOnFinish = false;
        this.mForceDownload = false;
    }

    public String getUrlConnection() {
        return mUrlConnection;
    }

    public String getTitle() {
        return mTitle;
    }

    public boolean isNotifyOnFinish() {
        return mNotifyOnFinish;
    }

    public DownloadItem(DownloadItem item) {
        this.mUrlConnection = item.getUrlConnection();
        this.mTitle = item.getTitle();
        this.mNotifyOnFinish = item.isNotifyOnFinish();
        this.mForceDownload = item.mForceDownload;
    }
}
