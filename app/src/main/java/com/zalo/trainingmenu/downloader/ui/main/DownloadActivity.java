package com.zalo.trainingmenu.downloader.ui.main;


import android.content.ComponentName;
import android.content.Intent;

import android.content.res.ColorStateList;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zalo.trainingmenu.App;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.downloader.base.BaseTask;
import com.zalo.trainingmenu.downloader.base.Task;
import com.zalo.trainingmenu.downloader.model.DownloadItem;
import com.zalo.trainingmenu.downloader.model.TaskInfo;
import com.zalo.trainingmenu.downloader.service.RemoteForTaskService;
import com.zalo.trainingmenu.downloader.service.TaskService;
import com.zalo.trainingmenu.downloader.ui.base.BaseActivity;
import com.zalo.trainingmenu.downloader.ui.base.OptionBottomSheet;
import com.zalo.trainingmenu.downloader.ui.setting.SettingActivity;
import com.zalo.trainingmenu.model.CountSectionItem;
import com.zalo.trainingmenu.util.Util;

import java.util.ArrayList;

import java.util.List;

import es.dmoral.toasty.Toasty;


public class DownloadActivity extends BaseActivity {
    public static final String ACTION_TRY_TO_RESUME = "try_to_resume";
    private static final String TAG = "DownloadActivity";
    public static final String ACTION_NEW_DOWNLOAD_DIALOG = "new_download_dialog";
    public static final String ACTION_APPEND_TASK ="append_task";
    public static final String ACTION_RESUME_DOWNLOAD = "resume_download";
    public static final String ACTION_RESTART_DOWNLOAD = "restart_download";
    public static final String ACTION_OPEN_FILE = "open_file";

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        super.onServiceConnected(componentName, iBinder);
        handleIntent(getIntent());
    }

    FloatingActionButton mAddButton;
    ImageView mMenuButton;
    DownloadAdapter mAdapter;
    GridLayoutManager mGridLayoutManager;

    void addNewTask() {
        Intent intent = new Intent(ACTION_NEW_DOWNLOAD_DIALOG);
        executeWriteStorageAction(intent);
    }

    void addNewTask(String url) {
        AddDownloadDialog.newInstance(url).show(getSupportFragmentManager(), AddDownloadDialog.TAG);
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

            case ACTION_NEW_DOWNLOAD_DIALOG:
                if(granted) {
                    AddDownloadDialog.newInstance().show(getSupportFragmentManager(), AddDownloadDialog.TAG);
                }
                else Toasty.error(App.getInstance().getApplicationContext(),R.string.error_permissions_new_download_dialog).show();
                break;
            case ACTION_APPEND_TASK:
                if(granted) {
                    DownloadItem item = intent.getParcelableExtra(BaseTask.EXTRA_DOWNLOAD_ITEM);
                    if(item!=null) RemoteForTaskService.appendTask(item);
                } else Toasty.error(App.getInstance().getApplicationContext(),R.string.error_permissions_append_task).show();
            case ACTION_RESUME_DOWNLOAD:
                if(granted) {
                    int id = intent.getIntExtra(BaseTask.EXTRA_TASK_ID,-1);
                    if(id!=-1) RemoteForTaskService.resumeTaskWithTaskId(id);
                }
                else Toasty.error(App.getInstance().getApplicationContext(),R.string.error_permissions_resume_download).show();
                break;
            case ACTION_RESTART_DOWNLOAD:
                if(granted) {
                    int id = intent.getIntExtra(BaseTask.EXTRA_TASK_ID,-1);
                    if(id!=-1) RemoteForTaskService.restartTaskWithTaskId(id);
                }
                else Toasty.error(App.getInstance().getApplicationContext(),R.string.error_permissions_restart).show();
                break;
            case ACTION_TRY_TO_RESUME:
                if(granted) {
                    int id = intent.getIntExtra(BaseTask.EXTRA_TASK_ID,-1);
                    if(id!=-1) RemoteForTaskService.tryToResume(id);
                }
                else Toasty.error(App.getInstance().getApplicationContext(),R.string.error_permissions_try_to_resume).show();
                break;
            case ACTION_OPEN_FILE:
                if(granted) {
                    TaskInfo info = intent.getParcelableExtra(BaseTask.EXTRA_TASK_INFO);
                    if(info!=null) RemoteForTaskService.openFinishedTaskInfo(this,info);
                }
                else Toasty.error(App.getInstance().getApplicationContext(),R.string.error_permissions_open_file).show();
                break;
        }
    }

    private void addButtons() {
        ViewGroup mRoot = findViewById(R.id.root);

        // Add new-task button
        mAddButton = new FloatingActionButton(this);
        mAddButton.setId(View.generateViewId());
        mAddButton.setImageResource(R.drawable.ic_add_black_24dp);
        float oneDP = getResources().getDimension(R.dimen.oneDp);
        mAddButton.setCustomSize((int) (60*oneDP));

        ViewGroup.MarginLayoutParams params = null;
        ConstraintSet addSet = null;

        if(mRoot instanceof CoordinatorLayout) {
            params = new CoordinatorLayout.LayoutParams((int)(60*oneDP),(int)(60*oneDP));
            ((CoordinatorLayout.LayoutParams) params).gravity = Gravity.BOTTOM | Gravity.END;

        } else if (mRoot instanceof ConstraintLayout) {
            params = new ConstraintLayout.LayoutParams((int)(60*oneDP),(int)(60*oneDP));
            addSet = new ConstraintSet();
            addSet.clone((ConstraintLayout) mRoot);
            addSet.connect(mAddButton.getId(),ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM);
            addSet.connect(mAddButton.getId(),ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END);

        }

        if(params!=null) {
            params.bottomMargin = (int) (oneDP * 16);
            params.setMarginEnd(params.bottomMargin);
        }

        mAddButton.setOnClickListener(view -> plusButtonClick());
        mRoot.addView(mAddButton,params);
        if(addSet!=null) addSet.applyTo((ConstraintLayout) mRoot);
        // Add menu button
        mMenuButton = new ImageView(this);
        mMenuButton.setId(View.generateViewId());
        mMenuButton.setImageResource(R.drawable.ic_menu_24dp);
        ViewGroup.MarginLayoutParams menuParams = null;
        ConstraintSet menuSet = null;

        if(mRoot instanceof CoordinatorLayout) {
            menuParams = new CoordinatorLayout.LayoutParams((int)(50*oneDP),(int)(50*oneDP));
            ((CoordinatorLayout.LayoutParams) menuParams).gravity = Gravity.TOP | Gravity.END;
        } else if(mRoot instanceof ConstraintLayout) {
            menuParams = new ConstraintLayout.LayoutParams((int)(50*oneDP),(int)(50*oneDP));
            menuSet = new ConstraintSet();
            menuSet.clone((ConstraintLayout) mRoot);
            menuSet.connect(mMenuButton.getId(),ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP);
            menuSet.connect(mMenuButton.getId(),ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END);
        }
        if(menuParams!=null) {
            menuParams.topMargin = (int) (16 * oneDP);
            menuParams.setMarginEnd(menuParams.topMargin);
        }

        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
        mMenuButton.setBackgroundResource(R.drawable.circle_background_support_touch);
        mMenuButton.setColorFilter(getResources().getColor(R.color.FlatWhite));
        int dpTwelve = (int)(15*oneDP);
        mMenuButton.setPadding(dpTwelve,dpTwelve,dpTwelve,dpTwelve);
        mMenuButton.setOnClickListener(view -> showMenu());
        mRoot.addView(mMenuButton,menuParams);
        if(menuSet !=null)
            menuSet.applyTo((ConstraintLayout) mRoot);
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
                        Util.openDirectoryIntent(DownloadActivity.this, Util.getCurrentDownloadDirectoryPath());
                    break;
                case R.string.settings:
                    DownloadActivity.this.startActivity(new Intent(DownloadActivity.this, SettingActivity.class));
                    break;
                case R.string.restart_all:
                    RemoteForTaskService.restartAll();
                    break;
                case R.string.clear_all:
                    RemoteForTaskService.clearAllTasks();
                    break;
                case R.string.restart_selected_tasks:
                    RemoteForTaskService.restartTasks(mAdapter.getSelectedTasks());
                    mAdapter.goOutSelectMode();
                    break;
                case R.string.clear_selected_tasks:
                    RemoteForTaskService.clearTasks(mAdapter.getSelectedTasks());
                    mAdapter.goOutSelectMode();
                    break;
            }
            return true;
        }
    };

    private ImageView mIconImageView;


    @Override
    protected void onInitRecyclerView() {
        mIconImageView = findViewById(R.id.icon);
        if(mIconImageView!=null) {
            mIconImageView.setVisibility(View.VISIBLE);
            mIconImageView.setImageResource(R.drawable.download_icon);
            mIconImageView.setBackground(null);
        }
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
        mAddButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.Tomato)));
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
        List<TaskInfo> taskList = RemoteForTaskService.getSessionTaskList();
        int downloadingSize = 0;
        int downloadedSize = 0;

        if(taskList!=null&&!taskList.isEmpty()) {


            for (TaskInfo info :
                    taskList) {
                if (info.getSectionState() == TaskInfo.SECTION_DOWNLOADING) {
                    list.add(info);
                    downloadingSize++;
                }
            }

            for (TaskInfo info :
                    taskList) {
                if (info.getSectionState() != TaskInfo.SECTION_DOWNLOADING) {
                    list.add(info);
                    downloadedSize++;
                }
            }

        } else {

        }
        list.add(0,new CountSectionItem(getResources().getString(R.string.downloading),downloadingSize));
        list.add(downloadingSize+1,new CountSectionItem(getResources().getString(R.string.downloaded),downloadedSize));
        mAdapter.setData(list);
        getSwipeRefreshLayout().setRefreshing(false);
    }

    /*@Override
    protected void onTaskUpdated(int id, int state, float progress, boolean progress_support, long downloaded, long fileContentLength, float speed) {
       if(true) return;
        if(!mAdapter.onTaskUpdated(id,state, progress, progress_support,downloaded, fileContentLength, speed))
          mAdapter.onTaskAdded(RemoteForTaskService.getTaskInfoWithTaskId(id));
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(handleIntent(intent)) setIntent(null);
    }
    private boolean handleIntent(Intent intent) {
        if(intent!=null) {
            String action = intent.getAction();
            if (TaskService.ACTION_OPEN_NEW_TASK_DIALOG.equals(action)) {
                String url = intent.getStringExtra(Task.EXTRA_URL);
                addNewTask(url);
                return true;
            }
        }
        return false;
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
