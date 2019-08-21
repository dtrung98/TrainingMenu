package com.zalo.trainingmenu.downloader.task.simple;

import com.zalo.trainingmenu.downloader.base.BaseTaskController;
import com.zalo.trainingmenu.downloader.database.DownloadDBHelper;
import com.zalo.trainingmenu.downloader.model.DownloadItem;


public class SimpleTaskController extends BaseTaskController<SimpleDownloadTask> {
    private static final String TAG = "SimpleTaskController";

    public SimpleTaskController() {
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
