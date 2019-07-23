package com.zalo.servicetraining.downloader.service.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.zalo.servicetraining.R;
import com.zalo.servicetraining.downloader.base.BaseTask;
import com.zalo.servicetraining.downloader.base.BaseTaskManager;
import com.zalo.servicetraining.downloader.service.DownloaderService;
import com.zalo.servicetraining.downloader.ui.DownloaderActivity;


public class TaskNotificationManager {

    private static final String NOTIFICATION_CHANNEL_ID = "downloader_service_notification";


    private static final int NOTIFY_MODE_BACKGROUND = 0;
    private static final int NOTIFY_MODE_FOREGROUND = 1;

    private int mNotifyMode = NOTIFY_MODE_BACKGROUND;
    private NotificationManager mNotificationManager;
    private DownloaderService mService;


    public synchronized void init(DownloaderService service) {
        this.mService = service;
        mNotificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
    }

    private BaseTaskManager getDownloadManager() {
        return mService.getDownloadManager();
    }

    private boolean shouldForeground() {
        BaseTaskManager downloadManager = getDownloadManager();
        return downloadManager.isSomeTaskRunning();
    }

    public synchronized void notifyTaskNotificationChanged(BaseTask task) {

        int NOTIFICATION_ID = task.getId();
        int STATE = task.getState();
        float PROGRESS = task.getProgress();

        Intent intent = new Intent(mService, DownloaderActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mService,0,intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mService, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_style_black_24dp)
                .setContentTitle("Notification for task id "+ NOTIFICATION_ID)
                .setContentText("This task state is " + BaseTask.getStateName(STATE)+", with progress is "+PROGRESS)
                .setOngoing(STATE == BaseTask.RUNNING)
                .setContentIntent(pendingIntent);

        postNotification(builder.build(), NOTIFICATION_ID, STATE==BaseTask.RUNNING);
    }

    private synchronized void postNotification(Notification notification, int NOTIFICATION_ID, boolean isOnGoing) {
        int newNotifyMode;
        if (isOnGoing||shouldForeground()) {
            newNotifyMode = NOTIFY_MODE_FOREGROUND;
        } else {
            newNotifyMode = NOTIFY_MODE_BACKGROUND;
        }

        if (mNotifyMode != newNotifyMode && newNotifyMode == NOTIFY_MODE_BACKGROUND) {
            mService.stopForeground(false);
        }

        if (mNotifyMode==NOTIFY_MODE_BACKGROUND && newNotifyMode == NOTIFY_MODE_FOREGROUND) {
            mService.startForeground(NOTIFICATION_ID, notification);
        } else {
            mNotificationManager.notify(NOTIFICATION_ID, notification);
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
            channel.enableVibration(true);
        }

        mNotificationManager.createNotificationChannel(channel);
    }

   /* public void startInForeground() {
        Intent notificationIntent = new Intent(mService, DownloaderService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mService,0,notificationIntent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mService, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_style_black_24dp)
                .setContentTitle("Time Service is running")
                .setTicker("This is ticker")
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();

        if(Build.VERSION.SDK_INT>=O) {
            createNotificationChannel();
        }

        mService.startForeground(NOTIFICATION_ID,notification);
    }
*/
}
