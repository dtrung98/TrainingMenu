package com.zalo.trainingmenu.downloader.service;

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

import com.zalo.trainingmenu.App;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.downloader.base.BaseTask;
import com.zalo.trainingmenu.downloader.base.BaseTaskController;
import com.zalo.trainingmenu.downloader.ui.main.DownloadActivity;
import com.zalo.trainingmenu.util.Util;

import java.util.List;


public class NotificationController {
    private static final String DOWNLOADING_GROUP = TaskService.PACKAGE_NAME +".DOWNLOADING";
    private static final String DOWNLOADED_GROUP = TaskService.PACKAGE_NAME +".DOWNLOADED";
    private static final String TAG = "NotificationManager";
    private static final String NOTIFICATION_CHANNEL_ID = "downloader_service_notification";

    public static final int NOTIFY_MODE_BACKGROUND = 0;
    public static final int NOTIFY_MODE_FOREGROUND = 1;

    public int getNotifyMode() {
        return mNotifyMode;
    }

    public void setNotifyMode(int notifyMode) {
        mNotifyMode = notifyMode;
    }

    private int mNotifyMode = NOTIFY_MODE_BACKGROUND;
    private NotificationManager mNotificationManager;
    private TaskService mService;
    private SparseArray<NotificationCompat.Builder> mIndexBuilders = new SparseArray<>();

    public synchronized void init(TaskService service) {
        this.mService = service;
        mNotificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
     //   notifyTaskNotificationChanged(1,BaseTask.RUNNING,0.5f,false);
    }

    private BaseTaskController getDownloadManager() {
        return mService.getDownloadManager();
    }

