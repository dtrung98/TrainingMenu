package com.zalo.servicetraining.downloader.task.simple;

import com.zalo.servicetraining.downloader.base.BaseTaskManager;
import com.zalo.servicetraining.downloader.database.DownloadDBHelper;
import com.zalo.servicetraining.downloader.model.DownloadItem;
import com.zalo.servicetraining.downloader.task.partial.FileDownloadTask;


public class SimpleTaskManager extends BaseTaskManager<SimpleDownloadTask> {
    private static final String TAG = "SimpleTaskManager";

    public SimpleTaskManager() {
        super();
    }


    @Override
    public void init(CallBack callBack) {
        super.init(callBack);
    }

    @Override
    public synchronized SimpleDownloadTask newInstance(DownloadItem item) {
        int nextId = (int)DownloadDBHelper.getInstance().generateNewTaskId(item);

        return new SimpleDownloadTask(nextId,this,item);
    }
}
