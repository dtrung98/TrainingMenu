package com.zalo.trainingmenu.downloader.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
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

    private static final String MIDDLE_DOT = " • ";


    public static final int NOTIFY_MODE_BACKGROUND = 0;
    public static final int NOTIFY_MODE_FOREGROUND = 1;

    private NotificationCompat.Action getAction(final String actionStr, final int id) {
        switch (actionStr) {
            case TaskService.ACTION_CONTROL_PAUSE:
                return new NotificationCompat.Action(R.drawable.ic_pause_black_24dp,mService.getResources().getString(R.string.pause),retrieveAction(actionStr,1,id));

            case TaskService.ACTION_CONTROL_RESTART:
                return new  NotificationCompat.Action(R.drawable.ic_refresh_black_24dp,mService.getResources().getString(R.string.restart),retrieveAction(actionStr,2,id));

            case TaskService.ACTION_CONTROL_CANCEL:
                return new NotificationCompat.Action(R.drawable.ic_delete_black_24dp,mService.getResources().getString(R.string.cancel),retrieveAction(actionStr,3,id));

            case TaskService.ACTION_CONTROL_OPEN:
                return new NotificationCompat.Action(R.drawable.ic_play_arrow_black_24dp,mService.getResources().getString(R.string.open),retrieveAction(actionStr,4,id));

            case TaskService.ACTION_CONTROL_RESUME:
                return new NotificationCompat.Action(R.drawable.ic_play_arrow_black_24dp,mService.getResources().getString(R.string.resume),retrieveAction(actionStr,5,id));

            case TaskService.ACTION_CONTROL_TRY_TO_RESUME:
                return new NotificationCompat.Action(R.drawable.ic_play_arrow_black_24dp,mService.getResources().getString(R.string.try_to_resume),retrieveAction(actionStr,6,id));

            case TaskService.ACTION_CONTROL_DUPLICATE:
                return new NotificationCompat.Action(R.drawable.ic_arrow_downward_black_24dp,mService.getResources().getString(R.string.duplicate),retrieveAction(actionStr,7,id));

            case TaskService.ACTION_CONTROL_CLEAR:
            default:
                return new NotificationCompat.Action(R.drawable.ic_delete_black_24dp,mService.getResources().getString(R.string.clear),retrieveAction(actionStr,8,id));
        }
    }


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
    private int mCurrentIconPos = 0;
    private final int[] mDownloadingIcon = new int[] {
            R.drawable.downloading_head,
            R.drawable.downloading_middle,
            R.drawable.downloading_overmiddle,
            R.drawable.downloading_tail,
            R.drawable.downloading_none
    };

    private int getNextDownloadingIcon() {
        mCurrentIconPos++;
        if(mCurrentIconPos >=mDownloadingIconSize) mCurrentIconPos = 0;
        return mDownloadingIcon[mCurrentIconPos];
    }

    private final int mDownloadingIconSize = mDownloadingIcon.length;

    public synchronized void init(TaskService service) {
        this.mService = service;
        mNotificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
    }

    private BaseTaskController getDownloadManager() {
        return mService.getDownloadManager();
    }

    private boolean shouldForeground() {
        BaseTaskController downloadManager = getDownloadManager();
        return downloadManager.isSomeTaskRunning();
    }

    private NotificationCompat.Builder newBuilder(BaseTask task, final int NOTIFICATION_ID) {
        Log.d(TAG, "thread " + Thread.currentThread().getId() + ", null builder for id " + NOTIFICATION_ID);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mService, NOTIFICATION_CHANNEL_ID);
        Intent intent = new Intent(App.getInstance().getApplicationContext(), DownloadActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(App.getInstance().getApplicationContext(), 0, intent, 0);
        builder.setContentIntent(pendingIntent).setContentTitle(task.getFileTitle());
        return builder;
    }

    @SuppressLint("RestrictedApi")
    private void bindAction(NotificationCompat.Builder builder, final int id, final int STATE) {
        builder.mActions.clear();
        switch (STATE) {
            case BaseTask.RUNNING:
               builder.addAction(getAction(TaskService.ACTION_CONTROL_PAUSE,id));
                builder.addAction(getAction(TaskService.ACTION_CONTROL_CANCEL,id));
                break;
            case BaseTask.SUCCESS:
                builder.addAction(getAction(TaskService.ACTION_CONTROL_OPEN,id));
                builder.addAction(getAction(TaskService.ACTION_CONTROL_CLEAR,id));

                break;
            case BaseTask.PAUSED:
                builder.addAction(getAction(TaskService.ACTION_CONTROL_RESUME,id));
                builder.addAction(getAction(TaskService.ACTION_CONTROL_CLEAR,id));
                break;
            case BaseTask.CANCELLED:
            case BaseTask.FAILURE_TERMINATED:
                builder.addAction(getAction(TaskService.ACTION_CONTROL_TRY_TO_RESUME,id));
                builder.addAction(getAction(TaskService.ACTION_CONTROL_RESTART,id));
                builder.addAction(getAction(TaskService.ACTION_CONTROL_CLEAR,id));
                break;
            case BaseTask.CONNECTING:
            case BaseTask.PENDING:
                builder.addAction(getAction(TaskService.ACTION_CONTROL_CANCEL,id));
                break;
        }



    }

    private void bindNotificationContent(NotificationCompat.Builder builder, final int NOTIFICATION_ID, final int STATE, final int INT_PROGRESS, final boolean PROGRESS_SUPPORT, BaseTask task) {

        switch (STATE) {
            case BaseTask.RUNNING:
                String speed;
                speed =Util.humanReadableByteCount((long) task.getSpeedInBytes())+"/s";
                if(PROGRESS_SUPPORT)
                    builder.setStyle(new NotificationCompat.BigTextStyle().bigText(INT_PROGRESS+"%"+MIDDLE_DOT+speed));
                else
                    builder.setStyle(new NotificationCompat.BigTextStyle().bigText("Downloading" + MIDDLE_DOT+speed));

                builder.setColor(mService.getResources().getColor(R.color.LightTealBlue));
                builder.setSmallIcon(getNextDownloadingIcon());

                break;
            case BaseTask.SUCCESS:
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(mService.getResources().getString(R.string.download_completed)+MIDDLE_DOT+Util.humanReadableByteCount(task.getDownloadedInBytes())));
                builder.setColor(mService.getResources().getColor(R.color.FlatGreen));
                builder.setSmallIcon(R.drawable.downloaded);
                break;
            case BaseTask.PAUSED:
                if(PROGRESS_SUPPORT) {
                    builder.setStyle(new NotificationCompat.BigTextStyle().bigText(INT_PROGRESS +"%" +MIDDLE_DOT+mService.getResources().getString(R.string.paused)));
                } else {
                    builder.setStyle(new NotificationCompat.BigTextStyle().bigText(mService.getResources().getString(R.string.paused)));
                }

                builder.setColor(mService.getResources().getColor(R.color.FlatOrange))
                        .setSmallIcon(R.drawable.download_pause);
                break;
            case BaseTask.FAILURE_TERMINATED:
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(mService.getResources().getString(R.string.failed_notification_message,task.getMessage())));
                builder.setColor(mService.getResources().getColor(R.color.FlatRed))
                        .setSmallIcon(R.drawable.download_failed);
                break;
            default:
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(BaseTask.getStateName(mService,STATE)));
                builder.setColor(mService.getResources().getColor(R.color.FlatTealBlue))
                        .setSmallIcon(R.drawable.downloading_white);
        }

        if(PROGRESS_SUPPORT)
            builder.setSubText(Util.humanReadableByteCount(task.getDownloadedInBytes())+" of "+ Util.humanReadableByteCount(task.getFileContentLength()));
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
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(("Downloading...")
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
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(("Some tasks downloaded")
                            .setGroupSummary(true)
                            .setOnlyAlertOnce(false)
                            .setGroup(DOWNLOADED_GROUP)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("Some tasks downloaded"))
                            .setContentIntent(pendingIntent);
            mNotificationManager.notify(1,groupBuilder.build());
            builder.setGroup(DOWNLOADED_GROUP);*/
        }
    }


    public synchronized void notifyTaskNotificationChanged(BaseTask task) {
        final int NOTIFICATION_ID = task.getId();
        final int STATE = task.getState();
        final int INT_PROGRESS = task.getProgressInteger();
        final boolean PROGRESS_SUPPORT = task.isProgressSupport();

        Log.d(TAG, "thread "+Thread.currentThread().getId()+", start updating id "+NOTIFICATION_ID+", state "+ BaseTask.getStateName(null,STATE)+", progress "+ INT_PROGRESS);

        NotificationCompat.Builder builder = getBuilder(task,NOTIFICATION_ID);
        bindAction(builder,NOTIFICATION_ID,STATE);
        bindNotificationContent(builder,NOTIFICATION_ID,STATE,INT_PROGRESS,PROGRESS_SUPPORT,task);
        postNotification(builder.build(), NOTIFICATION_ID, STATE== BaseTask.RUNNING, STATE == BaseTask.SUCCESS);

        if(STATE!= BaseTask.RUNNING)
            releaseBuilder(NOTIFICATION_ID);
    }

    private void postNotification(Notification notification, int NOTIFICATION_ID, boolean isOnGoing, boolean isSuccess) {
        if(mStopped) return;
        int newNotifyMode;
        if (isOnGoing||shouldForeground()) {
            newNotifyMode = NOTIFY_MODE_FOREGROUND;
        } else {
            newNotifyMode = NOTIFY_MODE_BACKGROUND;
        }

        if (mNotifyMode != newNotifyMode && newNotifyMode == NOTIFY_MODE_BACKGROUND) {
            mService.stopForeground(false);
            mService.releaseWakeLock();
            Log.d(TAG, "thread "+Thread.currentThread().getId()+", stop foreground id "+NOTIFICATION_ID+", isOnGoing false");
        }

         if ((mNotifyMode ==NOTIFY_MODE_BACKGROUND && newNotifyMode == NOTIFY_MODE_FOREGROUND)
         //||(newNotifyMode==NOTIFY_MODE_FOREGROUND&&mIndexBuilders.get(mFlagForegroundID)==null)
         ) {
            mService.startForeground(NOTIFICATION_ID, notification);
            mService.acquire();
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

    private NotificationCompat.Builder getBuilder(BaseTask task, final int id) {
        NotificationCompat.Builder builder = mIndexBuilders.get(id);

        if(builder==null) builder = newBuilder(task,id);
        return builder;
    }

    private void releaseBuilder(final int id) {
        mIndexBuilders.delete(id);
    }

    private PendingIntent retrieveAction(final String action,int code, final int id) {
        final ComponentName serviceName = new ComponentName(mService, TaskService.class);
        Intent intent = new Intent(action);
        intent.putExtra(BaseTask.EXTRA_TASK_ID,id);
        intent.setComponent(serviceName);
        return PendingIntent.getService(mService, code, intent, 0);
    }
}
