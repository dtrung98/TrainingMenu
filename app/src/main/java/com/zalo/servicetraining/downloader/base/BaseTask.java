package com.zalo.servicetraining.downloader.base;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.zalo.servicetraining.downloader.model.DownloadItem;
import com.zalo.servicetraining.util.Util;

public abstract class BaseTask<T extends BaseTaskManager> implements Runnable {
    public static final String EXTRA_PROGRESS_SUPPORT = "progress_support";
    private static final String TAG = "BaseTask";

    public static final String EXTRA_NOTIFICATION_ID = "notification_id";
    public static final String EXTRA_STATE = "state";
    public static final String EXTRA_PROGRESS ="progress";
    public static final String EXTRA_DOWNLOADED_IN_BYTES = "downloaded_in_bytes";
    public static final String EXTRA_FILE_CONTENT_LENGTH = "file_content_length";
    public static final String EXTRA_SPEED ="speed";

    public static final int PENDING = 0;
    public static final int RUNNING = 1;
    public static final int PAUSED = 3;
    public static final int CONNECTING = 4;
    public static final int SUCCESS = 5;
    public static final int FAILURE_TERMINATED = 6;
    public static final int CANCELLED = 7;

    private final static int TASK_CHANGED = 1;

    public static final int EXECUTE_MODE_NEW_DOWNLOAD = 5;

    public static final int EXECUTE_MODE_RESTART = 7;
    public static final int EXECUTE_MODE_RESUME = 8;


    private int mMode = EXECUTE_MODE_NEW_DOWNLOAD;
    private final int mId;
    private String mMessage = "";

    private T mTaskManager;

    private float mProgress = 0;

    private long mDownloadedInBytes = 0;
    private long mFileContentLength = -1;
    private final long mCreatedTime;
    private long mExecutedTime = -1;

    private long mFinishedTime = -1;
    private long mRunningTime = 0;
    private final String mFileTitle ;
    private final String mDirectory;
    private final String mURLString;

    private float mSpeedInBytes = 0;

    public long getCreatedTime() {
        return mCreatedTime;
    }

