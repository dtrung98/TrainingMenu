package com.zalo.servicetraining.downloader.base;

import com.zalo.servicetraining.downloader.model.DownloadItem;
import com.zalo.servicetraining.downloader.model.TaskInfo;
import com.zalo.servicetraining.downloader.service.DownloaderService;
import com.zalo.servicetraining.downloader.service.task.SimpleDownloadTask;

import java.util.ArrayList;
import java.util.List;
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
        T task = newInstance(item);
        task.setMode(AbsTask.EXECUTE_MODE_NEW_DOWNLOAD);
        mTaskList.add(task);
        if(mExecutor==null) throw new NullPointerException("Executor is null");
        mExecutor.execute(task);
        notifyTaskChanged(task);
    }

    public void executeExistedTask(AbsTask task) {
        if(mExecutor==null) throw new NullPointerException("Executor is null");
        mExecutor.execute(task);
        notifyTaskChanged(task);
    }


    public abstract T newInstance(DownloadItem item);

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

    public synchronized List<TaskInfo> getSessionTaskList() {
        List<T> tasks = getAllTask();
        List<TaskInfo> infos = new ArrayList<>();
        for (int i = tasks.size() -1; i >= 0; i--) {
            T task = tasks.get(i);
            TaskInfo info = new TaskInfo();
            if (task instanceof SimpleDownloadTask)
                info.setDownloadItem(new DownloadItem(((SimpleDownloadTask) task).getDownloadItem()));

            info.setId(task.getId()).setState(task.getState())
                    .setProgress(task.getProgress())
                    .setProgressSupport(task.isProgressSupport());
            infos.add(info);
        }

        return infos;
    }

    public TaskInfo getTaskInfo(int id) {
        List<T> tasks = getAllTask();
        AbsTask task = null;
        for (T t:
                tasks) {
            if(t.getId()==id) {
                task = t;
            }
        }

        if(task!=null) {
            TaskInfo info = new TaskInfo();
            if(task instanceof SimpleDownloadTask)
                info.setDownloadItem(new DownloadItem(((SimpleDownloadTask)task).getDownloadItem()));
            info.setId(task.getId())
                    .setState(task.getState())
                    .setProgress(task.getProgress())
                    .setProgressSupport(task.isProgressSupport());
            return info;
        }
        return null;
    }

    public void pauseTaskFromUser(int id) {
        List<T> tasks = getAllTask();
        AbsTask task = null;

        for (T t:
                tasks) {
            if(t.getId()==id) {
                task = t;
            }
        }

        if(task!=null) task.pauseByUser();

    }

    public void cancelTaskFromUser(int id) {
        List<T> tasks = getAllTask();
        AbsTask task = null;

        for (T t:
                tasks) {
            if(t.getId()==id) {
                task = t;
            }
        }

        if(task!=null) task.cancelByUser();
    }

    public void resumeTaskByUser(int id) {
        List<T> tasks = getAllTask();
        AbsTask task = null;

        for (T t:
                tasks) {
            if(t.getId()==id) {
                task = t;
            }
        }

        if(task!=null&&task.getState()==AbsTask.PAUSED) {
            task.resumeByUser();
        }
    }

    public void restartTaskByUser(int id) {
        List<T> tasks = getAllTask();
        AbsTask task = null;

        for (T t:
                tasks) {
            if(t.getId()==id) {
                task = t;
            }
        }

        if(task!=null) {
            task.restartByUser();
        }
    }
}
