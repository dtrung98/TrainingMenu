package com.zalo.servicetraining.downloader.base;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.zalo.servicetraining.downloader.model.DownloadItem;
import com.zalo.servicetraining.downloader.service.DownloaderService;

import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class AbsTaskManager<T extends AbsTask> {
    /*
     * Number of cores to decide the number of threads
     */
    public static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    public ThreadPoolExecutor mExecutor;
    private DownloaderService mService;

    private final static int WHAT_TASK_CHANGED = 1;

    private ArrayList<T> mTaskList = new ArrayList<>();

    public AbsTaskManager() {
    }

    public void init(DownloaderService service) {
        mService = service;
    }

    public void updatePreference() {

    }

    public void addNewTask(DownloadItem item) {
        T task = onNewTaskAdded(item);
        mTaskList.add(task);
        if(mExecutor==null) throw new NullPointerException("Executor is null");
        mExecutor.execute(task);
    }

    public abstract T onNewTaskAdded(DownloadItem item);

    public void destroy() {
        mService = null;
    }

    public void notifyManagerChanged(){
        if(mService!=null) {
            mService.updateFromTaskManager(this);
        }

    }

    public void notifyTaskChanged(AbsTask task) {
        if(mService!=null) {
            mService.updateFromTask(task);
        }
    }

    public ArrayList<T> getAllTask() {
        return mTaskList;
    }

    public synchronized boolean isSomeTaskRunning() {
        for (T task :
                mTaskList) {
            if(task.getState()== AbsTask.RUNNING) return true;
        }
        return false;
    }

}
