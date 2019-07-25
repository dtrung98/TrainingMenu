package com.zalo.servicetraining.downloader.model;

import com.zalo.servicetraining.downloader.base.AbsTask;

public class TaskInfo {
    public static final int STATE_DOWNLOADING = 0;
    public static final int STATE_DOWNLOADED = 1;

    private float mProgress = 0;
    private boolean mProgressSupport = false;
    private int mState = AbsTask.PENDING;
    private DownloadItem mDownloadItem;

    public int getSectionState() {
        return mState==AbsTask.SUCCESS? STATE_DOWNLOADED : STATE_DOWNLOADING;
    }

    public DownloadItem getDownloadItem() {
        return mDownloadItem;
    }

    public TaskInfo setDownloadItem(DownloadItem downloadItem) {
        mDownloadItem = downloadItem;
        return this;
    }

    public int getState() {
        return mState;
    }

    public TaskInfo setState(int state) {
        mState = state;
        return this;

    }

    public boolean isProgressSupport() {
        return mProgressSupport;
    }

    public TaskInfo setProgressSupport(boolean progressSupport) {
        mProgressSupport = progressSupport;
        return this;
    }

    public float getProgress() {
        return mProgress;
    }

    public TaskInfo setProgress(float progress) {
        mProgress = progress;
        return this;

    }
}
