package com.zalo.servicetraining.ui.network;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zalo.servicetraining.R;
import com.zalo.servicetraining.model.LocalArea;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LocalAreaAdapter extends RecyclerView.Adapter<LocalAreaAdapter.MenuItemHolder> {
    private static final String TAG = "LocalAreaAdapter";

    private ArrayList<LocalArea> mData = new ArrayList<>();

    public List<LocalArea> getData() {
        return mData;
    }

    public interface OnItemClickListener {
        void onLocalAreaItemClick(LocalArea item);
    }

    private OnItemClickListener mListener;
    public void setListener(OnItemClickListener listener) {
        mListener = listener;
    }
    public void removeListener() {
        mListener = null;
    }


    public void setData(List<LocalArea> data) {
        mData.clear();
        if (data !=null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public MenuItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

         return new MenuItemHolder(inflater.inflate(R.layout.item_card,parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemHolder holder, int position) {
        holder.bind(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }



    public class MenuItemHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.title)
        TextView mTitle;

        @BindView(R.id.description)
        TextView mDescription;

        public MenuItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @OnClick({R.id.constraint_root})
        void clickPanel() {
            if(mListener!=null) mListener.onLocalAreaItemClick(mData.get(getAdapterPosition()));
        }

        public void bind(LocalArea item) {
            mTitle.setText(item.getLocalizedName());


            if(item.getLocalizedType()!=null&&item.getID()!=null) {
                mDescription.setVisibility(View.VISIBLE);

                String id = item.getID();
                if(id.isEmpty()) mDescription.setText(item.getLocalizedType());
                else mDescription.setText(new StringBuilder().append(item.getLocalizedType()).append(" ( ").append(item.getID()).append(" )").toString());
            }
            else {
                mDescription.setVisibility(View.INVISIBLE);
            }

        }
    }
}
