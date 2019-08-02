package com.zalo.servicetraining.downloader.base;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.zalo.servicetraining.downloader.model.DownloadItem;
import com.zalo.servicetraining.util.Util;

import java.lang.ref.WeakReference;

public abstract class BaseTask<T extends BaseTaskManager> implements Runnable {
    public static final String EXTRA_PROGRESS_SUPPORT = "progress_support";
    private static final String TAG = "BaseTask";

    public static final String RANGE_PROPERTY = "Range";
    public static final String CONTENT_LENGTH = "content-length";

    public static final String EXTRA_TASK_ID = "notification_id";
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

    private final T mTaskManager;

    private float mProgress = 0;

    private long mDownloadedInBytes = 0;
    private long mFileContentLength = -1;
    private final long mCreatedTime;

    private long mFirstExecutedTime = -1;

    public long getLastExecutedTime() {
        return mLastExecutedTime;
    }

    private long mLastExecutedTime = -1;

    private long mFinishedTime = -1;
    private long mRunningTime = 0;
    private final String mFileTitle ;
    private final String mDirectory;
    private final String mURLString;

    private float mSpeedInBytes = 0;

    public long getCreatedTime() {
        return mCreatedTime;
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
        if(!mStopped) return mRunningTime + (System.currentTimeMillis() - mLastExecutedTime);
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
        mTaskManager = t;
        mDirectory = item.getDirectoryPath();
        mURLString = item.getUrlString();
        mCreatedTime = System.currentTimeMillis();
        mFileTitle = item.getFileTitle();
    }
    protected BaseTask(final int id, T t, String directory, String url, long createdTime, String fileTitle) {
        mId = id;
        mTaskManager = t;
        mDirectory = directory;
        mURLString = url;
        mCreatedTime = createdTime;
        mFileTitle = fileTitle;
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
        //Log.d(TAG, "setState with message: "+ message);
    }

    private int mState = PENDING;
    public synchronized float getProgress() {
        return mProgress;
    }

    public synchronized int getProgressInteger() {
        return (int) (mProgress*100);
    }

    /**
     * This class must not be called or override by subclasses
     * <br>Please use {@link #runTask} instead
     */
    @Override
    public void run() {
        recordProperties();
        startNotifier();

        runTask();

        stopRecord();
        releaseSafely();
    }

    public abstract void runTask();

    private void recordProperties() {
        mLastExecutedTime = System.currentTimeMillis();

        switch (getMode()) {
            case EXECUTE_MODE_NEW_DOWNLOAD:
            case EXECUTE_MODE_RESTART:
                mFirstExecutedTime = mLastExecutedTime;
                mRunningTime = 0;
                break;
            case EXECUTE_MODE_RESUME:
                if (mFirstExecutedTime == -1) mFirstExecutedTime = mLastExecutedTime;
        }
    }

