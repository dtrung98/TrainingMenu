package com.zalo.trainingmenu.downloader.task.partial;

import com.zalo.trainingmenu.downloader.base.BaseTaskController;
import com.zalo.trainingmenu.downloader.database.DownloadDBHelper;
import com.zalo.trainingmenu.downloader.model.DownloadItem;


public class PartialTaskController extends BaseTaskController<FileDownloadTask> {
    private static final String TAG = "PartialTaskController";

    public PartialTaskController() {
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
