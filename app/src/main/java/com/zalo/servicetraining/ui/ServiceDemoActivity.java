package com.zalo.servicetraining.ui;


import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zalo.servicetraining.R;
import com.zalo.servicetraining.service.TimeTrackRemote;
import com.zalo.servicetraining.service.TimeTrackService;

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
    }

    @OnClick(R.id.start_button)
    void clickOnStartButton() {
        TimeTrackRemote.startThenBindService(this,this);
    }

    @OnClick(R.id.end_button)
    void clickOnEndButton(){
        TimeTrackRemote.unBind(this);
        checkStatus();
    }

    private TimeTrackService mService;

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
        TimeTrackRemote.unBind(this);
        mService = null;
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
    private void notifyTime(int time) {
        Log.d(TAG, "notifyTime: time = "+time);
        mNotificationTextView.setText(String.format("%d", time));
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if(iBinder instanceof TimeTrackService.TimeTrackBinder) {
            Log.d(TAG, "onServiceConnected: detect service : "+componentName);
            mService = ((TimeTrackService.TimeTrackBinder)iBinder).getService();
            checkStatus();

        }
        else Log.d(TAG, "onServiceConnected: not the TimeTrackService");

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        if(componentName.getClassName().equals("com.zalo.servicetraining.service.TimeTrackService")) {
            Log.d(TAG, "onServiceDisconnected : detect service " +componentName.getClassName());
            checkStatus();
        } else Log.d(TAG, "onServiceDisconnected: not the TimeTrackService");

    }
}
