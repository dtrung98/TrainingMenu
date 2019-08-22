package com.zalo.trainingmenu.downloader.ui.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zalo.trainingmenu.App;
import com.zalo.trainingmenu.R;
import com.zalo.trainingmenu.downloader.base.BaseTask;
import com.zalo.trainingmenu.downloader.model.DownloadItem;
import com.zalo.trainingmenu.downloader.model.TaskInfo;
import com.zalo.trainingmenu.downloader.service.RemoteForTaskService;
import com.zalo.trainingmenu.downloader.ui.base.OptionBottomSheet;
import com.zalo.trainingmenu.downloader.ui.detail.TaskDetailActivity;
import com.zalo.trainingmenu.downloader.ui.widget.MultipartProgressBar;
import com.zalo.trainingmenu.model.CountSectionItem;
import com.zalo.trainingmenu.model.Item;
import com.zalo.trainingmenu.util.Util;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class DownloadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "DownloadAdapter";

    private static final int BIND_INFO = 1;
    private static final int BIND_PROGRESS_CHANGED = 2;
    private static final int BIND_STATE_CHANGED = 3;
    private static final int BIND_PROGRESS_SUPPORT = 4;
    private static final int BIND_SELECT = 5;

    private Context mContext;
    private final ArrayList<Object> mData = new ArrayList<>();

    public List<Object> getData() {
        return mData;
    }

    private GridLayoutManager.SpanSizeLookup mSpanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
        @Override
        public int getSpanSize(int position) {
            int type = getItemViewType(position);
            if(type == TYPE_DOWNLOADING_ITEM) return 2;
            else if(type== TYPE_SECTION) return 2;
            else return 2;
        }
    };

    GridLayoutManager.SpanSizeLookup getSpanSizeLookup() {
        return mSpanSizeLookup;
    }

    public boolean onTaskUpdated(TaskInfo newInfo) {
        int id = newInfo.getId();
        int size = mData.size();
        int posFound = -1;
        for (int i = 0; i < size; i++) {
            Object object = mData.get(i);
            if(object instanceof TaskInfo && ((TaskInfo)object).getId()==id) {
                posFound = i;
                break;
            }
        }

        if(posFound!=-1) {
            TaskInfo oldInfo = (TaskInfo) mData.get(posFound);
            if ((oldInfo.getState() != BaseTask.SUCCESS && newInfo.getState() == BaseTask.SUCCESS) || (oldInfo.getState() == BaseTask.SUCCESS && newInfo.getState() != BaseTask.SUCCESS)) {
                if(mContext instanceof DownloadActivity) {
                    Log.d(TAG, "onUpdateTask: need to refresh");
                    ((DownloadActivity) mContext).refreshData();
                }
            } else {
                // info.setState(state).setProgress(progress).setProgressSupport(progress_support);
                Log.d(TAG, "onUpdateTask: just update");

                synchronized (mData) {
                    mData.set(posFound, newInfo);
                }


                if(oldInfo.isProgressSupport()!=newInfo.isProgressSupport()) {
                    notifyItemChanged(posFound,BIND_PROGRESS_SUPPORT);
                }

                if(oldInfo.getProgress()!=newInfo.getProgress()) {
                    notifyItemChanged(posFound,BIND_PROGRESS_CHANGED);
                }
                else if(!oldInfo.getFileTitle().equals(newInfo.getFileTitle()) || !oldInfo.getDirectory().equals(newInfo.getDirectory())) {
                    notifyItemChanged(posFound, BIND_INFO);
                }

                if(oldInfo.getState()!=newInfo.getState()) {
                    notifyItemChanged(posFound,BIND_STATE_CHANGED);
                }
            }
            Log.d(TAG, "onUpdateTask posFound = "+posFound);
            return true;
        } else {
            // not in list
            return false;
        }
    }

    @Deprecated
    boolean onTaskUpdated(int id, int state, float progress, boolean progress_support, long downloaded, long fileContentLength, float speed) {
        int size = mData.size();
        int posFound = -1;
        for (int i = 0; i < size; i++) {
            Object object = mData.get(i);
            if(object instanceof TaskInfo && ((TaskInfo)object).getId()==id) {
               posFound = i;
               break;
            }
        }

        if(posFound!=-1) {
            TaskInfo info = (TaskInfo) mData.get(posFound);
            if ((info.getState() != BaseTask.SUCCESS && state == BaseTask.SUCCESS) || (info.getState() == BaseTask.SUCCESS && state != BaseTask.SUCCESS)) {
                if(mContext instanceof DownloadActivity) {
                    Log.d(TAG, "onUpdateTask: need to refresh");
                    ((DownloadActivity) mContext).refreshData();
                }
            } else {
               // info.setState(state).setProgress(progress).setProgressSupport(progress_support);
                Log.d(TAG, "onUpdateTask: just update");
                info.setDownloadedInBytes(downloaded);
                info.setFileContentLength(fileContentLength);
                info.setSpeedInBytes(speed);

                if(info.isProgressSupport()!=progress_support) {
                    info.setProgressSupport(progress_support);
                    notifyItemChanged(posFound,BIND_PROGRESS_SUPPORT);
                }

                if(info.getProgress()!=progress) {
                    info.setProgress(progress);
                    notifyItemChanged(posFound,BIND_PROGRESS_CHANGED);
                }

                if(info.getState()!=state) {
                    info.setState(state);
                    notifyItemChanged(posFound,BIND_STATE_CHANGED);
                }
            }
            Log.d(TAG, "onUpdateTask posFound = "+posFound);
            return true;
        } else {
            // not in list
            return false;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {

        if(payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else if(holder instanceof TaskInfoItemHolder)
            ((TaskInfoItemHolder) holder).bind((TaskInfo) mData.get(position), payloads);

    }

    void onTaskAdded(TaskInfo info) {
        Log.d(TAG, "onTaskAdded");
        mData.add(1,info);
        notifyItemInserted(1);
        Object section = mData.get(0);
        if(section instanceof CountSectionItem) {
            ((CountSectionItem) section).setCount(((CountSectionItem) section).getCount()+1);
            notifyItemChanged(0);
        }
    }

    void onTaskCleared(int id) {
        int posFound = -1;
        for (int i = 0; i < mData.size(); i++) {
            Object object = mData.get(i);
            if(object instanceof TaskInfo && ((TaskInfo)object).getId()==id) {
               posFound = i;
                break;
            }
        }

        if(posFound!=-1) {
            mData.remove(posFound);
            notifyItemRemoved(posFound);
            Object downloadingSection = mData.get(0);
            if(downloadingSection instanceof CountSectionItem) {
                int downloadingSize= ((CountSectionItem) downloadingSection).getCount();
                if(posFound<=downloadingSize) {
                    ((CountSectionItem) downloadingSection).downCount();
                    notifyItemChanged(0);
                }
                else if(posFound>downloadingSize+1) {
                    Object downloadedSection = mData.get(downloadingSize+1);
                    if(downloadedSection instanceof CountSectionItem) {
                        ((CountSectionItem) downloadedSection).downCount();
                        notifyItemChanged(downloadingSize+1);
                    }
                }
            }
        }
    }

    DownloadAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(List<Object> data) {
        mData.clear();
        mSelectedPos.clear();
        checkMode();
        if (data !=null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    void destroy() {
        mContext = null;
    }

    private static final int TYPE_DOWNLOADING_ITEM = 0;
    private static final int TYPE_SECTION = 1;
    private static final int TYPE_DOWNLOADED_ITEM = 2;

    @Override
    public int getItemViewType(int position) {
      if(mData.get(position) instanceof TaskInfo)  {
          if(((TaskInfo)(mData.get(position))).getSectionState()==TaskInfo.SECTION_DOWNLOADING) return TYPE_DOWNLOADING_ITEM;
          else return TYPE_DOWNLOADED_ITEM;
      } else return TYPE_SECTION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case TYPE_DOWNLOADING_ITEM: return new DownloadingItemHolder(inflater.inflate(R.layout.item_downloading_multipart,parent,false));
            case TYPE_DOWNLOADED_ITEM: return new DownloadedItemHolder(inflater.inflate(R.layout.item_downloaded,parent,false));
            case TYPE_SECTION:
            default: return new SectionItemHolder(inflater.inflate(R.layout.section_text_view,parent,false));
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SectionItemHolder) {
            ((SectionItemHolder) holder).bind(mData.get(position));
        } else if (holder instanceof TaskInfoItemHolder) {
            ((TaskInfoItemHolder) holder).bind((TaskInfo) mData.get(position));
        }
    }

    @Override
    public int getItemCount() {
       return mData.size();
    }
    int getSpanCount(){
        return 2;
    }

    private int[] mPendingOptionIDs = new int[] {
            R.string.cancel,
            R.string.duplicate,
            R.string.warning_divider,
            R.string.clear,
            R.string.focus_divider,
            R.string.properties
    };

    private int[] mRunningOptionIDs = new int[] {
            R.string.pause,
            R.string.duplicate,
            R.string.warning_divider,
            R.string.cancel,
            R.string.focus_divider,
            R.string.properties
    };

    private int[] mPausedOptionIDs = new int[] {
            R.string.resume,
            R.string.restart,
            R.string.duplicate,
            R.string.warning_divider,
            R.string.clear,
            R.string.focus_divider,
            R.string.properties,

    };

    private int[] mStoppedOptionIDs = new int[] {
            R.string.try_to_resume,
            R.string.restart,
            R.string.duplicate,
            R.string.warning_divider,
            R.string.clear,
            R.string.focus_divider,
            R.string.properties
    };
    private int[] mFinishedOptionIDs = new int[] {
            R.string.open,
            R.string.restart,
            R.string.duplicate,
            R.string.warning_divider,
            R.string.clear,
            R.string.focus_divider,
            R.string.properties
    };

    private List<Integer> mSelectedPos = new ArrayList<>();

    private boolean isSelected(int position) {
        try {
            return mSelectedPos.contains(position);
        } catch (Exception e) {
            return false;
        }
    }

    private synchronized void select(int position) {
        try {
            mSelectedPos.add(position);
            checkMode();
            Log.d(TAG, "select "+position);
            notifyItemChanged(position,BIND_SELECT);
        } catch (Exception ignore) {}
    }


    public void goOutSelectMode() {
        List<Integer> temp = new ArrayList<>(mSelectedPos);
        mSelectedPos.clear();

        for (Integer pos :
                temp) {
            notifyItemChanged(pos);
        }
        checkMode();
    }

    private synchronized void removeSelect(int position) {
        try {
            mSelectedPos.remove((Integer)position);
            Log.d(TAG, "remove select "+position);
            notifyItemChanged(position,BIND_SELECT);
        } catch (Exception ignore) {}
    }

    public boolean isInSelectMode() {
        return !mSelectedPos.isEmpty();
    }

    private void onMenuButtonClick(int position, Object object) {
        if(object instanceof TaskInfo) {
            final TaskInfo info = (TaskInfo)object;
            showOptionsForTask(info);
        }
    }

    private void showOptionsForTask(final TaskInfo info) {
        int[] optionIDs;
        switch (info.getState()) {
            case BaseTask.PENDING:
                optionIDs = mPendingOptionIDs;
                break;
            case BaseTask.RUNNING:
            case BaseTask.CONNECTING:
                optionIDs = mRunningOptionIDs;
                break;
            case BaseTask.CANCELLED:
            case BaseTask.FAILURE_TERMINATED:
                optionIDs = mStoppedOptionIDs;
                break;
            case BaseTask.PAUSED:
                optionIDs = mPausedOptionIDs;
                break;

            case BaseTask.SUCCESS:
                optionIDs = mFinishedOptionIDs;
                break;
            default:
                optionIDs = null;
        }

        if(optionIDs!=null && mContext instanceof AppCompatActivity) {
            OptionBottomSheet.newInstance(optionIDs, mOptionTaskInfoCallBack.attach(mContext,info))
                    .show(((AppCompatActivity)mContext).getSupportFragmentManager(),OptionBottomSheet.TAG);
        }
    }
    private OptionTaskInfoCallBack mOptionTaskInfoCallBack = new OptionTaskInfoCallBack();

    public List<Integer> getSelectedTasks() {
        List<Integer> ids = new ArrayList<>();
        for (Integer pos :
                mSelectedPos) {
            Object o = mData.get(pos);
            if(o instanceof TaskInfo)
            ids.add(((TaskInfo) o).getId());
        }
        return ids;
    }


    private static class OptionTaskInfoCallBack implements OptionBottomSheet.CallBack {
        private TaskInfo mActiveTaskInfo;
        private Context mContext;
        OptionBottomSheet.CallBack attach(Context context, TaskInfo info) {
            mActiveTaskInfo = info;
            mContext = context;
            return this;
        }

        void detach() {
            mActiveTaskInfo = null;
            mContext = null;
        }

        @Override
        public boolean onOptionClicked(int optionID) {
            if(mActiveTaskInfo!=null&&mContext instanceof DownloadActivity) {
                Intent intent;
                switch (optionID) {
                    case R.string.open:
                        intent = new Intent(DownloadActivity.ACTION_OPEN_FILE);
                        intent.putExtra(BaseTask.EXTRA_TASK_INFO,mActiveTaskInfo);
                        ((DownloadActivity) mContext).executeWriteStorageAction(intent);
                        break;
                    case R.string.pause:
                        RemoteForTaskService.pauseTaskWithTaskId(mActiveTaskInfo.getId());
                        break;
                    case R.string.resume:
                        intent = new Intent(DownloadActivity.ACTION_RESUME_DOWNLOAD);
                        intent.putExtra(BaseTask.EXTRA_TASK_ID,mActiveTaskInfo.getId());
                        ((DownloadActivity) mContext).executeWriteStorageAction(intent);
                        break;
                    case R.string.cancel:
                        RemoteForTaskService.cancelTaskWithTaskId(mActiveTaskInfo.getId());
                        break;
                    case R.string.clear:
                        RemoteForTaskService.clearTask(mActiveTaskInfo.getId());
                        break;
                    case R.string.restart:
                        intent = new Intent(DownloadActivity.ACTION_RESTART_DOWNLOAD);
                        intent.putExtra(BaseTask.EXTRA_TASK_ID,mActiveTaskInfo.getId());
                        ((DownloadActivity) mContext).executeWriteStorageAction(intent);
                        break;
                    case R.string.duplicate:
                        intent = new Intent(DownloadActivity.ACTION_APPEND_TASK);
                        intent.putExtra(BaseTask.EXTRA_DOWNLOAD_ITEM,new DownloadItem(mActiveTaskInfo.getURLString()));
                        ((DownloadActivity) mContext).executeWriteStorageAction(intent);
                        break;
                    case R.string.try_to_resume:
                        intent = new Intent(DownloadActivity.ACTION_TRY_TO_RESUME);
                        intent.putExtra(BaseTask.EXTRA_TASK_ID,mActiveTaskInfo.getId());
                        ((DownloadActivity) mContext).executeWriteStorageAction(intent);
                        break;
                  /*  case R.string.copy_url_link:
                        if(Util.setClipboard(mContext,mActiveTaskInfo.getFileTitle(),mActiveTaskInfo.getURLString()))
                            Toasty.success(App.getInstance().getApplicationContext(),"Copied").show();
                        else Toasty.error(App.getInstance().getApplicationContext(),"Something went wrong, can not copy this field").show();
                        break;*/
                    case R.string.properties:
                        intent = new Intent(mContext, TaskDetailActivity.class);
                        intent.setAction(TaskDetailActivity.VIEW_TASK_DETAIL);
                        intent.putExtra(BaseTask.EXTRA_TASK_ID, mActiveTaskInfo.getId());
                        mContext.startActivity(intent);
                        break;
                }
                detach();
            }
            return true;
        }
    }

    private void onIconClick(int position, Object object) {
        if(object instanceof TaskInfo && mContext instanceof DownloadActivity) {
            TaskInfo info = ((TaskInfo)object);
            Intent intent;
            switch (info.getState()) {
                case BaseTask.PENDING:
                    Toasty.info(App.getInstance().getApplicationContext(),"Task is pending, please wait");
                    break;
                case BaseTask.PAUSED:
                    intent = new Intent(DownloadActivity.ACTION_RESUME_DOWNLOAD);
                    intent.putExtra(BaseTask.EXTRA_TASK_ID,info.getId());
                    ((DownloadActivity) mContext).executeWriteStorageAction(intent);
                    break;
                case BaseTask.CONNECTING:
                case BaseTask.RUNNING:
                    RemoteForTaskService.pauseTaskWithTaskId(info.getId());
                    break;
                case BaseTask.FAILURE_TERMINATED:
                    intent = new Intent(DownloadActivity.ACTION_RESTART_DOWNLOAD);
                    intent.putExtra(BaseTask.EXTRA_TASK_ID,info.getId());
                    ((DownloadActivity) mContext).executeWriteStorageAction(intent);
                    break;
                case BaseTask.SUCCESS:
                    intent = new Intent(mContext, TaskDetailActivity.class);
                    intent.setAction(TaskDetailActivity.VIEW_TASK_DETAIL);
                    intent.putExtra(BaseTask.EXTRA_TASK_ID,info.getId());
                    mContext.startActivity(intent);
                    break;

            }
        } else
        Toasty.info(App.getInstance().getApplicationContext(),"Icon Clicked but this feature's not written yet :)").show();
    }

    private boolean onItemLongClick(int position, Object object) {
        Util.vibrate();
        if(!isInSelectMode() || !isSelected(position)) {
            select(position);

        } else {
           goOutSelectMode();
        }
        return true;
    }

    private boolean mIsSelectMode = false;

    private void checkMode() {
        boolean newest = isInSelectMode();
        if(newest!=mIsSelectMode) {
            mIsSelectMode = newest;
            if(mContext instanceof DownloadActivity)
            if(mIsSelectMode) ((DownloadActivity)mContext).switchToSelectMode();
            else ((DownloadActivity)mContext).switchToNormalMode();
        }
    }

    private void onItemClick(int position, Object object) {
        // Check if adapter is in select mode
        if(isInSelectMode()) {
            if(!isSelected(position)) select(position);
            else removeSelect(position);

            checkMode();
            return;
        }
       if(object instanceof TaskInfo && mContext instanceof Activity && ((TaskInfo)object).getState()== BaseTask.SUCCESS) {
           TaskInfo info = (TaskInfo) object;
           Intent intent = new Intent(DownloadActivity.ACTION_OPEN_FILE);
           intent.putExtra(BaseTask.EXTRA_TASK_INFO,info);
           ((DownloadActivity) mContext).executeWriteStorageAction(intent);

       } else if(object instanceof TaskInfo) {
           Intent intent = new Intent(mContext, TaskDetailActivity.class);
           intent.setAction(TaskDetailActivity.VIEW_TASK_DETAIL);
           intent.putExtra(BaseTask.EXTRA_TASK_ID,((TaskInfo)object).getId());
           mContext.startActivity(intent);
       }
    }

    public class SectionItemHolder extends RecyclerView.ViewHolder {
        TextView mTitle;
        TextView mCount;

        SectionItemHolder(View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.title);
            mCount = itemView.findViewById(R.id.number);
        }
        public void bind(Object object) {
            if(object instanceof CountSectionItem) {
                mTitle.setText(((CountSectionItem) object).getTitle());
                mCount.setText(String.valueOf(((CountSectionItem) object).getCount()));
            } else if(object instanceof String) {
                mTitle.setText((String)object);
            } else mTitle.setText(R.string.invalid_section);
        }
    }

    public class DownloadedItemHolder extends TaskInfoItemHolder {

        @Override
        public void bind(TaskInfo info) {
            super.bind(info);
            long fileSize = info.getDownloadedInBytes();
            long created = info.getCreatedTime();
            String state =
                    Util.humanReadableByteCount(fileSize)+" • "+
                    DateUtils.formatDateTime(mStateTextView.getContext(), created, DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE |
                    DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY);
            mStateTextView.setText(state);
        }

        DownloadedItemHolder(View itemView) {
            super(itemView);
            itemView.findViewById(R.id.clear).setOnClickListener(this);
            itemView.findViewById(R.id.restart).setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            super.onClick(view);
            switch (view.getId()) {
                case R.id.clear:
                    RemoteForTaskService.clearTask(((TaskInfo)mData.get(getAdapterPosition())).getId());
                    break;
                case R.id.restart:
                    Intent intent = new Intent(DownloadActivity.ACTION_RESTART_DOWNLOAD);
                    intent.putExtra(BaseTask.EXTRA_TASK_ID,((TaskInfo)mData.get(getAdapterPosition())).getId());
                    ((DownloadActivity) mContext).executeWriteStorageAction(intent);
                    break;
            }
        }
    }

    public class DownloadingItemHolder extends TaskInfoItemHolder {
        MultipartProgressBar mProgressBar;

        void bindProgress(TaskInfo info) {
            String stateText;
            String speed;
            String MIDDLE_DOT = " • ";
            if(info.getState()==BaseTask.RUNNING) {
                speed =MIDDLE_DOT+ Util.humanReadableByteCount((long) info.getSpeedInBytes())+"/s";
            } else speed = "";

            if(info.isProgressSupport()) {
                int progress  = (int)(info.getProgress()*100);
                mProgressBar.setPercentages(info.getPercentages());
                //mProgressBar.setProgress(progress);
                stateText = progress +"%"+speed+MIDDLE_DOT+ Util.humanReadableByteCount(info.getDownloadedInBytes())+" of "+ Util.humanReadableByteCount(info.getFileContentLength());
            } else stateText = "Downloading"+speed+MIDDLE_DOT+Util.humanReadableByteCount(info.getDownloadedInBytes());

            mStateTextView.setText(stateText);
        }
        void bindProgressSupport(TaskInfo info) {
          //  if(true) return;
            Log.d(TAG, "bind progress support : "+info.isProgressSupport()+" when state is "+BaseTask.getStateName(info.getState()));
            if(info.isProgressSupport()) {
                mProgressBar.setIndeterminate(false);
            } else {
                mProgressBar.setIndeterminate(true);
                switch (info.getState()) {
                    case BaseTask.PENDING:
                    case BaseTask.FAILURE_TERMINATED:
                        break;
                    case BaseTask.RUNNING:
                        mStateTextView.setText(R.string.downloading);
                        break;
                }
            }
        }

        @SuppressLint("SetTextI18n")
        void bindState(TaskInfo info) {
            int progress  = (int)(info.getProgress()*100);

            switch (info.getState()) {
                case BaseTask.PENDING:
                    mProgressBar.setVisibility(View.GONE);
                    mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatGreen));

                    if(progress!=0)
                    mStateTextView.setText(progress+"% • "+mStateTextView.getResources().getString(R.string.pending));
                    else mStateTextView.setText(R.string.pending);
                    mImageView.setColorFilter(mImageView.getResources().getColor(R.color.FlatGreen));
                    mImageView.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
                    break;
                case BaseTask.CONNECTING:
                    mProgressBar.setVisibility(View.VISIBLE);
                    mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatTealBlue));
                    mImageView.setColorFilter(mImageView.getResources().getColor(R.color.FlatTealBlue));
                    mImageView.setImageResource(R.drawable.ic_pause_black_24dp);
                    if(info.isProgressSupport()&&progress>=0 && progress <=100) {
                        mStateTextView.setText(progress + "%" + " • " + mStateTextView.getResources().getString(R.string.connecting));
                        //mProgressBar.setProgress(progress);
                        mProgressBar.setPercentages(info.getPercentages());

                    }
                    else {
                    mStateTextView.setText(R.string.connecting);
                    }
                    break;
                case BaseTask.RUNNING:
                    mProgressBar.setForegroundBarColor(mProgressBar.getResources().getColor(R.color.FlatTealBlue));
                    mProgressBar.setVisibility(View.VISIBLE);
                    mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatTealBlue));
                    mImageView.setColorFilter(mImageView.getResources().getColor(R.color.FlatTealBlue));
                    mImageView.setImageResource(R.drawable.ic_pause_black_24dp);
                    break;
                case BaseTask.PAUSED:
                mProgressBar.setForegroundBarColor(mProgressBar.getResources().getColor(R.color.FlatOrange));
                mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatOrange));
                mImageView.setColorFilter(mImageView.getResources().getColor(R.color.FlatOrange));
                mImageView.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    if(info.isProgressSupport()&&progress>=0 && progress <=100) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        mStateTextView.setText(progress+"%"+" • "+mStateTextView.getResources().getString(R.string.paused));
                    } else {
                        mProgressBar.setVisibility(View.GONE);
                        mStateTextView.setText(R.string.paused);
                    }
                break;
                case BaseTask.CANCELLED:
                    mProgressBar.setForegroundBarColor(mProgressBar.getResources().getColor(R.color.FocusColorTwo));
                    mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FocusColorTwo));
                    mImageView.setColorFilter(mImageView.getResources().getColor(R.color.FocusColorTwo));
                    mImageView.setImageResource(R.drawable.ic_refresh_black_24dp);
                    if(info.isProgressSupport()&&progress>=0 && progress <=100) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        mStateTextView.setText(progress+"%"+" • "+mStateTextView.getResources().getString(R.string.cancelled));
                    } else {
                        mProgressBar.setVisibility(View.GONE);
                        mStateTextView.setText(R.string.cancelled);
                    }
                    break;
                case BaseTask.FAILURE_TERMINATED:
                    mProgressBar.setForegroundBarColor(mProgressBar.getResources().getColor(R.color.FlatRed));
                    mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatRed));
                    mImageView.setColorFilter(mImageView.getResources().getColor(R.color.FocusColorTwo));
                    mImageView.setImageResource(R.drawable.ic_refresh_black_24dp);
                    if(info.isProgressSupport()&&progress>=0 && progress <=100) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        mStateTextView.setText(progress+"%"+" • "+mStateTextView.getResources().getString(R.string.failure)+", tap to see detail");
                    } else {
                        mProgressBar.setVisibility(View.GONE);
                        mStateTextView.setText(mStateTextView.getResources().getString(R.string.failure)+", tap to see detail");
                    }
                    break;
            }
        }

        @Override
        public void bind(TaskInfo info) {
            super.bind(info);
            bindProgressSupport(info);
            bindProgress(info);
            bindState(info);
        }

        @Override
        public void bind(TaskInfo info, List<Object> payloads) {
            super.bind(info, payloads);
            for (Object object :
                    payloads) {
                if(object instanceof Integer) {
                    switch ((int)object) {
                        case BIND_INFO:
                            bindInfo(info);
                            break;
                        case BIND_PROGRESS_SUPPORT:
                            bindProgressSupport(info);
                            break;
                        case BIND_PROGRESS_CHANGED:
                            bindProgress(info);
                            break;
                        case BIND_STATE_CHANGED:
                            bindState(info);
                            break;

                    }
                }
            }
        }

        DownloadingItemHolder(View itemView) {
            super(itemView);
            mProgressBar = itemView.findViewById(R.id.progress_bar);
        }
    }

    public abstract class TaskInfoItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView mTitleTextView;
        TextView mStateTextView;
        TextView mDescriptionTextView;
        ImageView mImageView;

        TaskInfoItemHolder(View itemView) {
            super(itemView);
            mTitleTextView = itemView.findViewById(R.id.title);
            mStateTextView = itemView.findViewById(R.id.state_text_view);
            mDescriptionTextView = itemView.findViewById(R.id.description);
            mImageView = itemView.findViewById(R.id.image_view);
            mImageView.setOnClickListener(this);
            itemView.findViewById(R.id.menu_button).setOnClickListener(this);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void bind(TaskInfo info) {
                bindInfo(info);
                bindSelect();
        }

        public void bind(TaskInfo info, List<Object> payloads) {
            for (Object object :
                    payloads) {
                if (object instanceof Integer) {
                    switch ((int) object) {
                        case BIND_SELECT:
                            bindSelect();
                            break;
                    }
                }
            }
        }

        final void bindInfo(TaskInfo info) {
            mTitleTextView.setText(info.getFileTitle());
            mDescriptionTextView.setText(info.getURLString());
        }

        private void bindSelect() {
            Log.d(TAG, "receive bind select command: selected "+isSelected(getAdapterPosition()));
            if(isSelected(getAdapterPosition())) {
                itemView.setBackgroundResource(R.drawable.background_item_user_data_with_border);
            } else {
                itemView.setBackgroundResource(R.drawable.background_item_user_data);
            }
        }


        @Override
        public void onClick(View view) {
            if(view==itemView) onItemClick(getAdapterPosition(),mData.get(getAdapterPosition()));
            else
            switch (view.getId()) {
                case R.id.menu_button: onMenuButtonClick(getAdapterPosition(), mData.get(getAdapterPosition()));
                break;
                case R.id.image_view: onIconClick(getAdapterPosition(),mData.get(getAdapterPosition()));
                break;
            }
        }

        @Override
        public boolean onLongClick(View view) {
            Log.d(TAG, "detect long click item "+getAdapterPosition());
            return onItemLongClick(getAdapterPosition(),mData.get(getAdapterPosition()));
        }
    }
}