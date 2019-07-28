package com.zalo.servicetraining.downloader.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.mbms.DownloadRequest;
import android.util.Log;
import android.view.Gravity;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zalo.servicetraining.R;
import com.zalo.servicetraining.downloader.base.AbsTask;
import com.zalo.servicetraining.downloader.model.DownloadItem;
import com.zalo.servicetraining.downloader.model.TaskInfo;
import com.zalo.servicetraining.downloader.service.DownloaderRemote;
import com.zalo.servicetraining.downloader.service.DownloaderService;
import com.zalo.servicetraining.downloader.service.ServiceToken;
import com.zalo.servicetraining.downloader.ui.base.BasePermissionActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import java.util.List;

import es.dmoral.toasty.Toasty;


public class DownloaderActivity extends BasePermissionActivity implements ServiceConnection, DownloadAdapter.ItemClickListener {
    private static final String TAG = "DownloaderActivity";
    public static final String ACTION_ADD_NEW_DOWNLOAD = "add_new_download";
    public static final String ACTION_RESUME_DOWNLOAD = "resume_download";


    FloatingActionButton mAddButton;
    DownloadAdapter mAdapter;
    GridLayoutManager mGridLayoutManager;

    void addNewTask() {
        Intent intent = new Intent(ACTION_ADD_NEW_DOWNLOAD);
        executeWriteStorageAction(intent);
    }

    @Override
    public void onPermissionResult(Intent intent, boolean granted) {
        if(intent==null) return;
        String action = intent.getAction();
        if(action!=null&&!action.isEmpty())
        switch (action) {
            case ACTION_ADD_NEW_DOWNLOAD:
                if(granted) {
                    AddDownloadDialog.newInstance().show(getSupportFragmentManager(), AddDownloadDialog.TAG);
                }
                else Toasty.error(this,"Can't add new task because you denied permission!").show();
                break;
            case ACTION_RESUME_DOWNLOAD:
                break;
        }
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
        mAdapter = new DownloadAdapter(this);
        mAdapter.setListener(this);
        getRecyclerView().setAdapter(mAdapter);


        mGridLayoutManager = new GridLayoutManager(this,mAdapter.getSpanCount());

        mGridLayoutManager.setSpanSizeLookup(mAdapter.getSpanSizeLookup());
        getRecyclerView().setLayoutManager(mGridLayoutManager);
    }

    public void updateGridLayoutManager() {
        mGridLayoutManager.setSpanCount(mAdapter.getSpanCount());

    }


    @Override
    protected void refreshData() {
        ArrayList<Object> list = new ArrayList<>();
        List<TaskInfo> task_list = DownloaderRemote.getSessionTaskList();

        /*for (int i = 0; i < 4; i++) {
            list.add(new TaskInfo().setState(AbsTask.RUNNING);
        }
*/
       /* for (int i = 0; i < 20; i++) {
            list.add(new TaskInfo().setState(AbsTask.SUCCESS));
        }*/
        list.add("Downloading");

        if(task_list!=null&&!task_list.isEmpty()) {


            for (TaskInfo info :
                    task_list) {
                if (info.getSectionState() == TaskInfo.STATE_DOWNLOADING)
                    list.add(info);
            }
            list.add("Downloaded");

            for (TaskInfo info :
                    task_list) {
                if (info.getSectionState() != TaskInfo.STATE_DOWNLOADING)
                    list.add(info);
            }
        } else list.add("Downloaded");
        mAdapter.setData(list);
        getSwipeRefreshLayout().setRefreshing(false);
    }

    public void doSomething() {
         DownloaderRemote.appendTask(new DownloadItem("http://www.effigis.com/wp-content/uploads/2015/02/Airbus_Pleiades_50cm_8bit_RGB_Yogyakarta.jpg"));
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

    private void taskUpdated(int id, int state, float progress, boolean progress_support) {
       if(!mAdapter.onTaskUpdated(id,state, progress, progress_support))
          mAdapter.onTaskAdded(DownloaderRemote.getTaskInfoWithTaskId(id));
    }

    private DownloaderBroadcastReceiver mReceiver;

    private static final class DownloaderBroadcastReceiver extends BroadcastReceiver {
        private final WeakReference<DownloaderActivity> mWeakRefActivity;
        public DownloaderBroadcastReceiver(final DownloaderActivity activity) {
            mWeakRefActivity = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            DownloaderActivity activity = mWeakRefActivity.get();

            if(activity!=null && action!=null && !action.isEmpty()) {
                switch (action) {
                    case DownloaderService.ACTION_TASK_CHANGED:
                        Bundle bundle = intent.getExtras();
                        if (bundle != null) {
                            final int id = bundle.getInt(AbsTask.EXTRA_NOTIFICATION_ID, -1);
                            final int state = bundle.getInt(AbsTask.EXTRA_STATE, -1);
                            final float progress = bundle.getFloat(AbsTask.EXTRA_PROGRESS, -1);
                            final boolean progress_support = bundle.getBoolean(AbsTask.EXTRA_PROGRESS_SUPPORT, false);
                            activity.taskUpdated(id,state,progress,progress_support);
                        }
                        Log.d(TAG, "onReceive: action_task_changed");
                        break;
                }
            }
        }
    }

    private ServiceToken mServiceToken;

    private boolean mReceiverRegistered = false;
    private void register() {
        if(!mReceiverRegistered) {
            mReceiver = new DownloaderBroadcastReceiver(this);

            final IntentFilter filter = new IntentFilter();
            filter.addAction(DownloaderService.ACTION_TASK_CHANGED);

            Log.d(TAG, "registered");
            try {
                LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
            } catch (Exception ignored) {};

            mReceiverRegistered = true;
        }
    }

    private void unregister() {
        if (mReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
            } catch (Exception ignored) {}
            mReceiver = null;
            mReceiverRegistered = false;
        }
    }

    @Override
    protected void onDestroy() {
        unregister();
        DownloaderRemote.unBind(mServiceToken);
        mAdapter.destroy();
        mGridLayoutManager = null;
        mServiceToken = null;
        super.onDestroy();
    }

    @Override
    public void onItemClick(Object object) {

    }
}
