package com.zalo.servicetraining.downloader.task.partial;

import com.zalo.servicetraining.downloader.base.BaseTaskManager;
import com.zalo.servicetraining.downloader.database.DownloadDBHelper;
import com.zalo.servicetraining.downloader.model.DownloadItem;


public class PartialTaskManager extends BaseTaskManager<FileDownloadTask> {
    private static final String TAG = "PartialTaskManager";

    public PartialTaskManager() {
        super();
    }


    @Override
    public void init(CallBack callBack) {
        super.init(callBack);
    }

    @Override
    public synchronized FileDownloadTask newInstance(DownloadItem item) {
        int nextId = (int)DownloadDBHelper.getInstance().generateNewTaskId(item);

        return new FileDownloadTask(nextId,this,item);
    }
}
