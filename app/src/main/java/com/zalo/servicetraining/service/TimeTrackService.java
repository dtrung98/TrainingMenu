package com.zalo.servicetraining.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.zalo.servicetraining.R;
import com.zalo.servicetraining.ui.MainActivity;

import static android.os.Build.VERSION_CODES.O;

public class TimeTrackService extends Service {
    public static final String TAG = "TimeTrackService";

    static final int NOTIFICATION_ID = 1;
    static final String NOTIFICATION_CHANNEL_ID = "time_listening_notification";



    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mTimeTrackBinder;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        startInForeground();
    }

    public void startInForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
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
