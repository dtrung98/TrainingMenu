package com.zalo.trainingmenu.downloader.task.simple;

import com.zalo.trainingmenu.downloader.base.BaseTaskManager;
import com.zalo.trainingmenu.downloader.database.DownloadDBHelper;
import com.zalo.trainingmenu.downloader.model.DownloadItem;


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