    private void stopRecord() {
        mFinishedTime = System.currentTimeMillis();
        mRunningTime += (mFinishedTime - mLastExecutedTime);
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

    protected synchronized void notifyTaskChanged() {
        notifyTaskChanged(TASK_CHANGED);
    }


    protected void notifyTaskChanged(int which){

        if(mNotifyHandler==null)  getTaskManager().notifyTaskChanged(this);
        else {
            // Nếu chưa có order nào, thì hãy đợi 500s sau, t sẽ gửi
            if (!mProgressUpdateFlag) {
                mNotifyHandler.sendEmptyMessageDelayed(which, 1250);
                mProgressUpdateFlag = true;
                //Log.d(TAG, "thread id "+Thread.currentThread().getId()+": task id " + mId + " orders to update, plz wait for 1250ms");
            } else if (mFirstTime) {
                mFirstTime = false;
                mNotifyHandler.sendEmptyMessage(which);
            } else {
                // Nếu đã có order
                // bỏ qua
                //Log.d(TAG, "thread id "+Thread.currentThread().getId()+": task id " + mId + " is ignored, task will update soon");
            }
        }
    }
    private boolean mStopped = false;
    private void releaseSafely() {
        mStopped = true;
        notifyTaskChanged(TASK_CHANGED);
    }

    private void release() {
        if (mNotifyHandler!= null) {
            final Looper looper = mNotifyHandler.getLooper();
            looper.quitSafely();
            mNotifyHandler = null;
        }

        if(mNotifyThread!=null) {
            mNotifyThread.quitSafely();
            mNotifyThread = null;
        }

    }
    private HandlerThread mNotifyThread;
    private NotifyHandler mNotifyHandler;

    private void startNotifier(){
        mStopped = false;
        mNotifyThread = new HandlerThread("HandlerThread"+getId());
        mNotifyThread.start();
        mNotifyHandler = new NotifyHandler(this, mNotifyThread.getLooper());
    }


    public synchronized String getMessage() {
        return mMessage;
    }

    public synchronized void setMessage(String message) {
        mMessage = message;
    }

    public synchronized boolean isProgressSupport() {
        return mFileContentLength != -1;
    }

    private boolean mUserCancelledFlag = false;

    private boolean mUserPauseFlag = false;

    protected synchronized final boolean isPausedOrCancelled() {
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

    public synchronized final boolean isStopByUser() {
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

    public synchronized long getDownloadedInBytes() {
        return mDownloadedInBytes;
    }

    public synchronized void setDownloadedInBytes(long downloadedInBytes) {
        mDownloadedInBytes = downloadedInBytes;
    }

    public synchronized void appendDownloadedBytes(long bytes) {
        setDownloadedInBytes(getDownloadedInBytes()+ bytes);
        updateProgress();
    }

    protected synchronized void updateProgress() {
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

    public synchronized long getFileContentLength() {
        return mFileContentLength;
    }

    public synchronized void setFileContentLength(long fileContentLength) {
        mFileContentLength = fileContentLength;
    }

    public synchronized int getMode() {
        return mMode;
    }

    public synchronized void setMode(int mode) {
        mMode = mode;
    }

    public synchronized void resumeByUser() {
        if(getState()==PAUSED) {
            setMode(EXECUTE_MODE_RESUME);
            setState(PENDING);
            clearUserFlag();
            getTaskManager().executeExistedTask(this);
        }
    }

    public synchronized void restartByUser() {
        setMode(EXECUTE_MODE_RESTART);
        setDownloadedInBytes(0);
        setState(PENDING);
        clearUserFlag();
        getTaskManager().executeExistedTask(this);
    }

    public synchronized float getSpeedInBytes() {
        return mSpeedInBytes;
    }


    protected void restoreProgress(float progress) {
        mProgress = progress;
    }

    private static class NotifyHandler extends Handler {
        private final WeakReference<BaseTask> mWeakRefTask;
        NotifyHandler(BaseTask task, Looper looper) {
            super(looper);
            mWeakRefTask = new WeakReference<>(task);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseTask task = mWeakRefTask.get();
            if(task==null) return;
            switch (msg.what) {
                case TASK_CHANGED:

                    task.mProgressUpdateFlag = false;
                    //Log.d(TAG, "thread id "+Thread.currentThread().getId()+": task id "+task.mId+" is updating with progress "+task.getProgress());
                    task.getTaskManager().notifyTaskChanged(task);
                    if(task.mStopped) {
                        //Log.d(TAG, "and stop too");
                        task.release();
                    }
                    break;
            }
        }
    }


    public synchronized void initSpeed() {
        mLastUpdatedSpeedTime = System.currentTimeMillis();
        mLastUpdatedSpeedDownloaded = getDownloadedInBytes();
        mSpeedInBytes = 0;
    }

    public synchronized void calculateSpeed() {

        long currentDownloaded = getDownloadedInBytes();
        long currentTime = System.currentTimeMillis();

        if(currentTime-mLastUpdatedSpeedTime<800) return;

         mSpeedInBytes = (currentDownloaded - mLastUpdatedSpeedDownloaded +0.0f)/(currentTime- mLastUpdatedSpeedTime)*1000;
         mLastUpdatedSpeedTime = currentTime;
         mLastUpdatedSpeedDownloaded = currentDownloaded;
    }
}
