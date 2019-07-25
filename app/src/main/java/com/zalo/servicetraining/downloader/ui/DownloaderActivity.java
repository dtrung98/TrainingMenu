package com.zalo.servicetraining.downloader.ui;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zalo.servicetraining.R;
import com.zalo.servicetraining.downloader.model.DownloadItem;
import com.zalo.servicetraining.downloader.model.TaskInfo;
import com.zalo.servicetraining.downloader.service.DownloaderRemote;
import com.zalo.servicetraining.downloader.service.ServiceToken;
import com.zalo.servicetraining.ui.base.AbsListActivity;

import java.util.ArrayList;

import java.util.List;


public class DownloaderActivity extends AbsListActivity implements ServiceConnection, DownloadAdapter.ItemClickListener {
    private static final String TAG = "DownloaderActivity";

    FloatingActionButton mAddButton;
    DownloadAdapter mAdapter;
    GridLayoutManager mGridLayoutManager;

    void addNewTask() {
        AddDownloadDialog.newInstance().show(getSupportFragmentManager(), AddDownloadDialog.TAG);
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
        if(task_list!=null&&!task_list.isEmpty()) {
            list.add("Downloading");


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
        }
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
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.d(TAG, "onServiceConnected: detach mService");

    }

    private ServiceToken mServiceToken;

    @Override
    protected void onDestroy() {
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
