package com.zalo.servicetraining.downloader.base;

import com.zalo.servicetraining.downloader.model.DownloadItem;
import com.zalo.servicetraining.downloader.service.DownloaderService;

import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class BaseTaskManager {
    /*
     * Number of cores to decide the number of threads
     */
    public static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    public ThreadPoolExecutor mExecutor;
    private DownloaderService mService;

    ArrayList<BaseTask> mTaskList = new ArrayList<>();

    public BaseTaskManager() {
    }

    public void init(DownloaderService service) {
        mService = service;
    }

    public void updatePreference() {

    }

    public void addNewTask(DownloadItem item) {
        BaseTask task = onNewTaskAdded(item);
        mTaskList.add(task);
        if(mExecutor==null) throw new NullPointerException("Executor is null");
        mExecutor.execute(task);
    }

    public abstract BaseTask onNewTaskAdded(DownloadItem item);

    public void destroy() {
        mService = null;
    }

    public void notifyManagerChanged(){
        if(mService!=null) {
            mService.updateFromTaskManager(this);
        }
    }
    public void notifyTaskChanged(BaseTask task) {
        if(mService!=null) {
            mService.updateFromTask(task);
        }
    }

    public ArrayList<BaseTask> getAllTask() {
        return mTaskList;
    }

    public synchronized boolean isSomeTaskRunning() {
        for (BaseTask task :
                mTaskList) {
            if(task.getState()==BaseTask.RUNNING) return true;
        }
        return false;
    }

}
