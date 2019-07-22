package com.zalo.servicetraining.ui;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.zalo.servicetraining.R;
import com.zalo.servicetraining.service.TimeTrackRemote;
import com.zalo.servicetraining.service.TimeTrackService;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class ServiceDemoActivity extends AppCompatActivity implements ServiceConnection{
    public static final String TAG = "ServiceDemoActivity";

    @BindView(R.id.notification_text_view)
    TextView mNotificationTextView;

    @BindView(R.id.start_button)
    Button mStartButton;

    @BindView(R.id.end_button)
    Button mEndButton;

    TimeTrackRemote.ServiceToken mServiceToken;

    @OnClick(R.id.back_button)
    void back() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_service_activity);
        ButterKnife.bind(this);
        checkStatus();

        if(mStatus==RUNNING)
        mServiceToken = TimeTrackRemote.bindServiceAndStartIfNotRunning(this,this);

    }

    @OnClick(R.id.start_button)
    void clickOnStartButton() {
       mServiceToken = TimeTrackRemote.bindServiceAndStartIfNotRunning(this,this);
    }

    @OnClick(R.id.end_button)
    void clickOnEndButton(){
        mServiceToken = null;
        TimeTrackRemote.stopService();
        checkStatus();
    }

    private static final int UN_SET = 0;
    private static final int ERROR = 1;
    private static final int NOT_RUNNING = 2;
    private static final int RUNNING = 3;

    private int mStatus = UN_SET;

    private void checkStatus() {
        Boolean serviceRunning = TimeTrackRemote.isServiceRunning(this);
        if(serviceRunning==null) {
            Toasty.error(this, "Sorry, Can not get service status").show();
            mStatus = ERROR;
        } else if(serviceRunning) {
            mStatus = RUNNING;
        } else {
            mStatus = NOT_RUNNING;
        }
        bindStatusButton();
    }

    @Override
    protected void onDestroy() {
        TimeTrackRemote.unBind(mServiceToken);
        mServiceToken = null;
        unregister();
        super.onDestroy();
    }


    private void bindStatusButton() {
        if(mStatus==UN_SET) checkStatus();
        switch (mStatus) {
            case ERROR:
            case NOT_RUNNING:
                mStartButton.setEnabled(true);
                mStartButton.setBackgroundResource(R.drawable.background_sign_in);
                mEndButton.setEnabled(false);
                mStartButton.setText(R.string.start_new_service);
                break;
            case RUNNING:
                mStartButton.setEnabled(false);
                mStartButton.setBackgroundResource(R.drawable.background_purchase_disable);
                mEndButton.setEnabled(true);
                mStartButton.setText("RUNNING");
                break;
        }

    }

    private void startServiceWithNormalWay() {
        Intent intent = new Intent(this, TimeTrackService.class);
        intent.setAction("start_service");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

    }

    @SuppressLint("DefaultLocale")
    private void notifyTime(long time) {
        Log.d(TAG, "notifyTime: time = "+time);
        mNotificationTextView.setText(String.format("%d", time/1000)+"\n Time Track is still running");
    }

    private void notifyStop(long time) {
        Log.d(TAG, "notifyStop: time = "+time);
        mNotificationTextView.setText((time/1000)+ "\nTime Track had stopped!");
    }

    private boolean mReceiverRegistered = false;
    private void register() {
        if(!mReceiverRegistered) {
            mTimeTrackReceiver = new TimeTrackReceiver(this);

            final IntentFilter filter = new IntentFilter();
            filter.addAction(TimeTrackService.ACTION_NOTIFY_TIME);
            filter.addAction(TimeTrackService.ACTION_NOTIFY_STOP);

            Log.d(TAG, "registered");
            LocalBroadcastManager.getInstance(this).registerReceiver(mTimeTrackReceiver,filter);

            mReceiverRegistered = true;
        }
    }

    private void unregister() {
        if (mReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mTimeTrackReceiver);
            mTimeTrackReceiver = null;
            mReceiverRegistered = false;
        }
    }

    private TimeTrackReceiver mTimeTrackReceiver;

    private static final class TimeTrackReceiver extends BroadcastReceiver {
        private final WeakReference<ServiceDemoActivity> mWeakRefActivity;
        public TimeTrackReceiver(final ServiceDemoActivity activity) {
            mWeakRefActivity = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            ServiceDemoActivity activity = mWeakRefActivity.get();

            if(activity!=null && action!=null && !action.isEmpty()) {
                switch (action) {
                    case TimeTrackService.ACTION_NOTIFY_TIME:
                        activity.notifyTime(intent.getLongExtra(TimeTrackService.EXTRA_CURRENT_TIME_TRACK,0));
                        Log.d(TAG, "onReceive: action_notify_time");
                        break;
                    case TimeTrackService.ACTION_NOTIFY_STOP:
                        activity.notifyStop(intent.getLongExtra(TimeTrackService.EXTRA_CURRENT_TIME_TRACK,0));
                        Log.d(TAG, "onReceive: action_notify_stop");
                        break;
                }
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if(iBinder instanceof TimeTrackService.TimeTrackBinder) {
            Log.d(TAG, "onServiceConnected: detect service : "+componentName);
            checkStatus();

        }
        else Log.d(TAG, "onServiceConnected: not the TimeTrackService");

        register();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        if(componentName.getClassName().equals("com.zalo.servicetraining.service.TimeTrackService")) {
            Log.d(TAG, "onServiceDisconnected : detect service " +componentName.getClassName());
            checkStatus();
        } else Log.d(TAG, "onServiceDisconnected: not the TimeTrackService");

        unregister();
    }
}
