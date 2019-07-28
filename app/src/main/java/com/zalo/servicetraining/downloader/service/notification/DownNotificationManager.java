package com.zalo.servicetraining.downloader.service.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.zalo.servicetraining.App;
import com.zalo.servicetraining.R;
import com.zalo.servicetraining.downloader.base.AbsTask;
import com.zalo.servicetraining.downloader.base.AbsTaskManager;
import com.zalo.servicetraining.downloader.service.DownloaderService;
import com.zalo.servicetraining.downloader.ui.DownloaderActivity;



public class DownNotificationManager {
    private static final String TAG = "DownNotificationManager";
    private static final String NOTIFICATION_CHANNEL_ID = "downloader_service_notification";

    private static final int NOTIFY_MODE_BACKGROUND = 0;
    private static final int NOTIFY_MODE_FOREGROUND = 1;

    private int mNotifyMode = NOTIFY_MODE_BACKGROUND;
    private NotificationManager mNotificationManager;
    private DownloaderService mService;
    private SparseArray<NotificationCompat.Builder> mIndexBuilders = new SparseArray<>();

    public synchronized void init(DownloaderService service) {
        this.mService = service;
        mNotificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
     //   notifyTaskNotificationChanged(1,AbsTask.RUNNING,0.5f,false);
    }

    private AbsTaskManager getDownloadManager() {
        return mService.getDownloadManager();
    }

    private boolean shouldForeground() {
        AbsTaskManager downloadManager = getDownloadManager();
        return downloadManager.isSomeTaskRunning();
    }
    public synchronized void notifyTaskNotificationChanged(final int NOTIFICATION_ID, final int STATE, final float PROGRESS, final boolean PROGRESS_SUPPORT) {
        Log.d(TAG, "thread "+Thread.currentThread().getId()+", start updating id "+NOTIFICATION_ID+", state "+ AbsTask.getStateName(STATE)+", progress "+ PROGRESS);

        NotificationCompat.Builder builder = mIndexBuilders.get(NOTIFICATION_ID);
        if(builder==null) {
            Log.d(TAG, "thread "+Thread.currentThread().getId()+", null builder for id "+ NOTIFICATION_ID);
            builder = new NotificationCompat.Builder(mService, NOTIFICATION_CHANNEL_ID);
            Intent intent = new Intent(App.getInstance().getApplicationContext(), DownloaderActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(App.getInstance().getApplicationContext(),0,intent, 0);
            builder.setContentIntent(pendingIntent);
            builder.setSmallIcon(R.drawable.downloading)
                   .setContentTitle("Task ID "+ NOTIFICATION_ID)
                   .setOnlyAlertOnce(true)
            .setAutoCancel(true);
            mIndexBuilders.put(NOTIFICATION_ID,builder);

        }

        builder.setContentText("State is " + AbsTask.getStateName(STATE)+", progress is "+PROGRESS)
                .setOngoing(STATE == AbsTask.RUNNING);

        if(STATE!= AbsTask.SUCCESS) {
            if(PROGRESS_SUPPORT)
            builder.setProgress(100, (int) (PROGRESS * 100), false);
            else if(STATE==AbsTask.RUNNING)
                builder.setProgress(100,0,true);
            Log.d(TAG, "thread "+Thread.currentThread().getId()+", set progress: 100, "+((int)PROGRESS*100)+", false");
        }
        else {
            builder.setOnlyAlertOnce(false);
            builder.setProgress(0,0,false);
            Log.d(TAG, "thread "+Thread.currentThread().getId()+", set progress: 0, 0, false");
        }

        postNotification(builder.build(), NOTIFICATION_ID, STATE== AbsTask.RUNNING);
        if(STATE!= AbsTask.RUNNING) {
            mIndexBuilders.delete(NOTIFICATION_ID);
            Log.d(TAG, "thread "+Thread.currentThread().getId()+", delete key id "+ NOTIFICATION_ID+" from IndexBuilders");
        }
    }

    public void notifyTaskNotificationChanged(AbsTask task) {

        int NOTIFICATION_ID = task.getId();
        int STATE = task.getState();
        float PROGRESS = task.getProgress();
        notifyTaskNotificationChanged(NOTIFICATION_ID,STATE, PROGRESS, task.isProgressSupport());
    }
    private int mFlagForegroundID = -1;

    private void postNotification(Notification notification, int NOTIFICATION_ID, boolean isOnGoing) {
        if(mStopped) return;
        int newNotifyMode;
        if (isOnGoing||shouldForeground()) {
            newNotifyMode = NOTIFY_MODE_FOREGROUND;
        } else {
            newNotifyMode = NOTIFY_MODE_BACKGROUND;
        }
       // if(!isOnGoing) mNotificationManager.cancel(NOTIFICATION_ID);

        if (mNotifyMode != newNotifyMode && newNotifyMode == NOTIFY_MODE_BACKGROUND) {
            mService.stopForeground(false);
            Log.d(TAG, "thread "+Thread.currentThread().getId()+", stop foreground id "+NOTIFICATION_ID+", isOnGoing "+ isOnGoing);
        }

         if ((mNotifyMode ==NOTIFY_MODE_BACKGROUND && newNotifyMode == NOTIFY_MODE_FOREGROUND)
         ||(newNotifyMode==NOTIFY_MODE_FOREGROUND&&mIndexBuilders.get(mFlagForegroundID)==null)) {
            mService.startForeground(NOTIFICATION_ID, notification);
            mFlagForegroundID = NOTIFICATION_ID;
            Log.d(TAG, "thread "+Thread.currentThread().getId()+", start foreground id "+NOTIFICATION_ID+", isOnGoing "+ isOnGoing);
        } else {
            mNotificationManager.notify(NOTIFICATION_ID, notification);
            Log.d(TAG, "thread "+Thread.currentThread().getId()+", notify id "+NOTIFICATION_ID+", isOnGoing "+ isOnGoing);

        }

        mNotifyMode = newNotifyMode;

    }

    @RequiresApi(26)
    private void createNotificationChannel() {
        NotificationChannel channel;

        channel = mNotificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID);

        if(channel==null) {
            // create new chanel
            channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,mService.getString(R.string.downloader_notification_chanel_name),NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(mService.getString(R.string.downloader_notification_chanel_description));
            channel.enableLights(true);
          //  channel.enableVibration(true);
        }

        mNotificationManager.createNotificationChannel(channel);
    }
    private boolean mStopped = false;

    public void cancelAll() {
        mStopped = true;
        mNotificationManager.cancelAll();
    }
}
