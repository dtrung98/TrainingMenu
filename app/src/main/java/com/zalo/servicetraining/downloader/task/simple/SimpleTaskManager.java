package com.zalo.servicetraining.downloader.task.simple;

import android.os.Process;
import android.util.Log;

import com.zalo.servicetraining.downloader.base.BaseTask;
import com.zalo.servicetraining.downloader.base.BaseTaskManager;
import com.zalo.servicetraining.downloader.model.DownloadItem;
import com.zalo.servicetraining.downloader.service.DownloaderService;
import com.zalo.servicetraining.downloader.task.ranges.FileDownloadTask;
import com.zalo.servicetraining.downloader.threading.PriorityThreadFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SimpleTaskManager extends BaseTaskManager {
    private static final String TAG = "SimpleTaskManager";
    private static int mIdCounting = 1;

    public static int DOWNLOAD_MODE_SIMPLE = 0;
    public static int DOWNLOAD_MODE_PARTIAL = 1;

    private synchronized static int getNextId() {
        int current = mIdCounting;
        mIdCounting++;

        return current;
    }

    public SimpleTaskManager() {
        super();
    }

    private int mMode = DOWNLOAD_MODE_PARTIAL;
    public void setMode(int mode) {
        mMode = mode;
    }

    @Override
    public void init(DownloaderService service) {
        super.init(service);

        // setting the thread factory
        ThreadFactory backgroundPriorityThreadFactory = new
                PriorityThreadFactory(Process.THREAD_PRIORITY_BACKGROUND);


        if(mExecutor ==null)
            mExecutor = new ThreadPoolExecutor(
                    4,
                    4,
                    60L,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    backgroundPriorityThreadFactory
            );
        Log.d(TAG, "initExecutor with corePoolSize = "+(NUMBER_OF_CORES*2));
    }

    @Override
    public synchronized BaseTask newInstance(DownloadItem item) {
        if(mMode==DOWNLOAD_MODE_SIMPLE)
        return new SimpleDownloadTask(getNextId(),this,item);
        return new FileDownloadTask(getNextId(),this,item,5);
    }


}
