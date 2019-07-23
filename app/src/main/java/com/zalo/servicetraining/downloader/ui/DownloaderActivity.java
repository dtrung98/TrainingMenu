package com.zalo.servicetraining.downloader.ui;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zalo.servicetraining.R;
import com.zalo.servicetraining.downloader.model.DownloadItem;
import com.zalo.servicetraining.downloader.service.DownloaderRemote;
import com.zalo.servicetraining.downloader.service.ServiceToken;
import com.zalo.servicetraining.ui.base.AbsListActivity;


public class DownloaderActivity extends AbsListActivity implements ServiceConnection {
    private static final String TAG = "DownloaderActivity";

    FloatingActionButton mAddButton;

    void addNewTask() {
        doSomething();
       // AddDownloadDialog.newInstance().show(getSupportFragmentManager(), AddDownloadDialog.TAG);
    }

    private void addPlusButton() {
        CoordinatorLayout mRoot = findViewById(R.id.root);

        mAddButton = new FloatingActionButton(this);
        mAddButton.setImageResource(R.drawable.ic_add_black_24dp);
        float oneDP = getResources().getDimension(R.dimen.oneDp);
        mAddButton.setCustomSize((int) (60*oneDP));
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams((int)(60*oneDP),(int)(60*oneDP));
        params.gravity = Gravity.BOTTOM|Gravity.END;
        params.bottomMargin = (int)(oneDP*16);
        params.setMarginEnd(params.bottomMargin);
        mAddButton.setOnClickListener(view -> addNewTask());
        mRoot.addView(mAddButton,params);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mServiceToken = DownloaderRemote.bindServiceAndStartIfNotRunning(this,this);

    }

    @Override
    protected void onInitRecyclerView() {
        addPlusButton();
    }

    @Override
    protected void refreshData() {
        super.refreshData();
    }

    public void doSomething() {
         DownloaderRemote.appendTask(new DownloadItem(""));
    }
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.d(TAG, "onServiceConnected: receive mService");
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.d(TAG, "onServiceConnected: detach mService");

    }

    private ServiceToken mServiceToken;

    @Override
    protected void onDestroy() {
        DownloaderRemote.unBind(mServiceToken);
        mServiceToken = null;
        super.onDestroy();
    }
}
