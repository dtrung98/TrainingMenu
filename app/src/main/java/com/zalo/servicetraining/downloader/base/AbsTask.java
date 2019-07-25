package com.zalo.servicetraining.downloader.base;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

public abstract class AbsTask<T extends AbsTaskManager> implements Runnable {
    public static final String EXTRA_PROGRESS_SUPPORT = "progress_support";
    private static final String TAG = "AbsTask";

    public static final String EXTRA_NOTIFICATION_ID = "notification_id";
    public static final String EXTRA_STATE = "state";
    public static final String EXTRA_PROGRESS ="progress";

    public static final int PENDING = 0;
    public static final int RUNNING = 1;
    public static final int FAILURE_TERMINATED = 2;
    public static final int SUCCESS = 3;
    public static final int PAUSED = 4;

    private final static int PROGRESS_CHANGED = 1;

    private String mMessage = "";

    private T mTaskManager;

    private boolean mIsProgressSupport = false;


    public AbsTask(final int id, T t) {
        mId = id;
        setTaskManager(t);
    }

    private final int mId;
    public AbsTask(final int id) {
        mId = id;
    }

    public void setTaskManager(T manager) {
        this.mTaskManager = manager;
    }

    public T getTaskManager() {
        return mTaskManager;
    }

    public static String getStateName(int state) {
        switch (state) {
            case PENDING: return "PENDING";
            case RUNNING: return "RUNNING";
            case FAILURE_TERMINATED: return "FAILURE_TERMINATED";
            case SUCCESS: return "SUCCESS";
            case PAUSED: return "PAUSED";
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
        Log.d(TAG, "setSectionState with message: "+ message);
    }

    private int mState = PENDING;
    private float mProgress = 0;
    public synchronized float getProgress() {
        return mProgress;
    }

    @Override
    public void run() {
        startHandlerThread();
    }

    protected synchronized final void setProgress(float value) {
        mProgress = value;
        if(mProgress==1) setState(SUCCESS);
    }

    protected synchronized final void setProgressAndNotify(float value) {
        if(mProgress !=value) {
            mProgress = value;
            if(mProgress ==1) setState(SUCCESS);
            notifyProgressChanged();
        }
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized int getId() {
        return mId;
    }
    private boolean mFirstTime = true;
    private boolean mProgressUpdateFlag = false;

    protected void notifyProgressChanged(){

        // Nếu chưa có order nào, thì hãy đợi 500s sau, t sẽ gửi
        if(!mProgressUpdateFlag) {
            mNotifyHandler.sendEmptyMessageDelayed(PROGRESS_CHANGED,1250);
            mProgressUpdateFlag = true;
            Log.d(TAG, "task id "+mId+", update after 1250ms");
        } else if(mFirstTime) {
            mFirstTime = false;
            mNotifyHandler.sendEmptyMessage(PROGRESS_CHANGED);
        }
        else {
            // Nếu đã có order
            // bỏ qua
            Log.d(TAG, "task id "+mId+", update ignored");
        }

    }

    private HandlerThread mHandlerThread;

    public void startHandlerThread(){
        mHandlerThread = new HandlerThread("HandlerThread");
        mHandlerThread.start();
        mNotifyHandler = new NotifyHandler(this, mHandlerThread.getLooper());
    }

    private NotifyHandler mNotifyHandler;

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public boolean isProgressSupport() {
        return mIsProgressSupport;
    }

    public void setProgressSupport(boolean progressSupport) {
        mIsProgressSupport = progressSupport;
    }

    private static class NotifyHandler extends Handler {
        private final AbsTask mTask;
        NotifyHandler(AbsTask task, Looper looper) {
            super(looper);
            mTask = task;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROGRESS_CHANGED:
                    mTask.mProgressUpdateFlag = false;
                    Log.d(TAG, "task id "+mTask.mId+" is updating with progress "+mTask.getProgress());

                    mTask.getTaskManager().notifyTaskChanged(mTask);
                    break;
            }
        }
    }
}
