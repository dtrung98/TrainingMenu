package com.zalo.servicetraining.downloader.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.zalo.servicetraining.App;
import com.zalo.servicetraining.downloader.base.BaseTask;
import com.zalo.servicetraining.downloader.base.BaseTaskManager;
import com.zalo.servicetraining.downloader.model.DownloadItem;
import com.zalo.servicetraining.downloader.model.TaskInfo;
import com.zalo.servicetraining.downloader.service.notification.DownNotificationManager;
import com.zalo.servicetraining.downloader.task.simple.SimpleTaskManager;

import java.util.ArrayList;

public class DownloaderService extends Service {
    public static final String TAG = "DownloaderService";

    public static final String PACKAGE_NAME = "com.zalo.servicetraining.downloader.mService";

    private static final int UPDATE_FROM_TASK = 2;

    public static final String ACTION_TASK_CHANGED = "action_task_changed";

    private BaseTaskManager mDownloadManager;
    private DownNotificationManager mNotificationManager;

    public void initManager() {
        if(mDownloadManager==null) {
            mDownloadManager = new SimpleTaskManager();
            mDownloadManager.init(this);
        }
    }

    public void addNewTask(DownloadItem item) {
        if(mDownloadManager==null) initManager();
        mDownloadManager.addNewTask(item);
    }

    public void updateFromTaskManager(BaseTaskManager manager) {

    }

    public void updateFromTask(BaseTask task) {
       mNotificationManager.notifyTaskNotificationChanged(task);
       Intent intent = new Intent();
       intent.setAction(ACTION_TASK_CHANGED);
        intent.putExtra(BaseTask.EXTRA_TASK_ID,task.getId());
        intent.putExtra(BaseTask.EXTRA_STATE,task.getState());
        intent.putExtra(BaseTask.EXTRA_PROGRESS,task.getProgress());
        intent.putExtra(BaseTask.EXTRA_PROGRESS_SUPPORT, task.isProgressSupport());
        intent.putExtra(BaseTask.EXTRA_DOWNLOADED_IN_BYTES,task.getDownloadedInBytes());
        intent.putExtra(BaseTask.EXTRA_FILE_CONTENT_LENGTH,task.getFileContentLength());
        intent.putExtra(BaseTask.EXTRA_SPEED,task.getSpeedInBytes());
        LocalBroadcastManager.getInstance(App.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    public BaseTaskManager getDownloadManager() {
        return mDownloadManager;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        initManager();
        initNotification();
    }

    private void initNotification() {
        if(mNotificationManager ==null) {
            mNotificationManager = new DownNotificationManager();
            mNotificationManager.init(this);
        }
    }

    public void stopForegroundThenStopSelf() {
        stopForeground(true);
        stopSelf();

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mNotificationManager.cancelAll();
        mDownloadManager.destroy();
        mNotificationManager = null;
        mDownloadManager = null;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent==null) Log.d(TAG, "onStartCommand: null intent");
        else if(intent.getAction()==null)
        Log.d(TAG, "onStartCommand : receive intent with no action");
        else Log.d(TAG, "onStartCommand: receive intent with action :["+intent.getAction()+"]");
        return super.onStartCommand(intent, flags, startId);
    }
    public void stopIfModeBackground() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O &&mNotificationManager!=null && mNotificationManager.getNotifyMode()==DownNotificationManager.NOTIFY_MODE_BACKGROUND)
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

    public class Binder extends android.os.Binder {
        @NonNull
        public DownloaderService getService() {
            return DownloaderService.this;
        }
    }

}
