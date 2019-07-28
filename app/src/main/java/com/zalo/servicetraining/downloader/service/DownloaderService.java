package com.zalo.servicetraining.downloader.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.zalo.servicetraining.App;
import com.zalo.servicetraining.downloader.base.AbsTask;
import com.zalo.servicetraining.downloader.base.AbsTaskManager;
import com.zalo.servicetraining.downloader.model.DownloadItem;
import com.zalo.servicetraining.downloader.model.TaskInfo;
import com.zalo.servicetraining.downloader.service.notification.DownNotificationManager;
import com.zalo.servicetraining.downloader.service.taskmanager.SimpleTaskManager;

import java.lang.ref.WeakReference;
import java.util.List;

public class DownloaderService extends Service {
    public static final String TAG = "DownloaderService";

    public static final String PACKAGE_NAME = "com.zalo.servicetraining.downloader.mService";

    private static final int UPDATE_FROM_TASK = 2;

    public static final String ACTION_TASK_CHANGED = "action_task_changed";

    private AbsTaskManager mDownloadManager;
    private DownNotificationManager mDownNotificationManager;

    public void initManager() {
        mDownloadManager = new SimpleTaskManager();
        mDownloadManager.init(this);
    }

    public void addNewTask(DownloadItem item) {
        if(mDownloadManager==null) initManager();
        mDownloadManager.addNewTask(item);
    }

    public void updateFromTaskManager(AbsTaskManager manager) {

    }

    public void updateFromTask(AbsTask task) {
       mDownNotificationManager.notifyTaskNotificationChanged(task);
       Intent intent = new Intent();
       intent.setAction(ACTION_TASK_CHANGED);
        intent.putExtra(AbsTask.EXTRA_NOTIFICATION_ID,task.getId());
        intent.putExtra(AbsTask.EXTRA_STATE,task.getState());
        intent.putExtra(AbsTask.EXTRA_PROGRESS,task.getProgress());
        intent.putExtra(AbsTask.EXTRA_PROGRESS_SUPPORT, task.isProgressSupport());

        LocalBroadcastManager.getInstance(App.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    public void updateFromTaskRunInUIThread(AbsTask task) {
        Message message = new Message();
        message.what = UPDATE_FROM_TASK;
        Bundle bundle = new Bundle();
        bundle.putInt(AbsTask.EXTRA_NOTIFICATION_ID,task.getId());
        bundle.putInt(AbsTask.EXTRA_STATE,task.getState());
        bundle.putFloat(AbsTask.EXTRA_PROGRESS,task.getProgress());
        bundle.putBoolean(AbsTask.EXTRA_PROGRESS_SUPPORT, task.isProgressSupport());
        message.setData(bundle);
        mServiceHandler.sendMessage(message);
    }

    public AbsTaskManager getDownloadManager() {
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
        mServiceHandler = new ServiceHandler(this);
        initManager();
        initNotification();
    }

    private void initNotification() {
        mDownNotificationManager = new DownNotificationManager();
        mDownNotificationManager.init(this);
    }



    public void stopForegroundThenStopSelf() {
        stopForeground(true);
        stopSelf();

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mDownNotificationManager.cancelAll();
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

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    private ServiceHandler mServiceHandler;

    public List<TaskInfo> getSessionTaskList() {
        return mDownloadManager.getSessionTaskList();
    }

    public TaskInfo getTaskInfoWithId(int id) {
       return mDownloadManager.getTaskInfo(id);
    }

    private static class ServiceHandler extends Handler {
        private final WeakReference<DownloaderService> mRefService;

        public ServiceHandler(@NonNull Looper looper, DownloaderService service) {
            super(looper);
            mRefService = new WeakReference<>(service);
        }

        ServiceHandler(DownloaderService service) {
            mRefService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            DownloaderService service = mRefService.get();
            if(service!=null)
            switch (msg.what) {
                case UPDATE_FROM_TASK :
                    Bundle bundle = msg.getData();
                    final int id = bundle.getInt(AbsTask.EXTRA_NOTIFICATION_ID,-1);
                    final int state = bundle.getInt(AbsTask.EXTRA_STATE,-1);
                    final float progress = bundle.getFloat(AbsTask.EXTRA_PROGRESS,-1);
                    final boolean progress_support = bundle.getBoolean(AbsTask.EXTRA_PROGRESS_SUPPORT, false);
                    service.mDownNotificationManager.notifyTaskNotificationChanged(id,state,progress, progress_support);
                    break;

            }
        }
    }


    public IBinder mBinder = new Binder();

    public class Binder extends android.os.Binder {
        @NonNull
        public DownloaderService getService() {
            return DownloaderService.this;
        }
    }

}
