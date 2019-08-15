package com.zalo.trainingmenu.downloader.ui.main;


import android.content.Intent;

import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zalo.trainingmenu.App;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.downloader.model.TaskInfo;
import com.zalo.trainingmenu.downloader.service.TaskServiceRemote;
import com.zalo.trainingmenu.downloader.ui.base.BaseActivity;
import com.zalo.trainingmenu.downloader.ui.base.OptionBottomSheet;
import com.zalo.trainingmenu.downloader.ui.setting.SettingActivity;

import java.util.ArrayList;

import java.util.List;

import es.dmoral.toasty.Toasty;


public class DownloadActivity extends BaseActivity {
    private static final String TAG = "DownloadActivity";
    public static final String ACTION_ADD_NEW_DOWNLOAD = "add_new_download";
    public static final String ACTION_RESUME_DOWNLOAD = "resume_download";


    FloatingActionButton mAddButton;
    ImageView mMenuButton;
    DownloadAdapter mAdapter;
    GridLayoutManager mGridLayoutManager;

    void addNewTask() {
        Intent intent = new Intent(ACTION_ADD_NEW_DOWNLOAD);
        executeWriteStorageAction(intent);
    }

    void plusButtonClick() {
        if(mAdapter!=null && mAdapter.isInSelectMode()) {
            mAdapter.goOutSelectMode();
        } else addNewTask();
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

    private void addButtons() {
        CoordinatorLayout mRoot = findViewById(R.id.root);

        // Add new-task button
        mAddButton = new FloatingActionButton(this);
        mAddButton.setImageResource(R.drawable.ic_add_black_24dp);
        float oneDP = getResources().getDimension(R.dimen.oneDp);
        mAddButton.setCustomSize((int) (60*oneDP));
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams((int)(60*oneDP),(int)(60*oneDP));
        params.gravity = Gravity.BOTTOM|Gravity.END;
        params.bottomMargin = (int)(oneDP*16);
        params.setMarginEnd(params.bottomMargin);
        mAddButton.setOnClickListener(view -> plusButtonClick());
        mRoot.addView(mAddButton,params);
        // Add menu button
        mMenuButton = new ImageView(this);
        mMenuButton.setImageResource(R.drawable.ic_menu_24dp);
        CoordinatorLayout.LayoutParams menuParams = new CoordinatorLayout.LayoutParams((int)(50*oneDP),(int)(50*oneDP));
        menuParams.gravity = Gravity.TOP | Gravity.END;
        menuParams.topMargin = (int)(16*oneDP);
        menuParams.setMarginEnd(menuParams.topMargin);

        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
        mMenuButton.setBackgroundResource(R.drawable.circle_background_support_touch);
        mMenuButton.setColorFilter(getResources().getColor(R.color.FlatWhite));
        int dpTwelve = (int)(15*oneDP);
        mMenuButton.setPadding(dpTwelve,dpTwelve,dpTwelve,dpTwelve);
        mMenuButton.setOnClickListener(view -> showMenu());
        mRoot.addView(mMenuButton,menuParams);

    }

    private int[] mMenuIDs = new int[] {

            R.string.normal,
            R.string.add_new_download,
            R.string.go_to_download_folder,
            R.string.warning_divider,
            R.string.restart_all,
            R.string.dangerous_divider,
            R.string.clear_all,
            R.string.focus_divider,
            R.string.settings,

    };

    private int[] mSelectIDs = new int[] {
            R.string.warning_divider,
            R.string.restart_selected_tasks,
            R.string.dangerous_divider,
            R.string.clear_selected_tasks
    };

    private void showMenu() {
        if(mAdapter!=null && mAdapter.isInSelectMode())
            OptionBottomSheet.newInstance(mSelectIDs,mMenuCallBack).showNow(getSupportFragmentManager(),OptionBottomSheet.TAG+"_MENU");
        else
        OptionBottomSheet.newInstance(mMenuIDs,mMenuCallBack).showNow(getSupportFragmentManager(),OptionBottomSheet.TAG+"_MENU");
    }

    private OptionBottomSheet.CallBack mMenuCallBack = new OptionBottomSheet.CallBack() {
        @Override
        public boolean onOptionClicked(int optionID) {
            switch (optionID) {
                case R.string.add_new_download:
                    addNewTask();
                    break;
                case R.string.go_to_download_folder:
                    break;
                case R.string.settings:
                    DownloadActivity.this.startActivity(new Intent(DownloadActivity.this, SettingActivity.class));
                    break;
                case R.string.restart_all:
                    TaskServiceRemote.restartAll();
                    break;
                case R.string.clear_all:
                    TaskServiceRemote.clearAllTasks();
                    break;
                case R.string.restart_selected_tasks:
                    TaskServiceRemote.restartTasks(mAdapter.getSelectedTasks());
                    mAdapter.goOutSelectMode();
                    break;
                case R.string.clear_selected_tasks:
                    TaskServiceRemote.clearTasks(mAdapter.getSelectedTasks());
                    mAdapter.goOutSelectMode();
                    break;
            }
            return true;
        }
    };


    @Override
    protected void onInitRecyclerView() {
        addButtons();
        mAdapter = new DownloadAdapter(this);
        getRecyclerView().setAdapter(mAdapter);

        mGridLayoutManager = new GridLayoutManager(this,mAdapter.getSpanCount());

        mGridLayoutManager.setSpanSizeLookup(mAdapter.getSpanSizeLookup());
        getRecyclerView().setLayoutManager(mGridLayoutManager);
    }

    public void updateGridLayoutManager() {
        mGridLayoutManager.setSpanCount(mAdapter.getSpanCount());

    }

    public void switchToSelectMode() {
        mAddButton.animate().rotation(135);
        mAddButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.FocusYellowColor)));
        mMenuButton.setColorFilter(getResources().getColor(R.color.FlatOrange));
        mMenuButton.setBackgroundResource(R.drawable.circle_background_support_touch_focus);
    }

    public void switchToNormalMode() {
        mAddButton.animate().rotation(0);
        mAddButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.FlatYellow)));
        mMenuButton.setColorFilter(getResources().getColor(R.color.FlatWhite));
        mMenuButton.setBackgroundResource(R.drawable.circle_background_support_touch);
    }

    @Override
    protected void refreshData() {
        ArrayList<Object> list = new ArrayList<>();
        List<TaskInfo> task_list = TaskServiceRemote.getSessionTaskList();
        list.add(getString(R.string.downloading));

        if(task_list!=null&&!task_list.isEmpty()) {


            for (TaskInfo info :
                    task_list) {
                if (info.getSectionState() == TaskInfo.SECTION_DOWNLOADING)
                    list.add(info);
            }
            list.add(getString(R.string.downloaded));

            for (TaskInfo info :
                    task_list) {
                if (info.getSectionState() != TaskInfo.SECTION_DOWNLOADING)
                    list.add(info);
            }
        } else list.add(getString(R.string.downloaded));
        mAdapter.setData(list);
        getSwipeRefreshLayout().setRefreshing(false);
    }

    /*@Override
    protected void onTaskUpdated(int id, int state, float progress, boolean progress_support, long downloaded, long fileContentLength, float speed) {
       if(true) return;
        if(!mAdapter.onTaskUpdated(id,state, progress, progress_support,downloaded, fileContentLength, speed))
          mAdapter.onTaskAdded(TaskServiceRemote.getTaskInfoWithTaskId(id));
    }*/

    @Override
    protected void onTaskUpdated(TaskInfo info) {
        if(!mAdapter.onTaskUpdated(info))
            mAdapter.onTaskAdded(info);

    }

    @Override
    protected void onClearTask(int id) {
        mAdapter.onTaskCleared(id);
    }

    @Override
    protected void onDestroy() {
        mAdapter.destroy();
        mGridLayoutManager = null;
        super.onDestroy();
    }

    @Override
    protected int title() {
        return R.string.downloader;
    }
}
