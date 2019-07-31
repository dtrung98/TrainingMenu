package com.zalo.servicetraining.downloader.ui.main;


import android.content.Intent;

import android.view.Gravity;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zalo.servicetraining.App;
import com.zalo.servicetraining.R;
import com.zalo.servicetraining.downloader.model.TaskInfo;
import com.zalo.servicetraining.downloader.service.DownloaderRemote;
import com.zalo.servicetraining.downloader.ui.base.BaseActivity;

import java.util.ArrayList;

import java.util.List;

import es.dmoral.toasty.Toasty;


public class DownloadActivity extends BaseActivity {
    private static final String TAG = "DownloadActivity";
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
                else Toasty.error(App.getInstance().getApplicationContext(),"Can't create new download task because you denied permissions!").show();
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
    protected void onInitRecyclerView() {
        addPlusButton();
        mAdapter = new DownloadAdapter(this);
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
        list.add("Downloading");

        if(task_list!=null&&!task_list.isEmpty()) {


            for (TaskInfo info :
                    task_list) {
                if (info.getSectionState() == TaskInfo.SECTION_DOWNLOADING)
                    list.add(info);
            }
            list.add("Downloaded");

            for (TaskInfo info :
                    task_list) {
                if (info.getSectionState() != TaskInfo.SECTION_DOWNLOADING)
                    list.add(info);
            }
        } else list.add("Downloaded");
        mAdapter.setData(list);
        getSwipeRefreshLayout().setRefreshing(false);
    }

    @Override
    protected void taskUpdated(int id, int state, float progress, boolean progress_support, long downloaded, long fileContentLength, float speed) {
        if(!mAdapter.onTaskUpdated(id,state, progress, progress_support,downloaded, fileContentLength, speed))
          mAdapter.onTaskAdded(DownloaderRemote.getTaskInfoWithTaskId(id));
    }

    @Override
    protected void onDestroy() {
        mAdapter.destroy();
        mGridLayoutManager = null;
        super.onDestroy();
    }
}
