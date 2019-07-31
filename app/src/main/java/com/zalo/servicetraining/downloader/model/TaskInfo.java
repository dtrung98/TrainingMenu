package com.zalo.servicetraining.downloader.model;

import com.zalo.servicetraining.downloader.base.BaseTask;

public class TaskInfo {
    // Used by Adapter
    public static final int SECTION_DOWNLOADING = 0;
    public static final int SECTION_DOWNLOADED = 1;

    public int getId() {
        return mId;
    }

    public long getCreatedTime() {
        return mCreatedTime;
    }

    public String getFileTitle() {
        return mFileTitle;
    }

    public String getDirectory() {
        return mDirectory;
    }

    public String getURLString() {
        return mURLString;
    }

    // Task Property
    private final int mId;
    private final long mCreatedTime;
    private final String mFileTitle ;
    private final String mDirectory;
    private final String mURLString;

    private float mSpeedInBytes=0;

    private String mMessage;

    private float mProgress = 0;
    private boolean mIsProgressSupport;

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public long getDownloadedInBytes() {
        return mDownloadedInBytes;
    }

    public void setDownloadedInBytes(long downloadedInBytes) {
        mDownloadedInBytes = downloadedInBytes;
    }

    public long getFileContentLength() {
        return mFileContentLength;
    }

    public void setFileContentLength(long fileContentLength) {
        mFileContentLength = fileContentLength;
    }

    public long getFirstExecutedTime() {
        return mFirstExecutedTime;
    }

    public long getFinishedTime() {
        return mFinishedTime;
    }

    public void setFinishedTime(long finishedTime) {
        mFinishedTime = finishedTime;
    }

    public long getRunningTime() {
        return mRunningTime;
    }

    public void setRunningTime(long runningTime) {
        mRunningTime = runningTime;
    }

    private long mDownloadedInBytes;
    private long mFileContentLength = -1;

    private long mFirstExecutedTime = -1;
    private long mLastExecutedTime = -1;
    private long mFinishedTime = -1;
    private long mRunningTime = 0;

    public TaskInfo(int id, long createdTime, String fileTitle, String directory, String URLString) {
        mId = id;
        mCreatedTime = createdTime;
        mFileTitle = fileTitle;
        mDirectory = directory;
        mURLString = URLString;
    }

    public static TaskInfo newInstance(BaseTask task) {
        TaskInfo info = new TaskInfo(task.getId(),task.getCreatedTime(),task.getFileTitle(),task.getDirectory(),task.getURLString());
        info.setState(task.getState());
        info.setProgress(task.getProgress());
        info.setMessage(task.getMessage());
        info.setProgressSupport(task.isProgressSupport());
        info.setSpeedInBytes(task.getSpeedInBytes());
        info.mFirstExecutedTime = task.getFirstExecutedTime();
        info.mLastExecutedTime = task.getLastExecutedTime();
        info.mFinishedTime = task.getFinishedTime();
        info.mRunningTime = task.getRunningTime();
        info.mFileContentLength = task.getFileContentLength();
        info.mDownloadedInBytes = task.getDownloadedInBytes();
        return info;
    }

    private int mState = BaseTask.PENDING;

    public int getSectionState() {
        return mState== BaseTask.SUCCESS? SECTION_DOWNLOADED : SECTION_DOWNLOADING;
    }

    public int getState() {
        return mState;
    }

    public TaskInfo setState(int state) {
        mState = state;
        return this;

    }

    public boolean isProgressSupport() {
        return mIsProgressSupport;
    }

    public TaskInfo setProgressSupport(boolean progressSupport) {
        mIsProgressSupport = progressSupport;
        return this;
    }

    public float getProgress() {
        return mProgress;
    }

    public TaskInfo setProgress(float progress) {
        mProgress = progress;
        return this;

    }

    public float getSpeedInBytes() {
        return mSpeedInBytes;
    }

    public void setSpeedInBytes(float speedInBytes) {
        mSpeedInBytes = speedInBytes;
    }

    public long getLastExecutedTime() {
        return mLastExecutedTime;
    }

    public void setLastExecutedTime(long lastExecutedTime) {
        mLastExecutedTime = lastExecutedTime;
    }
}
