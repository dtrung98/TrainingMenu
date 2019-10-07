package com.zalo.trainingmenu.downloader.ui.base;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ldt.menulayout.ui.AbsListActivity;
import com.zalo.trainingmenu.downloader.base.BaseTask;
import com.zalo.trainingmenu.downloader.model.TaskInfo;
import com.zalo.trainingmenu.downloader.service.RemoteForTaskService;
import com.zalo.trainingmenu.downloader.service.TaskService;
import com.zalo.trainingmenu.fundamental.servicedemo.service.ServiceToken;

import java.lang.ref.WeakReference;

public abstract class BaseActivity extends AbsListActivity implements ServiceConnection {
    private static final String TAG = "BaseActivity";

    private ServiceToken mServiceToken;

    private boolean mReceiverRegistered = false;
    protected final boolean isServiceConnected() {
        return mServiceToken!=null;
    }

    private void register() {
        if (!mReceiverRegistered) {
            mReceiver = new DownloaderBroadcastReceiver(this);

            final IntentFilter filter = new IntentFilter();
            filter.addAction(TaskService.ACTION_TASK_MANAGER_CHANGED);
            filter.addAction(TaskService.ACTION_TASK_CHANGED);
            filter.addAction(TaskService.ACTION_TASK_CLEAR);

            Log.d(TAG, "registered");
            try {
                LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
            } catch (Exception ignored) {
            }

            mReceiverRegistered = true;
        }
    }

    private void unregister() {
        if (mReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
            } catch (Exception ignored) {
            }
            mReceiver = null;
            mReceiverRegistered = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mServiceToken = RemoteForTaskService.bindServiceAndStartIfNotRunning(this,this);
    }

    @Override
    protected void onDestroy() {
        unregister();
        RemoteForTaskService.unBind(mServiceToken);
        mServiceToken = null;
        super.onDestroy();
    }

    private DownloaderBroadcastReceiver mReceiver;

    private static final class DownloaderBroadcastReceiver extends BroadcastReceiver {
        private final WeakReference<BaseActivity> mWeakRefActivity;

        public DownloaderBroadcastReceiver(final BaseActivity activity) {
            mWeakRefActivity = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            BaseActivity activity = mWeakRefActivity.get();

            if (activity != null && action != null && !action.isEmpty()) {
                switch (action) {
                    case TaskService.ACTION_TASK_MANAGER_CHANGED:
                        Log.d(TAG, "receive broad cast task manager changed ");
                        activity.refreshData();
                        break;
                    case TaskService.ACTION_TASK_CHANGED:
                        Bundle bundle = intent.getExtras();
                        if (bundle != null) {
                            TaskInfo info = intent.getParcelableExtra(BaseTask.EXTRA_TASK_INFO);
                        /*    final int id = bundle.getInt(BaseTask.EXTRA_TASK_ID, -1);
                            final int state = bundle.getInt(BaseTask.EXTRA_STATE, -1);
                            final float progress = bundle.getFloat(BaseTask.EXTRA_PROGRESS, -1);
                            final boolean progress_support = bundle.getBoolean(BaseTask.EXTRA_PROGRESS_SUPPORT, false);
                            final long downloaded = bundle.getLong(BaseTask.EXTRA_DOWNLOADED_IN_BYTES);
                            final long fileLength = bundle.getLong(BaseTask.EXTRA_FILE_CONTENT_LENGTH);
                            final float speedInBytes = bundle.getFloat(BaseTask.EXTRA_SPEED);
                            activity.onTaskUpdated(id, state, progress, progress_support, downloaded, fileLength, speedInBytes);
                  */          if(info!=null) activity.onTaskUpdated(info);
                        }
                        Log.d(TAG, "onReceive: action_task_changed");
                        break;
                    case TaskService.ACTION_TASK_CLEAR:
                        int id = intent.getIntExtra(BaseTask.EXTRA_TASK_ID,-1);
                        if(id!=-1)
                        activity.onClearTask(id);
                }
            }
        }
    }

    protected void onClearTask(int id) {
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.d(TAG, "onServiceConnected: receive mService");
        refreshData();
        register();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        unregister();
        Log.d(TAG, "onServiceConnected: detach mService");

    }

   /* protected void onTaskUpdated(int id, int state, float progress, boolean progress_support, long downloaded, long fileContentLength, float speed) {

    }*/

    protected void onTaskUpdated(TaskInfo info) {
    }
}
