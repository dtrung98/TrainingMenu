package com.zalo.servicetraining.downloader.task.simple;

import android.os.Process;
import android.util.Log;

import com.zalo.servicetraining.downloader.base.BaseTaskManager;
import com.zalo.servicetraining.downloader.database.DownloadDBHelper;
import com.zalo.servicetraining.downloader.model.DownloadItem;
import com.zalo.servicetraining.downloader.service.DownloaderService;
import com.zalo.servicetraining.downloader.task.ranges.FileDownloadTask;
import com.zalo.servicetraining.downloader.threading.PriorityThreadFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SimpleTaskManager extends BaseTaskManager<FileDownloadTask> {
    private static final String TAG = "SimpleTaskManager";

    public SimpleTaskManager() {
        super();
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
    public synchronized FileDownloadTask newInstance(DownloadItem item) {
        int nextId = (int)DownloadDBHelper.getInstance().generateNewTaskId(item);

        return new FileDownloadTask(nextId,this,item,5);
    }
}
