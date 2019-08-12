package com.zalo.trainingmenu.downloader.task.partial;

import com.zalo.trainingmenu.downloader.base.BaseTaskManager;
import com.zalo.trainingmenu.downloader.database.DownloadDBHelper;
import com.zalo.trainingmenu.downloader.model.DownloadItem;


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
