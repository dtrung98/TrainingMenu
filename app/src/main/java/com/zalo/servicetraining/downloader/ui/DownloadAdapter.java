package com.zalo.servicetraining.downloader.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zalo.servicetraining.R;
import com.zalo.servicetraining.downloader.base.AbsTask;
import com.zalo.servicetraining.downloader.model.TaskInfo;
import com.zalo.servicetraining.downloader.service.DownloaderRemote;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class DownloadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "DownloadAdapter";

    private static final int BIND_PROGRESS_CHANGED = 1;
    private static final int BIND_STATE_CHANGED = 2;
    private static final int BIND_PROGRESS_SUPPORT = 3;

    Context mContext;
    private ArrayList<Object> mData = new ArrayList<>();

    public List<Object> getData() {
        return mData;
    }

    GridLayoutManager.SpanSizeLookup mSpanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
        @Override
        public int getSpanSize(int position) {
            int type = getItemViewType(position);
            if(type == TYPE_DOWNLOADING_ITEM) return 2;
            else if(type== TYPE_SECTION) return 2;
            else return 2;
        }
    };

    public GridLayoutManager.SpanSizeLookup getSpanSizeLookup() {
        return mSpanSizeLookup;
    }

    public boolean onTaskUpdated(int id, int state, float progress, boolean progress_support) {
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
            if ((info.getState() != AbsTask.SUCCESS && state == AbsTask.SUCCESS) || (info.getState() == AbsTask.SUCCESS && state != AbsTask.SUCCESS)) {
                if(mContext instanceof DownloaderActivity) {
                    Log.d(TAG, "onTaskUpdated: need to refresh");
                    ((DownloaderActivity) mContext).refreshData();
                }
            } else {
               // info.setState(state).setProgress(progress).setProgressSupport(progress_support);
                Log.d(TAG, "onTaskUpdated: just update");

                if(info.getState()!=state) {
                    info.setState(state);
                    notifyItemChanged(posFound,BIND_STATE_CHANGED);
                }

                if(info.getProgress()!=progress) {
                    info.setProgress(progress);
                    notifyItemChanged(posFound,BIND_PROGRESS_CHANGED);
                }

                if(info.isProgressSupport()!=progress_support) {
                    info.setProgressSupport(progress_support);
                    notifyItemChanged(posFound,BIND_PROGRESS_SUPPORT);
                }


              /*  if(info.getState()==state&&info.getProgress()==progress) return true;
                else if(info.getState()!=state) {
                    info.setState(state);
                    notifyItemChanged(posFound,ONLY_STATE_CHANGED);
                } else if(info.getProgress()!=progress) {
                    info.setProgress(progress);
                    notifyItemChanged(posFound,ONLY_PROGRESS_CHANGED);
                }
                notifyItemChanged(posFound, PROGRESS_N_STATE_CHANGED);*/
            }
            Log.d(TAG, "onTaskUpdated posFound = "+posFound);
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

    public void onTaskAdded(TaskInfo info) {
        Log.d(TAG, "onTaskAdded");
        mData.add(1,info);
        notifyItemInserted(1);
    }

    public interface ItemClickListener {
        void onItemClick(Object object);
    }

    private ItemClickListener mListener;
    public void setListener(ItemClickListener listener) {
        mListener = listener;
    }
    public void removeListener() {
        mListener = null;
    }

    public DownloadAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(List<Object> data) {
        mData.clear();
        if (data !=null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void destroy() {
        mContext = null;
        mListener = null;
    }

    public static final int TYPE_DOWNLOADING_ITEM = 0;
    public static final int TYPE_SECTION = 1;
    public static final int TYPE_DOWNLOADED_ITEM = 2;

    @Override
    public int getItemViewType(int position) {
      if(mData.get(position) instanceof TaskInfo)  {
          if(((TaskInfo)(mData.get(position))).getSectionState()==TaskInfo.STATE_DOWNLOADING) return TYPE_DOWNLOADING_ITEM;
          else return TYPE_DOWNLOADED_ITEM;
      } else return TYPE_SECTION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public int getSpanCount(){
        return 2;
    }


    public void onMenuButtonClick(int position, Object object) {
        Toasty.info(mContext,"Menu Clicked but this feature's not written yet :)").show();

    }
    public void onIconClick(int position, Object object) {
        if(object instanceof TaskInfo) {
            TaskInfo info = ((TaskInfo)object);
            switch (info.getState()) {
                case AbsTask.PENDING:
                    Toasty.info(mContext,"Task is pending, please wait");
                    break;
                case AbsTask.PAUSED:
                    DownloaderRemote.resumeTaskWithTaskId(info.getId());
                    break;
                case AbsTask.CONNECTING:
                case AbsTask.RUNNING:
                    DownloaderRemote.pauseTaskWithTaskId(info.getId());
                    break;
                case AbsTask.FAILURE_TERMINATED:
                    DownloaderRemote.restartTaskWithTaskId(info.getId());

            }
        } else
        Toasty.info(mContext,"Icon Clicked but this feature's not written yet :)").show();

    }

    public void onItemClick(int position, Object object) {
       if(object instanceof TaskInfo && mContext instanceof Activity && ((TaskInfo)object).getState()==AbsTask.SUCCESS) {
           TaskInfo info = (TaskInfo) object;

          try {
              File filePath = new File(info.getDownloadItem().getDirectoryPath());
              File fileToWrite = new File(filePath, info.getDownloadItem().getTitle());
              final Uri data = FileProvider.getUriForFile(mContext, "com.zalo.servicetraining.provider", fileToWrite);
              mContext.grantUriPermission(mContext.getPackageName(), data, Intent.FLAG_GRANT_READ_URI_PERMISSION);
              String fileExtension = info.getDownloadItem().getTitle().substring(info.getDownloadItem().getTitle().lastIndexOf("."));
              Log.d(TAG, "onItemClick: extension " + fileExtension);
              final Intent intent = new Intent(Intent.ACTION_VIEW);
              if (fileExtension.contains("apk")) {
                  Log.d(TAG, "open as apk");
                  intent.setDataAndType(data, "application/vnd.android.package-archive");
             // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              }
              else
              intent.setData(data);
              intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
              mContext.startActivity(intent);
          } catch (ActivityNotFoundException e) {
              Toasty.error(mContext,"Not found any app that could open this file").show();
          } catch (Exception e) {
              Toasty.error(mContext,"Sorry, something went wrong").show();
          }
       }
    }



    public class SectionItemHolder extends RecyclerView.ViewHolder {
        TextView mTextView;

        public SectionItemHolder(View itemView) {
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
                    Toasty.info(mTitleTextView.getContext(),"Clear!").show();
                    break;
                case R.id.restart:
                    DownloaderRemote.restartTaskWithTaskId(((TaskInfo)mData.get(getAdapterPosition())).getId());
                    break;
            }
        }
    }

    public class DownloadingItemHolder extends TaskInfoItemHolder {
        ProgressBar mProgressBar;

        public void bindProgress(TaskInfo info) {
            int progress  = (int)(info.getProgress()*100);
            mProgressBar.setProgress(progress);
            if(!mProgressBar.isIndeterminate())
                mStateTextView.setText(progress+"%");

        }
        public void bindProgressSupport(TaskInfo info) {
            if(info.isProgressSupport()) {
                mProgressBar.setIndeterminate(false);

            } else {
                mProgressBar.setIndeterminate(true);
                switch (info.getState()) {
                    case AbsTask.PENDING:
                    case AbsTask.FAILURE_TERMINATED:
                        break;
                    case AbsTask.RUNNING:
                        mStateTextView.setText(R.string.downloading);
                        break;
                }
            }
        }

        @SuppressLint("SetTextI18n")
        public void bindState(TaskInfo info) {
            int progress  = (int)(info.getProgress()*100);

            switch (info.getState()) {
                case AbsTask.PENDING:
                    mProgressBar.setVisibility(View.GONE);
                    mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatGreen));
                    mStateTextView.setText(R.string.pending);
                    mImageView.setColorFilter(mImageView.getResources().getColor(R.color.FlatGreen));
                    mImageView.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
                    break;
                case AbsTask.CONNECTING:
                    mProgressBar.setVisibility(View.VISIBLE);
                    mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatTealBlue));
                    mImageView.setColorFilter(mImageView.getResources().getColor(R.color.FlatTealBlue));
                    mImageView.setImageResource(R.drawable.ic_pause_black_24dp);
                    if(info.isProgressSupport()&&progress>=0 && progress <=100)
                        mStateTextView.setText(progress+"%"+", "+mStateTextView.getResources().getString(R.string.connecting));
                    else {
                    mStateTextView.setText(R.string.connecting);
                    }
                    break;
                case AbsTask.RUNNING:
                    mProgressBar.setProgressTintList(ColorStateList.valueOf(mProgressBar.getResources().getColor(R.color.FlatTealBlue)));
                    mProgressBar.setVisibility(View.VISIBLE);
                    mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatTealBlue));
                    mImageView.setColorFilter(mImageView.getResources().getColor(R.color.FlatTealBlue));
                    mImageView.setImageResource(R.drawable.ic_pause_black_24dp);
                    break;
                case AbsTask.PAUSED:
                mProgressBar.setProgressTintList(ColorStateList.valueOf(mProgressBar.getResources().getColor(R.color.FlatOrange)));
                mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatOrange));
                mImageView.setColorFilter(mImageView.getResources().getColor(R.color.FlatOrange));
                mImageView.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    if(info.isProgressSupport()&&progress>=0 && progress <=100) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        mStateTextView.setText(progress+"%, "+mStateTextView.getResources().getString(R.string.paused));
                    } else {
                        mProgressBar.setVisibility(View.GONE);
                        mStateTextView.setText(R.string.paused);
                    }
                break;
                case AbsTask.CANCELLED:
                    mProgressBar.setProgressTintList(ColorStateList.valueOf(mProgressBar.getResources().getColor(R.color.FocusColorTwo)));
                    mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FocusColorTwo));
                    mImageView.setColorFilter(mImageView.getResources().getColor(R.color.FocusColorTwo));
                    mImageView.setImageResource(R.drawable.ic_refresh_black_24dp);
                    if(info.isProgressSupport()&&progress>=0 && progress <=100) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        mStateTextView.setText(progress+"%, "+mStateTextView.getResources().getString(R.string.cancelled));
                    } else {
                        mProgressBar.setVisibility(View.GONE);
                        mStateTextView.setText(R.string.cancelled);
                    }
                    break;
                case AbsTask.FAILURE_TERMINATED:
                    mProgressBar.setProgressTintList(ColorStateList.valueOf(mProgressBar.getResources().getColor(R.color.FlatRed)));
                    mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatRed));
                    mImageView.setColorFilter(mImageView.getResources().getColor(R.color.FocusColorTwo));
                    mImageView.setImageResource(R.drawable.ic_refresh_black_24dp);
                    if(info.isProgressSupport()&&progress>=0 && progress <=100) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        mStateTextView.setText(progress+"%, Failure");
                    } else {
                        mProgressBar.setVisibility(View.GONE);
                        mStateTextView.setText(R.string.failure);
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
               case AbsTask.PENDING:
                   mProgressBar.setVisibility(View.GONE);
                   mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatGreen));
                   mStateTextView.setText(R.string.pending);
                   mImageView.setColorFilter(mImageView.getResources().getColor(R.color.FlatGreen));
                   mImageView.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
                   break;
               case AbsTask.CONNECTING:
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
               case AbsTask.RUNNING:
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
               case AbsTask.PAUSED:
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
                   case AbsTask.CANCELLED:
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
               case AbsTask.FAILURE_TERMINATED:
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
            if(info.getDownloadItem()!=null) {
                mTitleTextView.setText(info.getDownloadItem().getTitle());
                mDescriptionTextView.setText(info.getDownloadItem().getUrlString());
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
    }
}