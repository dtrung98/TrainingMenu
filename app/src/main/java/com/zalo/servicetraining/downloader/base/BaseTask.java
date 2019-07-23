package com.zalo.servicetraining.downloader.base;

public abstract class BaseTask implements Runnable {
    public static final int PENDING = 0;
    public static final int RUNNING = 1;
    public static final int FAILURE_TERMINATED = 2;
    public static final int SUCCESS = 3;
    public static final int PAUSED = 4;

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
    }

    private int mState = PENDING;
    private float mProgress = 0;
    public synchronized float getProgress() {
        return mProgress;
    }

    protected synchronized final void setProgress(float value) {
        mProgress = value;
        if(mProgress==1) setState(SUCCESS);
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized int getId() {
        return mId;
    }

    private final int mId;
    public BaseTask(final int id) {
        mId = id;
    }
}
