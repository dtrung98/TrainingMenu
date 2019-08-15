package com.zalo.trainingmenu.downloader.base;

import android.os.Process;
import android.util.Log;

import com.zalo.trainingmenu.downloader.database.DownloadDBHelper;
import com.zalo.trainingmenu.downloader.model.DownloadItem;
import com.zalo.trainingmenu.downloader.model.TaskInfo;
import com.zalo.trainingmenu.downloader.model.TaskList;
import com.zalo.trainingmenu.downloader.task.partial.FileDownloadTask;
import com.zalo.trainingmenu.downloader.task.partial.PartialTaskManager;
import com.zalo.trainingmenu.downloader.threading.PriorityThreadFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class BaseTaskManager<T extends BaseTask> {
    private static final String TAG = "BaseTaskManager";

    /*
     * Number of cores to decide the number of threads
     */
    public static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    public ThreadPoolExecutor mExecutor;

    private final static int WHAT_TASK_CHANGED = 1;

    protected final ArrayList<T> mTaskList = new ArrayList<>();

    public BaseTaskManager() {

    }

    public void init(CallBack callBack) {
        mCallBack = callBack;
        mSimultaneousDownloadsNumber = mCallBack.getSimultaneousDownloads();
        mConnectionsPerTask = mCallBack.getConnectionsPerTask();

        // setting the thread factory
        ThreadFactory backgroundPriorityThreadFactory = new
                PriorityThreadFactory(Process.THREAD_PRIORITY_BACKGROUND);
        if(mExecutor ==null) {

            mExecutor = new ThreadPoolExecutor(
                    mSimultaneousDownloadsNumber,
                    mSimultaneousDownloadsNumber*2,
                    60L,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(),
                    backgroundPriorityThreadFactory
            );
            Log.d(TAG, "initExecutor with corePoolSize = " + mSimultaneousDownloadsNumber);
        }
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
        if(this instanceof PartialTaskManager)
            for (TaskInfo info: infos) {
                FileDownloadTask task = FileDownloadTask.restoreInstance((PartialTaskManager) this,info);
                ((PartialTaskManager)this).mTaskList.add(task);
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
        mCallBack = null;
        mTaskList.clear();
    }

    public void notifyManagerChanged(){
        if(mCallBack!=null)
            mCallBack.onUpdateTaskManager(this);
    }

    public void notifyTaskChanged(BaseTask task) {
        if(mCallBack!=null) {
            mCallBack.onUpdateTask(task);
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

    public void restartAll() {
        for (BaseTask task:mTaskList
             ) {
            if(task.getState()!=BaseTask.RUNNING) task.restartByUser();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
        }
    }

    private T findTaskById(int id) {
        T task = null;
        for (T t:
                mTaskList) {
            if(t.getId()==id) {
                task = t;
            }
        }

        return task;
    }

    public synchronized void clearTask(int id) {
        T task = findTaskById(id);

        if(task!=null&&task.getState()!=BaseTask.RUNNING) {
            if(mCallBack!=null) mCallBack.onClearTask(task.getId());
            mTaskList.remove(task);
            DownloadDBHelper.getInstance().deleteTask(TaskInfo.newInstance(task));
        }
    }

    public static int getRecommendSimultaneousDownloadsNumber() {
        return 3;
    }

    public static int getRecommendConnectionPerTask() {
        return NUMBER_OF_CORES*2;
    }

    private int mSimultaneousDownloadsNumber;

    public synchronized int getSimultaneousDownloadsNumber() {
        return mSimultaneousDownloadsNumber;
    }

    public synchronized int getConnectionsPerTask() {
        return mConnectionsPerTask;
    }

    private int mConnectionsPerTask;

    public synchronized void setSimultaneousDownloadsNumber(int number) {
        mSimultaneousDownloadsNumber = number;
        mExecutor.setCorePoolSize(number);
        mExecutor.setMaximumPoolSize(number*2);
    }

    public synchronized void setConnectionsPerTask(int number) {
        mConnectionsPerTask = number;
    }

    /**
     * Clear All Non-Running Tasks
     */
    public void clearAllTasks() {
        synchronized (mTaskList) {
            for (BaseTask task :
                    mTaskList) {
                if(task.getState()==BaseTask.RUNNING) task.cancelByUser();
            }
        }

        mTaskList.clear();
        DownloadDBHelper.getInstance().deleteAllTasks();
        if(mCallBack!=null) mCallBack.onUpdateTaskManager(this);
    }

    public void clearTasks(List<Integer> ids) {
        for (Integer id :
                ids) {
            clearTask(id);
        }
    }

    public void restartTasks(List<Integer> ids) {
        for (Integer id :
                ids) {
            restartTaskByUser(id);
        }
    }

    public interface CallBack {
        void onClearTask(int id);
        void onUpdateTask(BaseTask task);
        void onUpdateTaskManager(BaseTaskManager baseTaskManager);
        int getConnectionsPerTask();
        int getSimultaneousDownloads();
    }

    private CallBack mCallBack;
}
