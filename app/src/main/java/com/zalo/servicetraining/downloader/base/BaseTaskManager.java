package com.zalo.servicetraining.downloader.base;

import com.zalo.servicetraining.downloader.database.DownloadDBHelper;
import com.zalo.servicetraining.downloader.model.DownloadItem;
import com.zalo.servicetraining.downloader.model.TaskInfo;
import com.zalo.servicetraining.downloader.model.TaskList;
import com.zalo.servicetraining.downloader.service.DownloaderService;
import com.zalo.servicetraining.downloader.task.ranges.FileDownloadTask;
import com.zalo.servicetraining.downloader.task.simple.SimpleTaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class BaseTaskManager<T extends BaseTask> {
    /*
     * Number of cores to decide the number of threads
     */
    public static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    public ThreadPoolExecutor mExecutor;
    private DownloaderService mService;

    private final static int WHAT_TASK_CHANGED = 1;

    protected ArrayList<T> mTaskList = new ArrayList<>();

    public BaseTaskManager() {
    }

    public void init(DownloaderService service) {
        mService = service;
    }

    public void updatePreference() {

    }
    private Runnable mRestoreInstanceRunnable = new Runnable() {
        @Override
        public void run() {
            restoreInstanceInBackground();
        }
    };
    private void restoreInstanceInBackground() {
        mTaskList.clear();
        List<TaskInfo> infos =DownloadDBHelper.getInstance().getSavedTaskFromDatabase();
        if(this instanceof SimpleTaskManager)
            for (TaskInfo info: infos) {
                FileDownloadTask task = FileDownloadTask.restoreInstance((SimpleTaskManager) this,info);
                ((SimpleTaskManager)this).mTaskList.add(task);
            }
        notifyManagerChanged();
    }

    public void addNewTask(DownloadItem item) {
        T task = newInstance(item);
        task.setMode(BaseTask.EXECUTE_MODE_NEW_DOWNLOAD);
        mTaskList.add(task);
        if(mExecutor==null) throw new NullPointerException("Executor is null");
        mExecutor.execute(task);
        notifyTaskChanged(task);
    }

    public void executeExistedTask(BaseTask task) {
        if(mExecutor==null) throw new NullPointerException("Executor is null");
        mExecutor.execute(task);
        notifyTaskChanged(task);
    }


    public abstract T newInstance(DownloadItem item);

    public void destroy() {
        mService = null;
        mTaskList.clear();
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
        TaskInfo info = TaskInfo.newInstance(task);
        DownloadDBHelper.getInstance().saveTask(info);
    }

    public ArrayList<T> getAllTask() {
        return mTaskList;
    }

    public synchronized boolean isSomeTaskRunning() {
        for (T task :
                mTaskList) {
            if(task.getState()== BaseTask.RUNNING) return true;
        }
        return false;
    }

    public synchronized void restoreInstance() {
       mExecutor.execute(mRestoreInstanceRunnable);
    }

    public synchronized TaskList getSessionTaskList() {
        ArrayList<T> tasks = getAllTask();
        ArrayList<TaskInfo> infos = new ArrayList<>();
        for (int i = tasks.size() -1; i >= 0; i--) {
            infos.add(TaskInfo.newInstance(tasks.get(i)));
        }

        return new TaskList().setList(infos);
    }

    public TaskInfo getTaskInfo(int id) {
        List<T> tasks = getAllTask();
        BaseTask task = null;
        for (T t:
                tasks) {
            if(t.getId()==id) {
                task = t;
            }
        }

        if(task!=null) {
            return TaskInfo.newInstance(task);
        }
        return null;
    }

    public void pauseTaskFromUser(int id) {
        List<T> tasks = getAllTask();
        BaseTask task = null;

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
        BaseTask task = null;

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
        BaseTask task = null;

        for (T t:
                tasks) {
            if(t.getId()==id) {
                task = t;
            }
        }

        if(task!=null&&task.getState()== BaseTask.PAUSED) {
            task.resumeByUser();
        }
    }

    public void restartTaskByUser(int id) {
        List<T> tasks = getAllTask();
        BaseTask task = null;

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
