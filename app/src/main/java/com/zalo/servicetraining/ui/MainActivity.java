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

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    @BindView(R.id.notification_text_view)
    TextView mNotificationTextView;

    @BindView(R.id.start_button)
    Button mStartButton;

    @BindView(R.id.end_button)
    Button mEndButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        checkStatus();
    }

    @OnClick(R.id.start_button)
    void clickOnStartButton() {
        startServiceWithNormalWay();
        checkStatus();
    }

    @OnClick(R.id.end_button)
    void clickOnEndButton(){
        checkStatus();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: service is now connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected: service is disconnected");
        }
    };
    private static final int UN_SET = 0;
    private static final int ERROR = 1;
    private static final int NOT_RUNNING = 2;
    private static final int RUNNING = 3;

    private int mStatus = UN_SET;

    private void checkStatus() {
        Boolean serviceRunning = TimeTrackRemote.isServiceRunning(this);
        if(serviceRunning==null) {
            Toasty.error(this, "Sorry, Cannt get service status").show();
            mStatus = ERROR;
        } else if(serviceRunning) {
            mStatus = RUNNING;
        } else {
            mStatus = NOT_RUNNING;
        }
        bindStatusButton();
    }

    private void bindStatusButton() {
        if(mStatus==UN_SET) checkStatus();
        switch (mStatus) {
            case ERROR:
            case NOT_RUNNING:
                mStartButton.setEnabled(true);
                mEndButton.setEnabled(false);
                mStartButton.setText("Start Service");
                break;
            case RUNNING:
                mStartButton.setEnabled(true);
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

}
