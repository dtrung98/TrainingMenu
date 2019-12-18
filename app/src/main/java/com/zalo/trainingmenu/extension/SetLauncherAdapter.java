package com.zalo.trainingmenu.extension;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ldt.menulayout.model.Item;
import com.ldt.menulayout.ui.MenuAdapter;
import com.ldt.menulayout.ui.OnItemClickListener;
import com.zalo.trainingmenu.R;

import java.util.ArrayList;
import java.util.List;

public class SetLauncherAdapter extends RecyclerView.Adapter<SetLauncherAdapter.TickableHolder> implements OnItemClickListener {
    public ArrayList<Item> mData = new ArrayList<>();

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<Item> data) {
        mData.clear();
        if (data !=null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    public int mCurrentTickValue = -1;
    public void setCurrentTickValue(int position) {
        int old = mCurrentTickValue;
        mCurrentTickValue = position;
        if(old!=-1 && old < getItemCount()) notifyItemChanged(old);
        notifyItemChanged(mCurrentTickValue);
    }
    @NonNull
    @Override
    public TickableHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new TickableHolder(inflater.inflate(R.layout.item_card_tickable,parent,false), this);

    }

    @Override
    public void onBindViewHolder(@NonNull TickableHolder holder, int position) {
        holder.bind(mData.get(position));
    }

    public void setListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public OnItemClickListener mListener;

    @Override
    public void onEventItemClick(Item item, int position) {
        if(mListener!=null) mListener.onEventItemClick(item, position);
    }

    public class TickableHolder extends MenuAdapter.MenuItemHolder {
        ImageView mTickIcon;
        public TickableHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView, listener);
            mTickIcon = itemView.findViewById(R.id.tick_icon);
        }

        public void bind(Item item) {
            super.bind(item);
            if(mCurrentTickValue==getAdapterPosition()) {
                mTickIcon.setVisibility(View.VISIBLE);
            } else mTickIcon.setVisibility(View.INVISIBLE);
        }
    }
}
