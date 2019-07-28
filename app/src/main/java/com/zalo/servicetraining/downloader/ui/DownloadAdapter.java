package com.zalo.servicetraining.downloader.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zalo.servicetraining.R;
import com.zalo.servicetraining.downloader.base.AbsTask;
import com.zalo.servicetraining.downloader.model.TaskInfo;

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
                        case BIND_PROGRESS_CHANGED:
                            ((DownloadingItemHolder)holder).bindProgress((TaskInfo) mData.get(position));
                            break;
                        case BIND_STATE_CHANGED:
                            ((DownloadingItemHolder)holder).bindState((TaskInfo) mData.get(position));
                            break;
                        case BIND_PROGRESS_SUPPORT:
                            ((DownloadingItemHolder)holder).bindProgressSupport((TaskInfo) mData.get(position));
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

    public void onItemClick(int position, Object object) {
        Toasty.info(mContext,"Clicked but this feature's not written yet :)").show();
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
                    Toasty.info(mTitleTextView.getContext(),"Restart!").show();
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

        public void bindState(TaskInfo info) {
            switch (info.getState()) {
                case AbsTask.PENDING:
                    mProgressBar.setVisibility(View.GONE);
                    mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatGreen));
                    mStateTextView.setText(R.string.pending);
                    break;
                case AbsTask.RUNNING:
                    mProgressBar.setVisibility(View.VISIBLE);
                     mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatTealBlue));

                    break;
                case AbsTask.FAILURE_TERMINATED:
                    mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatRed));

                    if(mProgressBar.isIndeterminate()) {
                        mProgressBar.setVisibility(View.GONE);
                        mStateTextView.setText(R.string.failure);
                    } else
                        mProgressBar.setVisibility(View.VISIBLE);
                    break;
            }
        }


        @Override
        public void bind(TaskInfo info) {
            super.bind(info);
            int progress  = (int)(info.getProgress()*100);

           switch (info.getState()) {
               case AbsTask.PENDING:
                   mProgressBar.setVisibility(View.GONE);
                   mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatGreen));
                   mStateTextView.setText(R.string.pending);
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
                   break;
               case AbsTask.FAILURE_TERMINATED:
                   mProgressBar.setVisibility(View.VISIBLE);
                   mStateTextView.setTextColor(mStateTextView.getResources().getColor(R.color.FlatRed));
                   if(info.isProgressSupport()&&progress>=0&&progress <=100)  {
                       mProgressBar.setIndeterminate(false);
                       mProgressBar.setProgress(progress);
                       mStateTextView.setText(progress+"%");
                   } else {
                       mProgressBar.setVisibility(View.GONE);
                       mStateTextView.setText(R.string.failure);
                   }
                   break;
           }
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

        TaskInfoItemHolder(View itemView) {
            super(itemView);
            mTitleTextView = itemView.findViewById(R.id.title);
            mStateTextView = itemView.findViewById(R.id.state_text_view);
            mDescriptionTextView = itemView.findViewById(R.id.description);
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
            }
        }
    }
}