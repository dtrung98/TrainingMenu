package com.zalo.servicetraining.downloader.service.taskmanager;

import android.util.Log;

import com.zalo.servicetraining.downloader.base.BaseTask;
import com.zalo.servicetraining.downloader.model.DownloadItem;

public class SimpleDownloadTask extends BaseTask {
    private static final String TAG = "SimpleDownloadTask";

    private static final int MODE_DOWNLOAD_FROM_START = 5;
    private static final int MODE_DOWNLOAD_TRY_TO_APPEND = 6;

    private static final int MODE_CONTINUE_RESTART = 7;
    private static final int MODE_CONTINUE_RESUME = 8;

    private int mMode = MODE_DOWNLOAD_FROM_START;

    private final DownloadItem mDownloadItem;
    private final SimpleTaskManager mDownloadManager;

    public SimpleDownloadTask(final int id, SimpleTaskManager manager, DownloadItem item) {
        super(id);
        mDownloadManager = manager;
        mDownloadItem = item;
    }

    @Override
    public void run() {
        longRunningTask();
    }

    private void longRunningTask() {
        setState(RUNNING);
        mDownloadManager.notifyTaskChanged(this);

        for (int i = 1; i <= 20; i++) {
            try {
                Thread.sleep(1000);
                Log.d(TAG, "task "+getId()+" run: sleep "+i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setProgress((float) (i/20.0));
            mDownloadManager.notifyTaskChanged(this);
        }
        Log.d(TAG, "run: finish");
    }

}
