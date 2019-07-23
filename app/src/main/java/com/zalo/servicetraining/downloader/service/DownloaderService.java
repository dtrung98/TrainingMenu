package com.zalo.servicetraining.downloader.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.zalo.servicetraining.downloader.base.BaseTask;
import com.zalo.servicetraining.downloader.base.BaseTaskManager;
import com.zalo.servicetraining.downloader.model.DownloadItem;
import com.zalo.servicetraining.downloader.service.notification.TaskNotificationManager;
import com.zalo.servicetraining.downloader.service.taskmanager.SimpleTaskManager;

import java.lang.ref.WeakReference;

public class DownloaderService extends Service {
    public static final String TAG = "DownloaderService";

    public static final String PACKAGE_NAME = "com.zalo.servicetraining.downloader.mService";

    static final int A_SECOND_LOOP = 1;
    static final int STOP_LOOP = 2;

    private BaseTaskManager mDownloadManager;
    private TaskNotificationManager mTaskNotificationManager;

    public void initManager() {
        mDownloadManager = new SimpleTaskManager();
        mDownloadManager.init(this);
    }

    public void addNewTask(DownloadItem item) {
        if(mDownloadManager==null) initManager();
        mDownloadManager.addNewTask(item);
    }

    public void updateFromTaskManager(BaseTaskManager manager) {

    }

    public void updateFromTask(BaseTask task) {
        mTaskNotificationManager.notifyTaskNotificationChanged(task);
    }

    public BaseTaskManager getDownloadManager() {
        return mDownloadManager;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mTimeTrackBinder;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        initManager();
        initNotification();
    }

    private void initNotification() {
        mTaskNotificationManager = new TaskNotificationManager();
        mTaskNotificationManager.init(this);
    }



    public void stopForegroundThenStopSelf() {
        stopForeground(true);
        stopSelf();

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction()==null)
        Log.d(TAG, "onStartCommand : receive intent with no action");
        else Log.d(TAG, "onStartCommand: receive intent with action :["+intent.getAction()+"]");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }
    public IBinder mTimeTrackBinder = new Binder();

    public class Binder extends android.os.Binder {
        @NonNull
        public DownloaderService getService() {
            return DownloaderService.this;
        }
    }

}
