package com.zalo.servicetraining.downloader.service.taskmanager;

import android.util.Log;

import com.zalo.servicetraining.downloader.model.DownloadItem;
import com.zalo.servicetraining.downloader.service.DownloaderService;

import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

public class RangeDownloadTask implements Runnable {
    private static final String TAG = "RangeDownloadTask";
    private static final int PENDING = 0;
    private static final int RUNNING = 1;
    private static final int FAILURE_TERMINATED = 2;
    private static final int SUCCESS = 3;
    private static final int PAUSED = 4;

    private static final int MODE_DOWNLOAD_FROM_START = 5;
    private static final int MODE_DOWNLOAD_TRY_TO_APPEND = 6;

    private static final int MODE_CONTINUE_RESTART = 7;
    private static final int MODE_CONTINUE_RESUME = 8;

    public int getId() {
        return mId;
    }

    private final int mId;
    private final DownloadItem mDownloadItem;

    private final WeakReference<DownloaderService> mRefService;

    public RangeDownloadTask(final int id, DownloaderService service, DownloadItem item) {
        mId = id;
        mRefService = new WeakReference<>(service);
        mDownloadItem = item;
    }

    @Override
    public void run() {
        longRunningTask();
    }

    private void longRunningTask() {
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(1000);
                Log.d(TAG, "task "+getId()+" run: sleep "+i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            reportStatus();
        }
        Log.d(TAG, "run: finish");
    }

    private void reportStatus() {

    }

}
