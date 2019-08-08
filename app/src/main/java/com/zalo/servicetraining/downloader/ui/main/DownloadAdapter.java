package com.zalo.servicetraining.downloader.ui.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zalo.servicetraining.App;
import com.zalo.servicetraining.R;
import com.zalo.servicetraining.downloader.base.BaseTask;
import com.zalo.servicetraining.downloader.model.TaskInfo;
import com.zalo.servicetraining.downloader.service.DownloaderRemote;
import com.zalo.servicetraining.downloader.ui.base.OptionBottomSheet;
import com.zalo.servicetraining.downloader.ui.detail.TaskDetailActivity;
import com.zalo.servicetraining.util.Util;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class DownloadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "DownloadAdapter";

    private static final int BIND_PROGRESS_CHANGED = 1;
    private static final int BIND_STATE_CHANGED = 2;
    private static final int BIND_PROGRESS_SUPPORT = 3;

    private Context mContext;
    private ArrayList<Object> mData = new ArrayList<>();

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
        } else if(holder instanceof DownloadingItemHolder )
            for (Object object :
                    payloads) {
                if(object instanceof Integer) {
                    switch ((int)object) {
                        case BIND_PROGRESS_SUPPORT:
                            ((DownloadingItemHolder)holder).bindProgressSupport((TaskInfo) mData.get(position));
                            break;
                        case BIND_PROGRESS_CHANGED:
                            ((DownloadingItemHolder)holder).bindProgress((TaskInfo) mData.get(position));
                            break;
                        case BIND_STATE_CHANGED:
                            ((DownloadingItemHolder)holder).bindState((TaskInfo) mData.get(position));
                            break;

                    }
                }
            }
    }

    void onTaskAdded(TaskInfo info) {
        Log.d(TAG, "onTaskAdded");
        mData.add(1,info);
        notifyItemInserted(1);
    }

    void onTaskCleared(int id) {
        for (int i = 0; i < mData.size(); i++) {
            Object object = mData.get(i);
            if(object instanceof TaskInfo && ((TaskInfo)object).getId()==id) {
                mData.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    DownloadAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(List<Object> data) {
        mData.clear();
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
            case TYPE_DOWNLOADING_ITEM: return new DownloadingItemHolder(inflater.inflate(R.layout.item_downloading,parent,false));
            case TYPE_DOWNLOADED_ITEM: return new DownloadedItemHolder(inflater.inflate(R.layout.item_downloaded,parent,false));
            case TYPE_SECTION:
            default: return new SectionItemHolder(inflater.inflate(R.layout.section_text_view,parent,false));
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
       if(holder instanceof SectionItemHolder) {
           ((SectionItemHolder) holder).bind(mData.get(position));
       } else if(holder instanceof DownloadedItemHolder) {
           ((DownloadedItemHolder) holder).bind((TaskInfo) mData.get(position));
       } else if(holder instanceof DownloadingItemHolder) {
           ((DownloadingItemHolder) holder).bind((TaskInfo) mData.get(position));
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
            R.string.warning_divider,
            R.string.clear,
            R.string.focus_divider,
            R.string.properties
    };

    private int[] mRunningOptionIDs = new int[] {
            R.string.pause,
            R.string.warning_divider,
            R.string.cancel,
            R.string.focus_divider,
            R.string.properties
    };

    private int[] mPausedOptionIDs = new int[] {
            R.string.resume,
            R.string.restart,
            R.string.warning_divider,
            R.string.clear,
            R.string.focus_divider,
            R.string.properties,

    };

    private int[] mStoppedOptionIDs = new int[] {
            R.string.restart,
            R.string.warning_divider,
            R.string.clear,
            R.string.focus_divider,
            R.string.properties
    };
    private int[] mFinishedOptionIDs = new int[] {
            R.string.open,
            R.string.restart,
            R.string.warning_divider,
            R.string.clear,
            R.string.focus_divider,
            R.string.properties
    };
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
            if(mActiveTaskInfo!=null&&mContext!=null) {
                switch (optionID) {
                    case R.string.open:
                        DownloaderRemote.openFinishedTaskInfo(mContext, mActiveTaskInfo);
                        break;
                    case R.string.pause:
                        DownloaderRemote.pauseTaskWithTaskId(mActiveTaskInfo.getId());
                        break;
                    case R.string.resume:
                        DownloaderRemote.resumeTaskWithTaskId(mActiveTaskInfo.getId());
                        break;
                    case R.string.cancel:
                        DownloaderRemote.cancelTaskWithTaskId(mActiveTaskInfo.getId());
                        break;
                    case R.string.clear:
                        DownloaderRemote.clearTask(mActiveTaskInfo.getId());
                        break;
                    case R.string.restart:
                        DownloaderRemote.restartTaskWithTaskId(mActiveTaskInfo.getId());
                        break;
                    case R.string.properties:
                        Intent intent = new Intent(mContext, TaskDetailActivity.class);
                        intent.setAction(TaskDetailActivity.VIEW_TASK_DETAIL);
                        intent.putExtra(BaseTask.EXTRA_TASK_ID, mActiveTaskInfo.getId());
                        mContext.startActivity(intent);
                        break;
                }
                detach();
            }
            return true;
        }
    };

    private void onIconClick(int position, Object object) {
        if(object instanceof TaskInfo) {
            TaskInfo info = ((TaskInfo)object);
            switch (info.getState()) {
                case BaseTask.PENDING:
                    Toasty.info(App.getInstance().getApplicationContext(),"Task is pending, please wait");
                    break;
                case BaseTask.PAUSED:
                    DownloaderRemote.resumeTaskWithTaskId(info.getId());
                    break;
                case BaseTask.CONNECTING:
                case BaseTask.RUNNING:
                    DownloaderRemote.pauseTaskWithTaskId(info.getId());
                    break;
                case BaseTask.FAILURE_TERMINATED:
                    DownloaderRemote.restartTaskWithTaskId(info.getId());
                    break;
                case BaseTask.SUCCESS:
                    Intent intent = new Intent(mContext, TaskDetailActivity.class);
                    intent.setAction(TaskDetailActivity.VIEW_TASK_DETAIL);
                    intent.putExtra(BaseTask.EXTRA_TASK_ID,info.getId());
                    mContext.startActivity(intent);
                    break;

            }
        } else
        Toasty.info(App.getInstance().getApplicationContext(),"Icon Clicked but this feature's not written yet :)").show();
    }

    private void onItemClick(int position, Object object) {
       if(object instanceof TaskInfo && mContext instanceof Activity && ((TaskInfo)object).getState()== BaseTask.SUCCESS) {
           TaskInfo info = (TaskInfo) object;
           DownloaderRemote.openFinishedTaskInfo(mContext, info);

       } else if(object instanceof TaskInfo) {
           Intent intent = new Intent(mContext, TaskDetailActivity.class);
           intent.setAction(TaskDetailActivity.VIEW_TASK_DETAIL);
           intent.putExtra(BaseTask.EXTRA_TASK_ID,((TaskInfo)object).getId());
           mContext.startActivity(intent);
       }
    }

    public class SectionItemHolder extends RecyclerView.ViewHolder {
        TextView mTextView;

        SectionItemHolder(View itemView) {
            super(itemView);
            mTextView = (TextView)itemView;

        }
        public void bind(Object object) {
            if(object instanceof String) {
                mTextView.setText((String)object);
            } else mTextView.setText(R.string.invalid_section);
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
                    DownloaderRemote.clearTask(((TaskInfo)mData.get(getAdapterPosition())).getId());
                    break;
                case R.id.restart:
                    DownloaderRemote.restartTaskWithTaskId(((TaskInfo)mData.get(getAdapterPosition())).getId());
                    break;
            }
        }
    }

    public class DownloadingItemHolder extends TaskInfoItemHolder {
        ProgressBar mProgressBar;

        void bindProgress(TaskInfo info) {
            String stateText;
            String speed;
            String MIDDLE_DOT = " • ";
            if(info.getState()==BaseTask.RUNNING) {
                speed =MIDDLE_DOT+ Util.humanReadableByteCount((long) info.getSpeedInBytes())+"/s";
            } else speed = "";

            if(info.isProgressSupport()) {
                int progress  = (int)(info.getProgress()*100);
                mProgressBar.setProgress(progress);
                stateText = progress +"%"+speed+MIDDLE_DOT+ Util.humanReadableByteCount(info.getDownloadedInBytes())+" of "+ Util.humanReadableByteCount(info.getFileContentLength());
            } else stateText = "Downloading"+speed+MIDDLE_DOT+Util.humanReadableByteCount(info.getDownloadedInBytes());

            mStateTextView.setText(stateText);
        }
        void bindProgressSupport(TaskInfo info) {
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
                        mProgressBar.setProgress(progress);
                    }
                    else {
                    mStateTextView.setText(R.string.connecting);
                    }
                    break;
                case BaseTask.RUNNING:
                    mProgressBar.setProgressTintList(ColorStateList.valueOf(mProgressBar.getResources().getColor(R.color.FlatTealBlue)));
                    mProgressBar.setVisibility(View.VISIBLE);
                    mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatTealBlue));
                    mImageView.setColorFilter(mImageView.getResources().getColor(R.color.FlatTealBlue));
                    mImageView.setImageResource(R.drawable.ic_pause_black_24dp);
                    break;
                case BaseTask.PAUSED:
                mProgressBar.setProgressTintList(ColorStateList.valueOf(mProgressBar.getResources().getColor(R.color.FlatOrange)));
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
                    mProgressBar.setProgressTintList(ColorStateList.valueOf(mProgressBar.getResources().getColor(R.color.FocusColorTwo)));
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
                    mProgressBar.setProgressTintList(ColorStateList.valueOf(mProgressBar.getResources().getColor(R.color.FlatRed)));
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


         /*   if(true) return;
            int progress  = (int)(info.getProgress()*100);

           switch (info.getState()) {
               case BaseTask.PENDING:
                   mProgressBar.setVisibility(View.GONE);
                   mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatGreen));
                   mStateTextView.setText(R.string.pending);
                   mImageView.setColorFilter(mImageView.getResources().getColor(R.color.FlatGreen));
                   mImageView.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
                   break;
               case BaseTask.CONNECTING:
                   mProgressBar.setVisibility(View.VISIBLE);
                   if(info.isProgressSupport()&&progress>=0 && progress <=100) {
                       mProgressBar.setIndeterminate(false);
                       mProgressBar.setProgress(progress);
                       mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatTealBlue));
                       mStateTextView.setText(progress+"%"+", "+mStateTextView.getResources().getString(R.string.connecting));
                   } else {
                       mProgressBar.setIndeterminate(true);
                       mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatTealBlue));
                       mStateTextView.setText(R.string.connecting);
                   }
                   mImageView.setColorFilter(mImageView.getResources().getColor(R.color.FlatTealBlue));
                   mImageView.setImageResource(R.drawable.ic_pause_black_24dp);
               break;
               case BaseTask.RUNNING:
                   mProgressBar.setVisibility(View.VISIBLE);
                   if(info.isProgressSupport()&&progress>=0 && progress <=100) {
                       mProgressBar.setIndeterminate(false);
                       mProgressBar.setProgress(progress);
                       mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatTealBlue));
                       mStateTextView.setText(progress+"%");
                   } else {
                       mProgressBar.setIndeterminate(true);
                       mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatTealBlue));
                       mStateTextView.setText(R.string.downloading);
                   }
                   mImageView.setColorFilter(mImageView.getResources().getColor(R.color.FlatTealBlue));
                   mImageView.setImageResource(R.drawable.ic_pause_black_24dp);
                   break;
               case BaseTask.PAUSED:
                   mProgressBar.setVisibility(View.VISIBLE);
                   mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatOrange));
                   if(info.isProgressSupport()&&progress>=0&&progress <=100)  {
                       mProgressBar.setIndeterminate(false);
                       mProgressBar.setProgress(progress);
                       mStateTextView.setText(progress+"%, Paused");
                   } else {
                       mProgressBar.setVisibility(View.GONE);
                       mStateTextView.setText("Paused");
                   }
                   mImageView.setColorFilter(mImageView.getResources().getColor(R.color.FlatOrange));
                   mImageView.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                   break;
                   case BaseTask.CANCELLED:
                       mProgressBar.setVisibility(View.VISIBLE);
                       mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FocusColorTwo));
                       if(info.isProgressSupport()&&progress>=0&&progress <=100)  {
                           mProgressBar.setIndeterminate(false);
                           mProgressBar.setProgress(progress);
                           mStateTextView.setText(progress+"%, Cancelled");
                       } else {
                           mProgressBar.setVisibility(View.GONE);
                           mStateTextView.setText("Cancelled");
                       }
                       mImageView.setColorFilter(mImageView.getResources().getColor(R.color.FocusColorTwo));
                       mImageView.setImageResource(R.drawable.ic_refresh_black_24dp);
                       break;
               case BaseTask.FAILURE_TERMINATED:
                   mProgressBar.setVisibility(View.VISIBLE);
                   mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatRed));
                   if(info.isProgressSupport()&&progress>=0&&progress <=100)  {
                       mProgressBar.setIndeterminate(false);
                       mProgressBar.setProgress(progress);
                       mStateTextView.setText(progress+"%, Failure");
                   } else {
                       mProgressBar.setVisibility(View.GONE);
                       mStateTextView.setText(R.string.failure);
                   }
                   mImageView.setColorFilter(mImageView.getResources().getColor(R.color.FocusColorTwo));
                   mImageView.setImageResource(R.drawable.ic_refresh_black_24dp);
                   break;
           }*/
        }

        DownloadingItemHolder(View itemView) {
            super(itemView);
            mProgressBar = itemView.findViewById(R.id.progress_bar);
        }
    }

    public abstract class TaskInfoItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
        }

        public void bind(TaskInfo info) {
                mTitleTextView.setText(info.getFileTitle());
                mDescriptionTextView.setText(info.getURLString());
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
    }
}