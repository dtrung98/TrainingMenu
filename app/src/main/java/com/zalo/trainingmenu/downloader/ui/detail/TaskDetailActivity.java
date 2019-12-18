package com.zalo.trainingmenu.downloader.ui.detail;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.GridLayoutManager;

import com.ldt.menulayout.model.Item;
import com.ldt.menulayout.ui.MenuAdapter;
import com.ldt.menulayout.ui.OnItemClickListener;
import com.zalo.trainingmenu.App;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.downloader.base.BaseTask;
import com.zalo.trainingmenu.downloader.base.Task;
import com.zalo.trainingmenu.downloader.model.TaskInfo;
import com.zalo.trainingmenu.downloader.service.RemoteForTaskService;
import com.zalo.trainingmenu.downloader.ui.base.BaseActivity;
import com.zalo.trainingmenu.util.Util;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class TaskDetailActivity extends BaseActivity implements OnItemClickListener {
    private static final String TAG = "TaskDetailActivity";
    DetailAdapter mAdapter;
    public static final String VIEW_TASK_DETAIL ="view_task_detail";
    private int mTaskId = -1;
    private TaskInfo mTaskInfo;
    private ImageView mIconImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSwipeRefreshLayout().setEnabled(false);
        getParameters();
    }

    @Override
    protected int title() {
        return R.string.properties;
    }

    public void getParameters() {
        if(getIntent()!=null) {

            if(getIntent().getAction()!=null&&getIntent().getAction().equals(VIEW_TASK_DETAIL)) {
                mTaskId = getIntent().getIntExtra(BaseTask.EXTRA_TASK_ID,-1);
            }
        }

        if(mTaskId==-1) {
            Toasty.error(App.getInstance().getApplicationContext(),"Task ID is invalid").show();
            finish();
        }
    }

    @Override
    protected void onInitRecyclerView() {
        mIconImageView = findViewById(R.id.icon);
        mIconImageView.setVisibility(View.VISIBLE);
        mAdapter = new DetailAdapter();
        mAdapter.setListener(this);
        getRecyclerView().setAdapter(mAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,mAdapter.getSpanCount());

        gridLayoutManager.setSpanSizeLookup(mAdapter.getSpanSizeLookup());
        getRecyclerView().setLayoutManager(gridLayoutManager);
    }


    @Override
    protected void refreshData() {
        if(isServiceConnected()) {
            mTaskInfo = RemoteForTaskService.getTaskInfoWithTaskId(mTaskId);
        }
        if(mTaskInfo!=null) {
            bind();
        }

        getSwipeRefreshLayout().setRefreshing(false);
    }

    @Override
    protected int contentLayout() {
        return com.ldt.menulayout.R.layout.motion_list_layout;
    }

    private void bind() {
        setTitle(mTaskInfo.getFileTitle());
        bindIcon();
        ArrayList<Item> list = new ArrayList<>();
        list.add(Item.with(this).setTitle(R.string.parent_folder).setDescription(mTaskInfo.getDirectory()).get());
        list.add(Item.with(this).setTitle(R.string.path).setDescription(mTaskInfo.getDirectory()+'/'+mTaskInfo.getFileTitle()).get());
        list.add(Item.with(this).setTitle(R.string.link).setDescription(mTaskInfo.getURLString()).get());
        list.add(Item.with(this).setTitle(R.string.support_resume).setDescription((mTaskInfo.isProgressSupport()) ? R.string.yes : R.string.no).get());
        list.add(Item.with(this).setTitle(R.string.connections_number).setDescription(mTaskInfo.getPartialInfoList().size()+ " "+getResources().getString(R.string.connections)).get());
        list.add(Item.with(this).setTitle(R.string.index).setDescription(String.valueOf(mTaskInfo.getId())).get());
        list.add(Item.with(this).setTitle(R.string.state).setDescription(Task.getStateName(this,mTaskInfo.getState())).get());
        list.add(Item.with(this).setTitle(R.string.message).setDescription((mTaskInfo.getMessage().isEmpty())? this.getResources().getString(R.string.empty): mTaskInfo.getMessage()).get());
        list.add(Item.with(this).setTitle(R.string.size).setDescription(Util.humanReadableByteCount(mTaskInfo.getFileContentLength())).get());
        list.add(Item.with(this).setTitle(R.string.downloaded_size).setDescription(Util.humanReadableByteCount(mTaskInfo.getDownloadedInBytes())).get());
        list.add(Item.with(this).setTitle(R.string.progress).setDescription(((int)(mTaskInfo.getProgress()*100))+"%").get());
        list.add(Item.with(this).setTitle(R.string.creation_time).setDescription(Util.formatPrettyDateTimeWithSecond(mTaskInfo.getCreatedTime())).get());
        list.add(Item.with(this).setTitle(R.string.execution_time).setDescription(Util.formatPrettyDateTimeWithSecond(mTaskInfo.getFirstExecutedTime())).get());
        list.add(Item.with(this).setTitle(R.string.finishing_time).setDescription(Util.formatPrettyDateTimeWithSecond(mTaskInfo.getFinishedTime())).get());
        list.add(Item.with(this).setTitle(R.string.running_duration).setDescription(Util.formatDuration(mTaskInfo.getRunningTime())).get());
        mAdapter.setData(list);
    }

    private void bindIcon() {
        switch (mTaskInfo.getState()) {
            case BaseTask.SUCCESS:
                mIconImageView.setImageResource(R.drawable.tick);
                mIconImageView.setColorFilter(getResources().getColor(R.color.FlatGreen));
                break;
            case BaseTask.RUNNING:
                mIconImageView.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
                mIconImageView.setColorFilter(getResources().getColor(R.color.FlatTealBlue));
                break;
            case BaseTask.PENDING:
                mIconImageView.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
                mIconImageView.setColorFilter(getResources().getColor(R.color.FlatGreen));
                break;
            case BaseTask.FAILURE_TERMINATED:
                mIconImageView.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
                mIconImageView.setColorFilter(getResources().getColor(R.color.FlatRed));
                break;
            default:
                mIconImageView.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
                mIconImageView.setColorFilter(getResources().getColor(R.color.FlatOrange));
                break;
        }
    }

    @Override
    protected void onTaskUpdated(TaskInfo info) {
        if(mTaskInfo!=null&&mTaskId==info.getId()) {
            mTaskInfo = info;
            bind();
        }
    }

  /*  @Override
    protected void onTaskUpdated(int id, int state, float progress, boolean progress_support, long downloaded, long fileContentLength, float speed) {
        super.onTaskUpdated(id, state, progress, progress_support, downloaded, fileContentLength, speed);
        if(mTaskInfo!=null && mTaskId == id) {
         refreshData();
        }
    }*/

    @Override
    protected void onClearTask(int id) {
        Toasty.warning(App.getInstance().getApplicationContext(),"Task was removed!").show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onEventItemClick(Item item, int position) {
        if(item.getDrawableRes()==null) {
            // Property
            if(Util.setClipboard(App.getInstance().getApplicationContext(),item.getTitle(), item.getDescription()))
            Toasty.success(App.getInstance().getApplicationContext(),"Copied").show();
            else Toasty.error(App.getInstance().getApplicationContext(),"Something went wrong, can not copy this field").show();
        } else {
            // Action
        }
    }
}