    private boolean shouldForeground() {
        BaseTaskController downloadManager = getDownloadManager();
        return downloadManager.isSomeTaskRunning();
    }
    public synchronized void notifyTaskNotificationChanged(BaseTask task, final int NOTIFICATION_ID, final int STATE, final int INT_PROGRESS, final boolean PROGRESS_SUPPORT) {
        Log.d(TAG, "thread "+Thread.currentThread().getId()+", start updating id "+NOTIFICATION_ID+", state "+ BaseTask.getStateName(null,STATE)+", progress "+ INT_PROGRESS);

        NotificationCompat.Builder builder = mIndexBuilders.get(NOTIFICATION_ID);
        if(builder==null) {
            Log.d(TAG, "thread " + Thread.currentThread().getId() + ", null builder for id " + NOTIFICATION_ID);
            builder = new NotificationCompat.Builder(mService, NOTIFICATION_CHANNEL_ID);
            Intent intent = new Intent(App.getInstance().getApplicationContext(), DownloadActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(App.getInstance().getApplicationContext(), 0, intent, 0);
            builder.setContentIntent(pendingIntent);
            builder.setSmallIcon(R.drawable.downloading)
                    .setContentTitle(task.getFileTitle());
        }

            // bind content text
            String MIDDLE_DOT = " • ";

            switch (STATE) {
                case BaseTask.RUNNING:
                    String speed;
                        speed =Util.humanReadableByteCount((long) task.getSpeedInBytes())+"/s";
                        if(PROGRESS_SUPPORT)
                            builder.setContentText(INT_PROGRESS+"%"+MIDDLE_DOT+speed);
                        else
                            builder.setContentText("Downloading" + MIDDLE_DOT+speed);

                    builder.setColor(mService.getResources().getColor(R.color.FlatGreen));
                  /*  Intent intent = new Intent(TaskService.ACTION_PAUSE);
                    PendingIntent pendingIntent = PendingIntent.getService(mService,0,intent,0);
                    NotificationCompat.Action action = new NotificationCompat.Action(R.drawable.ic_pause_black_24dp,mService.getResources().getString(R.string.pause),pendingIntent);
                    builder.mActions.clear();
                    builder.addAction(action);*/
                    break;
                 case BaseTask.SUCCESS:
                     builder.setContentText(mService.getResources().getString(R.string.download_completed)+MIDDLE_DOT+Util.humanReadableByteCount(task.getDownloadedInBytes()));
                     builder.setColor(mService.getResources().getColor(R.color.FlatGreen));
                     break;
                case BaseTask.PAUSED:
                    if(PROGRESS_SUPPORT) {
                        builder.setContentText(INT_PROGRESS +"%" +MIDDLE_DOT+"Paused");
                    } else {
                        builder.setContentText("Paused");
                    }

                    builder.setColor(mService.getResources().getColor(R.color.FlatOrange));
                    break;
                case BaseTask.FAILURE_TERMINATED:
                    builder.setContentText(mService.getResources().getString(R.string.failed_notification_message)+task.getMessage()+", tap for more info");
                    builder.setColor(mService.getResources().getColor(R.color.FlatRed));

                    break;
                 default:
                     builder.setContentText(BaseTask.getStateName(mService,STATE));
                     builder.setColor(mService.getResources().getColor(R.color.FlatTealBlue));
            }

        if(PROGRESS_SUPPORT)
            builder.setSubText( Util.humanReadableByteCount(task.getDownloadedInBytes())+" of "+ Util.humanReadableByteCount(task.getFileContentLength()));
        else builder.setSubText(Util.humanReadableByteCount(task.getDownloadedInBytes()));

            builder.setOnlyAlertOnce(true).setAutoCancel(true).setOngoing(STATE == BaseTask.RUNNING);
            mIndexBuilders.put(NOTIFICATION_ID,builder);

        if(STATE!= BaseTask.SUCCESS) {
            if(PROGRESS_SUPPORT)
            builder.setProgress(100, INT_PROGRESS, false);
            else if(STATE == BaseTask.RUNNING)
                builder.setProgress(100,0,true);

            Log.d(TAG, "thread "+Thread.currentThread().getId()+", set progress: 100, "+INT_PROGRESS+", false");
         /*   Intent intent = new Intent(App.getInstance().getApplicationContext(), DownloadActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(App.getInstance().getApplicationContext(), 0, intent, 0);

            NotificationCompat.Builder groupBuilder =
                    new NotificationCompat.Builder(mService, NOTIFICATION_CHANNEL_ID)
                            .setContentTitle("Tasks are downloading")
                            .setSmallIcon(R.drawable.background_folder_icon)
                            .setContentText("Downloading...")
                            .setGroupSummary(true)
                            .setOnlyAlertOnce(false)
                            .setGroup(DOWNLOADING_GROUP)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("Downloading..."))
                            .setContentIntent(pendingIntent);
            mNotificationManager.notify(1,groupBuilder.build());*/
        }
        else {
            builder.setProgress(0,0,false);
            Log.d(TAG, "thread "+Thread.currentThread().getId()+", set progress: 0, 0, false");
      /*      Intent intent = new Intent(App.getInstance().getApplicationContext(), DownloadActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(App.getInstance().getApplicationContext(), 0, intent, 0);

            NotificationCompat.Builder groupBuilder =
                    new NotificationCompat.Builder(mService, NOTIFICATION_CHANNEL_ID)
                            .setContentTitle("Downloaded")
                            .setSmallIcon(R.drawable.tick)
                            .setContentText("Some tasks downloaded")
                            .setGroupSummary(true)
                            .setOnlyAlertOnce(false)
                            .setGroup(DOWNLOADED_GROUP)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("Some tasks downloaded"))
                            .setContentIntent(pendingIntent);
            mNotificationManager.notify(1,groupBuilder.build());
            builder.setGroup(DOWNLOADED_GROUP);*/
        }

        postNotificationAndroidO(builder.build(), NOTIFICATION_ID, STATE== BaseTask.RUNNING, STATE == BaseTask.SUCCESS);
        if(STATE!= BaseTask.RUNNING) {
            mIndexBuilders.delete(NOTIFICATION_ID);
            Log.d(TAG, "thread "+Thread.currentThread().getId()+", delete key id "+ NOTIFICATION_ID+" with IndexBuilders");
        }
    }

    public void notifyTaskNotificationChanged(BaseTask task) {

        int NOTIFICATION_ID = task.getId();
        int STATE = task.getState();
        int PROGRESS = task.getProgressInteger();
        notifyTaskNotificationChanged(task, NOTIFICATION_ID,STATE, PROGRESS, task.isProgressSupport());
    }

    private void postNotificationAndroidO(Notification notification, int NOTIFICATION_ID, boolean isOnGoing, boolean isSuccess) {
        if(mStopped) return;
        int newNotifyMode;
        if (isOnGoing||shouldForeground()) {
            newNotifyMode = NOTIFY_MODE_FOREGROUND;
        } else {
            newNotifyMode = NOTIFY_MODE_BACKGROUND;
        }

        if (mNotifyMode != newNotifyMode && newNotifyMode == NOTIFY_MODE_BACKGROUND) {
            mService.stopForeground(false);
            Log.d(TAG, "thread "+Thread.currentThread().getId()+", stop foreground id "+NOTIFICATION_ID+", isOnGoing false");
        }

         if ((mNotifyMode ==NOTIFY_MODE_BACKGROUND && newNotifyMode == NOTIFY_MODE_FOREGROUND)
         //||(newNotifyMode==NOTIFY_MODE_FOREGROUND&&mIndexBuilders.get(mFlagForegroundID)==null)
         ) {
            mService.startForeground(NOTIFICATION_ID, notification);
             int flagForegroundID = NOTIFICATION_ID;
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

    public void notifyTaskClear(int id) {
        mNotificationManager.cancel(id);
        mIndexBuilders.delete(id);
    }

    public void cancel(List<Integer> selectedTasks) {
        for (Integer id:
             selectedTasks) {
            mNotificationManager.cancel(id);
        }
    }
}