    public long getExecutedTime() {
        return mExecutedTime;
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

    public String getFileTitle() {
        return mFileTitle;
    }

    public String getDirectory() {
        return mDirectory;
    }

    public String getURLString() {
        return mURLString;
    }

    public BaseTask(final int id, T t, DownloadItem item) {
        mId = id;
        setTaskManager(t);
        mDirectory = item.getDirectoryPath();
        mURLString = item.getUrlString();
        mCreatedTime = System.currentTimeMillis();
        mFileTitle = item.getFileTitle();
    }

    public BaseTask(final int id, DownloadItem item) {
        mId = id;
        mDirectory = item.getDirectoryPath();
        mURLString = item.getUrlString();
        mCreatedTime = System.currentTimeMillis();
        mFileTitle = item.getFileTitle();
    }

    public void setTaskManager(T manager) {
        this.mTaskManager = manager;
    }

    public T getTaskManager() {
        return mTaskManager;
    }

    public static String getStateName(int state) {
        switch (state) {
            case PENDING: return "Pending";
            case RUNNING: return "Running";
            case FAILURE_TERMINATED: return "Failure Terminated";
            case SUCCESS: return "Success";
            case PAUSED: return "Paused";
            case CANCELLED: return "Cancelled";
            case CONNECTING: return "Connecting";
            default: return null;
        }
    }

    protected synchronized void setState(int mState) {
        this.mState = mState;
        setMessage("");
    }

    protected synchronized void setState(int state, String message) {
        this.mState = state;
        setMessage(message);
        Log.d(TAG, "setState with message: "+ message);
    }

    private int mState = PENDING;
    public synchronized float getProgress() {
        return mProgress;
    }

    public synchronized int getProgressInteger() {
        return (int) (mProgress*100);
    }

    @Override
    public void run() {
        startHandlerThread();
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized int getId() {
        return mId;
    }
    private boolean mFirstTime = true;
    private long mLastUpdatedSpeedTime = 0;
    private long mLastUpdatedSpeedDownloaded = 0;
    private boolean mProgressUpdateFlag = false;

    protected void notifyTaskChanged() {
        notifyTaskChanged(TASK_CHANGED);
    }

    protected void notifyTaskChanged(int whichChanged){

        if(mNotifyHandler==null)  getTaskManager().notifyTaskChanged(this);
        else {
            // Nếu chưa có order nào, thì hãy đợi 500s sau, t sẽ gửi
            if (!mProgressUpdateFlag) {
                mNotifyHandler.sendEmptyMessageDelayed(TASK_CHANGED, 1250);
                mProgressUpdateFlag = true;
                Log.d(TAG, "task id " + mId + ", update after 1250ms");
            } else if (mFirstTime) {
                mFirstTime = false;
                mNotifyHandler.sendEmptyMessage(TASK_CHANGED);
            } else {
                // Nếu đã có order
                // bỏ qua
                Log.d(TAG, "task id " + mId + ", update ignored");
            }
        }
    }

    public void startHandlerThread(){
        HandlerThread handlerThread = new HandlerThread("HandlerThread");
        handlerThread.start();
        mNotifyHandler = new NotifyHandler(this, handlerThread.getLooper());
    }

    private NotifyHandler mNotifyHandler;

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public boolean isProgressSupport() {
        return mFileContentLength != -1;
    }

    private boolean mUserCancelledFlag = false;

    private boolean mUserPauseFlag = false;

    protected final boolean isPausedOrCancelled() {
        return mUserPauseFlag|| mUserCancelledFlag;
    }

    protected final void clearUserFlag() {
        mUserCancelledFlag = false;
        mUserPauseFlag = false;
    }

    protected final void pauseByUser() {
        if(!isPausedOrCancelled()) mUserPauseFlag = true;
        if(getState()==PENDING) {
            setState(PAUSED);
            notifyTaskChanged();
        }
    }

    protected final void cancelByUser() {
        if(!isPausedOrCancelled()) mUserCancelledFlag = true;
        if(getState()==PENDING) {
            setState(CANCELLED);
            notifyTaskChanged();
        }
    }

    protected final boolean shouldStopByUser() {
         if(mUserCancelledFlag) {
            setState(CANCELLED);
            notifyTaskChanged();
            return true;
        }
        if(mUserPauseFlag) {
             setState(PAUSED);
             notifyTaskChanged();
             return true;
         }

        return false;
    }

    public long getDownloadedInBytes() {
        return mDownloadedInBytes;
    }

    public void setDownloadedInBytes(long downloadedInBytes) {
        mDownloadedInBytes = downloadedInBytes;
    }

    public void setDownloadedAndUpdateProgress(long bytes) {
        setDownloadedInBytes(bytes);
        if(isProgressSupport()) {
            float newProgress = (mDownloadedInBytes+0.0f)/mFileContentLength;
            if(newProgress>1) newProgress = 0.99f;
            else if(newProgress<0) newProgress = 0f;

            if(newProgress!=mProgress) {
                mProgress = newProgress;
                if (mProgress == 1) setState(SUCCESS);
                notifyTaskChanged();
            }
        }
    }

    public long getFileContentLength() {
        return mFileContentLength;
    }

    public void setFileContentLength(long fileContentLength) {
        mFileContentLength = fileContentLength;
    }

    public int getMode() {
        return mMode;
    }

    public void setMode(int mode) {
        mMode = mode;
    }

    public void resumeByUser() {
        if(getState()==PAUSED) {
            setMode(EXECUTE_MODE_RESUME);
            setState(PENDING);
            clearUserFlag();
            getTaskManager().executeExistedTask(this);
        }
    }

    public void restartByUser() {
        setMode(EXECUTE_MODE_RESTART);
        setDownloadedInBytes(0);
        setState(PENDING);
        clearUserFlag();
        getTaskManager().executeExistedTask(this);
    }

    public float getSpeedInBytes() {
        return mSpeedInBytes;
    }

    public String getSpeedInBytesString() {
        if(isProgressSupport()) return Util.humanReadableByteCount((long) getSpeedInBytes(),true)+"/s";
        return "";
    }

    private static class NotifyHandler extends Handler {
        private final BaseTask mTask;
        NotifyHandler(BaseTask task, Looper looper) {
            super(looper);
            mTask = task;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TASK_CHANGED:
                    mTask.mProgressUpdateFlag = false;
                    Log.d(TAG, "task id "+mTask.mId+" is updating with progress "+mTask.getProgress());
                    mTask.getTaskManager().notifyTaskChanged(mTask);
                    break;
            }
        }
    }
    protected void initSpeed() {
        mLastUpdatedSpeedTime = System.currentTimeMillis();
        mLastUpdatedSpeedDownloaded = getDownloadedInBytes();
        mSpeedInBytes = 0;
    }

    protected void calculateSpeed() {

        long currentDownloaded = getDownloadedInBytes();
        long currentTime = System.currentTimeMillis();

        if(currentTime-mLastUpdatedSpeedTime<800) return;

         mSpeedInBytes = (currentDownloaded - mLastUpdatedSpeedDownloaded +0.0f)/(currentTime- mLastUpdatedSpeedTime)*1000;
         mLastUpdatedSpeedTime = currentTime;
         mLastUpdatedSpeedDownloaded = currentDownloaded;
    }
}
