package com.zalo.servicetraining.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.zalo.servicetraining.R;
import com.zalo.servicetraining.ui.ServiceDemoActivity;

import java.lang.ref.WeakReference;

import static android.os.Build.VERSION_CODES.O;

public class TimeTrackService extends Service {
    public static final String TAG = "TimeTrackService";

    static final int NOTIFICATION_ID = 1;
    static final String NOTIFICATION_CHANNEL_ID = "time_listening_notification";
    public static final String ACTION_NOTIFY_TIME = "com.zalo.servicetraining.service.ACTION_NOTIFY_TIME";
    public static final String ACTION_NOTIFY_STOP = "com.zalo.servicetraining.service.ACTION_NOTIFY_STOP";
    public static final String EXTRA_CURRENT_TIME_TRACK = "current_time_track";
    static final int A_SECOND_LOOP = 1;
    static final int STOP_LOOP = 2;

    private TimeHandler mTimeHandler;

    private static class TimeHandler extends Handler {
        private final WeakReference<TimeTrackService> mWeakRefService;

        public TimeHandler(final TimeTrackService service, @NonNull final Looper looper) {
            super(looper);
            mWeakRefService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            final TimeTrackService service = mWeakRefService.get();
            if(service == null) return;
            long time = System.currentTimeMillis();

            if(msg.what==A_SECOND_LOOP) {
                    Intent intent = new Intent(ACTION_NOTIFY_TIME);
                    intent.putExtra(EXTRA_CURRENT_TIME_TRACK,time);
                    LocalBroadcastManager.getInstance(service).sendBroadcast(intent);
                    service.notifyTime(time);
                this.sendEmptyMessageDelayed(A_SECOND_LOOP,1000);
            } else if(msg.what==STOP_LOOP) {
                    Intent intent = new Intent(ACTION_NOTIFY_STOP);
                    intent.putExtra(EXTRA_CURRENT_TIME_TRACK,System.currentTimeMillis());
                service.notifyStop(System.currentTimeMillis());
                removeMessages(A_SECOND_LOOP);
                LocalBroadcastManager.getInstance(service).sendBroadcast(intent);
            }
        }

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
        mTimeHandler = new TimeHandler(this,Looper.getMainLooper());
        startInForeground();
        mTimeHandler.sendEmptyMessage(A_SECOND_LOOP);
    }

    public void startInForeground() {
        Intent notificationIntent = new Intent(this, ServiceDemoActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_style_black_24dp)
                .setContentTitle("Time Service is running")
                .setTicker("This is ticker")
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();

        if(Build.VERSION.SDK_INT>=O) {
            createNotificationChannel();

        }
        startForeground(NOTIFICATION_ID,notification);
    }

    public void notifyTime(long time) {
        Intent notificationIntent = new Intent(this, ServiceDemoActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_style_black_24dp)
                .setContentTitle("Time Service is running. Current time track is "+ (time/1000))
                .setTicker("This is ticker")
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager!=null)
            notificationManager.notify(NOTIFICATION_ID,notification);
    }

    private void notifyStop(long time) {

            Intent notificationIntent = new Intent(this, ServiceDemoActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_style_black_24dp)
                    .setContentTitle("Time Service is stopped with end time track "+ (time/1000))
                    .setTicker("This is ticker")
                    .setContentIntent(pendingIntent);
            Notification notification = builder.build();
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            if(notificationManager!=null)
            notificationManager.notify(NOTIFICATION_ID,notification);

    }


    public void stopForegroundThenStopSelf() {
        stopForeground(true);
        stopSelf();

    }

    @RequiresApi(26)
    private void createNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel;

        channel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID);

        if(channel==null) {
            // create new chanel
            channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,getString(R.string.notification_chanel_name),NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(getString(R.string.notification_chanel_description));
            channel.enableLights(true);
            channel.enableVibration(true);
        }

        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mTimeHandler.sendEmptyMessage(STOP_LOOP);
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
    public IBinder mTimeTrackBinder = new TimeTrackBinder();

    public class TimeTrackBinder extends Binder {
        @NonNull
        public TimeTrackService getService() {
            return TimeTrackService.this;
        }
    }

}
