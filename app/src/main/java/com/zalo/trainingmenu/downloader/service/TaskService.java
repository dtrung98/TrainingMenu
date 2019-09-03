package com.zalo.trainingmenu.downloader.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.zalo.trainingmenu.App;
import com.zalo.trainingmenu.downloader.base.BaseTask;
import com.zalo.trainingmenu.downloader.base.BaseTaskController;
import com.zalo.trainingmenu.downloader.model.DownloadItem;
import com.zalo.trainingmenu.downloader.model.TaskInfo;
import com.zalo.trainingmenu.downloader.task.partial.PartialTaskController;
import com.zalo.trainingmenu.downloader.ui.setting.SettingFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TaskService extends Service implements BaseTaskController.CallBack, SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String TAG = "TaskService";
    public static final String PACKAGE_NAME = "com.zalo.trainingmenu.downloader";
    public static final String ACTION_CONTROL_PAUSE = PACKAGE_NAME +".pause";
    public static final String ACTION_CONTROL_RESTART = PACKAGE_NAME +".restart";
    public static final String ACTION_CONTROL_CANCEL = PACKAGE_NAME +".cancel";
    public static final String ACTION_CONTROL_OPEN = PACKAGE_NAME +".open";
    public static final String ACTION_CONTROL_CLEAR = PACKAGE_NAME +".clear";
    public static final String ACTION_CONTROL_CLEAR_NOTIFICATION = PACKAGE_NAME +".clear_notification";
    public static final String ACTION_CONTROL_RESUME = PACKAGE_NAME + ".resume";
    public static final String ACTION_CONTROL_TRY_TO_RESUME = PACKAGE_NAME +".try_to_resume";
    public static final String ACTION_CONTROL_DUPLICATE = PACKAGE_NAME +".duplicate";
    public static final int ACTION_CONTROLS_SIZE = 8;

    public static int DOWNLOAD_MODE_SIMPLE = 0;
    public static int DOWNLOAD_MODE_PARTIAL = 1;

    public static final String ACTION_TASK_CHANGED = "action_task_changed";
    public static final String ACTION_TASK_CLEAR = "action_task_clear";
    public static final String ACTION_TASK_MANAGER_CHANGED = "action_task_manager_changed";

    private BaseTaskController mDownloadManager;
    private NotificationController mNotificationController;

    private ActionForServiceReceiver mReceiver;
    private boolean mReceiverRegistered = false;

    public void initManager() {
        if(mDownloadManager==null) {
            mDownloadManager = new PartialTaskController();
            mDownloadManager.init(this);
            mDownloadManager.restoreInstance();
        }
    }

    public void addNewTask(DownloadItem item) {
        if(mDownloadManager==null) initManager();
        mDownloadManager.addNewTask(item);
    }

    @Override
    public void onUpdateTaskManager(BaseTaskController manager) {
        Intent intent = new Intent();
        intent.setAction(ACTION_TASK_MANAGER_CHANGED);
        Log.d(TAG, "service sends action task manager changed");
        LocalBroadcastManager.getInstance(App.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public int getConnectionsPerTask() {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(App.getInstance().getApplicationContext()).getString(SettingFragment.EXTRA_CONNECTIONS_PER_TASK,String.valueOf(BaseTaskController.getRecommendConnectionPerTask())));
    }

    @Override
    public int getSimultaneousDownloads() {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(App.getInstance().getApplicationContext()).getString(SettingFragment.EXTRA_SIMULTANEOUS_DOWNLOADS,String.valueOf(BaseTaskController.getRecommendSimultaneousDownloadsNumber())));
    }

    @Override
    public void onUpdateTask(BaseTask task) {
        long start = System.currentTimeMillis();
       mNotificationController.notifyTaskNotificationChanged(task);
       Intent intent = new Intent();
       intent.setAction(ACTION_TASK_CHANGED);
       intent.putExtra(BaseTask.EXTRA_TASK_INFO,TaskInfo.newInstance(task));
        Log.d(TAG, "thread "+Thread.currentThread().getId()+" service sends action task id"+task.getId()+" changed, costs "+(System.currentTimeMillis() - start));
     /*   intent.putExtra(BaseTask.EXTRA_TASK_ID,task.getId());
        intent.putExtra(BaseTask.EXTRA_STATE,task.getState());
        intent.putExtra(BaseTask.EXTRA_PROGRESS,task.getProgress());
        intent.putExtra(BaseTask.EXTRA_PROGRESS_SUPPORT, task.isProgressSupport());
        intent.putExtra(BaseTask.EXTRA_DOWNLOADED_IN_BYTES,task.getDownloadedInBytes());
        intent.putExtra(BaseTask.EXTRA_FILE_CONTENT_LENGTH,task.getFileContentLength());
        intent.putExtra(BaseTask.EXTRA_SPEED,task.getSpeedInBytes());*/
        LocalBroadcastManager.getInstance(App.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onClearTask(int id) {
        mNotificationController.notifyTaskClear(id);
        Intent intent = new Intent();
        intent.setAction(ACTION_TASK_CLEAR);
        intent.putExtra(BaseTask.EXTRA_TASK_ID, id);
        Log.d(TAG, "service sends action task id"+id+" deleted");
        LocalBroadcastManager.getInstance(App.getInstance().getApplicationContext())
                .sendBroadcast(intent);
    }

    public BaseTaskController getDownloadManager() {
        return mDownloadManager;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
    }
    private PowerManager.WakeLock mWakeLock;

    @SuppressLint("WakelockTimeout")
    public void acquire() {
        if(mWakeLock!=null) mWakeLock.acquire();
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        if(powerManager!=null)
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());

        if(mWakeLock!=null) mWakeLock.setReferenceCounted(false);

        PreferenceManager.getDefaultSharedPreferences(App.getInstance().getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
        initReceiver();
        initManager();
        initNotification();
    }

    public void releaseWakeLock() {
        if (mWakeLock!=null&&mWakeLock.isHeld()) {
            try {
                mWakeLock.release();
            } catch (Exception ignored) {}
        }
    }


    private void initNotification() {
        if(mNotificationController ==null) {
            mNotificationController = new NotificationController();
            mNotificationController.init(this);
        }
    }

    public void stopForegroundThenStopSelf() {
        stopForeground(true);
        stopSelf();

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mNotificationController.cancelAll();
        mDownloadManager.destroy();
        releaseReceiver();
        mNotificationController = null;
        mDownloadManager = null;
        PreferenceManager.getDefaultSharedPreferences(App.getInstance().getApplicationContext()).unregisterOnSharedPreferenceChangeListener(this);
        releaseWakeLock();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        controlService(intent);
        return START_NOT_STICKY;
    }

    private void controlService(Intent intent) {
        if(intent!=null) {
            String action = intent.getAction();
            final int id = intent.getIntExtra(BaseTask.EXTRA_TASK_ID,-1);
            Log.d(TAG, "controlService: "+action+", id = "+id);
            if(action!=null && id !=-1) {
                switch (action) {
                    case ACTION_CONTROL_PAUSE:
                        pauseTaskWithTaskId(id);
                        break;
                    case ACTION_CONTROL_CANCEL:
                        cancelTaskWithTaskId(id);
                        break;
                    case ACTION_CONTROL_OPEN:
                        TaskInfo info = getTaskInfoWithId(id);
                        if(info!=null)
                        RemoteForTaskService.openFinishedTaskInfo(this,info);
                        break;
                    case ACTION_CONTROL_CLEAR:
                        Log.d(TAG, "clear action");
                        clearTask(id);
                        break;
                    case ACTION_CONTROL_CLEAR_NOTIFICATION:
                        if(mNotificationController!=null) mNotificationController.notifyTaskClear(id);
                        break;
                    case ACTION_CONTROL_RESUME:
                        resumeTaskWithTaskId(id);
                        break;
                    case ACTION_CONTROL_RESTART:
                        restartTaskWithTaskId(id);
                        break;
                    case ACTION_CONTROL_DUPLICATE:
                        break;
                }
            }
        }
    }

    public void stopIfModeBackground() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O && mNotificationController !=null && mNotificationController.getNotifyMode()== NotificationController.NOTIFY_MODE_BACKGROUND)
            stopSelf();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");

        return super.onUnbind(intent);
    }


    public ArrayList<TaskInfo> getSessionTaskList() {
        return mDownloadManager.getSessionTaskList().getList();
    }

    public TaskInfo getTaskInfoWithId(int id) {
       return mDownloadManager.getTaskInfo(id);
    }

    public void pauseTaskWithTaskId(int id) {
        mDownloadManager.pauseTaskFromUser(id);
    }

    public void cancelTaskWithTaskId(int id) {
        mDownloadManager.cancelTaskFromUser(id);
    }

    public void resumeTaskWithTaskId(int id) {
        mDownloadManager.resumeTaskByUser(id);
    }

    public void restartTaskWithTaskId(int id) {
        mDownloadManager.restartTaskByUser(id) ;
    }

    public IBinder mBinder = new Binder();

    public void clearTask(int id) {
        if(mDownloadManager!=null) mDownloadManager.clearTask(id);
        else Log.d(TAG, "download manager is null");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case SettingFragment.EXTRA_SIMULTANEOUS_DOWNLOADS:
                if(mDownloadManager!=null) {
                    int number = Integer.parseInt(sharedPreferences.getString(key,String.valueOf(BaseTaskController.getRecommendSimultaneousDownloadsNumber())));
                    mDownloadManager.setSimultaneousDownloadsNumber(number);
                }
                break;
            case SettingFragment.EXTRA_CONNECTIONS_PER_TASK:
                if(mDownloadManager!=null) {
                    int number = Integer.parseInt(sharedPreferences.getString(key,String.valueOf(BaseTaskController.getRecommendConnectionPerTask())));
                    mDownloadManager.setConnectionsPerTask(number);
                }
                break;
        }
    }

    public void clearAllTasks() {
        if(mDownloadManager!=null) mDownloadManager.clearAllTasks();
        if(mNotificationController !=null) mNotificationController.cancelAll();
    }

    public void restartAll() {
        if(mDownloadManager!=null) mDownloadManager.restartAll();
    }

    public void clearTasks(List<Integer> ids) {
        if(mDownloadManager!=null) mDownloadManager.clearTasks(ids);
        if(mNotificationController !=null) mNotificationController.cancel(ids);
    }

    public void restartTasks(List<Integer> ids) {
        if(mDownloadManager!=null) mDownloadManager.restartTasks(ids);
    }

    public void tryToResume(int id) {
        if(mDownloadManager!=null) mDownloadManager.tryToResume(id);
    }

    private void initReceiver() {
        if (!mReceiverRegistered) {
            mReceiver = new ActionForServiceReceiver(this);

            final IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_CONTROL_PAUSE);
            filter.addAction(ACTION_CONTROL_OPEN);
            filter.addAction(ACTION_CONTROL_RESTART);
            filter.addAction(ACTION_CONTROL_CANCEL);
            filter.addAction(ACTION_CONTROL_CLEAR);
            filter.addAction(ACTION_CONTROL_DUPLICATE);

            try {
                LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
            } catch (Exception ignored) {
            }

            mReceiverRegistered = true;
        }
    }

    private void releaseReceiver() {
        if (mReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
            } catch (Exception ignored) {
            }
            mReceiver = null;
            mReceiverRegistered = false;
        }
    }

    public static final class ActionForServiceReceiver extends BroadcastReceiver {
        private static final String TAG = "ServiceReceiver";
        private final WeakReference<TaskService> mWeakReference;

        public ActionForServiceReceiver(TaskService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: "+action);
            TaskService service = mWeakReference.get();

            if(service!=null && action != null && !action.isEmpty() ) {
                switch (action) {
                    case ACTION_CONTROL_PAUSE:
                        break;
                    case ACTION_CONTROL_CLEAR:
                        break;
                    case ACTION_CONTROL_CANCEL:
                        break;
                    case ACTION_CONTROL_OPEN:
                        break;
                    case ACTION_CONTROL_RESTART:
                        break;
                    case ACTION_CONTROL_DUPLICATE:
                        break;
                }
            }
        }
    }

    public class Binder extends android.os.Binder {
        @NonNull
        public TaskService getService() {
            return TaskService.this;
        }
    }

}
