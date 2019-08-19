package com.zalo.trainingmenu.downloader.ui.setting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zalo.trainingmenu.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FolderChooserAdapter extends RecyclerView.Adapter<FolderChooserAdapter.ItemHolder> {
    private ArrayList<String> mData = new ArrayList<>();

    public void setData(List<String> data) {
        mData.clear();
        if(data!=null) mData.addAll(data);
        notifyDataSetChanged();
    }

    public void setData(String[] data) {
        int size = mData.size();
        mData.clear();
        if(data!=null) mData.addAll(Arrays.asList(data));
        notifyDataSetChanged();
    }

    public void destroy() {
        mListener = null;
    }

    interface OnClickFolderItem {
        void onClickFolderItem(int position, String name);
    }

    public void removeOnClickFolderListener() {
        mListener = null;
    }

    public void setOnClickFolderListener(OnClickFolderItem listener) {
        mListener = listener;
    }

    private OnClickFolderItem mListener;

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        holder.bind(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView mImage;
        TextView mTitle;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.image);
            mTitle = itemView.findViewById(R.id.title);
            itemView.setOnClickListener(this);
        }

        public void bind(String name) {
            mTitle.setText(name);
            int tenDP = (int) (mTitle.getResources().getDimension(R.dimen.oneDp)*10);
            if("..".equals(name)) {
                mImage.setBackground(null);
                mImage.setImageResource(R.drawable.ic_arrow_back_24dp);
            }
            else {
                mImage.setBackgroundResource(R.drawable.background_folder_icon);
                mImage.setImageResource(R.drawable.ic_folder_open_black_24dp);
            }
            mImage.setPadding(tenDP,tenDP,tenDP,tenDP);

        }

        @Override
        public void onClick(View view) {
            if(mListener!=null) mListener.onClickFolderItem(getAdapterPosition(), mData.get(getAdapterPosition()));
        }
    }
}
